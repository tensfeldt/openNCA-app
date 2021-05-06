package com.pfizer.pgrd.equip.dataframe.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipModifiable;

/**
 * 
 * @author QUINTJ16
 *
 */
public class Metadatum extends EquipObject implements EquipCreatable, EquipModifiable {
	public static final String ENTITY_TYPE = "Metadatum";
	
	public static final String COMPUTE_STD_OUT_KEY = "ComputeStdOut";
	public static final String CE_VERSION_KEY = "CE Version";
	public static final String CLIENT_VERSION_KEY = "Client Version";
	public static final String CLIENT_NAME_KEY = "Client Name";
	public static final String STRING_TYPE = "STRING", DOUBLE_TYPE = "DOUBLE", LONG_TYPE = "LONG", DATE_TYPE = "DATE",
			BOOLEAN_TYPE = "BOOLEAN";

	private boolean isDeleted;
	private String key;
	private List<String> value = new ArrayList<>();
	private byte[] complexValue;
	private String complexValueId;
	private String valueType;
	
	// EquipCreatable
	private String createdBy;
	private Date created;
	
	// EquipModifiable
	private String modifiedBy;
	private Date modifiedDate;
	
	public Metadatum() {
		this(null, new ArrayList<String>(), null);
	}
	
	public Metadatum(String key) {
		this(key, new ArrayList<>(), null);
	}

	public Metadatum(String key, String value) {
		this(key, Metadatum.wrap(value), null);
	}

	public Metadatum(String key, List<String> value) {
		this(key, value, null);
	}

	public Metadatum(String key, List<String> value, String valueType) {
		this.key = key;
		this.value = value;
		this.valueType = valueType;

		if (this.value != null && this.valueType == null) {
			this.valueType = Metadatum.STRING_TYPE;
		}
		
		this.setEntityType(Metadatum.ENTITY_TYPE);
	}

	private static List<String> wrap(String s) {
		List<String> list = null;
		if (s != null) {
			list = new ArrayList<>();
			list.add(s);
		}

		return list;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}
	
	public void setValue(String value) {
		this.value = Arrays.asList(value);
	}
	
	public void addValue(String value) {
		if(this.value == null) {
			this.value = new ArrayList<>();
		}
		
		this.value.add(value);
	}

	public byte[] getComplexValue() {
		return complexValue;
	}

	public void setComplexValue(byte[] complexValue) {
		this.complexValue = complexValue;
	}

	public String getComplexValueId() {
		return complexValueId;
	}

	public void setComplexValueId(String complexValueId) {
		this.complexValueId = complexValueId;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof Metadatum)) {
			Metadatum md = (Metadatum) obj;
			return (md.key.equals(this.key) && md.valueType.equalsIgnoreCase(this.valueType) && equalsValue(md));
		}
		return false;
	}
	
	/**
	 * Checks if the passed Metadatum md value has the same values as this Metadatum object.
	 * Note that although originally more ambitious, there are only two types used:
	 * STRING and METADATUM (which is also just an array of strings).
	 * @param md
	 * @return true if md has the same values as this.
	 */
	private boolean equalsValue(Metadatum md) {
		switch(md.valueType.toUpperCase()) {
		case DOUBLE_TYPE:
		case LONG_TYPE:
		case DATE_TYPE:
		case BOOLEAN_TYPE:
		case STRING_TYPE:
		default:
			return md.value.equals(this.value);
		}
	}

	@Override
	public Date getCreated() {
		return this.created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public String getCreatedBy() {
		return this.createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String getModifiedBy() {
		return this.modifiedBy;
	}

	@Override
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	@Override
	public Metadatum clone() {
		Metadatum clone = new Metadatum();
		clone.setComplexValue(this.getComplexValue());
		clone.setComplexValueId(this.getComplexValueId());
		clone.setCreated(this.getCreated());
		clone.setCreatedBy(this.getCreatedBy());
		clone.setDeleted(this.isDeleted());
		clone.setEntityType(this.getEntityType());
		clone.setId(this.getId());
		clone.setKey(this.getKey());
		clone.setModifiedBy(this.getModifiedBy());
		clone.setModifiedDate(this.getModifiedDate());
		clone.setValue(this.getValue());
		clone.setValueType(this.getValueType());
		
		return clone;
	}
}