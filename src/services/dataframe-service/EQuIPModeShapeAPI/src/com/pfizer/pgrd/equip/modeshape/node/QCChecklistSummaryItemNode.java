package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistSummaryItem;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;

public class QCChecklistSummaryItemNode extends ModeShapeNode implements EquipCreatedMixin, EquipIdMixin {
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
	
	public QCChecklistSummaryItemNode() {
		this(null);
	}
	
	public QCChecklistSummaryItemNode(QCChecklistSummaryItem item) {
		super();
		this.setPrimaryType(QCChecklistSummaryItemNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<QCChecklistSummaryItem> toQCChecklistSummaryItem(List<QCChecklistSummaryItemNode> items) {
		List<QCChecklistSummaryItem> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistSummaryItemNode dto : items) {
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
		
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		item.setMetadata(metadata);
		
		LibraryReferenceNode templateId = this.getTemplateId();
		if(templateId != null) {
			LibraryReference libRef = templateId.toLibraryReference();
			item.setChecklistTemplateSummaryItemId(libRef);
		}
		
		return item;
	}
	
	public QCChecklistSummaryItem toQCChecklistSummaryItem() {
		return (QCChecklistSummaryItem) this.toEquipObject();
	}
	
	public static List<QCChecklistSummaryItemNode> fromQCChecklistSummaryItem(List<QCChecklistSummaryItem> items) {
		List<QCChecklistSummaryItemNode> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistSummaryItem item : items) {
				QCChecklistSummaryItemNode dto = new QCChecklistSummaryItemNode(item);
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
			
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(item.getMetadata());
			this.setMetadata(metadata);
			
			if(item.getChecklistTemplateSummaryItemId() != null) {
				LibraryReferenceNode id = new LibraryReferenceNode(item.getChecklistTemplateSummaryItemId());
				this.setTemplateId(id);
			}
		}
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public LibraryReferenceNode getTemplateId() {
		return (LibraryReferenceNode) this.getChild("equip:checklistTemplateSummaryItemId");
	}
	
	public void setTemplateId(LibraryReferenceNode templateId) {
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
