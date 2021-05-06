package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.util.Date;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipLockable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.CommentDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import oracle.net.aso.p;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class EntityCommentResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityCommentResource.class);
	private static final String NO_COMMENT_ERROR = "No Comment was provided.";
	
	/**
	 * A {@link Route} that will fetch the comments associated with the node ID.
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
						
						List<CommentDTO> dtos = node.getChildren(CommentDTO.class);
						List<Comment> comments = CommentDTO.toComment(dtos);

						if (Props.isAudit()) {
							if (node instanceof EquipID && node instanceof EquipVersionable) {
								EquipID equipIdObject = (EquipID) node;
								EquipVersion equipVersionableObject = (EquipVersion) node;
								/*asc.logAuditEntry("Access of comment", equipIdObject.getEquipId(), "Comment", userId,
										Props.isAudit(), Const.AUDIT_SUCCESS,
										equipVersionableObject.getVersionNumber());*/
								
								AuditDetails details = asc.new AuditDetails("Access of comment", node.toEquipObject(), userId);
								asc.logAuditEntryAsync(details);
							}
						}

						json = marshalObject(comments);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No object with ID '" + objId + "' could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No ID was provided.");
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}

	};

	/**
	 * A {@link Route} that will post a new comment to the node ID.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			String parentId = null;
			Comment comment = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());
			
			
			
			try {
				userId = request.headers("IAMPFIZERUSERCN");
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					parentId = request.params(":id");
					if (parentId != null) {
						ModeShapeDAO msDao = new ModeShapeDAO();
						EquipObject parent = msDao.getEquipObject(parentId, false);
						if (parent != null) {
							ServiceBaseResource.handleUserAccess(parent, userId);
							
							AuthorizationDAO authDao = new AuthorizationDAO();
							if(!authDao.canModifyExtras(parent, userId)) {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userId + " may not add comments to node " + parentId + ".");
							}
							
							String commentJson = request.body();
							if (commentJson != null) {
								List<Comment> list = unmarshalObject(commentJson, Comment.class);
								if (list != null && !list.isEmpty()) {
									comment = list.get(0);
									if (comment != null) {
										String commentType = comment.getCommentType();
										boolean isQcComment = commentType != null &&  commentType.equalsIgnoreCase(Comment.QC_TYPE);
										
										if(parent instanceof EquipLockable) {
											EquipLockable el = (EquipLockable) parent;
											if(el.isLocked() && el.getLockedByUser() != null && !el.getLockedByUser().equalsIgnoreCase(userId) && !isQcComment) {
												Spark.halt(HTTPStatusCodes.CONFLICT, "The parent entity is locked and cannot be modified.");
											}
										}
										
										if (comment.getCreated() == null) {
											comment.setCreated(new Date());
										}
										if (comment.getCreatedBy() == null) {
											if (userId != null) {
												comment.setCreatedBy(userId);
											} else {
												Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
											}
										}

										CommentDAO dao = getCommentDAO();
										comment = dao.insertComment(comment, parentId);

										if (Props.isAudit()) {
											if (comment != null) {
												/*asc.logAuditEntry("Creation of comment", comment.getId(), "Comment",
														userId, Props.isAudit(), Const.AUDIT_SUCCESS, 1L, parentId,
														null);*/
												AuditDetails details = asc.new AuditDetails("Creation of comment", comment, userId);
												details.setContextEntity(parent);
												asc.logAuditEntryAsync(details);
											}
										}
										
										updateAssociatedProtocol(userId, parentId);
										
										// If this is not a QC comment, update the modified properties of the parent node.
										if(commentType == null || !isQcComment) {
											msDao.updateModified(parentId, userId);
										}
										
										EquipObject eo = msDao.getEquipObject(parentId);
										if(eo instanceof Assembly) {
											Assembly a = (Assembly) eo;
											if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
												EntityVersioningResource.updateAtrFlag(a, false, userId);
											}
										}

										returnJson = comment.getId();
										response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
										response.header(HTTPHeaders.LOCATION, "/comments");
									} else {
										Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
									}
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
								}
							}
							else {
								Spark.halt(HTTPStatusCodes.NOT_FOUND, "No node with ID '" + parentId + "' could be found.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
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
					if (userId != null && comment != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to create comment failed with exception " + ex.getMessage(),
									comment.getId(), "Comment", userId, Props.isAudit(), Const.AUDIT_FAILURE, 0L);*/
							AuditDetails details = asc.new AuditDetails("Attempt to create comment failed with exception " + ex.getMessage(), comment, userId);
							details.setActionStatus( Const.AUDIT_FAILURE);
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
	 * A {@link Route} that will update a comment
	 */
	public static final Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			String commentId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					commentId = request.params(":id");
					if (commentId != null) {
						userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						
						String commentJson = request.body();
						if (commentJson != null) {
							List<Comment> list = unmarshalObject(commentJson, Comment.class);
							if (!list.isEmpty()) {
								Comment comment = list.get(0);
								if (comment != null) {
									CommentDAO dao = getCommentDAO();
									Comment actualComment = dao.getComment(commentId);
									String commentType = actualComment.getCommentType();
									boolean isQcComment = commentType != null && commentType.equalsIgnoreCase(Comment.QC_TYPE);
									
									//Comment old = dao.getComment(commentId)
									ModeShapeDAO mdao = new ModeShapeDAO();
									ModeShapeNode node = mdao.getNode(commentId);
									String parentPath = node.getUp();
									EquipObject parentNode = mdao.getEquipObject(mdao.fetchId(parentPath));
									
									AuthorizationDAO authDao = new AuthorizationDAO();
									if(!authDao.canModifyExtras(parentNode, userId)) {
										Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User " + userId + " may not modify the comments of node " + parentNode.getId() + ".");
									}
									
									if(parentNode instanceof EquipLockable) {
										EquipLockable el = (EquipLockable) parentNode;
										if(el.isLocked() && el.getLockedByUser() != null && !el.getLockedByUser().equalsIgnoreCase(userId) && !isQcComment) {
											Spark.halt(HTTPStatusCodes.CONFLICT, "The parent entity is locked and cannot be modified.");
										}
									}
									
									dao.updateComment(comment, commentId);

									/*asc.logAuditEntry(
											"Comment was set to: " + "commentType=" + comment.getCommentType()
													+ ",body=" + comment.getBody(),
											commentId, "Comment", userId, Props.isAudit(), Const.AUDIT_SUCCESS, 1L);
									AuditDetails details = asc.new AuditDetails("Comment was set to: " + "commentType=" + comment.getCommentType()
									+ ",body=" + comment.getBody(), comment, userId);*/
									
									AuditDetails details = asc.new AuditDetails("Comment updated", comment, userId);
									details.setContextEntity(parentNode);
									asc.logAuditEntryAsync(details);
									
									ModeShapeDAO bdao = new ModeShapeDAO();
									ModeShapeNode commentNode = bdao.getNode(commentId);
									String upPath = commentNode.getUp();
									String parentId = bdao.fetchId(upPath);
									updateAssociatedProtocol(userId, parentId);
									
									// Update the modified properties of the parent node.
									// If this is not a QC comment, update the modified properties of the parent node.
									if(commentType == null || !isQcComment) {
										bdao.updateModified(parentId, userId);
									}
									
									EquipObject eo = bdao.getEquipObject(parentId);
									if(eo instanceof Assembly) {
										Assembly a = (Assembly) eo;
										if(a.getAssemblyType().equalsIgnoreCase(Assembly.REPORTING_EVENT_TYPE)) {
											EntityVersioningResource.updateAtrFlag(a, false, userId);
										}
									}

									returnJson = commentId;
									response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
								} else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_COMMENT_ERROR);
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No Comment ID was provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				try {
					if (userId != null && commentId != null) {
						if (Props.isAudit()) {
							/*asc.logAuditEntry("Attempt to update comment failed with exception: " + ex.getMessage(),
									commentId, "Comment", userId, Props.isAudit(), Const.AUDIT_FAILURE, 0L);*/
							ModeShapeDAO bdao = new ModeShapeDAO();
							ModeShapeNode commentNode = bdao.getNode(commentId);
							AuditDetails details = asc.new AuditDetails("Attempt to create comment failed with exception " + ex.getMessage(), commentNode.toEquipObject(), userId);
							details.setActionStatus( Const.AUDIT_FAILURE);
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

	// updates the timestamp on the associated protocol for certain types of parent
	// objects

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
			} else if (eo instanceof ReportingEventItem) {
				ReportingAndPublishingDAO rpDao = ModeShapeDAO.getReportingAndPublishingDAO();
				ReportingEventItem rei = rpDao.getReportingItem(parentId);
				String dfId = rei.getDataFrameId();
				if (dfId != null && dfId.length() > 0) {
					EquipObject eo2 = bdao.getEquipObject(dfId);
					if (eo2 instanceof Dataframe) {
						studyIds = ((Dataframe) eo2).getStudyIds();
					}
				}
			} else if (eo instanceof PublishItem) {
				ModeShapeNode publishItemNode = bdao.getNode(parentId);
				String upPath = publishItemNode.getUp();
				String reportingEventItemId = bdao.fetchId(upPath);

				if (reportingEventItemId != null && reportingEventItemId.length() > 0) {
					ReportingAndPublishingDAO rpDao = ModeShapeDAO.getReportingAndPublishingDAO();
					ReportingEventItem rei = rpDao.getReportingItem(reportingEventItemId);
					String dfId = rei.getDataFrameId();
					if (dfId != null && dfId.length() > 0) {
						EquipObject eo2 = bdao.getEquipObject(dfId);
						if (eo2 instanceof Dataframe) {
							studyIds = ((Dataframe) eo2).getStudyIds();
						}
					}
				}
			}

			if (studyIds != null && studyIds.isEmpty() == false) {
				try {
					OpmetaServiceClient osc = new OpmetaServiceClient();
					osc.setHost(Props.getOpmetaServiceServer());
					osc.setPort(Props.getOpmetaSerivcePort());
					for (String studyId : studyIds) {
						LOGGER.info("EntityCommentResource: update protocol for study id=" + studyId);
						osc.updateProtocolModifiedDate(userId, studyId);
					}
				} catch (Exception err) {
					LOGGER.warn("EntityCommentResource: Error updating protocol modification time for comment parent "
							+ parentId, err);
				}
			}
		}

	}
}
