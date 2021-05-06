package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipDeleteMixin;

public class MetadatumNode extends ModeShapeNode implements EquipDeleteMixin {
	public static final String PRIMARY_TYPE = "equip:kvp";
	
	@Expose
	@SerializedName("equip:key")
	private String key;
	
	@Expose
	@SerializedName("equip:value")
	private List<String> value = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:valueType")
	private String valueType;
	
	@Expose
	@SerializedName("equip:deleteFlag")
	private boolean isDeleted;
	
	public MetadatumNode() {
		this(null);
	}
	
	public MetadatumNode(Metadatum md) {
		super();
		this.setPrimaryType(MetadatumNode.PRIMARY_TYPE);
		this.setNodeName("equip:metadatum");
		
		this.populate(md);
	}
	
	public static List<Metadatum> toMetadatum(List<MetadatumNode> metadata) {
		List<Metadatum> list = new ArrayList<>();
		if(metadata != null) {
			for(MetadatumNode dto : metadata) {
				Metadatum md = dto.toMetadatum();
				list.add(md);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Metadatum md = new Metadatum();
		md.setDeleted(this.isDeleted());
		md.setKey(this.getKey());
		md.setValue(this.getValue());
		md.setValueType(this.getValueType());
		md.setId(this.getJcrId());
		
		return md;
	}
	
	public Metadatum toMetadatum() {
		return (Metadatum) this.toEquipObject();
	}
	
	public static List<MetadatumNode> fromMetadatum(List<Metadatum> metadata) {
		List<MetadatumNode> list = new ArrayList<>();
		if(metadata != null) {
			for(Metadatum md : metadata) {
				MetadatumNode dto = new MetadatumNode(md);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Metadatum md) {
		if(md != null) {
			this.setDeleted(md.isDeleted());
			this.setKey(md.getKey());
			this.setValue(md.getValue());
			this.setValueType(md.getValueType());
			this.setJcrId(md.getId());
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	
	public NTFileNode getComplexValue() {
		return this.getChild(NTFileNode.class);
	}
	
	public void setComplexData(NTFileNode file) {
		if(file != null) {
			file.setNodeName("equip:complexValue");
		}
		this.replaceChild(NTFileNode.class, file);
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public boolean isObsolete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setObsolete(boolean isObsolete) {
		// TODO Auto-generated method stub
		
	}
}
