package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistSummaryItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequestItem;
import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;

public class QCRequestDTO extends ModeShapeNode implements EquipCreated, EquipID {
	public static final String PRIMARY_TYPE = "equip:qcrequest";
	
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
	
	@Expose
	@SerializedName("equip:equipId")
	private String equipId;
	
	@Expose
	@SerializedName("equip:dataframeId")
	private String dataframeId;
	
	@Expose
	@SerializedName("equip:assemblyId")
	private String assemblyId;
	
	@Expose
	@SerializedName("equip:qcStatus")
	private String qcStatus;
	
	@Expose
	@SerializedName("equip:qcDueDate")
	private Date dueDate;
	
	//public QCRequestDTO() {
	//	this(null);
	//}
	
	public QCRequestDTO(QCRequest request) {
		super();
		this.setPrimaryType(QCRequestDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(request);
	}
	
	public QCRequestDTO(QCRequestItem qcRequestItem) {
		super();
		this.setPrimaryType("equip:qcrequestitem");
		this.setNodeName(this.getPrimaryType());
		
		this.populateItem(qcRequestItem);
	}

	public static List<QCRequest> toQCRequest(List<QCRequestDTO> requests) {
		List<QCRequest> list = new ArrayList<>();
		if(requests != null) {
			for(QCRequestDTO dto : requests) {
				QCRequest req = dto.toQCRequest();
				list.add(req);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		QCRequest request = new QCRequest();
		request.setAssemblyId(this.getAssemblyId());
		request.setCreated(this.getCreated());
		request.setCreatedBy(this.getCreatedBy());
		request.setDataframeId(this.getDataframeId());
		request.setEquipId(this.getEquipId());
		request.setModifiedBy(this.getModifiedBy());
		request.setModifiedDate(this.getModified());
		request.setId(this.getJcrId());
		request.setQcDueDate(this.getDueDate());
		request.setQcStatus(this.getQcStatus());
		
		List<QCWorkflowItem> wfi = QCWorkflowItemDTO.toQCWorkflowItem(this.getWorkflowItems());
		List<QCChecklistSummaryItem> clsi = QCChecklistSummaryItemDTO.toQCChecklistSummaryItem(this.getChecklistSummaryItems());
		List<QCChecklistItem> cli = QCChecklistItemDTO.toQCChecklistItem(this.getChecklistItems());
		request.setQcWorkflowItems(wfi);
		request.setQcChecklistSummaryItems(clsi);
		request.setQcChecklistItems(cli);
		
		LibraryReferenceDTO libRef = this.getTemplateId();
		if(libRef != null) {
			LibraryReference id = libRef.toLibraryReference();
			request.setChecklistTemplateId(id);
		}
		
		return request;
	}
	
	public QCRequest toQCRequest() {
		return (QCRequest) this.toEquipObject();
	}
	
	public static List<QCRequestDTO> fromQCRequest(List<QCRequest> requests) {
		List<QCRequestDTO> list = new ArrayList<>();
		if(requests != null) {
			for(QCRequest request : requests) {
				QCRequestDTO dto = new QCRequestDTO(request);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public QCRequestItem toQCRequestItem() {
		QCRequestItem request = new QCRequestItem();
		request.setAssemblyId(this.getAssemblyId());
		request.setCreated(this.getCreated());
		request.setCreatedBy(this.getCreatedBy());
		request.setDataframeId(this.getDataframeId());
		request.setEquipId(this.getEquipId());
		request.setModifiedBy(this.getModifiedBy());
		request.setModifiedDate(this.getModified());
		request.setId(this.getJcrId());
		request.setQcStatus(this.getQcStatus());
		
		List<QCWorkflowItem> wfi = QCWorkflowItemDTO.toQCWorkflowItem(this.getWorkflowItems());
		request.setQcWorkflowItems(wfi);
	
		return request;
	}
	
	
	public void populateItem(QCRequestItem request) {
		if(request != null) {
			this.setAssemblyId(request.getAssemblyId());
			this.setCreated(request.getCreated());
			this.setCreatedBy(request.getCreatedBy());
			this.setDataframeId(request.getDataframeId());
			this.setEquipId(request.getEquipId());
			this.setModified(request.getModifiedDate());
			this.setModifiedBy(request.getModifiedBy());
			this.setQcStatus(request.getQcStatus());
			
			List<QCWorkflowItemDTO> wfItems = QCWorkflowItemDTO.fromQCWorkflowItem(request.getQcWorkflowItems());
			this.setWorkflowItems(wfItems);
		}
		
	}
	
	public void populate(QCRequest request) {
		if(request != null) {
			this.setAssemblyId(request.getAssemblyId());
			this.setCreated(request.getCreated());
			this.setCreatedBy(request.getCreatedBy());
			this.setDataframeId(request.getDataframeId());
			this.setDueDate(request.getQcDueDate());
			this.setEquipId(request.getEquipId());
			this.setModified(request.getModifiedDate());
			this.setModifiedBy(request.getModifiedBy());
			this.setQcStatus(request.getQcStatus());
			
			List<QCWorkflowItemDTO> wfItems = QCWorkflowItemDTO.fromQCWorkflowItem(request.getQcWorkflowItems());
			List<QCChecklistSummaryItemDTO> clsi = QCChecklistSummaryItemDTO.fromQCChecklistSummaryItem(request.getQcChecklistSummaryItems());
			List<QCChecklistItemDTO> cli = QCChecklistItemDTO.fromQCChecklistItem(request.getQcChecklistItems());
			this.setWorkflowItems(wfItems);
			this.setChecklistSummaryItems(clsi);
			this.setChecklistItems(cli);
			
			if(request.getChecklistTemplateId() != null) {
				LibraryReferenceDTO libRef = new LibraryReferenceDTO(request.getChecklistTemplateId());
				this.setTemplateId(libRef);
			}
		}
	}
	
	public LibraryReferenceDTO getTemplateId() {
		return (LibraryReferenceDTO) this.getChild("equip:checklistTemplateId");
	}
	
	public void setTemplateId(LibraryReferenceDTO templateId) {
		this.replaceChild("equip:checklistTemplateId", templateId);
	}
	
	public List<QCWorkflowItemDTO> getWorkflowItems() {
		return this.getChildren(QCWorkflowItemDTO.class);
	}
	
	public void setWorkflowItems(List<QCWorkflowItemDTO> workflowItems) {
		this.replaceChildren(QCWorkflowItemDTO.class, workflowItems);
	}
	
	public List<QCChecklistSummaryItemDTO> getChecklistSummaryItems() {
		return this.getChildren(QCChecklistSummaryItemDTO.class);
	}
	
	public void setChecklistSummaryItems(List<QCChecklistSummaryItemDTO> summaryItems) {
		this.replaceChildren(QCChecklistSummaryItemDTO.class, summaryItems);
	}
	
	public List<QCChecklistItemDTO> getChecklistItems() {
		return this.getChildren(QCChecklistItemDTO.class);
	}
	
	public void setChecklistItems(List<QCChecklistItemDTO> checklistItems) {
		this.replaceChildren(QCChecklistItemDTO.class, checklistItems);
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

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
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

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}


}