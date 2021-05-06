package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.Date;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;

public class BatchDTO extends AssemblyDTO {
	public static final String PRIMARY_TYPE = "equip:batch";
	
	public BatchDTO() {
		this(null);
	}
	
	public BatchDTO(Batch batch) {
		super();
		this.setPrimaryType(BatchDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(batch);
		this.setAssemblyType(Assembly.BATCH_TYPE);
	}
	
	@Override
	public String generateNodeName() {
		return "BAT-" + new Date().getTime() + "-" + Thread.currentThread().getId();
	}
	
	public void populate(Batch batch) {
		if(batch != null) {
			this.populate((Assembly)batch);
		}
	}
	
	@Override
	public EquipObject toEquipObject() {
		Batch batch = new Batch();
		this.populateAssembly(batch);
		return batch;
	}
	
	public Batch toBatch() {
		return (Batch) this.toEquipObject();
	}
}