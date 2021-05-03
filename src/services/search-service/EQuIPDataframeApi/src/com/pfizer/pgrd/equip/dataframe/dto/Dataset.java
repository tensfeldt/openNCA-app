package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;

public class Dataset extends EquipObject implements EquipMetadatable {
	public static final String ENTITY_TYPE = "Dataset";
	
	private String stdIn;
	private String stdOut;
	private String stdErr;

	private String data;
	private long dataSize;
	private String mimeType;
	private String complexDataId;
	private List<Column> columns;
	
	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();
	
	public Dataset() {
		this.setEntityType(Dataset.ENTITY_TYPE);
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getComplexDataId() {
		return complexDataId;
	}

	public void setComplexDataId(String complexDataId) {
		this.complexDataId = complexDataId;
	}

	public String getStdIn() {
		return stdIn;
	}

	public void setStdIn(String stdIn) {
		this.stdIn = stdIn;
	}

	public String getStdOut() {
		return stdOut;
	}

	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}

	public String getStdErr() {
		return stdErr;
	}

	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}
	
	public List<String> getParameters() {
		Metadatum params = this.getMetadatum("parameters");
		if(params != null) {
			return params.getValue();
		}
		
		return new ArrayList<>();
	}
	
	public void setParameters(List<String> parameters) {
		Metadatum params = this.getMetadatum("parameters");
		if(params == null) {
			params = new Metadatum();
			params.setKey("parameters");
			this.metadata.add(params);
		}
		
		params.setValue(parameters);
	}

	@Override
	public Metadatum getMetadatum(String key) {
		Metadatum metadatum = null;
		if(this.metadata != null && key != null) {
			for(Metadatum md : this.metadata) {
				if(md.getKey().equalsIgnoreCase(key)) {
					metadatum = md;
					break;
				}
			}
		}
		
		return metadatum;
	}
	
	@Override
	public String getMetadatumValue(String key) {
		String value = null;
		Metadatum md = this.getMetadatum(key);
		if(md != null && md.getValue() != null && !md.getValue().isEmpty()) {
			return md.getValue().get(0);
		}
		
		return value;
	}
}
