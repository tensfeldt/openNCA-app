package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Row;

public class RowNode extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "cell:row";
	
	@Expose
	@SerializedName("cell:data")
	private String data;
	
	public RowNode() {
		this(null);
	}
	
	public RowNode(Row row) {
		super();
		this.setPrimaryType(RowNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(row);
	}
	
	public static List<Row> toRow(List<RowNode> rows) {
		List<Row> list = new ArrayList<>();
		if(rows != null) {
			for(RowNode row : rows) {
				Row r = row.toRow();
				list.add(r);
			}
		}
		
		return list;
	}
	
	public static List<RowNode> fromRow(List<Row> rows) {
		List<RowNode> list = new ArrayList<>();
		if(rows != null) {
			for(Row row : rows) {
				RowNode dto = new RowNode(row);
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