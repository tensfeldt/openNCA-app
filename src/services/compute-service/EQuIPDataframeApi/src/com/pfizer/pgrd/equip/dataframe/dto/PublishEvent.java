package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;

public class PublishEvent extends EquipObject implements EquipCreatable, EquipID, EquipCommentable {
	public static final String ENTITY_TYPE = "Publish Event";

	// publishingEvent specific
	private String publishingEventName;
	private String publishingEventPublishStatusKey;
	private String publishingEventPublishedDate;
	private String publishingEventExpirationDate;
	
	private String name;
	
	public PublishEvent() {
		this.setEntityType(PublishEvent.ENTITY_TYPE);
	}

	public String getPublishingEventExpirationDate() {
		return publishingEventExpirationDate;
	}

	public void setPublishingEventExpirationDate(String publishingEventExpirationDate) {
		this.publishingEventExpirationDate = publishingEventExpirationDate;
	}

	private List<String> publishItemIds = new ArrayList<>();

	// EquipCreatable
	private String createdBy;
	private Date created;

	// EquipID
	private String equipId = "";

	// EquipCommentable
	private List<Comment> comments = new ArrayList<>();

	public String getPublishingEventPublishStatusKey() {
		return publishingEventPublishStatusKey;
	}

	public void setPublishingEventPublishStatusKey(String publishingEventPublishStatusKey) {
		this.publishingEventPublishStatusKey = publishingEventPublishStatusKey;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getPublishingEventName() {
		return publishingEventName;
	}

	public void setPublishingEventName(String publishingEventName) {
		this.publishingEventName = publishingEventName;
	}

	public String getPublishingEventPublishedDate() {
		return publishingEventPublishedDate;
	}

	public void setPublishingEventPublishedDate(String publishingEventPublishedDate) {
		this.publishingEventPublishedDate = publishingEventPublishedDate;
	}

	public List<String> getPublishItemIds() {
		return publishItemIds;
	}

	public void setPublishItemIds(List<String> publishItemIds) {
		this.publishItemIds = publishItemIds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
