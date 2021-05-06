package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;

public class ReportingEvent {
	private String name;
	private String equipId;
	private long version;
	private String eventType;
	private String createdBy;
	private Date createdDate;
	private String id;
	
	private List<ReportingEventItemSummary> eventItemSummaries = new ArrayList<>();
	private List<StatusChangeEvent> releaseReopenEvents = new ArrayList<>();
	private List<Comment> comments = new ArrayList<>();
	
	public static final ReportingEvent fromAssembly(Assembly re) {
		ReportingEvent event = null;
		if(re != null) {
			event = new ReportingEvent();
			event.setComments(Comment.fromComment(re.getComments()));
			event.setCreatedDate(re.getCreated());
			event.setCreatedBy(re.getCreatedBy());
			event.setEquipId(re.getEquipId());
			event.setName(re.getName());
			event.setEventType(re.getItemType());
			event.setVersion(re.getVersionNumber());
		}
		
		return event;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String type) {
		this.eventType = type;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date created) {
		this.createdDate = created;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public List<ReportingEventItemSummary> getEventItemSummaries() {
		return eventItemSummaries;
	}
	public void setEventItemSummaries(List<ReportingEventItemSummary> eventItemSummaries) {
		this.eventItemSummaries = eventItemSummaries;
	}

	public List<StatusChangeEvent> getReleaseReopenEvents() {
		return releaseReopenEvents;
	}

	public void setReleaseReopenEvents(List<StatusChangeEvent> releaseReopenEvents) {
		this.releaseReopenEvents = releaseReopenEvents;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
