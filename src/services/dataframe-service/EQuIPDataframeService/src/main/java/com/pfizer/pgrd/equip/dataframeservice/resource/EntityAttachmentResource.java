package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

public class EntityAttachmentResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EntityAttachmentResource.class);

	/**
	 * A {@link Route} that will fetch the attachments associated with the
	 * dataframe ID.
	 */
	public static final Route get = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			String userId = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			List<Dataframe> dataframes = new ArrayList<Dataframe>();
			List<Dataframe> returnDataframes = new ArrayList<Dataframe>();

			try {
				String objId = request.params(":id");
				
				if (objId != null) {					
					userId = request.queryParams("userId");

					ModeShapeDAO dao = new ModeShapeDAO();
					EquipObject node = dao.getEquipObject(objId);
					
					if (node != null) {
						ServiceBaseResource.handleUserAccess(node, userId);
						
						DataframeDAO dataframeDao = new DataframeDAOImpl();
						if( node instanceof Assembly || node instanceof Analysis ) {
							dataframes = dataframeDao.getAssociatedAssemblyAttachments(objId, userId);
						}
						else if( node instanceof Dataframe) {
							dataframes = dataframeDao.getDataframeAttachments(objId, userId);
						}
						
						Map<String,List<Dataframe>> map = new HashMap<String,List<Dataframe>>();
						
						for( Dataframe df : dataframes) {
							List<Dataframe> list = map.get(df.getEquipId()) == null ? new ArrayList<Dataframe>() : map.get(df.getEquipId());
							list.add(df);
							map.put(df.getEquipId(), list);
						}

						for( String equipId : map.keySet()) {
							Dataframe latest = VersioningDAO.getLatestVersion(map.get(equipId), userId, false);
							returnDataframes.add(latest);
							if(Props.isAudit()){
								/*asc.logAuditEntry(	"Access of Dataframe", 
													objId,
													latest.getDataframeType(),
													userId,
													Props.isAudit(),
													Const.AUDIT_SUCCESS,
													0L);*/
								AuditDetails details = asc.new AuditDetails("Access of Dataframe", node, userId);
								
								asc.logAuditEntryAsync(details);
							}
						}
						
						json = marshalObject(returnDataframes);
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
}
