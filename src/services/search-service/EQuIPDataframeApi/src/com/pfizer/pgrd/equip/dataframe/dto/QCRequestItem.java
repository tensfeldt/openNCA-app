package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;
import com.pfizer.pgrd.equip.utils.EquipIdCalculator;

public class QCRequestItem extends EquipObject implements EquipID, EquipCreatable, EquipModifiable {
	public static final String ENTITY_TYPE = "QC Request Item";
	
	// specific
	private String dataframeId;
	private String assemblyId;
	private String qcStatus;
	private List<QCWorkflowItem> qcWorkflowItems = new ArrayList<>();
	
	// EquipCreatable
	private Date created;
	private String createdBy;
	
	// EquipModifiable
	private Date modifiedDate;
	private String modifiedBy;
	
	
	// EquipID
	private String equipId = "";
	
	public QCRequestItem() {
		this.setEntityType(QCRequestItem.ENTITY_TYPE);
	}
	
	public String getDataframeId() {
		return dataframeId;
	}

	public void setDataframeId(String dataframeId) {
		this.dataframeId = dataframeId;
	}

	public String getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(String assemblyId) {
		this.assemblyId = assemblyId;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public List<QCWorkflowItem> getQcWorkflowItems() {
		return qcWorkflowItems;
	}

	public void setQcWorkflowItems(List<QCWorkflowItem> qcWorkflowItems) {
		this.qcWorkflowItems = qcWorkflowItems;
	}



	@Override
	public String getEquipId() {
		return this.equipId;
	}
	@Override
	public void setEquipId(String equipId) {
		this.equipId = equipId;
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

}