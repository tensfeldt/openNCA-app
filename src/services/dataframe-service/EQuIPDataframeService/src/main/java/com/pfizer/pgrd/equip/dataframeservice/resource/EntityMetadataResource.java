package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.MetadataDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.MetadatumDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipLock;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

public class EntityMetadataResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityMetadataResource.class);

	/**
	 * A {@link Route} that will fetch the metadata associated with the dataframe
	 * ID.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			
			try {
				String objId = request.params(":id");
				if (objId != null) {

					userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}

					ModeShapeDAO dao = new ModeShapeDAO();
					ModeShapeNode node = dao.getNode(objId);
					if (node != null) {
						ServiceBaseResource.handleUserAccess(node, userId);
						
						List<MetadatumDTO> dtos = node.getChildren(MetadatumDTO.class);
						List<Metadatum> metadata = MetadatumDTO.toMetadatum(dtos);

						if (Props.isAudit()) {
							/*asc.logAuditEntry("Access of metadata", objId, "Metadata", userId, Props.isAudit(),
									Const.AUDIT_SUCCESS, 0L);*/
							AuditDetails details = asc.new AuditDetails("Access of metadata", node.toEquipObject(), userId);
							
							asc.logAuditEntryAsync(details);
						}

						json = marshalObject(metadata);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No object with ID '" + objId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No object ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

	/**
	 * A {@link Route} that will post new metadata to the node ID.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			List<Metadatum> metadata = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				userId = request.headers("IAMPFIZERUSERCN");
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String parentId = request.params(":id");
					if (parentId != null) {
						ModeShapeDAO msDao = new ModeShapeDAO();
						EquipObject parent = msDao.getEquipObject(parentId);
						if (parent != null) {
							ServiceBaseResource.handleUserAccess(parent, userId);
							
							AuthorizationDAO authDao = new AuthorizationDAO();
							if(!authDao.canModifyExtras(parent, userId)) {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userId + " may not add metadata to node " + parentId + ".");
							}
							
							if (userId == null) {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
							}
							ModeShapeDAO mdao = new ModeShapeDAO();
							ModeShapeNode node = mdao.getNode(parentId);
							ServiceBaseResource.handleUserAccess(node, userId);

							// if node is locked then exit and throw an error
							/*
							 * Block modified for ticket GBL32477288i
							 * Justin Quintanilla, June 2nd 2020
							 */
							if(!EntityVersioningResource.lockedByUser(userId, node)) {
								Spark.halt(HTTPStatusCodes.FORBIDDEN, "The parent object is locked and cannot be modified");
							}

							/*EquipLock lockObject = (EquipLock) node;
							if (lockObject.isLocked()) {
								Spark.halt(HTTPStatusCodes.FORBIDDEN, "This object is locked and cannot be modified");
							}*/

							String mdJson = request.body();
							if (mdJson != null) {
								metadata = unmarshalObject(mdJson, Metadatum.class);
								if (metadata != null && !metadata.isEmpty()) {
									for(Metadatum md : metadata) {
										if(md.getCreatedBy() == null || md.getCreatedBy().isEmpty()) {
											md.setCreatedBy(userId);
										}
										if(md.getCreated() == null) {
											md.setCreated(new Date());
										}
									}
									
									MetadataDAO dao = getMetadataDAO();									
									metadata = dao.insertMetadata(metadata, parentId);

									if (Props.isAudit()) {
										if (metadata != null && metadata.size() > 0) {
											/*asc.logAuditEntry("Creation of Metadata", metadata.get(0).getId(),
													"Metadata", userId, Props.isAudit(), Const.AUDIT_SUCCESS, 0L,
													parentId, null);*/
											AuditDetails details = asc.new AuditDetails("Creation of Metadata", metadata.get(0), userId);
											details.setContextEntity(node.toEquipObject());
											asc.logAuditEntryAsync(details);
										}
									}

									updateAssociatedProtocol(userId, parentId);
									
									// Update the modified properties of the parent node.
									msDao.updateModified(parentId, userId);
									EquipObject eo = msDao.getEquipObject(parentId);
									if(eo instanceof Assembly) {
										Assembly a = (Assembly) eo;
										if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
											EntityVersioningResource.updateAtrFlag(a, false, userId);
										}
									}

									returnJson = metadata.get(0).getId();
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No metadata provided.");
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No metadata provided.");
							}
						}
						else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND, "No node with ID '" + parentId + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No parent ID was provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && metadata != null && metadata.size() > 0) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to create Metadata failed with exception " + ex.getMessage(),
									metadata.get(0).getId(), "Metadata", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
									0L);*/
							AuditDetails details = asc.new AuditDetails("Attempt to create Metadata failed with exception " + ex.getMessage(),metadata.get(0), userId);
							
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
	 * A {@link Route} that will post new metadata to the node ID.
	 */
	public static final Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			String metadataId = null;
			
			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					metadataId = request.params(":id");

					userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}

					ModeShapeDAO mdao = new ModeShapeDAO();
					ModeShapeNode node = mdao.getNode(metadataId);
					String parentPath = node.getUp();
					EquipObject parentNode = mdao.getEquipObject(mdao.fetchId(parentPath));
					ServiceBaseResource.handleUserAccess(parentNode, userId);
					// if node is locked then exit and throw an error
					
					/*
					 * Block modified for ticket GBL32477288i
					 * Justin Quintanilla, June 2nd 2020
					 */
					if(!EntityVersioningResource.lockedByUser(userId, parentNode)) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "The parent object is locked and cannot be modified");
					}
					
					AuthorizationDAO authDao = new AuthorizationDAO();
					if(!authDao.canModifyExtras(parentNode, userId)) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userId + " may not modify the metadata of node " + parentNode.getId() + ".");
					}
					
					/*if (parentNode instanceof EquipLock) {
						EquipLock lockObject = (EquipLock) parentNode;
						if (lockObject.isLocked() && (lockObject.getLockedByUser() == null || !lockObject.getLockedByUser().equalsIgnoreCase(userId))) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN, "The parent object is locked and cannot be modified");
						}
					}*/
					
					if (Props.isAuditOnEntry()) {
						/*asc.logAuditEntry("Attempt to modify metadata", metadataId, "Metadatum", userId,
								Props.isAudit(), Const.AUDIT_SUCCESS, 0L);*/
						AuditDetails details = asc.new AuditDetails("Attempt to modify metadata", node.toEquipObject(), userId);
						details.setContextEntity(parentNode);
						asc.logAuditEntryAsync(details);
					}

					String mdJson = request.body();
					if (mdJson != null) {
						List<Metadatum> metadata = unmarshalObject(mdJson, Metadatum.class);
						if (metadata != null && !metadata.isEmpty()) {
							MetadataDAO dao = getMetadataDAO();
							Metadatum metadatum = metadata.get(0);
							if(metadatum.getModifiedBy() == null || metadatum.getModifiedBy().isEmpty()) {
								metadatum.setModifiedBy(userId);
							}
							if(metadatum.getModifiedDate() == null) {
								metadatum.setModifiedDate(new Date());
							}

							Metadatum newMetadatum = dao.updateMetadata(metadatum, metadataId);
							
							if (Props.isAudit()) {
								/*asc.logAuditEntry(
										"Metadatum was set to: " + "isDeleted=" + metadatum.isDeleted() + ",key="
												+ metadatum.getKey() + ", values=" + metadatum.getValue(),
										newMetadatum.getId(), "Metadatum", userId, Props.isAudit(), Const.AUDIT_SUCCESS,
										1L);*/
								AuditDetails details = asc.new AuditDetails("Metadatum was set to: " + "isDeleted=" + metadatum.isDeleted() + ",key="
										+ metadatum.getKey() ,node.toEquipObject(), userId);
								//+ ", values=" + metadatum.getValue()
								details.setContextEntity(parentNode);
								asc.logAuditEntryAsync(details);
							}

							ModeShapeDAO bdao = new ModeShapeDAO();
							ModeShapeNode mdNode = bdao.getNode(metadataId);
							String upPath = mdNode.getUp();
							String parentId = bdao.fetchId(upPath);
							updateAssociatedProtocol(userId, parentId);
							
							// Update the modified properties of the parent node.
							bdao.updateModified(parentId, userId);
							EquipObject eo = bdao.getEquipObject(parentId);
							if(eo instanceof Assembly) {
								Assembly a = (Assembly) eo;
								if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
									EntityVersioningResource.updateAtrFlag(a, false, userId);
								}
							}

							returnJson = marshalObject(newMetadatum);
							response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No metadata provided.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No metadata provided.");
					}

				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && metadataId != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to update metadatum failed with exception: " + ex.getMessage(),
									metadataId, "Metadatum", userId, Props.isAudit(), Const.AUDIT_FAILURE, 0L);*/
							ModeShapeDAO bdao = new ModeShapeDAO();
							ModeShapeNode mdNode = bdao.getNode(metadataId);
							AuditDetails details = asc.new AuditDetails("Attempt to update metadatum failed with exception: " + ex.getMessage()
							,mdNode.toEquipObject(), userId);
							
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

	private static void updateAssociatedProtocol(String userId, String parentId) {
		ModeShapeDAO bdao = new ModeShapeDAO();
		EquipObject eo = bdao.getEquipObject(parentId);

		if (eo != null) {
			List<String> studyIds = null;
			if (eo instanceof Analysis) {
				studyIds = ((Analysis) eo).getStudyIds();
			} else if (eo instanceof Assembly) {
				studyIds = ((Assembly) eo).getStudyIds();
			} else if (eo instanceof Dataframe) {
				studyIds = ((Dataframe) eo).getStudyIds();
			}
			if (studyIds != null && studyIds.isEmpty() == false) {
				try {
					OpmetaServiceClient osc = new OpmetaServiceClient();
					osc.setHost(Props.getOpmetaServiceServer());
					osc.setPort(Props.getOpmetaSerivcePort());
					for (String studyId : studyIds) {
						LOGGER.info("EntityMetadataResource: update protocol for study id=" + studyId);
						osc.updateProtocolModifiedDate(userId, studyId);
					}
				} catch (Exception err) {
					LOGGER.warn("EntityMetadataResource: Error updating protocol modification time for comment parent "
							+ parentId, err);
				}
			}
		}
	}
}
