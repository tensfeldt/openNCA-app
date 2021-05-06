package com.pfizer.pgrd.equip.dataframeservice.resource.dataframe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.FormattingUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
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
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
@MultipartConfig
public class DataframeRootResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataframeRootResource.class);

	/**
	 * A {@link Route} that will fetch all top-level dataframes from the repository.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				DataframeDAO dao = getDataframeDAO();
				List<Dataframe> list = dao.getDataframe();
				List<Dataframe> returnList = new ArrayList<>();
				AuthorizationDAO auth = new AuthorizationDAO();

				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				for (Dataframe df : list) {
					boolean canView = true;
					// canView = auth.canViewDataframe(df, userId);
					boolean hasPrivilege = auth.checkPrivileges(df.getDataframeType(), "GET", userId);

					if (canView && hasPrivilege) {
						returnList.add(df);
					}
				}

				if (!returnList.isEmpty()) {
					for (Dataframe df : list) {
						/*asc.logAuditEntry("Access of all top level dataframes, this is one", df.getEquipId(),
								"Dataframe", userId, Props.isAudit(), Const.AUDIT_SUCCESS, df.getVersionNumber());*/
						AuditDetails details = asc.new AuditDetails("Access of all top level dataframes, this is one", df, userId);
						
						details.setRequest(request);
						asc.logAuditEntryAsync(details);
					}

					json = marshalObject(list);
				} else {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userId + " does not have access to view any of these dataframes");
				}
				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

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

			long unmarshalTime = 0;
			long validationChecks = 0;
			long privilegesCheck = 0;
			long commentsCheck = 0;
			long getPreviousVersion = 0;
			long applyIncrementingLogic = 0;
			long modeshapeInsert = 0;
			long copyGroupAccess = 0;
			long getPreviousAttachments = 0;
			long updateAttachments = 0;
			long auditing = 0;
			long updateOpmeta = 0;

			long totalTime = System.currentTimeMillis();
			try {
				userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null) {
					boolean isJson = contentType.equalsIgnoreCase(HTTPContentTypes.JSON);
					if (isJson) {
						String dfJson = request.body();
						if (dfJson != null) {
							//LOGGER.info("<<POST DATAFRAME>> " + dfJson);
							unmarshalTime = System.currentTimeMillis();
							List<Dataframe> list = unmarshalObject(dfJson, Dataframe.class);
							unmarshalTime = System.currentTimeMillis() - unmarshalTime;

							if (!list.isEmpty()) {
								Dataframe dataframe = list.get(0);
								if (dataframe != null) {
									if (dataframe.getStudyIds() != null && !dataframe.getStudyIds().isEmpty()) {
										validationChecks = System.currentTimeMillis();
										if (dataframe.getPromotionStatus() == null
												|| dataframe.getPromotionStatus().isEmpty()) {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"Promotion Status must be provided");
										}

										if (dataframe.getDataStatus() == null || dataframe.getDataStatus().isEmpty()) {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Data Status must be provided");
										}

										if (dataframe.getDataBlindingStatus() == null
												|| dataframe.getDataBlindingStatus().isEmpty()) {
											Spark.halt(HTTPStatusCodes.BAD_REQUEST,
													"Data Blinding Status must be provided");
										}

										if (dataframe.getDataframeType() == null
												|| dataframe.getDataframeType().isEmpty()) {
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
										validationChecks = System.currentTimeMillis() - validationChecks;

										privilegesCheck = System.currentTimeMillis();
										AuthorizationDAO auth = new AuthorizationDAO();
										boolean isOk = auth.checkPrivileges(dataframe.getDataframeType(), "POST",
												userId);
										if (!isOk) {
											Spark.halt(HTTPStatusCodes.FORBIDDEN, "User " + userId
													+ " does not have privileges to post this type of dataframe ("
													+ dataframe.getDataframeType() + ")");
										}
										privilegesCheck = System.currentTimeMillis() - privilegesCheck;
										
										if(dataframe.getDataframeType().equalsIgnoreCase(Dataframe.ATTACHMENT_TYPE)) {
											String parentId = dataframe.getParentIds().get(0);
											ModeShapeDAO msDao = new ModeShapeDAO();
											EquipObject parent = msDao.getEquipObject(parentId);
											
											AuthorizationDAO authDao = new AuthorizationDAO();
											if(!authDao.canModifyExtras(parent, userId)) {
												Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userId + " may not add attachments to node " + parentId + ".");
											}
										}
										
										commentsCheck = System.currentTimeMillis();
										/*for (Comment c : dataframe.getComments()) {
											if (c.getCreated() == null) {
												c.setCreated(new Date());
											}
											if (c.getCreatedBy() == null) {
												c.setCreatedBy(userId);
											}
										}*/
										ServiceBaseResource.setSubInfo(dataframe, userId);
										commentsCheck = System.currentTimeMillis() - commentsCheck;

										DataframeDAO dao = getDataframeDAO();

										// get previous committed version so we can point any attachments to the new
										// data frame
										getPreviousVersion = System.currentTimeMillis();
										List<Dataframe> previousList = dao
												.getDataframeByEquipId(dataframe.getEquipId());
										Dataframe latestVersion = null;
										if (previousList != null && previousList.size() > 0) {
											latestVersion = VersioningDAO.getLatestVersion(previousList);
										}
										getPreviousVersion = System.currentTimeMillis() - getPreviousVersion;

										applyIncrementingLogic = System.currentTimeMillis();
										applyVersionIncrementingLogic(dataframe, dao);
										applyIncrementingLogic = System.currentTimeMillis() - applyIncrementingLogic;
										
										modeshapeInsert = System.currentTimeMillis();
										node = dao.insertDataframe(dataframe);
										modeshapeInsert = System.currentTimeMillis() - modeshapeInsert;
										
										copyGroupAccess = System.currentTimeMillis();
										isOk = dao.copyGroupAccess(dataframe, userId);
										if (!isOk) {
											Spark.halt(HTTPStatusCodes.CONFLICT, "Dataframe " + dataframe.getEquipId()
													+ " has been created but group access could not be copied from its parent dataframe(s)");
										}
										copyGroupAccess = System.currentTimeMillis() - copyGroupAccess;
										
										// If this is an ATR, we need to update the ATR flag of the corresponding reporting event.
										String subType = dataframe.getSubType();
										if(subType != null && subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE) && !dataframe.getAssemblyIds().isEmpty()) {
											String reId = dataframe.getAssemblyIds().get(0);
											EntityVersioningResource.updateAtrFlag(reId, true, dataframe.getCreatedBy());
										}
										
										if (node != null) { // point any previous attachments to this frame and update
											if (latestVersion != null) { // query for any attachments
												String previousId = latestVersion.getId();
												JCRQueryResultSet resultSet = null;
												String sql = "SELECT dataframe.* FROM [equip:dataframe] AS dataframe"
														+ "\r\n" + "WHERE dataframe.[equip:dataframeIds] = \""
														+ previousId + "\" "
														+ "AND dataframe.[equip:dataframeType] = \"Attachment\" AND dataframe.[equip:deleteFlag]= \"false\"";
												
												getPreviousAttachments = System.currentTimeMillis();
												ModeShapeDAO msDao = new ModeShapeDAO();
												ModeShapeClient client = msDao.getModeShapeClient();
												List<Dataframe> dfResults = null;
												try {
													resultSet = client.query(sql);
													DataframeExtractor de = new DataframeExtractor();
													de.setAlias("dataframe");
													dfResults = new ArrayList<Dataframe>();
													dfResults.addAll(de.extract(resultSet));
												} // exception from query
												catch (ModeShapeAPIException maie) { // delete the new node as it is incomplete
													try {
														msDao.deleteNode(node.getId());
													}
													catch (Exception ex) { }
													
													Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "There was an error when searching previous version attachments.");
												}
												
												try {
													updateAttachments = System.currentTimeMillis();
													for (Dataframe resultDf : dfResults) {
														String attachmentId = resultDf.getId();
														resultDf.getDataframeIds().add(node.getId());
														dao.updateDataframe(resultDf, attachmentId);
													}
													updateAttachments = System.currentTimeMillis() - updateAttachments;
												}
												catch (Exception ex) { // delete the new node as it is incomplete
													try {
														msDao.deleteNode(node.getId());
													} catch (Exception ex2) {
													}
													Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
															"There was an error when updating previous version attachments.");
												}
												getPreviousAttachments = System.currentTimeMillis() - getPreviousAttachments;
											}
											
											
											if (Props.isAudit()) {
												auditing = System.currentTimeMillis();
												/*asc.logAuditEntryAsync("Creation of dataframe via dataframes",
														node.getEquipId(), dataframe.getDataframeType(), userId,
														Props.isAudit(), Const.AUDIT_SUCCESS, node.getVersionNumber());*/
												AuditDetails details = asc.new AuditDetails("Creation of dataframe via dataframes", dataframe, userId);
												details.setRequest(request);
												asc.logAuditEntryAsync(details);
												if (Props.isAuditReportCreationAuditInContextOfAnalysis()) {
													if ((node.getDataframeType().equals(Dataframe.REPORT_TYPE) || node
															.getDataframeType().equals(Dataframe.REPORT_ITEM_TYPE))) {
														String parentType = null;
														String parentDataframeId = null;

														if (node.getDataframeIds() != null
																&& node.getDataframeIds().size() > 0) {
															ModeShapeDAO mDao = new ModeShapeDAO();
															ModeShapeNode msn = mDao.getModeShapeClient()
																	.getNodeByPath(node.getDataframeIds().get(0), true);

															Dataframe parentDataframe = dao
																	.getDataframe(msn.getJcrId());
															parentDataframeId = parentDataframe.getId();

															if (parentDataframe.getDataframeType()
																	.equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
																parentType = Dataframe.PRIMARY_PARAMETERS_TYPE;
															} else if (parentDataframe.getDataframeType().equals(
																	Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
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
															List<Analysis> analysis = mdao.getAnalysisFromFlag(
																	parentType, parentDataframeId, alias,
																	analysisExtractor);

															if (analysis != null && analysis.size() > 0) {
																String contextId = analysis.get(0).getEquipId() + " v."
																		+ analysis.get(0).getVersionNumber();
																/*asc.logAuditEntryAsync("Report created", node.getEquipId(),
																		"Dataframe", userId, Props.isAudit(),
																		Const.AUDIT_SUCCESS, node.getVersionNumber(),
																		contextId, null);*/
																AuditDetails details1 = asc.new AuditDetails("Report created", dataframe, userId);
																details1.setContextEntity(node);
																details1.setRequest(request);
																asc.logAuditEntryAsync(details1);
															}
														}
													}
												}
												auditing = System.currentTimeMillis() - auditing;
											}
											
											updateOpmeta = System.currentTimeMillis();
											// call opmeta service to update modification time on associated protocol
											try {
												OpmetaServiceClient osc = new OpmetaServiceClient();
												osc.setHost(Props.getOpmetaServiceServer());
												osc.setPort(Props.getOpmetaSerivcePort());
												List<String> studyIds = node.getStudyIds();
												for (String studyId : studyIds) {
													LOGGER.info("DataframeRootResource: update protocol for study id="
															+ studyId);
													//osc.updateProtocolModifiedDate(userId, studyId);
													osc.updateProtocolModifiedDateAsync(userId, studyId);
												}
											} catch (Exception err) {
												LOGGER.warn(
														"DataframeRootResource: Error updating protocol modification time for node "
																+ node.id,
														err);
											}
											updateOpmeta = System.currentTimeMillis() - updateOpmeta;

											returnJson = node.getId();
											response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
											response.header(HTTPHeaders.LOCATION, "/dataframes/" + node.getId());
										} else {
											Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
													"There was an error when creating the dataframe.");
										}
									} else {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST,
												"At least one study ID must be provided.");
									}
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, noDataframeError);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && node != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to create dataframe failed with exception " + ex.getMessage(),
									node.getEquipId(), "Dataframe", userId, Props.isAudit(), Const.AUDIT_FAILURE,
									node.getVersionNumber());*/
							AuditDetails details1 = asc.new AuditDetails("Attempt to create dataframe failed with exception " + ex.getMessage(), node, userId);
							details1.setContextEntity(node);
							details1.setActionStatus(Const.AUDIT_FAILURE);
							details1.setRequest(request);
							asc.logAuditEntryAsync(details1);
						}
					}
				} catch (Exception ex2) {
					LOGGER.error("", ex2); // intentionally swallowing exception, we want the original exception to be
											// reported.
				}

				ServiceExceptionHandler.handleException(ex);
			}
			totalTime = System.currentTimeMillis() - totalTime;
			
			FormattingUtils.logTime("Total Time", totalTime, totalTime);
			FormattingUtils.logTime("\tUnmarshal Time", unmarshalTime, totalTime);
			FormattingUtils.logTime("\tValidation Checks", validationChecks, totalTime);
			FormattingUtils.logTime("\tPrivileges Check", privilegesCheck, totalTime);
			FormattingUtils.logTime("\tComments Validation", commentsCheck, totalTime);
			FormattingUtils.logTime("\tGet Previous Version", getPreviousVersion, totalTime);
			FormattingUtils.logTime("\tApply Incrementing Logic", applyIncrementingLogic, totalTime);
			FormattingUtils.logTime("\tModeShape Insert", modeshapeInsert, totalTime);
			FormattingUtils.logTime("\tCopy Group Access", copyGroupAccess, totalTime);
			FormattingUtils.logTime("\tHandle Previous Attachments", getPreviousAttachments, totalTime);
			FormattingUtils.logTime("\t\tUpdate with Attachments", getPreviousAttachments, totalTime);
			FormattingUtils.logTime("\tAuditing", auditing, totalTime);
			FormattingUtils.logTime("\tUpdate OpMeta", updateOpmeta, totalTime);
			
			return returnJson;
		}

	};
	
	public static Dataframe fullInsert(Dataframe dataframe, String userId) throws ServiceCallerException {
		// Make sure any and all comments have the created and createdBy properties set.
		/*for (Comment c : dataframe.getComments()) {
			if (c.getCreated() == null) {
				c.setCreated(new Date());
			}
			if (c.getCreatedBy() == null) {
				c.setCreatedBy(userId);
			}
		}*/
		ServiceBaseResource.setSubInfo(dataframe, userId);
		
		// Insert the dataframe.
		DataframeDAO dao = getDataframeDAO();
		applyVersionIncrementingLogic(dataframe, dao);
		Dataframe node = dao.insertDataframe(dataframe);
		node = dao.getDataframe(node.getId(), false);
		
		// Copy group access.
		boolean isOk = dao.copyGroupAccess(dataframe, userId);
		if (!isOk) {
			Spark.halt(HTTPStatusCodes.CONFLICT, "Dataframe " + dataframe.getEquipId()
					+ " has been created, but group access could not be copied from its parent dataframe(s)");
		}
		
		// If this is an ATR, we need to update the ATR flag of the corresponding reporting event.
		String subType = dataframe.getSubType();
		if(subType != null && subType.equalsIgnoreCase(Dataframe.ATR_SUB_TYPE) && !dataframe.getAssemblyIds().isEmpty()) {
			String reId = dataframe.getAssemblyIds().get(0);
			EntityVersioningResource.updateAtrFlag(reId, true, dataframe.getCreatedBy());
		}
		
		if (node != null) {
			// Find any previous, committed version so that we can update them to reference this new dataframe.
			List<Dataframe> previousList = dao.getDataframeByEquipId(dataframe.getEquipId());
			Dataframe latestVersion = null;
			if (previousList != null && previousList.size() > 0) {
				latestVersion = VersioningDAO.getLatestVersion(previousList);
			}
			
			if (latestVersion != null) {
				String previousId = latestVersion.getId();
				JCRQueryResultSet resultSet = null;
				String sql = "SELECT dataframe.* FROM [equip:dataframe] AS dataframe"
						+ "\r\n" + "WHERE dataframe.[equip:dataframeIds] = \""
						+ previousId + "\" "
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
				catch (ModeShapeAPIException maie) {
					try {
						msDao.deleteNode(node.getId());
					}
					catch (Exception ex) { }
					
					Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "There was an error when searching previous version attachments.");
				}
				
				try {
					for (Dataframe resultDf : dfResults) {
						String attachmentId = resultDf.getId();
						resultDf.getDataframeIds().add(node.getId());
						dao.updateDataframe(resultDf, attachmentId);
					}
				}
				catch (Exception ex) {
					try {
						msDao.deleteNode(node.getId());
					} catch (Exception ex2) {
					}
					Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
							"There was an error when updating previous version attachments.");
				}
			}
			
			// Create audit logs.
			if (Props.isAudit()) {
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());
				/*asc.logAuditEntryAsync("Creation of dataframe via dataframes",
						node.getEquipId(), dataframe.getDataframeType(), userId,
						Props.isAudit(), Const.AUDIT_SUCCESS, node.getVersionNumber());*/
				AuditDetails details1 = asc.new AuditDetails("Creation of dataframe via dataframes", dataframe, userId);
				details1.setContextEntity(node);
				asc.logAuditEntryAsync(details1);
				if (Props.isAuditReportCreationAuditInContextOfAnalysis()) {
					if ((node.getDataframeType().equals(Dataframe.REPORT_TYPE) || node
							.getDataframeType().equals(Dataframe.REPORT_ITEM_TYPE))) {
						String parentType = null;
						String parentDataframeId = null;

						if (node.getDataframeIds() != null
								&& node.getDataframeIds().size() > 0) {
							ModeShapeDAO mDao = new ModeShapeDAO();
							
							String id = node.getDataframeIds().get(0);
							ModeShapeNode msn = null;
							if(id.startsWith("http")) {
								msn = mDao.getModeShapeClient()
										.getNodeByPath(id, true);
							}
							else {
								msn = mDao.getModeShapeClient().getNode(id, true);
							}

							Dataframe parentDataframe = dao
									.getDataframe(msn.getJcrId());
							parentDataframeId = parentDataframe.getId();

							if (parentDataframe.getDataframeType()
									.equals(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
								parentType = Dataframe.PRIMARY_PARAMETERS_TYPE;
							} else if (parentDataframe.getDataframeType().equals(
									Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
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
							List<Analysis> analysis = mdao.getAnalysisFromFlag(
									parentType, parentDataframeId, alias,
									analysisExtractor);

							if (analysis != null && analysis.size() > 0) {
								String contextId = analysis.get(0).getEquipId() + " v."
										+ analysis.get(0).getVersionNumber();
								/*asc.logAuditEntryAsync("Report created", node.getEquipId(),
										"Dataframe", userId, Props.isAudit(),
										Const.AUDIT_SUCCESS, node.getVersionNumber(),
										contextId, null);*/
								AuditDetails details2 = asc.new AuditDetails("Report created", node, userId);
								details2.setContextEntity(node);
								asc.logAuditEntryAsync(details2);
							}
						}
					}
				}
			}
			
			// Update opmeta.
			try {
				OpmetaServiceClient osc = new OpmetaServiceClient();
				osc.setHost(Props.getOpmetaServiceServer());
				osc.setPort(Props.getOpmetaSerivcePort());
				List<String> studyIds = node.getStudyIds();
				for (String studyId : studyIds) {
					LOGGER.info("DataframeRootResource: update protocol for study id="
							+ studyId);
					//osc.updateProtocolModifiedDate(userId, studyId);
					osc.updateProtocolModifiedDateAsync(userId, studyId);
				}
			} catch (Exception err) {
				LOGGER.warn(
						"DataframeRootResource: Error updating protocol modification time for node "
								+ node.id,
						err);
			}
		}
		
		return node;
	}
	
	public static String validate(Dataframe dataframe) {
		if(dataframe != null) {
			if(dataframe.getStudyIds() == null || dataframe.getStudyIds().isEmpty()) {
				return "Dataframe has no study ID.";
			}
			if(dataframe.getPromotionStatus() == null || dataframe.getPromotionStatus().isEmpty()) {
				return "Dataframe has no promotion status.";
			}
			if (dataframe.getDataStatus() == null || dataframe.getDataStatus().isEmpty()) {
				return "Dataframe has no data status.";
			}
			if (dataframe.getDataBlindingStatus() == null || dataframe.getDataBlindingStatus().isEmpty()) {
				return "Dataframe has no blinding status.";
			}
			if (dataframe.getDataframeType() == null || dataframe.getDataframeType().isEmpty()) {
				return "Dataframe has no dataframe type.";
			}
		}
		
		return null;
	}

	public static Dataframe insertDataframe(String userId, Dataframe dataframe) {
		Dataframe node = null;
		if (dataframe != null) {
			String validationError = DataframeRootResource.validate(dataframe);
			if(validationError == null) {
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

				for (Comment c : dataframe.getComments()) {
					if (c.getCreated() == null) {
						c.setCreated(new Date());
					}
					if (c.getCreatedBy() == null) {
						c.setCreatedBy(userId);
					}
				}

				DataframeDAO dao = getDataframeDAO();
				applyVersionIncrementingLogic(dataframe, dao);
				
				node = dao.insertDataframe(dataframe);
				if (!dao.copyGroupAccess(dataframe, userId)) {
					Spark.halt(HTTPStatusCodes.CONFLICT, "Dataframe " + dataframe.getEquipId()
							+ " has been created but group access could not be copied from its parent dataframe(s)");
				}

				// point any previous attachments to this frame and update
				// get previous committed version so we can point any attachments to the new
				// data frame
				if (node != null) {
					List<Dataframe> previousList = dao.getDataframeByEquipId(dataframe.getEquipId());
					Dataframe latestVersion = null;
					if (previousList != null && previousList.size() > 0) {
						latestVersion = VersioningDAO.getLatestVersion(previousList);
					}

					if (latestVersion != null) { // query for any attachments
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
						} // exception from query
						catch (ModeShapeAPIException maie) {
							try {
								// delete the incomplete node
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
						} catch (Exception ex) {
							try {
								// delete incomplete node
								msDao.deleteNode(node.getId());
							} catch (Exception ex2) {
							}
							Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
									"There was an error when updating previous version attachments.");
						}
					}
				} else {
					Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
							"There was an error when creating the dataframe.");
				}
			} else {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, validationError);
			}
		} else {
			Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No dataframe provided.");
		}

		return node;
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