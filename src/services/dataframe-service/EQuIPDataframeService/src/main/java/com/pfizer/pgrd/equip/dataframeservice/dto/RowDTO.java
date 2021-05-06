package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Row;

public class RowDTO extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "cell:row";
	
	@Expose
	@SerializedName("cell:data")
	private String data;
	
	public RowDTO() {
		this(null);
	}
	
	public RowDTO(Row row) {
		super();
		this.setPrimaryType(RowDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(row);
	}
	
	public static List<Row> toRow(List<RowDTO> rows) {
		List<Row> list = new ArrayList<>();
		if(rows != null) {
			for(RowDTO row : rows) {
				Row r = row.toRow();
				list.add(r);
			}
		}
		
		return list;
	}
	
	public static List<RowDTO> fromRow(List<Row> rows) {
		List<RowDTO> list = new ArrayList<>();
		if(rows != null) {
			for(Row row : rows) {
				RowDTO dto = new RowDTO(row);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Row row) {
		if(row != null) {
			this.setData(row.getData());
			this.setJcrId(row.getId());
		}
	}
	
	@Override
	public EquipObject toEquipObject() {
		return (EquipObject) this.toRow();
	}
	
	public Row toRow() {
		Row row = new Row();
		row.setData(this.getData());
		row.setId(this.getJcrId());
		
		return row;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}