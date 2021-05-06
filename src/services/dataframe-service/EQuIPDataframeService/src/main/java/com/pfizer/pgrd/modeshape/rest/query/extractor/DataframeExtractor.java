package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class DataframeExtractor implements ResultSetExtractor<Dataframe> {
	private String alias;
	
	@Override
	public String getAlias() {
		return this.alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public List<Dataframe> extract(JCRQueryResultSet resultSet) {
		return this.extract(resultSet, this.getAlias());
	}

	@Override
	public List<Dataframe> extract(JCRQueryResultSet resultSet, String alias) {
		List<Dataframe> list = new ArrayList<>();
		if( resultSet != null ){
			for (Row row : resultSet.getRows()) {
				Dataframe df = this.extract(row, alias);
				if(df != null) {
					list.add(df);
				}
			}
		}
		
		return list;
	}
	
	@Override
	public Dataframe extract(Row row, String alias) {
		Dataframe df = null;
		if(row != null) {
			df = new Dataframe();
			df.setAssemblyIds(row.getStringList(alias + ".equip:assemblyIds"));
			df.setCommitted(row.getBoolean(alias + ".equip:versionCommitted"));
			df.setCreated(row.getDate(alias + ".equip:created"));
			df.setCreatedBy(row.getString(alias + ".equip:createdBy"));
			df.setDataBlindingStatus(row.getString(alias + ".equip:dataBlindingStatus"));
			df.setDataframeIds(row.getStringList(alias + ".equip:dataframeIds"));
			df.setDataframeType(row.getString(alias + ".equip:dataframeType"));
			df.setDataStatus(row.getString(alias + ".equip:dataStatus"));
			df.setDeleteFlag(row.getBoolean(alias + ".equip:deleteFlag"));
			df.setEquipId(row.getString(alias + ".equip:equipId"));
			df.setId(row.getString(alias + ".mode:id"));
			df.setModifiedBy(row.getString(alias + ".equip:modifiedBy"));
			df.setModifiedDate(row.getDate(alias + ".equip:modified"));
			df.setObsoleteFlag(row.getBoolean(alias + ".equip:obsoleteFlag"));
			df.setProgramIds(row.getStringList(alias + ".equip:programIds"));
			df.setProjectIds(row.getStringList(alias + ".equip:projectIds"));
			df.setPromotionStatus(row.getString(alias + ".equip:promotionStatus"));
			df.setProtocolIds(row.getStringList(alias + ".equip:protocolIds"));
			df.setSubType(row.getString(alias + ".equip:subType"));
			
			if (row.getBoolean(alias + ".equip:released") != null) 
				{df.setReleased(row.getBoolean(alias + ".equip:released")); }
			if (row.getBoolean(alias + ".equip:published") != null) {df.setPublished(row.getBoolean(alias + ".equip:published")); }
			
			df.setQcStatus(row.getString(alias + ".equip:qcStatus"));
			df.setRestrictionStatus(row.getString(alias + ".equip:restrictionStatus"));
			df.setVersionNumber(row.getLong(alias + ".equip:versionNumber"));
			df.setVersionSuperSeded(row.getBoolean(alias + ".equip:versionSuperSeded"));
			df.setStudyIds(row.getStringList(alias + ".equip:studyId"));
			
			ModeShapeDAO dao = new ModeShapeDAO();
			df.setAssemblyIds(dao.fetchId(df.getAssemblyIds()));
			df.setDataframeIds(dao.fetchId(df.getDataframeIds()));
		}
		
		return df;
	}
}