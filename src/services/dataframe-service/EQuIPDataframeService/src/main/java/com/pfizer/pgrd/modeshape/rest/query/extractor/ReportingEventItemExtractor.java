package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class ReportingEventItemExtractor implements ResultSetExtractor<ReportingEventItem> {
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
	public List<ReportingEventItem> extract(JCRQueryResultSet resultSet) {
		return this.extract(resultSet, this.getAlias());
	}

	@Override
	public List<ReportingEventItem> extract(JCRQueryResultSet resultSet, String alias) {
		List<ReportingEventItem> list = new ArrayList<>();
		
		if(resultSet != null) {
			for(Row row : resultSet.getRows()) {
				ReportingEventItem rei = this.extract(row, alias);
				if(rei != null) {
					list.add(rei);
				}
			}				
		}
		
		return list;
	}
	
	@Override
	public ReportingEventItem extract(Row row, String alias) {
		ReportingEventItem rei = null;
		if(row != null) {
			rei = new ReportingEventItem();
			rei.setCommitted(row.getBoolean(alias + ".equip:versionCommitted"));
			rei.setVersionNumber(row.getLong(alias + ".equip:versionNumber"));
			rei.setDeleteFlag(row.getBoolean(alias + ".equip:deleteFlag"));
			rei.setVersionSuperSeded(row.getBoolean(alias + ".equip:versionSuperSeded"));
			rei.setObsoleteFlag(row.getBoolean(alias + ".equip:obsoleteFlag"));
			
			//new properties added to support versioning mh 08/27/18
			rei.setReportingEventId(row.getString(alias + ".equip:parentReportingEventId"));
			rei.setAssemblyId(row.getString(alias + ".equip:assemblyId"));
			rei.setCreated(row.getDate(alias + ".equip:created"));
			rei.setCreatedBy(row.getString(alias + ".equip:createdBy"));
			rei.setDataFrameId(row.getString(alias  + ".equip:dataframeId"));
			rei.setEquipId(row.getString(alias  + ".equip:equipId"));
			rei.setId(row.getString(alias  + ".mode:id"));
			rei.setIncluded(row.getBoolean(alias  + ".equip:included"));
		}
		
		return rei;
	}
}
