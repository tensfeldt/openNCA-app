package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.List;

public class DataLoad {
	private String equipId;
	private long version;
	private String source;
	private List<String> lineages = new ArrayList<>();
	private List<String> legacyLineages = new ArrayList<>();
	private List<File> files = new ArrayList<>();
	private String id;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public List<String> getLineages() {
		return lineages;
	}

	public void setLineages(List<String> lineages) {
		this.lineages = lineages;
	}

	public List<String> getLegacyLineages() {
		return legacyLineages;
	}

	public void setLegacyLineages(List<String> legacyLineages) {
		this.legacyLineages = legacyLineages;
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