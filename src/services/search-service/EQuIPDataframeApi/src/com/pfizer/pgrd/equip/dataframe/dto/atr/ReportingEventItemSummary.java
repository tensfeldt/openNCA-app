package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportingEventItemSummary {
	private String lineage;
	private String leagacyLineage;
	private List<Item> items = new ArrayList<>();
	
	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		this.lineage = lineage;
	}

	public String getLeagacyLineage() {
		return leagacyLineage;
	}
	
	public void setLeagacyLineage(String leagacyLineage) {
		this.leagacyLineage = leagacyLineage;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public class Item {
		private String eventItemName;
		private String equipId;
		private String id;
		private boolean isSelected;
		private boolean isPublished;
		private Date publishedDate;
		private String publishedBy;
		
		public Item() {}
		
		public String getEventItemName() {
			return eventItemName;
		}

		public void setEventItemName(String eventItemName) {
			this.eventItemName = eventItemName;
		}

		public String getEquipId() {
			return equipId;
		}

		public void setEquipId(String equipId) {
			this.equipId = equipId;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		public boolean isPublished() {
			return isPublished;
		}

		public void setPublished(boolean isPublished) {
			this.isPublished = isPublished;
		}

		public Date getPublishedDate() {
			return publishedDate;
		}

		public void setPublishedDate(Date publishedDate) {
			this.publishedDate = publishedDate;
		}

		public String getPublishedBy() {
			return publishedBy;
		}

		public void setPublishedBy(String publishedBy) {
			this.publishedBy = publishedBy;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
}
