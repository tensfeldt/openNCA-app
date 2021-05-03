package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.List;

public class Column extends EquipObject {
	private String name;
	private List<Row> rows = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Row> getRows() {
		return rows;
	}
	public void setRows(List<Row> rows) {
		this.rows = rows;
	}
}
