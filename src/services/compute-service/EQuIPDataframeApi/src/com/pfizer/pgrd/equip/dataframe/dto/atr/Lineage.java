package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

public class Lineage {
	private String lineageName;
	private String legacyLineageName;
	
	private List<ATRLineageItem> items = new ArrayList<>();
	
	public String getLineageName() {
		return lineageName;
	}
	public void setLineageName(String lineageName) {
		this.lineageName = lineageName;
	}
	public List<ATRLineageItem> getItems() {
		return items;
	}
	public void setItems(List<ATRLineageItem> items) {
		this.items = items;
	}
	public String getLegacyLineageName() {
		return legacyLineageName;
	}
	public void setLegacyLineageName(String legacyLineageName) {
		this.legacyLineageName = legacyLineageName;
	}
}
