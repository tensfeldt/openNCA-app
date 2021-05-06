package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public class ColumnNode extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "cell:column";
	
	@Expose
	@SerializedName("cell:name")
	private String name;
	
	public ColumnNode() {
		this(null);
	}
	
	public ColumnNode(Column column) {
		super();
		this.setPrimaryType(ColumnNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(column);
	}
	
	public void populate(Column column) {
		if(column != null) {
			this.setName(column.getName());
			this.setRows(RowNode.fromRow(column.getRows()));
			this.setJcrId(column.getId());
		}
	}
	
	public static List<ColumnNode> fromColumn(List<Column> columns) {
		List<ColumnNode> list = new ArrayList<>();
		if(columns != null) {
			for(Column col : columns) {
				ColumnNode dto = new ColumnNode(col);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public static List<Column> toColumn(List<ColumnNode> columns) {
		List<Column> list = new ArrayList<>();
		if(columns != null) {
			for(ColumnNode dto : columns) {
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
		c.setRows(RowNode.toRow(this.getRows()));
		c.setId(this.getJcrId());
		
		return c;
	}
	
	public List<RowNode> getRows() {
		return this.getChildren(RowNode.class);
	}
	
	public void setRows(List<RowNode> rows) {
		this.replaceChildren(RowNode.class, rows);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}