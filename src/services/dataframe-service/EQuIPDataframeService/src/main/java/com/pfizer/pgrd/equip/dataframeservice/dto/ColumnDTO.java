package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public class ColumnDTO extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "cell:column";
	
	@Expose
	@SerializedName("cell:name")
	private String name;
	
	public ColumnDTO() {
		this(null);
	}
	
	public ColumnDTO(Column column) {
		super();
		this.setPrimaryType(ColumnDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(column);
	}
	
	public void populate(Column column) {
		if(column != null) {
			this.setName(column.getName());
			this.setRows(RowDTO.fromRow(column.getRows()));
			this.setJcrId(column.getId());
		}
	}
	
	public static List<ColumnDTO> fromColumn(List<Column> columns) {
		List<ColumnDTO> list = new ArrayList<>();
		if(columns != null) {
			for(Column col : columns) {
				ColumnDTO dto = new ColumnDTO(col);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public static List<Column> toColumn(List<ColumnDTO> columns) {
		List<Column> list = new ArrayList<>();
		if(columns != null) {
			for(ColumnDTO dto : columns) {
				Column col = dto.toColumn();
				list.add(col);
			}
		}
		
		return list;
	}
	
	public EquipObject toEquipObject() {
		return (EquipObject) this.toColumn();
	}
	
	public Column toColumn() {
		Column c = new Column();
		c.setName(this.getName());
		c.setRows(RowDTO.toRow(this.getRows()));
		c.setId(this.getJcrId());
		
		return c;
	}
	
	public List<RowDTO> getRows() {
		return this.getChildren(RowDTO.class);
	}
	
	public void setRows(List<RowDTO> rows) {
		this.replaceChildren(RowDTO.class, rows);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}