package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class PublishItemExtractor implements ResultSetExtractor<PublishItem> {
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
	public List<PublishItem> extract(JCRQueryResultSet resultSet) {
		return this.extract(resultSet, this.getAlias());
	}

	@Override
	public List<PublishItem> extract(JCRQueryResultSet resultSet, String alias) {
		List<PublishItem> list = new ArrayList<>();
		
		if(resultSet != null) {
			for(Row row : resultSet.getRows()) {
				PublishItem rei = this.extract(row, alias);
				if(rei != null) {
					list.add(rei);
				}
			}
		}
		
		return list;
	}
	
	@Override
	public PublishItem extract(Row row, String alias) {
		PublishItem rei = null;
		if(row != null) {
			rei = new PublishItem();
			rei.setCommitted(row.getBoolean(alias + ".equip:versionCommitted"));
			rei.setVersionNumber(row.getLong(alias + ".equip:versionNumber"));
			rei.setDeleteFlag(row.getBoolean(alias + ".equip:deleteFlag"));
			rei.setVersionSuperSeded(row.getBoolean(alias + ".equip:versionSuperSeded"));
			rei.setObsoleteFlag(row.getBoolean(alias + ".equip:obsoleteFlag"));
		}
		
		return rei;
	}
}
