package com.pfizer.pgrd.modeshape.rest.query.extractor;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.modeshape.rest.query.JCRQueryResultSet;
import com.pfizer.pgrd.modeshape.rest.query.Row;

public class AnalysisExtractor implements ResultSetExtractor<Analysis> {
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
	public List<Analysis> extract(JCRQueryResultSet resultSet) {
		return this.extract(resultSet, this.getAlias());
	}

	@Override
	public List<Analysis> extract(JCRQueryResultSet resultSet, String alias) {
		List<Analysis> list = new ArrayList<>();
		if (resultSet != null) {
			for (Row row : resultSet.getRows()) {
				Analysis analysis = this.extract(row, alias);
				if(analysis != null) {
					list.add(analysis);
				}
			}
		}

		return list;
	}
	
	@Override
	public Analysis extract(Row row, String alias) {
		Analysis analysis = null;
		
		if(row != null) {
			AssemblyExtractor assemblyExtractor = new AssemblyExtractor();
			Assembly assembly = assemblyExtractor.extract(row, alias);
			
			if(assembly != null) {
				analysis = new Analysis();
				analysis.populate(assembly);
				
				analysis.setAnalysisType(row.getString(alias + ".equip:analysisType"));
				
				ModeShapeDAO dao = new ModeShapeDAO();
				analysis.setConfigurationTemplateId(dao.fetchId(row.getString(alias + ".equip:configurationTemplateId")));
				analysis.setKelFlagsDataframeId(dao.fetchId(row.getString(alias + ".equip:kelFlagsDataframeId")));
				analysis.setModelConfigurationDataframeId(dao.fetchId(row.getString(alias + ".equip:modelConfigurationDataframeId")));
				analysis.setParametersDataframeId(dao.fetchId(row.getString(alias + ".equip:parametersDataframeId")));
				analysis.setSubsetDataframeIds(dao.fetchId(row.getStringList(alias + ".equip:subsetDataframeIds")));
			}
		}
		
		return analysis;
	}
}
