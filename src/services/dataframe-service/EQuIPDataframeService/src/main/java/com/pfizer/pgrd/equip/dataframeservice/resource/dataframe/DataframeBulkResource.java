package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.Part;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataset.DataframeDatasetDataResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.extractor.AnalysisExtractor;
import com.pfizer.pgrd.modeshape.rest.query.extractor.DataframeExtractor;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

public class DataframeBulkResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeBulkResource.class);

	/**
	 * A {@link Route} that will insert any JSON represented dataframes found in the
	 * request body.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String noDataframeError = "No Dataframe was provided.";
			String returnJson = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			Dataframe node = null;

			try {
				userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null) {
					boolean isJson = contentType.equalsIgnoreCase(HTTPContentTypes.JSON);
					boolean isMultiPart = contentType.contains(HTTPContentTypes.MULTIPART_FORM);
					if (isJson || isMultiPart) {
						String dfJson = null;
						if (isMultiPart) {
							Part dataframesPart = request.raw().getPart("dataframes");
							if (dataframesPart != null && dataframesPart.getInputStream() != null) {
								StringBuilder stringBuilder = new StringBuilder();
								try (BufferedReader bufferedReader = new BufferedReader(
										new InputStreamReader(dataframesPart.getInputStream()))) {
									String line = null;
									while ((line = bufferedReader.readLine()) != null) {
										stringBuilder.append(line);
									}
								}
								
								dfJson = stringBuilder.toString();
							}
						} else {
							dfJson = request.body();
						}

						if (dfJson != null) {
							List<Dataframe> list = unmarshalObject(dfJson, Dataframe.class);
							if (!list.isEmpty()) {
								List<FullFile> files = new ArrayList<>();
								if (isMultiPart) {
									Part folder = request.raw().getPart("files");
									files = DataframeBulkResource.extractFiles(folder);
								}

								List<DataframeCreationReportEntry> report = DataframeBulkResource
										.createDataframes(userId, list, files);
								returnJson = DataframeRootResource.marshalObject(report);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
								response.status(HTTPStatusCodes.OK);
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be "
								+ HTTPContentTypes.JSON + " or " + HTTPContentTypes.MULTIPART_FORM);
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be "
							+ HTTPContentTypes.JSON + " or " + HTTPContentTypes.MULTIPART_FORM);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && node != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to create dataframe failed with exception " + ex.getMessage(),
									node.getEquipId(), "Dataframe", userId, Props.isAudit(), Const.AUDIT_FAILURE,
									node.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Attempt to create dataframe failed with exception " + ex.getMessage(), node, userId);
							details.setContextEntity(node);
							details.setActionStatus(Const.AUDIT_FAILURE);
							asc.logAuditEntryAsync(details);
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}

	};

	/**
	 * Returns a {@link List} object of {@link FullFile} objects extracted from the
	 * archive file in the provided {@link Part} object.
	 * 
	 * @param filePart
	 * @return {@link List}<{@link FullFile}>
	 * @throws IOException
	 */
	private static List<FullFile> extractFiles(Part filePart) throws IOException {
		List<FullFile> files = new ArrayList<>();
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		if (filePart != null && filePart.getInputStream() != null) {
			byte[] zipContents = IOUtils.toByteArray(filePart.getInputStream());
			if (zipContents != null && zipContents.length > 0) {
				String name = filePart.getSubmittedFileName();
				if (name != null) {
					String[] parts = name.split(".");
					String ext = parts[parts.length - 1];
					name = name.replace("." + ext, "");

					File zip = File.createTempFile(name, "." + ext);
					if (zip != null) {
						try (FileOutputStream fos = new FileOutputStream(zip)) {
							fos.write(zipContents);
						}

						if (ext.equalsIgnoreCase("zip")) {
							try (ZipFile zf = new ZipFile(zip.getAbsolutePath())) {
								Enumeration<? extends ZipEntry> entries = zf.entries();
								while (entries.hasMoreElements()) {
									ZipEntry entry = entries.nextElement();
									if (entry.isDirectory()) {
										continue;
									}
									FullFile ff = new FullFile();
									ff.fileName = entry.getName();
									ff.fileType = fileNameMap.getContentTypeFor(ff.fileName);
									ff.data = IOUtils.toByteArray(zf.getInputStream(entry));

									files.add(ff);
								}
							}
						} else if (ext.equalsIgnoreCase("gz")) {
							FileInputStream fis = new FileInputStream(zip);
							try (TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
								TarArchiveEntry tarEntry = null;
								while ((tarEntry = tis.getNextTarEntry()) != null) {
									if (tarEntry.isDirectory()) {
										continue;
									}
									FullFile ff = new FullFile();
									ff.fileName = tarEntry.getName();
									ff.fileType = fileNameMap.getContentTypeFor(ff.fileName);
									ff.data = Files.readAllBytes(tarEntry.getFile().toPath());

									files.add(ff);
								}
							}
						}

						zip.delete();
					}
				}
			}
		}

		return files;
	}

	private static List<DataframeCreationReportEntry> createDataframes(String userId, List<Dataframe> dataframes,
			List<FullFile> files) throws Exception {
		List<DataframeCreationReportEntry> report = new ArrayList<>();
		String noDataframeError = "No Dataframe was provided.";

		DataframeDAO dao = getDataframeDAO();
		for (Dataframe dataframe : dataframes) {
			DataframeCreationReportEntry reportEntry = null;
			try {
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());

				Dataframe node = null;
				if (dataframe != null) {
					reportEntry = new DataframeCreationReportEntry();
					if (dataframe.getStudyIds() != null && !dataframe.getStudyIds().isEmpty()) {
						if (dataframe.getPromotionStatus() == null || dataframe.getPromotionStatus().isEmpty()) {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Promotion Status must be provided");
						}

						if (dataframe.getDataStatus() == null || dataframe.getDataStatus().isEmpty()) {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Data Status must be provided");
						}

						if (dataframe.getDataBlindingStatus() == null || dataframe.getDataBlindingStatus().isEmpty()) {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Data Blinding Status must be provided");
						}

						if (dataframe.getDataframeType() == null || dataframe.getDataframeType().isEmpty()) {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Dataframe Type must be provided");
						}

						if (dataframe.getCreated() == null) {
							dataframe.setCreated(new Date());
						}
						if (dataframe.getCreatedBy() == null) {
							if (userId != null) {
								dataframe.setCreatedBy(userId);
							} else {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
							}

						}

						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges(dataframe.getDataframeType(), "POST", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to post this type of dataframe ("
											+ dataframe.getDataframeType() + ")");
						}

						for (Comment c : dataframe.getComments()) {
							if (c.getCreated() == null) {
								c.setCreated(new Date());
							}
							if (c.getCreatedBy() == null) {
								c.setCreatedBy(userId);
							}
						}

						// get previous committed version so we can point any attachments to the new
						// data frame
						List<Dataframe> previousList = dao.getDataframeByEquipId(dataframe.getEquipId());
						Dataframe latestVersion = null;
						if (previousList != null && previousList.size() > 0) {
							latestVersion = VersioningDAO.getLatestVersion(previousList);
						}

						applyVersionIncrementingLogic(dataframe, dao);

						node = dao.insertDataframe(dataframe);
						isOk = dao.copyGroupAccess(dataframe, userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.CONFLICT, "Dataframe " + dataframe.getEquipId()
									+ " has been created but group access could not be copied from its parent dataframe(s)");
						}

						if (node != null) {
							// point any previous attachments to this frame and update
							if (latestVersion != null) {
								// query for any attachments
								String previousId = latestVersion.getId();
								JCRQueryResultSet resultSet = null;
								String sql = "SELECT dataframe.* FROM [equip:dataframe] AS dataframe" + "\r\n"
										+ "WHERE dataframe.[equip:dataframeIds] = \"" + previousId + "\" "
										+ "AND dataframe.[equip:dataframeType] = \"Attachment\" AND dataframe.[equip:deleteFlag]= \"false\"";

								ModeShapeDAO msDao = new ModeShapeDAO();
								ModeShapeClient client = msDao.getModeShapeClient();
								List<Dataframe> dfResults = null;
								try {
									resultSet = client.query(sql);
									DataframeExtractor de = new DataframeExtractor();
									de.setAlias("dataframe");
									dfResults = new ArrayList<Dataframe>();
									dfResults.addAll(de.extract(resultSet));
								}
								// exception from query
								catch (ModeShapeAPIException maie) {
									// delete the new node as it is incomplete
									try {
										msDao.deleteNode(node.getId());
									} catch (Exception ex) {
									}
									Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
											"There was an error when searching previous version attachments.");
								}
								try {
									for (Dataframe resultDf : dfResults) {
										String attachmentId = resultDf.getId();
										resultDf.getDataframeIds().add(node.getId());
										dao.updateDataframe(resultDf, attachmentId);
									}
								}
								// exception from update would be a runtime exception
								catch (Exception ex) {
									// delete the new node as it is incomplete
									try {
										msDao.deleteNode(node.getId());
									} catch (Exception ex2) {
									}
									Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
											"There was an error when updating previous version attachments.");
								}
							}

							if (Props.isAudit()) {
								/*asc.logAuditEntry("Creation of dataframe via dataframes", node.getEquipId(),
										dataframe.getDataframeType(), userId, Props.isAudit(), Const.AUDIT_SUCCESS,
										node.getVersionNumber());*/
								AuditDetails details = asc.new AuditDetails("Creation of dataframe via dataframes",node , userId);
								details.setContextEntity(dataframe);
								//details.setActionStatus(Const.AUDIT_FAILURE);
								asc.logAuditEntryAsync(details);

								if (Props.isAuditReportCreationAuditInContextOfAnalysis()) {
									if ((node.getDataframeType().equals(Dataframe.REPORT_TYPE)
											|| node.getDataframeType().equals(Dataframe.REPORT_ITEM_TYPE))) {
										String parentType = null;
										String parentDataframeId = null;

										if (node.getDataframeIds() != null && node.getDataframeIds().size() > 0) {
											ModeShapeDAO mDao = new ModeShapeDAO();
											ModeShapeNode msn = mDao.getModeShapeClient()
													.getNodeByPath(node.getDataframeIds().get(0), true);

											Dataframe parentDataframe = dao.getDataframe(msn.getJcrId());
											parentDataframeId = parentDataframe.getId();

											if (parentDataframe.getDataframeType()
													.equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
												parentType = Dataframe.PRIMARY_PARAMETERS_TYPE;
											} else if (parentDataframe.getDataframeType()
													.equals(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
												parentType = Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE;
											} else if (parentDataframe.getDataframeType()
													.equals(Dataframe.KEL_FLAGS_TYPE)) {
												parentType = Dataframe.KEL_FLAGS_TYPE;
											}
										}

										if (parentType != null) {
											ModeShapeDAO mdao = new ModeShapeDAO();
											AnalysisExtractor analysisExtractor = new AnalysisExtractor();
											String alias = "analysis";
											analysisExtractor.setAlias(alias);
											List<Analysis> analysis = mdao.getAnalysisFromFlag(parentType,
													parentDataframeId, alias, analysisExtractor);

											if (analysis != null && analysis.size() > 0) {
												String contextId = analysis.get(0).getEquipId() + " v."
														+ analysis.get(0).getVersionNumber();
												/*asc.logAuditEntry("Report created", node.getEquipId(), "Dataframe",
														userId, Props.isAudit(), Const.AUDIT_SUCCESS,
														node.getVersionNumber(), contextId, null);*/
												AuditDetails adetails = asc.new AuditDetails("Report created",node , userId);
												adetails.setContextEntity(analysis.get(0));
												//details.setActionStatus(Const.AUDIT_FAILURE);
												asc.logAuditEntryAsync(adetails);
											}
										}
									}
								}
							}

							// call opmeta service to update modification time on associated protocol
							try {
								OpmetaServiceClient osc = new OpmetaServiceClient();
								osc.setHost(Props.getOpmetaServiceServer());
								osc.setPort(Props.getOpmetaSerivcePort());
								List<String> studyIds = node.getStudyIds();
								for (String studyId : studyIds) {
									LOGGER.info("DataframeRootResource: update protocol for study id=" + studyId);
									osc.updateProtocolModifiedDate(userId, studyId);
								}
							} catch (Exception err) {
								LOGGER.warn("DataframeRootResource: Error updating protocol modification time for node "
										+ node.id, err);
							}

							// Now handle the datasets
							if (node.getDataset() != null) {
								reportEntry.datasetId = node.getDataset().getId();
								String fName = node.getFileName();
								if (fName != null) {
									FullFile fullFile = null;
									for (FullFile ff : files) {
										if (fName.contains(ff.fileName)) {
											fullFile = ff;
											break;
										}
									}

									if (fullFile != null) {
										reportEntry.fileName = fullFile.fileName;
										reportEntry.complexDataId = DataframeDatasetDataResource.insertData(node,
												fullFile.data, fullFile.fileName, fullFile.fileType);
										files.remove(fullFile);
									} else {
										reportEntry.message = "No file with name '" + fName
												+ "' found in list of files.";
									}
								} else {
									reportEntry.message = "No file name found in metadata.";
								}
							} else {
								reportEntry.message = "No dataset associated.";
							}

							reportEntry.statusCode = HTTPStatusCodes.OK;
							reportEntry.dataframeId = node.getId();
						} else {
							Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
									"There was an error when creating the dataframe.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "At least one study ID must be provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
				}
			} catch (HaltException he) {
				if (reportEntry != null) {
					reportEntry.message = he.getMessage();
					reportEntry.statusCode = he.getStatusCode();
				}
			} catch (Exception e) {
				if (dataframes.size() < 2) {
					throw e;
				} else {
					reportEntry.message = e.getMessage();
					reportEntry.statusCode = HTTPStatusCodes.INTERNAL_SERVER_ERROR;
				}
			}

			if (reportEntry != null) {
				if (reportEntry.statusCode != 200 && reportEntry.dataframeId != null) {
					ModeShapeDAO msDao = new ModeShapeDAO();
					try {
						msDao.deleteNode(reportEntry.dataframeId);
						reportEntry.dataframeId = null;
						reportEntry.datasetId = null;
						reportEntry.complexDataId = null;
						reportEntry.rolledBack = true;
					} catch (Exception e) {
						reportEntry.message += " Unable to roll-back: " + e.getMessage();
					}
				}

				report.add(reportEntry);
			}
		}

		return report;
	}

	public static void applyVersionIncrementingLogic(Dataframe dataframe, DataframeDAO dao) {
		try {
			EquipVersionableListGetter dataframeSiblingGetter = equipId -> {
				List<Dataframe> dataframes = dao.getDataframeByEquipId(dataframe.getEquipId());
				List<EquipVersionable> ev = new ArrayList<>();
				for (Dataframe df : dataframes) {
					ev.add(df);
				}

				return ev;
			};

			new VersioningDAO().applyVersionIncrementingLogic(dataframe, dataframe.getDataframeType(),
					dataframeSiblingGetter);
		} catch (ErrorCodeException ex) {
			Spark.halt(ex.getErrorCode(), ex.getMessage());
		}
	}
}

class DataframeCreationReportEntry {
	String dataframeId;
	String datasetId;
	String complexDataId;
	int statusCode;
	String message;
	String fileName;
	boolean rolledBack;
}

class FullFile {
	String fileName;
	String fileType;
	byte[] data;
}