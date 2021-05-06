package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

@XmlRootElement

public class Script extends EquipObject implements EquipCommentable, EquipCreatable, EquipModifiable, EquipMetadatable {
	public static final String ENTITY_TYPE = "Script";
	
	// Script specific
	private LibraryReference scriptBody;
	private String computeContainer;
	private String environment;
	private String scriptCriteria;

	// EquipCreatable
	private Date created;
	private String createdBy;
	
	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;

	// EquipMetadatable
	private List<Metadatum> metadata = new ArrayList<>();

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();
	
	public Script() {
		this.setEntityType(Script.ENTITY_TYPE);
	}

	public LibraryReference getScriptBody() {
		return scriptBody;
	}

	public void setScriptBody(LibraryReference scriptBody) {
		this.scriptBody = scriptBody;
	}

	@Override
	public List<Comment> getComments() {
		return this.comments;
	}

	@Override
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public List<Metadatum> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadatum> metadata) {
		this.metadata = metadata;
	}

	public String getComputeContainer() {
		return computeContainer;
	}

	public void setComputeContainer(String computeContainer) {
		this.computeContainer = computeContainer;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getScriptCriteria() {
		return scriptCriteria;
	}

	public void setScriptCriteria(String scriptCriteria) {
		this.scriptCriteria = scriptCriteria;
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
