package com.pfizer.pgrd.equip.dataframeservice.resource.lineage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.EquipIDDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class LineageResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(LineageResource.class);
	private static final String NO_PARAMS_ERROR = "No study IDs or user ID was provided.";
	private static final int ANALYSIS_PREP_MODE = 1, DATA_LOAD_MODE = 2, PROMOTION_MODE = 3;
	
	private static String[] getStudyIds(Request request) {
		String[] studyIds = new String[0];
		if(request != null) {
			String q = request.queryParams("studyId");
			if(q != null) {
				studyIds = q.split(",");
			}
		}
		
		return studyIds;
	}
	
	private static String getUserId(Request request) {
		String userId = null;
		if(request != null) {
			String q = request.queryParams("userId");
			if(q != null) {
				userId = q.trim();
			}
		}
		
		return userId;
	}
	
	private static boolean includeDeleted(Request request) {
		boolean includeDeleted = false;
		if(request != null) {
			String q = request.queryParams("includeDeleted");
			if(q != null) { 
				q = q.trim();
				if(q.equalsIgnoreCase("y") || q.equalsIgnoreCase("yes") || q.equalsIgnoreCase("true")) {
					includeDeleted = true;
				}
			}
		}
		
		return includeDeleted;
	}
	
	/**
	 * This {@link Route} returns a JSON representation of a {@link List} of {@link AssemblyLineageItem} objects representing the analysis 
	 * lineage of the provided study IDs and/or user ID. The analysis lineage will contain all data load assemblies along with their member and 
	 * child dataframes and will halt at all analysis assemblies and subset dataframes. The lineage will start with all data load assemblies related to
	 * the provided study IDs and created by the provided user ID, if supplied, or all data load assemblies created by the user ID if no study IDs 
	 * are provided.
	 */
	public static final Route getAnalysisPrepLineage = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return LineageResource.getLineage(request, response, ANALYSIS_PREP_MODE);
		}
		
	};
	
	/**
	 * This {@link Route} returns a JSON representation of a {@link List} of {@link AssemblyLineageItem} objects representing the data load 
	 * lineage of the provided study IDs and/or user ID. The data load lineage will contain all data loads for the provided study IDs and/or user 
	 * ID including their member dataframes. The lineage will start with all data load assemblies related to
	 * the provided study IDs and created by the provided user ID, if supplied, or all data load assemblies created by the user ID if no study IDs 
	 * are provided.
	 */
	public static final Route getDataloadLineage = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return LineageResource.getLineage(request, response, DATA_LOAD_MODE);
		}
		
	};
	
	/**
	 * This {@link Route} returns a JSON representation of a {@link List} of {@link AssemblyLineageItem} objects representing the promotion 
	 * lineage of the provided study IDs and/or user ID. The data load lineage will contain all data loads for the provided study IDs and/or user 
	 * ID including their member and child dataframes and will halt at all promoted items. The lineage will start with all data load assemblies related to
	 * the provided study IDs and created by the provided user ID, if supplied, or all data load assemblies created by the user ID if no study IDs 
	 * are provided.
	 */
	public static final Route getPromotionLineage = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return LineageResource.getLineage(request, response, PROMOTION_MODE);
		}
		
	};
	
	private static final String getLineage(Request request, Response response, int mode) throws Exception {
		String json = null;
		
		if(request != null && response != null) {
			try {
				String[] studyIds = LineageResource.getStudyIds(request);
				String userId = LineageResource.getUserId(request);
				boolean includeDeleted = LineageResource.includeDeleted(request);
				
				//check for privileges to do this:
				String userCN = request.headers("IAMPFIZERUSERCN");
				if (userCN == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined.");
				}
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges("lineage", "GET", userCN);
				
				if (!isOk) {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userCN + " does not have privileges to get the lineage.");
				}
				
				if(userId == null) {
					userId = userCN;
				}
				
				if(studyIds.length > 0 || userId != null) {
					LineageDAO lDao = getLineageDAO();
					lDao.setAuthUserId(userCN);
					List<AssemblyLineageItem> lineage = new ArrayList<>();
					
					if(mode == ANALYSIS_PREP_MODE) {
						lineage = lDao.getAnalysisPrepLineage(studyIds, userId, includeDeleted);
					}
					else if(mode == DATA_LOAD_MODE) {
						lineage = lDao.getDataLoadLineage(studyIds, userId, includeDeleted);
					}
					else if(mode == PROMOTION_MODE) {
						lineage = lDao.getPromotionLineage(studyIds, userId, includeDeleted);
					}

					json =  ServiceBaseResource.marshalObject(lineage);
				}
				else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, NO_PARAMS_ERROR);
				}
				
				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			}
			catch(Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}
		}
		
		return json;
	}
	
	/**
	 * This {@link Route} returns a JSON representation of a {@link List} of {@link AssemblyLineageItem} objects representing the analysis 
	 * lineage of the provided EQuIP ID. The analysis lineage will contain all data loads related to the provided EQuIP ID, including their member 
	 * and child dataframes and will halt at all analysis assemblies and subset dataframes.
	 */
	public static final Route getIdLineage = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			
			try{
				String userId = LineageResource.getUserId(request);
				boolean includeDeleted = LineageResource.includeDeleted(request);
				
				//check for privileges to do this:
				String userCN = request.headers("IAMPFIZERUSERCN");
				if (userCN == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges("equip id", "GET", userCN);
				
				if (!isOk) {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userCN + " does not have privileges to get the lineage");
				}

				String equipId = request.params(":equipId");
				if(equipId != null) {
					equipId = equipId.trim();
					if(!equipId.isEmpty()) {
						EquipIDDAO eiDao = getEquipIDDAO();
						List<EquipObject> eo = eiDao.getItem(equipId);
						if(!eo.isEmpty()) {
							LineageDAO lDao = getLineageDAO();
							lDao.setAuthUserId(userCN);
							List<AssemblyLineageItem> lineage = lDao.getAnalysisPrepLineageByEquipId(equipId, userId, includeDeleted);

							json = ServiceBaseResource.marshalObject(lineage);
							response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
						}
						else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND, "No item with EQuIP ID '" + equipId + "' could be found.");
						}
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No EQuIP ID was provided.");
					}
				}
				else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No EQuIP ID was provided.");
				}
			}
			catch(Exception ex){
				ServiceExceptionHandler.handleException(ex);
			}
			
			return json;
		}
		
	};
	
	public static final Route updateLineage = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			VersioningDAO.ActionType actionType = VersioningDAO.ActionType.NO_ACTION;

			String outcome = null;
			try {
				// check authorization first
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				// check for privileges to do this:
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges("lineage update", "GET", userId);

				if (!isOk) {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userId + " does not have privileges to update lineage");
				}

				String id = request.params(":startId");
				String action = request.params(":action");
				
				if (id != null && action != null) {
					if( action.equalsIgnoreCase("delete") ) {
						actionType = VersioningDAO.ActionType.DELETE_ACTION;
					}
					else if( action.equalsIgnoreCase("commit") ) {
						actionType = VersioningDAO.ActionType.COMMIT_ACTION;
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Missing or invalid action: " + action);
					}
					
					ModeShapeDAO dao = new ModeShapeDAO();
					ModeShapeNode node = dao.getNode(id);
					if (node.getPrimaryType().equalsIgnoreCase("equip:assembly")) {
						AssemblyDAO adao = getAssemblyDAO();
						Assembly a = adao.getAssembly(node.getJcrId());
						if( a.getAssemblyType().equalsIgnoreCase("data load") ) {
							updateAssemblyDataframes(a, userId, actionType);

							// call opmeta service to update modification time on associated protocol
							try {
								OpmetaServiceClient osc = new OpmetaServiceClient();
								osc.setHost(Props.getOpmetaServiceServer());
								osc.setPort(Props.getOpmetaSerivcePort());
								List<String> studyIds = a.getStudyIds();
								for(String studyId: studyIds) {
									LOGGER.info("LineageResource: update protocl for study id=" + studyId);
									osc.updateProtocolModifiedDate(userId, studyId);
								}
							}
							catch(Exception err) {
								LOGGER.warn("LineageResource: Error updating protocol modification time for node " + node.getJcrId(), err);
							}
							
							response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
							outcome = "Node update was successful.";
						}
						else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Not a data load type=" + a.getAssemblyType());													
						}
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Not an assembly type=" + node.getPrimaryType());						
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No entity ID and/or actionwas provided.");
				}
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return outcome;
		}

	};
	
	// update assemblies or dataframes referenced by the dataload assembly
	
	private static void updateAssemblyDataframes(Assembly a, String userCN, VersioningDAO.ActionType actionType) {
		
		VersioningDAO vdao = new VersioningDAO(a);

		// updat direct child records of the dataload
		vdao.updateDownstream(a, userCN, actionType);

		List<String> dfIds = a.getDataframeIds();

		if( dfIds.isEmpty() == false ) {

			ModeShapeDAO bdao = new ModeShapeDAO();
			// get all of the associated dataframe ids
			Deque<String> idsToUpdate = new ArrayDeque<String>();
			for(String assocDf: dfIds) {
				//String assocDf = bdao.fetchId(dfURL);
				LOGGER.debug("updateAssemblyDataframes: adding initial id to check=" + assocDf);
				idsToUpdate.addFirst(assocDf);
			}
			LOGGER.debug("updateAssemblyDataframes: assoc df ids to check size=" + idsToUpdate.size());
			DataframeDAO ddao = new DataframeDAOImpl();
			do {
				String toUpdate = idsToUpdate.removeFirst();
				List<Dataframe> dependants = ddao.getDataframeByParentDataframeId(toUpdate);
				for(Dataframe dependant: dependants) {
					String newUpdate = dependant.id;
					if( idsToUpdate.contains(newUpdate) == false ) {
						LOGGER.debug("updateAssemblyDataframes: adding new dependant " + newUpdate + " id=" + dependant.getEquipId());
						idsToUpdate.addFirst(newUpdate);
					}
				}
				// update item according to action type
				PropertiesPayload pp = new PropertiesPayload();
				if( actionType == VersioningDAO.ActionType.DELETE_ACTION ) {
					pp.addProperty("equip:deleteFlag", true);
				} else if( actionType == VersioningDAO.ActionType.COMMIT_ACTION ) {
					pp.addProperty("equip:versionCommitted", true);
				}
				bdao.updateNode(toUpdate, pp);

				LOGGER.debug("updateAssemblyDataframes: updated dataframe " + toUpdate);
			} while(idsToUpdate.size() > 0);
			
			// now update the dataload itself
			PropertiesPayload payload = new PropertiesPayload();
			payload.addProperty("equip:modified", new Date());
			payload.addProperty("equip:modifiedBy", userCN);
			bdao.updateNode(a.id, payload);

		}
		LOGGER.debug("updateAssemblyDataframes: checked assoc dfs done");
	}
	
	
}