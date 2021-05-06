package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class EquipIDDAOImpl extends ModeShapeDAO implements EquipIDDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(EquipIDDAOImpl.class);	
	private static final String ALIAS = "eq";
	private static final String BASE_SELECT = "SELECT " + ALIAS + ".[mode:id] FROM [equip:searchable] AS " + ALIAS;
	
	@Override
	public List<EquipObject> getItem(String equipId) {
		return this.getItem(new String[] { equipId });
	}
	
	@Override
	public List<EquipObject> getItem(List<String> equipIds) {
		List<EquipObject> list = new ArrayList<>();
		if(equipIds != null) {
			list = this.getItem(equipIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<EquipObject> getItem(String[] equipIds) {
		List<EquipObject> list = new ArrayList<>();
		if(equipIds != null) {
			StringBuilder sqlBuilder = new StringBuilder();
			for(int i = 0; i < equipIds.length; i++) {
				String id = equipIds[i];
				if(i == 0) {
					sqlBuilder.append(BASE_SELECT + " WHERE ");
				}
				else {
					sqlBuilder.append(" OR ");
				}
				
				sqlBuilder.append(ALIAS + ".[equip:equipId] = \"" + id.trim() + "\"");
			}
			
			String sql = sqlBuilder.toString();
			List<String> nodeIds = this.query(sql);
			
			ModeShapeClient client = this.getModeShapeClient();
			for(String nodeId : nodeIds) {
				ModeShapeNode node = client.getNode(nodeId, true);
				EquipObject eo = node.toEquipObject();
				if(eo instanceof EquipID) {
					list.add(eo);
				}
			}
		}
		
		return list;
	}
	
	private List<String> query(String sql) {
		List<String> nodeIds = new ArrayList<>();
		if(sql != null) {
			ModeShapeClient client = this.getModeShapeClient();
			
			try {
				JCRQueryResultSet resultSet = client.query(sql);
				
				if(resultSet != null) {
					for(Row row : resultSet.getRows()) {
						nodeIds.add(row.getString("mode:id"));
					}
				}
			}
			catch(ModeShapeAPIException maie) {
				lOGGER.error("", maie);
				throw new RuntimeException("Persistence layer exception upon equip id dao query");				
			}
		}
		
		return nodeIds;
	}
}
