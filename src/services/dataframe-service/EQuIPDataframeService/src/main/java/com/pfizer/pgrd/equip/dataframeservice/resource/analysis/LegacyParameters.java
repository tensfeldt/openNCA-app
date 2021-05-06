package com.pfizer.pgrd.equip.dataframeservice.resource.analysis;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class LegacyParameters extends ServiceBaseResource {
	/**
	 * Retrieves and returns the {@code publishViewFilterCriteria} JSON for a specified legacy parameter node.
	 */
	public final static Route getPublishViewFilterCriteria = new Route() {
		private static final int L_PARAMS_INDEX = 0;
		private static final int L_PKP_INDEX = 1;
		
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if(userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
				
				String paramsId = request.params(":paramsId");
				
				Dataframe legacyParameters = this.getOGLegacyParams(paramsId);
				if(legacyParameters == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "No dataframe with id '" + paramsId + "' could be found.");
				}
				
				Dataframe[] dataframes = this.getLPKP(legacyParameters);
				Dataframe lpkp = null;
				if(dataframes != null && dataframes.length == 2) {
					lpkp = dataframes[L_PKP_INDEX];
				}
				
				if(lpkp == null) {
					//Spark.halt(HTTPStatusCodes.NOT_FOUND, "Dataframe " + paramsId + " has no L-PKP child.");
					return "";
				}
				
				ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
				List<Assembly> reportingEvents = rpDao.getReportingEventsByDataframeId(lpkp.getId());
				Assembly reportingEvent = null;
				for(Assembly re : reportingEvents) {
					if(re.getEquipId().startsWith("L-RE-PUB")) {
						reportingEvent = re;
						break;
					}
				}
				
				if(reportingEvent == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "Dataframe " + lpkp.getId() + " is not part of a legacy reporting event.");
				}
				
				PublishItem publishItem = null;
				ModeShapeDAO msDao = new ModeShapeDAO();
				for(String reiId : reportingEvent.getReportingItemIds()) {
					ModeShapeNode node = msDao.getNodeByPath(reiId);
					if(node != null) {
						EquipObject eo = node.toEquipObject();
						if(eo instanceof ReportingEventItem) {
							ReportingEventItem item = (ReportingEventItem) eo;
							if(item.getDataFrameId().equals(lpkp.getId())) {
								publishItem = item.getPublishItem();
								break;
							}
						}
					}
				}
				
				if(publishItem == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "Dataframe " + lpkp.getId() + " has no publish item.");
				}
				
				json = publishItem.getPublishedViewFilterCriteria();
				if(json == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "Publish item " + publishItem.getId() + " for dataframe " + lpkp.getId() + " has no published view filter criteria.");
				}
				
				ServiceBaseResource.returnJson(response);
				response.header("L-PARAMS-ID", dataframes[L_PARAMS_INDEX].getId());
				response.header("L-PKP-ID", lpkp.getId());
			}
			catch(HaltException he) {
				throw he;
			}
			catch(Exception e) {
				e.printStackTrace();
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
			
			return json;
		}
		
		private final Dataframe getOGLegacyParams(String id) {
			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			Dataframe legacyParameters = dDao.getDataframe(id, false);
			
			// If we found a legacy params whose EQUIP ID starts with L-PARAM, but it is not v1, it definitely will not have a PKP.
			// We need to get version 1.
			if(legacyParameters != null && legacyParameters.getEquipId().startsWith("L-PARAM") && legacyParameters.getVersionNumber() > 1) {
				List<Dataframe> allVersions = dDao.getDataframeByEquipId(legacyParameters.getEquipId());
				for(Dataframe df : allVersions) {
					if(df.getVersionNumber() == 1 && df.isCommitted()) {
						legacyParameters = df;
						break;
					}
				}
			}
			
			return legacyParameters;
		}
		
		private final Dataframe[] getLPKP(Dataframe parent) {
			Dataframe[] dataframes = null;
			Dataframe lpkp = null;
			String id = null;
			String eid = null;
			long v = 0;
			if(parent != null) {
				id = parent.getId();
				eid = parent.getEquipId();
				v = parent.getVersionNumber();
				DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
				List<Dataframe> children = dDao.getDataframeByParentDataframeId(parent.getId());
				for(Dataframe df : children) {
					if(df.getEquipId().startsWith("L-PKP")) {
						lpkp = df;
						dataframes = new Dataframe[] {parent, lpkp};
					}
				}
				
				// If no L-PKP was found, check to see if the parent is a copy of an L-PARAM.
				// Scour the comments to see what nodes it may have been copied from and perform this check recursively on those.
				if(lpkp == null && parent.getEquipId().startsWith("C-")) {
					if(parent.getVersionNumber() > 1) {
						List<Dataframe> vs = dDao.getDataframeByEquipId(parent.getEquipId());
						for(Dataframe df : vs) {
							if(df.getVersionNumber() == 1 && df.isCommitted()) {
								parent = dDao.getDataframe(df.getId(), false);
								break;
							}
						}
					}
					
					for(Comment c : parent.getComments()) {
						if(c.getCommentType().equalsIgnoreCase("Copy Comment") && (c.getBody().startsWith("Copied from") || c.getBody().startsWith("Re-executed from"))) {
							String[] parts = c.getBody().split(" ");
							String equipId = parts[2].replace(",", "");
							String versionString = parts[3].replace("v", "").replace(".", "");
							int version = Integer.parseInt(versionString);
							
							List<Dataframe> ogs = dDao.getDataframeByEquipId(equipId);
							Dataframe og = null;
							for(Dataframe df : ogs) {
								if(df.getVersionNumber() == version) {
									og = df;
									break;
								}
							}
							
							if(og != null) {
								dataframes = this.getLPKP(og);
								if(dataframes.length == 2) {
									lpkp = dataframes[L_PKP_INDEX];
									if(lpkp != null) {
										id = og.getId();
										eid = og.getEquipId();
										v = og.getVersionNumber();
										
										System.out.println("Retrieved L-PKP " + lpkp.getId() + " from node " + id + " (" + eid  +" v" + v + ")");
										break;
									}
								}
							}
						}
					}
				}
			}
			
			return dataframes;
		}
	};
}
