package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyUtils;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.CopyException;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class CopyNodeResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyNodeResource.class);

	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String id = null;
			String userId = null;
			EquipObject entity = null;
			List<String> studyIds = null;
			
			try {
				id = request.params(":id");
				if (id != null && !id.isEmpty()) {
					userId = request.headers("IAMPFIZERUSERCN");
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}
					
					AuthorizationDAO auth = new AuthorizationDAO();
					boolean isOk = auth.checkPrivileges("lineage copy", "GET", userId);
					
					if (!isOk) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN,
								"User " + userId + " does not have privileges to copy lineage");
					}
					
					ModeShapeDAO dao = new ModeShapeDAO();
					entity = dao.getEquipObject(id);
					if (entity != null) {
						entity = CopyUtils.copyNode(userId, entity, userId);
						
						if (entity != null) {
							if( Props.isAudit() ){
								EquipID equipIdObject = (EquipID)entity;
								EquipVersionable equipVersionableObject = (EquipVersionable)entity;								
							}
							
							json = marshalObject(entity);
							response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);

							String location = "";
							if (entity instanceof Analysis) {
								location = "/analyses/";
								studyIds = ((Analysis)entity).getStudyIds();
							} else if (entity instanceof Assembly) {
								location = "/assemblies/";
								studyIds = ((Assembly)entity).getStudyIds();
							} else if (entity instanceof Dataframe) {
								location = "/dataframes/";
								studyIds = ((Dataframe)entity).getStudyIds();
							}

							location += entity.getId();
							
							if( studyIds != null && studyIds.isEmpty() == false ) {
								// call opmeta service to update modification time on associated protocol
								try {
									OpmetaServiceClient osc = new OpmetaServiceClient();
									osc.setHost(Props.getOpmetaServiceServer());
									osc.setPort(Props.getOpmetaSerivcePort());
									for(String studyId: studyIds) {
										LOGGER.info("CopyNodeResource: update protocol for study id=" + studyId);
										osc.updateProtocolModifiedDate(userId, studyId);
									}
								}
								catch(Exception err) {
									LOGGER.warn("CopyNodeResource: Error updating protocol modification time for entity " + entity.getId(), err);
								}
							}
							
							response.header(HTTPHeaders.LOCATION, location);
						} else {
							Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "The entity failed to be copied.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No entity with ID '" + id + "' was found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No entity ID was provided.");
				}
			} catch (CopyException ce) {				
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, ce.getMessage());
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}
	};
}