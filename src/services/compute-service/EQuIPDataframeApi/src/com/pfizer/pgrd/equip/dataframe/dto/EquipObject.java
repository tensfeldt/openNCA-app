package com.pfizer.pgrd.equip.dataframe.dto;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;

/**
 * 
 * @author QUINTJ16
 *
 */
public class EquipObject {
	public String id = "";
	private String entityType;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getEntityType() {
		return this.entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getClientName() {
		if(this instanceof EquipMetadatable) {
			EquipMetadatable em = (EquipMetadatable) this;
			return em.getMetadatumValue(Metadatum.CLIENT_NAME_KEY);
		}
		
		return null;
	}
	
	public void setClientName(String name) {
		if(this instanceof EquipMetadatable) {
			EquipMetadatable em = (EquipMetadatable) this;
			Metadatum md = em.getMetadatum(Metadatum.CLIENT_NAME_KEY);
			if(md == null) {
				md = new Metadatum(Metadatum.CLIENT_NAME_KEY);
				em.getMetadata().add(md);
			}
			
			md.setValue(name);
		}
	}
	
	public String getClientVersion() {
		if(this instanceof EquipMetadatable) {
			EquipMetadatable em = (EquipMetadatable) this;
			return em.getMetadatumValue(Metadatum.CLIENT_VERSION_KEY);
		}
		
		return null;
	}
	
	public void setClientVersion(String version) {
		if(this instanceof EquipMetadatable) {
			EquipMetadatable em = (EquipMetadatable) this;
			Metadatum md = em.getMetadatum(Metadatum.CLIENT_VERSION_KEY);
			if(md == null) {
				md = new Metadatum(Metadatum.CLIENT_VERSION_KEY);
				em.getMetadata().add(md);
			}
			
			md.setValue(version);
		}
	}
}