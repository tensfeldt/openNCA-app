package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;

public class PromotionNode extends ModeShapeNode implements EquipCreatedMixin, EquipIdMixin {
	public static final String PRIMARY_TYPE = "equip:promotion";
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:equipId")
	private String equipId;
	
	@Expose
	@SerializedName("equip:modifiedBy")
	private String modifiedBy;
	
	@Expose
	@SerializedName("equip:modified")
	private Date modified;
	
	@Expose
	@SerializedName("equip:promotionStatus")
	private String promotionStatus;
	
	@Expose
	@SerializedName("equip:dataStatus")
	private String dataStatus;
	
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String restrictionStatus;
	
	@Expose
	@SerializedName("equip:dataBlindingStatus")
	private String dataBlindingStatus;
	
	public PromotionNode() {
		this(null);
	}
	
	public PromotionNode(Promotion promotion) {
		super();
		this.setPrimaryType(PromotionNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(promotion);
	}
	
	public static List<Promotion> toPromotion(List<PromotionNode> promotions) {
		List<Promotion> list = new ArrayList<>();
		if(promotions != null) {
			for(PromotionNode dto : promotions) {
				Promotion p = dto.toPromotion();
				list.add(p);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Promotion promotion = new Promotion();
		promotion.setDataStatus(this.getDataStatus());
		promotion.setPromotionStatus(this.getPromotionStatus());
		promotion.setRestrictionStatus(this.getRestrictionStatus());
		promotion.setCreated(this.getCreated());
		promotion.setCreatedBy(this.getCreatedBy());
		promotion.setEquipId(this.getEquipId());
		promotion.setId(this.getJcrId());
		promotion.setModifiedBy(this.getModifiedBy());
		promotion.setModifiedDate(this.getModified());
		
		List<Comment> comments = CommentNode.toComment(this.getComments());
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		promotion.setComments(comments);
		promotion.setMetadata(metadata);
		
		return promotion;
	}
	
	public Promotion toPromotion() {
		return (Promotion) this.toEquipObject();
	}
	
	public static List<PromotionNode> fromPromotion(List<Promotion> promotions) {
		List<PromotionNode> list = new ArrayList<>();
		if(promotions != null) {
			for(Promotion p : promotions) {
				PromotionNode dto = new PromotionNode(p);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Promotion promotion) {
		if(promotion != null) {
			this.setDataStatus(promotion.getDataStatus());
			this.setCreated(promotion.getCreated());
			this.setCreatedBy(promotion.getCreatedBy());
			this.setEquipId(promotion.getEquipId());
			this.setModified(promotion.getModifiedDate());
			this.setModifiedBy(promotion.getModifiedBy());
			this.setPromotionStatus(promotion.getPromotionStatus());
			this.setRestrictionStatus(promotion.getRestrictionStatus());
			
			List<CommentNode> comments = CommentNode.fromComment(promotion.getComments());
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(promotion.getMetadata());
			this.setComments(comments);
			this.setMetadata(metadata);
		}
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<CommentNode> getComments() {
		return this.getChildren(CommentNode.class);
	}
	
	public void setComments(List<CommentNode> comments) {
		this.replaceChildren("equip:comment", comments);
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

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getPromotionStatus() {
		return promotionStatus;
	}

	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getRestrictionStatus() {
		return restrictionStatus;
	}

	public void setRestrictionStatus(String restrictionStatus) {
		this.restrictionStatus = restrictionStatus;
	}
	
	public String getDataBlindingStatus() {
		return dataBlindingStatus;
	}

	public void setDataBlindingStatus(String dataBlindingStatus) {
		this.dataBlindingStatus = dataBlindingStatus;
	}
	
}