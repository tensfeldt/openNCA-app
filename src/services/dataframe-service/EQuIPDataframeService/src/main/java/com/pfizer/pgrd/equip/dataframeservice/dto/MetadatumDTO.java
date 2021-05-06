package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;

public class MetadatumDTO extends ModeShapeNode implements EquipDelete, EquipCreated {
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
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:modified")
	private Date modified;
	
	@Expose
	@SerializedName("equip:modifiedBy")
	private String modifiedBy;
	
	public MetadatumDTO() {
		this(null);
	}
	
	public MetadatumDTO(Metadatum md) {
		super();
		this.setPrimaryType(MetadatumDTO.PRIMARY_TYPE);
		this.setNodeName("equip:metadatum");
		
		this.populate(md);
	}
	
	public static List<Metadatum> toMetadatum(List<MetadatumDTO> metadata) {
		List<Metadatum> list = new ArrayList<>();
		if(metadata != null) {
			for(MetadatumDTO dto : metadata) {
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
		md.setCreated(this.getCreated());
		md.setCreatedBy(this.getCreatedBy());
		md.setModifiedBy(this.getModifiedBy());
		md.setModifiedDate(this.getModified());
		md.setEntityType(Metadatum.ENTITY_TYPE);
		return md;
	}
	
	public Metadatum toMetadatum() {
		return (Metadatum) this.toEquipObject();
	}
	
	public static List<MetadatumDTO> fromMetadatum(List<Metadatum> metadata) {
		List<MetadatumDTO> list = new ArrayList<>();
		if(metadata != null) {
			for(Metadatum md : metadata) {
				MetadatumDTO dto = new MetadatumDTO(md);
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
			this.setCreated(md.getCreated());
			this.setCreatedBy(md.getCreatedBy());
			this.setModified(md.getModifiedDate());
			this.setModifiedBy(md.getModifiedBy());
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
	
	public NTFile getComplexValue() {
		return this.getChild(NTFile.class);
	}
	
	public void setComplexData(NTFile file) {
		if(file != null) {
			file.setNodeName("equip:complexValue");
		}
		this.replaceChild(NTFile.class, file);
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

	@Override
	public Date getCreated() {
		return this.created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String getCreatedBy() {
		return this.createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Date getModified() {
		return this.modified;
	}

	@Override
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	@Override
	public String getModifiedBy() {
		return this.modifiedBy;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
}
