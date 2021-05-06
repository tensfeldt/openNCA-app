package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistSummaryItem;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipCreated;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipID;

public class QCChecklistSummaryItemDTO extends ModeShapeNode implements EquipCreated, EquipID {
	public static final String PRIMARY_TYPE = "equip:qcchecklistsummaryitem";
	
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
	@SerializedName("equip:checklistSummaryItemAnswer")
	private String answer = "Unanswered";
	
	public QCChecklistSummaryItemDTO() {
		this(null);
	}
	
	public QCChecklistSummaryItemDTO(QCChecklistSummaryItem item) {
		super();
		this.setPrimaryType(QCChecklistSummaryItemDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<QCChecklistSummaryItem> toQCChecklistSummaryItem(List<QCChecklistSummaryItemDTO> items) {
		List<QCChecklistSummaryItem> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistSummaryItemDTO dto : items) {
				QCChecklistSummaryItem item = dto.toQCChecklistSummaryItem();
				list.add(item);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		QCChecklistSummaryItem item = new QCChecklistSummaryItem();
		item.setChecklistSummaryItemAnswer(this.getAnswer());
		item.setCreated(this.getCreated());
		item.setCreatedBy(this.getCreatedBy());
		item.setEquipId(this.getEquipId());
		item.setId(this.getJcrId());
		item.setModifiedBy(this.getModifiedBy());
		item.setModifiedDate(this.getModified());
		
		List<Metadatum> metadata = MetadatumDTO.toMetadatum(this.getMetadata());
		item.setMetadata(metadata);
		
		LibraryReferenceDTO templateId = this.getTemplateId();
		if(templateId != null) {
			LibraryReference libRef = templateId.toLibraryReference();
			item.setChecklistTemplateSummaryItemId(libRef);
		}
		
		return item;
	}
	
	public QCChecklistSummaryItem toQCChecklistSummaryItem() {
		return (QCChecklistSummaryItem) this.toEquipObject();
	}
	
	public static List<QCChecklistSummaryItemDTO> fromQCChecklistSummaryItem(List<QCChecklistSummaryItem> items) {
		List<QCChecklistSummaryItemDTO> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistSummaryItem item : items) {
				QCChecklistSummaryItemDTO dto = new QCChecklistSummaryItemDTO(item);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(QCChecklistSummaryItem item) {
		if(item != null) {
			this.setAnswer(item.getChecklistSummaryItemAnswer());
			this.setCreated(item.getCreated());
			this.setCreatedBy(item.getCreatedBy());
			this.setEquipId(item.getEquipId());
			this.setModified(item.getModifiedDate());
			this.setModifiedBy(item.getModifiedBy());
			
			List<MetadatumDTO> metadata = MetadatumDTO.fromMetadatum(item.getMetadata());
			this.setMetadata(metadata);
			
			if(item.getChecklistTemplateSummaryItemId() != null) {
				LibraryReferenceDTO id = new LibraryReferenceDTO(item.getChecklistTemplateSummaryItemId());
				this.setTemplateId(id);
			}
		}
	}
	
	public List<MetadatumDTO> getMetadata() {
		return this.getChildren(MetadatumDTO.class);
	}
	
	public void setMetadata(List<MetadatumDTO> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public LibraryReferenceDTO getTemplateId() {
		return (LibraryReferenceDTO) this.getChild("equip:checklistTemplateSummaryItemId");
	}
	
	public void setTemplateId(LibraryReferenceDTO templateId) {
		this.replaceChild("equip:checklistTemplateSummaryItemId", templateId);
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

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
