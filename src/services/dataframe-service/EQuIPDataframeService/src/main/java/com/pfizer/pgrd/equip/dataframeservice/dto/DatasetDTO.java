package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class DatasetDTO extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "equip:dataset";
	
	@Expose
	@SerializedName("equip:data")
	private String data;
	
	@Expose
	@SerializedName("equip:stdIn")
	private String stdIn;
	
	@Expose
	@SerializedName("equip:stdOut")
	private String stdOut;
	
	@Expose
	@SerializedName("equip:stdErr")
	private String stdErr;
	
	@Expose
	@SerializedName("equip:dataSize")
	private long dataSize;
	
	public DatasetDTO() {
		this(null);
	}
	
	public DatasetDTO(Dataset dataset) {
		super();
		this.setPrimaryType(DatasetDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(dataset);
	}
	
	public static List<Dataset> toDataset(List<DatasetDTO> datasets) {
		List<Dataset> list = new ArrayList<>();
		if(datasets != null) {
			for(DatasetDTO dto : datasets) {
				Dataset set = dto.toDataset();
				list.add(set);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Dataset dataset = new Dataset();
		dataset.setData(this.getData());
		dataset.setId(this.getJcrId());
		dataset.setStdErr(this.getStdErr());
		dataset.setStdIn(this.getStdIn());
		dataset.setStdOut(this.getStdOut());
		dataset.setDataSize(this.getDataSize());
		dataset.setMimeType(this.getMimeType());
		
		NTFile complexValue = this.getComplexValue();
		if(complexValue != null) {
			dataset.setComplexDataId(complexValue.getJcrId());
		}
		
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		dataset.setMetadata(metadata);
		
		List<Column> columns = ColumnDTO.toColumn(this.getColumns());
		dataset.setColumns(columns);
		
		return dataset;
	}
	
	public Dataset toDataset() {
		return (Dataset) this.toEquipObject();
	}
	
	public static List<DatasetDTO> fromDataset(List<Dataset> datasets) {
		List<DatasetDTO> list = new ArrayList<>();
		if(datasets != null) {
			for(Dataset set : datasets) {
				DatasetDTO dto = new DatasetDTO(set);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Dataset dataset) {
		if(dataset != null) {
			this.setData(dataset.getData());
			this.setStdErr(dataset.getStdErr());
			this.setStdIn(dataset.getStdIn());
			this.setStdOut(dataset.getStdOut());
			this.setDataSize(dataset.getDataSize());
			
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(dataset.getMetadata());
			this.setMetadata(metadata);
			
			List<ColumnDTO> columns = ColumnDTO.fromColumn(dataset.getColumns());
			this.setColumns(columns);
		}
	}
	
	public List<MetadatumDTO> getMetadata() {
		return this.getChildren(MetadatumDTO.class);
	}
	
	public void setMetadata(List<MetadatumDTO> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<ColumnDTO> getColumns() {
		return this.getChildren(ColumnDTO.class);
	}
	
	public void setColumns(List<ColumnDTO> columns) {
		this.replaceChildren(ColumnDTO.class, columns);
	}
	
	public NTFile getComplexValue() {
		return this.getChild(NTFile.class);
	}
	
	public void setComplexData(NTFile file) {
		if(file != null) {
			file.setNodeName("equip:complexData");
		}
		this.replaceChild(NTFile.class, file);
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
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

	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	public String getMimeType() {
		String mimeType = null;
		NTFile file = this.getComplexValue();
		if(file != null) {
			NTResource r = file.getContent();
			if(r != null) {
				mimeType = r.getJcrMimeType();
				if(mimeType == null) {
					mimeType = "application/octet-stream";
				}
			}
		}
		
		return mimeType;
	}
}