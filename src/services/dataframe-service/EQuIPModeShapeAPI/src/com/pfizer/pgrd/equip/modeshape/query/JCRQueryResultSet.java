package com.pfizer.pgrd.equip.modeshape.query;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JCRQueryResultSet {
	private static Gson RESULT_GSON = null;
	
	private List<Column> columns;
	private List<Row> rows;
	
	public JCRQueryResultSet() {
		this.columns = new ArrayList<Column>();
		this.rows = new ArrayList<Row>();
	}
	
	public List<Column> getColumns() {
		return this.columns;
	}
	
	public List<Row> getRows() {
		return this.rows;
	}
	
	public static JCRQueryResultSet unmarshal(String json) {
		JCRQueryResultSet result = null;
		if(json != null) {
			initGson();
			result = RESULT_GSON.fromJson(json, JCRQueryResultSet.class);
		}
		
		return result;
	}
	
	private static void initGson() {
		if(RESULT_GSON == null) {
			GsonBuilder gb = new GsonBuilder();
			gb.registerTypeHierarchyAdapter(JCRQueryResultSet.class, new JCRQueryResultSetAdapter());
			
			RESULT_GSON = gb.create();
		}
	}
}

class JCRQueryResultSetAdapter implements JsonDeserializer<JCRQueryResultSet> {
	private static final String JCR_COLUMNS = "columns", JCR_ROWS = "rows";

	@Override
	public JCRQueryResultSet deserialize(JsonElement ele, Type eleType, JsonDeserializationContext context) {
		JCRQueryResultSet resultSet = new JCRQueryResultSet();
		if (ele != null && !ele.isJsonNull()) {
			JsonObject object = ele.getAsJsonObject();
			if (object != null && !object.isJsonNull()) {
				// HANDLE COLUMNS OBJECT
				JsonObject jcrColumns = object.getAsJsonObject(JCR_COLUMNS);
				if(jcrColumns != null) {
					Set<String> keys = jcrColumns.keySet();
					for(String key : keys) {
						String colType = jcrColumns.get(key).getAsString();
						Column col = new Column();
						col.setName(key);
						col.setDataType(colType);
						
						resultSet.getColumns().add(col);
					}
				}
				
				// HANDLE ROWS
				JsonArray jcrRows = object.getAsJsonArray(JCR_ROWS);
				if(jcrRows != null) {
					int size = jcrRows.size();
					for(int i = 0; i < size; i++) {
						JsonObject obj = jcrRows.get(i).getAsJsonObject();
						Row row = new Row();
						
						for(Column col : resultSet.getColumns()) {
							String colName = col.getName();
							
							JsonElement jcrVal = obj.get(colName);
							List<String> vals = new ArrayList<>();
							if(jcrVal != null) {
								if(jcrVal instanceof JsonArray) {
									JsonArray array = jcrVal.getAsJsonArray();
									int as = array.size();
									for(int j = 0; j < as; j++) {
										String v = array.get(j).getAsString();
										vals.add(v);
									}
								}
								else {
									String v = jcrVal.getAsString();
									vals.add(v);
								}
							}
							
							row.getData().put(colName, vals);
						}
						
						resultSet.getRows().add(row);
					}
				}
			}
		}
		
		return resultSet;
	}
}