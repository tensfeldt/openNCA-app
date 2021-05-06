package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class AssemblyExtractor implements ResultSetExtractor<Assembly> {
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
	public List<Assembly> extract(JCRQueryResultSet resultSet) {
		return this.extract(resultSet, this.getAlias());
	}

	@Override
	public List<Assembly> extract(JCRQueryResultSet resultSet, String alias) {
		List<Assembly> list = new ArrayList<>();
		if (resultSet != null) {
			for (Row row : resultSet.getRows()) {
				Assembly a = this.extract(row, alias);
				list.add(a);
			}
		}

		return list;
	}
	
	public Assembly extract(Row row, String alias) {
		Assembly a = null;
		if(row != null) {
			a = new Assembly();
			a.setAssemblyIds(row.getStringList(alias + ".equip:assemblyIds"));
			a.setAssemblyType(row.getString(alias + ".equip:assemblyType"));
			a.setCommitted(row.getBoolean(alias + ".equip:versionCommitted"));
			a.setCreated(row.getDate(alias + ".equip:created"));
			a.setCreatedBy(row.getString(alias + ".equip:createdBy"));
			a.setDataframeIds(row.getStringList(alias + ".equip:dataframeIds"));
			a.setDeleteFlag(row.getBoolean(alias + ".equip:deleteFlag"));
			a.setEquipId(row.getString(alias + ".equip:equipId"));
			a.setId(row.getString(alias + ".mode:id"));
			a.setLoadStatus(row.getString(alias + ".equip:loadStatus"));
			a.setModifiedDate(row.getDate(alias + ".equip:modified"));
			a.setModifiedBy(row.getString(alias + ".equip:modifiedBy"));
			a.setObsoleteFlag(row.getBoolean(alias + ".equip:obsoleteFlag"));
			a.setProgramIds(row.getStringList(alias + ".equip:programIds"));
			a.setProjectIds(row.getStringList(alias + ".equip:projectIds"));
			a.setProtocolIds(row.getStringList(alias + ".equip:protocolIds"));
			a.setPublishItemIds(row.getStringList(alias + ".equip:publishedItemIds"));
			a.setQcStatus(row.getString(alias + ".equip:qcStatus"));
			a.setReportingItemIds(row.getStringList(alias + ".equip:reportingEventItemIds"));
			a.setVersionNumber(row.getLong(alias + ".equip:versionNumber"));
			a.setVersionSuperSeded(row.getBoolean(alias + ".equip:versionSuperSeded"));
			a.setStudyIds(row.getStringList(alias + ".equip:studyId"));
			a.setName(row.getString(alias + ".equip:name"));
			a.setItemType(row.getString(alias + ".equip:itemType"));
			a.setParentIds(row.getStringList(alias + ".equip:parentIds"));
			a.setReleased(row.getBoolean(alias + ".equip:released"));
			a.setPublished(row.getBoolean(alias + ".equip:published"));
			a.setLocked(row.getBoolean(alias + ".equip:lockFlag"));
			a.setLockedByUser(row.getString(alias + ".equip:lockedByUser"));
		}
		
		return a;
	}
}
