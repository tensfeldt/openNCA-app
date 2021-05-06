package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryMCT;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipDelete;

public class LibraryMCTDTO extends ModeShapeNode implements EquipCreated, EquipDelete {
	public static final String PRIMARY_TYPE = "equipLibrary:mct";
	
	@Expose
	@SerializedName("equip:description")
	private String description;
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:name")
	private String name;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:subType")
	private String subType;
	
	@Expose
	@SerializedName("equip:deleteFlag")
	private String isDeleted;
	
	@Expose
	@SerializedName("equip:derivedDataStatus")
	private String derivedDataStatus;
	
	public LibraryMCTDTO() {
		super();
	}
	
	@Override
	public LibraryMCT toEquipObject() {
		LibraryMCT mct = new LibraryMCT();
		
		mct.setCreated(this.getCreated());
		mct.setCreatedBy(this.getCreatedBy());
		mct.setDeleted(this.isDeleted());
		mct.setDerivedDataStatus(this.getDerivedDataStatus());
		mct.setDescription(this.getDescription());
		mct.setId(this.getJcrId());
		mct.setName(this.getName());
		mct.setSubType(this.getSubType());
		
		for(CommentDTO cdto : this.getComments()) {
			Comment c = (Comment) cdto.toEquipObject();
			if(c.getCreated() == null) {
				c.setCreated(cdto.getJcrCreated());
			}
			if(c.getCreatedBy() == null) {
				c.setCreatedBy(mct.getCreatedBy());
			}
			
			mct.getComments().add(c);
		}
		
		return mct;
	}

	@Override
	public boolean isDeleted() {
		return Boolean.parseBoolean(this.isDeleted);
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		if(isDeleted) {
			this.isDeleted = "true";
		}
		else {
			this.isDeleted = "false";
		}
	}

	@Override
	public boolean isObsolete() {
		return false;
	}

	@Override
	public void setObsolete(boolean isObsolete) {}

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
		return null;
	}

	@Override
	public void setModified(Date modified) {}

	@Override
	public String getModifiedBy() {
		return null;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {}
	
	public List<CommentDTO> getComments() {
		return this.getChildren(CommentDTO.class);
	}
	
	public void setComments(List<CommentDTO> comments) {
		this.replaceChildren(CommentDTO.class, comments);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getDerivedDataStatus() {
		return derivedDataStatus;
	}

	public void setDerivedDataStatus(String derivedDataStatus) {
		this.derivedDataStatus = derivedDataStatus;
	}
}
