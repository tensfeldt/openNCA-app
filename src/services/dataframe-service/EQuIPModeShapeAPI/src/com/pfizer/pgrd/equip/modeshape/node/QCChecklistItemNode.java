package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.QCChecklistItem;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;

public class QCChecklistItemNode extends ModeShapeNode implements EquipCreatedMixin, EquipIdMixin {
public static final String PRIMARY_TYPE = "equip:qcchecklistitem";
	
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
	@SerializedName("equip:qcComment")
	private String qcComment;
	
	@Expose
	@SerializedName("equip:sourceComment")
	private String sourceComment;
	
	public QCChecklistItemNode() {
		this(null);
	}
	
	public QCChecklistItemNode(QCChecklistItem item) {
		super();
		this.setPrimaryType(QCChecklistItemNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(item);
	}
	
	public static List<QCChecklistItem> toQCChecklistItem(List<QCChecklistItemNode> items) {
		List<QCChecklistItem> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistItemNode dto : items) {
				QCChecklistItem item = dto.toQCChecklistItem();
				list.add(item);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		QCChecklistItem item = new QCChecklistItem();
		item.setCreated(this.getCreated());
		item.setCreatedBy(this.getCreatedBy());
		item.setEquipId(this.getEquipId());
		item.setId(this.getJcrId());
		item.setModifiedBy(this.getModifiedBy());
		item.setModifiedDate(this.getModified());
		item.setQcComment(this.getQcComment());
		item.setSourceComment(this.getSourceComment());
		
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		item.setMetadata(metadata);
		
		LibraryReferenceNode templateId = this.getTemplateId();
		if(templateId != null) {
			LibraryReference libRef = templateId.toLibraryReference();
			item.setChecklistTemplateItemId(libRef);
		}
		
		return item;
	}
	
	public QCChecklistItem toQCChecklistItem() {
		return (QCChecklistItem) this.toEquipObject();
	}
	
	public static List<QCChecklistItemNode> fromQCChecklistItem(List<QCChecklistItem> items) {
		List<QCChecklistItemNode> list = new ArrayList<>();
		if(items != null) {
			for(QCChecklistItem item : items) {
				QCChecklistItemNode dto = new QCChecklistItemNode(item);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(QCChecklistItem item) {
		if(item != null) {
			this.setCreated(item.getCreated());
			this.setCreatedBy(item.getCreatedBy());
			this.setEquipId(item.getEquipId());
			this.setModified(item.getModifiedDate());
			this.setModifiedBy(item.getModifiedBy());
			this.setQcComment(item.getQcComment());
			this.setSourceComment(item.getSourceComment());
			
			List<CommentNode> comments = CommentNode.fromComment(item.getComments());
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(item.getMetadata());
			this.setMetadata(metadata);
			
			if(item.getChecklistTemplateItemId() != null) {
				LibraryReferenceNode templateId = new LibraryReferenceNode(item.getChecklistTemplateItemId());
				this.setTemplateId(templateId);
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
		return (LibraryReferenceNode) this.getChild("equip:checklistTemplateItemId");
	}
	
	public void setTemplateId(LibraryReferenceNode templateId) {
		this.replaceChild("equip:checklistTemplateItemId", templateId);
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

	public String getQcComment() {
		return qcComment;
	}

	public void setQcComment(String qcComment) {
		this.qcComment = qcComment;
	}

	public String getSourceComment() {
		return sourceComment;
	}

	public void setSourceComment(String sourceComment) {
		this.sourceComment = sourceComment;
	}
}
