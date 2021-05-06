package com.pfizer.pgrd.equip.services.search;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.services.client.BaseClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

public class SearchServiceClient extends BaseClient {

	public SearchServiceClient() throws ServiceCallerException {
		super();
	}
	
	/**
	 * Searches Elastic for all unique {@link Assembly} and {@link Dataframe} objects whose study ID matches any of the ones provided.
	 * @param studyIds
	 * @return {@link List}<{@link EquipObject}>
	 * @throws ServiceCallerException
	 */
	public List<EquipObject> searchObjectsByStudyId(List<String> studyIds) throws ServiceCallerException {
		return this.searchObjectsByStudyId(studyIds, null, null);
	}
	
	/**
	 * Searches Elastic for all unique {@link Assembly} and {@link Dataframe} objects whose study ID matches any of the ones provided and whose node type matches the one provided.
	 * @param studyIds
	 * @return {@link List}<{@link EquipObject}>
	 * @throws ServiceCallerException
	 */
	public List<EquipObject> searchObjectsByStudyId(List<String> studyIds, String nodeType, String equipType) throws ServiceCallerException {
		List<EquipObject> objects = new ArrayList<>();
		if(studyIds != null) {
			for(String studyId : studyIds) {
				if(studyId != null) {
					List<EquipObject> sub = this.searchObjectsByStudyId(studyId, nodeType, equipType);
					for(EquipObject s : sub) {
						boolean notDup = true;
						for(EquipObject eo : objects) {
							if(s.getId().equals(eo.getId())) {
								notDup = false;
								break;
							}
						}
						
						if(notDup) {
							objects.add(s);
						}
					}
				}
			}
		}
		
		return objects;
	}
	
	/**
	 * Searches Elastic for all {@link Assembly} and {@link Dataframe} objects whose study ID matches the one provided.
	 * @param studyId
	 * @return {@link List}<{@link EquipObject}>
	 * @throws ServiceCallerException
	 */
	public List<EquipObject> searchObjectsByStudyId(String studyId) throws ServiceCallerException {
		return this.searchObjectsByStudyId(studyId, null, null);
	}
	
	/**
	 * Searches Elastic for all {@link Assembly} and {@link Dataframe} objects whose study ID matches the one provided and whose node type matches the one provided.
	 * @param studyId
	 * @return {@link List}<{@link EquipObject}>
	 * @throws ServiceCallerException
	 */
	public List<EquipObject> searchObjectsByStudyId(String studyId, String nodeType, String equipType) throws ServiceCallerException {
		List<EquipObject> objects = new ArrayList<>();
		ServiceResponse response = this.searchByStudyId(studyId, nodeType, equipType);
		if(response != null && response.getCode() == 200) {
			GsonBuilder gb = new GsonBuilder();
			gb.registerTypeHierarchyAdapter(EquipObject.class, new SearchServiceClient.SearchServiceResultAdapter());
			Gson gson = gb.create();
	
			String json = response.getResponseAsString();
			EquipObject[] equipObjects = gson.fromJson(json, EquipObject[].class);
			objects = Arrays.asList(equipObjects);
		}
		
		return objects;
	}
	
	/**
	 * Searches Elastic for all {@link Assembly} and {@link Dataframe} objects whose study ID matches the one provided.
	 * @param studyId
	 * @return {@link ServiceResponse}
	 * @throws ServiceCallerException
	 */
	public ServiceResponse searchByStudyId(String studyId) throws ServiceCallerException {
		return this.searchByStudyId(studyId, null, null);
	}
	
	/**
	 * Searches Elastic for all {@link Assembly} and {@link Dataframe} objects whose study ID matches the one provided and whose node type matches the one provided.
	 * @param studyId
	 * @return {@link ServiceResponse}
	 * @throws ServiceCallerException
	 */
	public ServiceResponse searchByStudyId(String studyId, String nodeType, String equipType) throws ServiceCallerException {
		String uri = this.getBaseURI() + "/searchLineage/" + studyId;
		
		if(nodeType != null) {
			nodeType = nodeType.trim();
			if(nodeType.isEmpty()) {
				nodeType = null;
			}
		}
		if(equipType != null) {
			equipType = equipType.trim();
			if(equipType.isEmpty()) {
				equipType = null;
			}
		}
		
		if(nodeType != null || equipType != null) {
			uri += "?";
		}
		
		if(nodeType != null) {
			uri += "nodeType=" + nodeType.replace(" ", "+");
		}
		if(equipType != null) {
			if(nodeType != null) {
				uri += "&";
				if(nodeType.equalsIgnoreCase("assembly")) {
					uri += "assemblyType";
				}
				else if(nodeType.equalsIgnoreCase("dataframe")) {
					uri += "dataframeType";
				}
				else {
					uri += "equipType";
				}
			}
			else {
				uri += "equipType";
			}
			
			uri += "=" + equipType.replace(" ", "+");
		}
		
		return this.get(uri);
	}
	
	protected String getBaseURI() {
		return super.getBaseURI() + "/EQUIPSearchService";
	}
	
	/**
	 * This class handles parsing a lineage Search Service response, which returns
	 * an array of Analysis, Assembly, and Dataframe objects.
	 * 
	 * @author QUINTJ16
	 *
	 */
	public static class SearchServiceResultAdapter implements JsonDeserializer<EquipObject> {
		private static final Gson GENERIC_GSON = new Gson();

		@Override
		public EquipObject deserialize(JsonElement ele, Type eleType, JsonDeserializationContext context) {
			EquipObject eo = null;
			if (ele != null && !ele.isJsonNull()) {
				JsonObject object = ele.getAsJsonObject();
				if (object != null && !object.isJsonNull()) {
					JsonElement ntEle = object.get("nodeType");
					if (ntEle != null && !ntEle.isJsonNull()) {
						String nodeType = ntEle.getAsString();

						if (nodeType.equalsIgnoreCase("dataframe")) {
							eo = GENERIC_GSON.fromJson(object, Dataframe.class);
						} else if (nodeType.equalsIgnoreCase("analysis")) {
							eo = GENERIC_GSON.fromJson(object, Analysis.class);
						} else if (nodeType.equalsIgnoreCase("assembly")) {
							eo = GENERIC_GSON.fromJson(object, Assembly.class);
						} else if (nodeType.equalsIgnoreCase(Assembly.BATCH_TYPE)) {
							eo = GENERIC_GSON.fromJson(object, Batch.class);
						} else if(nodeType.equalsIgnoreCase("reportingeventitem")) {
							eo = GENERIC_GSON.fromJson(object, ReportingEventItem.class);
						}
					}
				}
			}
			
			return eo;
		}
	}
}

