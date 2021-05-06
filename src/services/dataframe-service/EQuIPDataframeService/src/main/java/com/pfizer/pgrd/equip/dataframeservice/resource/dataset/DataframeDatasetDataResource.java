package com.pfizer.pgrd.equip.dataframeservice.resource.dataset;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Part;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.FormattingUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.utils.UtilsGeneral;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class DataframeDatasetDataResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeDatasetDataResource.class);

	/**
	 * A {@link Route} that will fetch the dataset json object, including the id of
	 * the file (if one exists)
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			byte[] data = null;
			String complexDataId = null;
			String userId = null;
			Dataframe df = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

			try {
				userId = request.headers("IAMPFIZERUSERCN");
				String asCSVParam = request.queryParams("asCSV");
				boolean asCSV = false;
				if(asCSVParam != null) {
					try {
						asCSV = Boolean.parseBoolean(asCSVParam);
					}
					catch(Exception e) { }
				}
				
				if (userId != null) {
					complexDataId = request.params(":id");
					if (complexDataId != null) {
												
						// Check that the user has access to the parent dataframe
						DatasetDAO ddao = getDatasetDAO();
						df = ddao.getParentDataframe(complexDataId);
						
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.canViewDataframe(df, userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userId
									+ " is not authorized to view the dataframe containing this dataset");
						}

						isOk = auth.checkPrivileges(df.getDataframeType(), "GET", userId);
						if (isOk) {
							ComplexData cd = ddao.getData(complexDataId);
							if (cd != null && cd.getBytes() != null) {
								data = cd.getBytes();
								String mimeType = cd.getMimeType();
								List<String> csvTypes = Arrays.asList(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE, Dataframe.KEL_FLAGS_TYPE, Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE);
								if(asCSV && csvTypes.contains(df.getDataframeType())) {
									String csv = UtilsGeneral.jsonToCSV(cd);
									if(csv != null) {
										data = csv.getBytes();
										mimeType = "text/csv";
									}
								}
								
								String extension = ".txt";	// reasonable default???
								if( mimeType != null && mimeType.contains("/") ) {
									extension = FormattingUtils.getExtension(mimeType);
									
									// obtain the extension from Tika using the type/subtype names
									String results[] = mimeType.split("\\/");
									String type = results[0];
									String subType = results[1];
									LOGGER.debug("DataframeDatasetDataResource get: type=" + type + " subtype=" + subType);
									LOGGER.debug("DataframeDatasetDataResource extension=" + extension);
								}
								
								String fileName = df.getPrettyFileName() + extension;
								LOGGER.debug("DataframeDatasetDataResource: derived file name=" + fileName);
								
								String ct = cd.getMimeType();
								if(cd.getEncoding() != null) {
									ct += ";charset=" + cd.getEncoding();
								}
								response.header(HTTPHeaders.CONTENT_TYPE, ct);
						        response.header("Content-Disposition", "attachment; filename=" + fileName);
							} else {
								Spark.halt(HTTPStatusCodes.NOT_FOUND, "The Dataset contains no binary data.");
							}
							
							if(Props.isAudit()){
								if(df != null){
									/*asc.logAuditEntry(	"Dataframe was accessed via complexDataId", 
														df.getEquipId(),
														df.getDataframeType(),
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														df.getVersionNumber());*/
									AuditDetails details1 = asc.new AuditDetails("Dataframe was accessed via complexDataId", df, userId);
									//details1.setContextEntity(df);
									details1.setRequest(request);
									asc.logAuditEntryAsync(details1);
									if( df.getRestrictionStatus().toUpperCase().equals("RESTRICTED")){
										/*asc.logAuditEntry(	"View access of restricted data", 
															df.getEquipId(),
															df.getDataframeType(),
															userId,
															Props.isAudit(),
															Const.AUDIT_SUCCESS,
															df.getVersionNumber());*/
										AuditDetails details2 = asc.new AuditDetails("View access of restricted data", df, userId);
										details2.setContextEntity(df);
										details2.setRequest(request);
										asc.logAuditEntryAsync(details2);
									}
									if( df.getDataBlindingStatus().toUpperCase().equals("UNBLINDED")){
										/*asc.logAuditEntry(	"View access of unblinded data", 
															df.getEquipId(),
															df.getDataframeType(),
															userId,
															Props.isAudit(),
															Const.AUDIT_SUCCESS,
															df.getVersionNumber());*/
										AuditDetails details3 = asc.new AuditDetails("View access of unblinded data", df, userId);
										//details3.setContextEntity(df);
										details3.setRequest(request);
										asc.logAuditEntryAsync(details3);
									}
								}
							}
							
						} else {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to view datasets");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No complex data ID was provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return data;
		}
	};
	
	public static final String toCSV(ComplexData cd) {
		String csv = null;
		
		return csv;
	}

	/**
	 * A {@link Route} that will post a file to an existing dataset it needs to
	 * create the complex data node
	 */
	public static final Route post = new Route() {
		@Override
		public String handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			String datasetId = null;
			
			try {
				datasetId = request.params(":id");
				if (datasetId != null) {
					DatasetDAO ddao = getDatasetDAO();
					Dataframe df = ddao.getParentDataframeFromDataset(datasetId);
					userId = request.headers("IAMPFIZERUSERCN");
					if (userId != null) {
						// need to get parent dataframe and check to make sure user has access to it
						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.canViewDataframe(df, userId);
						if (isOk) {
							isOk = auth.checkPrivileges(df.getDataframeType(), "POST", userId);
							if (isOk) {
								String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
								if (contentType.indexOf(HTTPContentTypes.MULTIPART_FORM) > -1) {
									Part filePart = request.raw().getPart("file");
									if (filePart != null) {
										byte[] fileContent = readContent(filePart);
										boolean success = ddao.insertData(datasetId, fileContent);
										if (success) {
											Dataset dataset = ddao.getDataset(datasetId);
											returnJson = dataset.getComplexDataId();
											String name = filePart.getSubmittedFileName(); //this returns full file name
											String fileContentType = filePart.getContentType();  //this returns application/vnd.ms-excel  - leaving it in here in case that is what we want to use instead of FileName
											
											//now update the dataframe with the column names
											if (name.length()>=3) {
												String extension = name.substring(name.length() - 3);
												if (extension.equalsIgnoreCase("csv")) {
													InputStream stream = filePart.getInputStream();
													BufferedReader reader = new BufferedReader(
															new InputStreamReader(stream));
													String firstLine = reader.readLine();
													List<String> columnNames =  Arrays.asList(firstLine.split(","));
													PropertiesPayload payload = new PropertiesPayload();
													//do we need to add modified/modified by to the dataframe here?
													payload.addProperty("equip:columnNames", columnNames);
													ModeShapeDAO dao = new ModeShapeDAO();
													dao.updateNode(df.getId(), payload);
												}												
											}
											
											response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
											response.header(HTTPHeaders.LOCATION,
													"/dataframes/data/" + dataset.getComplexDataId());
										} else {
											Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
													"Unable to add data to Dataset '" + datasetId + "'.");
										}
									} else {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No file was provided.");
									}
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, HTTPHeaders.CONTENT_TYPE + " must be "
											+ HTTPContentTypes.MULTIPART_FORM + ".");
								}
							} else {
								Spark.halt(HTTPStatusCodes.FORBIDDEN,
										"User does not have privileges to add data to the parent dataframe.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User does not have access to the parent dataframe.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Dataset ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}
	};
	
	public static final String insertData(Dataframe df, byte[] fileContent, String fileName, String fileType) throws IOException {
		String returnId = null;
		if(df.getDataset() != null && fileContent != null && fileContent.length > 0 && fileName != null && fileType != null) {
			DatasetDAO ddao = getDatasetDAO();
			String datasetId = df.getDataset().getId();
			boolean success = ddao.insertData(datasetId, fileContent);
			if (success) {
				Dataset dataset = ddao.getDataset(datasetId);
				returnId = dataset.getComplexDataId();
				
				//now update the dataframe with the column names
				if (fileName.length()>=3) {
					String extension = fileName.substring(fileName.length() - 3);
					if (extension.equalsIgnoreCase("csv")) {
						InputStream stream = new ByteArrayInputStream(fileContent);
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(stream));
						String firstLine = reader.readLine();
						List<String> columnNames =  Arrays.asList(firstLine.split(","));
						PropertiesPayload payload = new PropertiesPayload();
						//do we need to add modified/modified by to the dataframe here?
						payload.addProperty("equip:columnNames", columnNames);
						ModeShapeDAO dao = new ModeShapeDAO();
						dao.updateNode(df.getId(), payload);
					}												
				}
			} else {
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
						"Unable to add data to Dataset '" + datasetId + "'.");
			}
		}
		
		return returnId;
	}

}
