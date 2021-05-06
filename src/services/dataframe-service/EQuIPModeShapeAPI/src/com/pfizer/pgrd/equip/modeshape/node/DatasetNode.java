package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class DatasetNode extends ModeShapeNode {
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
	
	public DatasetNode() {
		this(null);
	}
	
	public DatasetNode(Dataset dataset) {
		super();
		this.setPrimaryType(DatasetNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(dataset);
	}
	
	public static List<Dataset> toDataset(List<DatasetNode> datasets) {
		List<Dataset> list = new ArrayList<>();
		if(datasets != null) {
			for(DatasetNode dto : datasets) {
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
		
		NTFileNode complexValue = this.getComplexValue();
		if(complexValue != null) {
			dataset.setComplexDataId(complexValue.getJcrId());
		}
		
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		dataset.setMetadata(metadata);
		
		List<Column> columns = ColumnNode.toColumn(this.getColumns());
		dataset.setColumns(columns);
		
		return dataset;
	}
	
	public Dataset toDataset() {
		return (Dataset) this.toEquipObject();
	}
	
	public static List<DatasetNode> fromDataset(List<Dataset> datasets) {
		List<DatasetNode> list = new ArrayList<>();
		if(datasets != null) {
			for(Dataset set : datasets) {
				DatasetNode dto = new DatasetNode(set);
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
			
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(dataset.getMetadata());
			this.setMetadata(metadata);
			
			List<ColumnNode> columns = ColumnNode.fromColumn(dataset.getColumns());
			this.setColumns(columns);
		}
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<ColumnNode> getColumns() {
		return this.getChildren(ColumnNode.class);
	}
	
	public void setColumns(List<ColumnNode> columns) {
		this.replaceChildren(ColumnNode.class, columns);
	}
	
	public NTFileNode getComplexValue() {
		return this.getChild(NTFileNode.class);
	}
	
	public void setComplexData(NTFileNode file) {
		if(file != null) {
			file.setNodeName("equip:complexData");
		}
		this.replaceChild(NTFileNode.class, file);
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
		NTFileNode file = this.getComplexValue();
		if(file != null) {
			NTResourceNode r = file.getContent();
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