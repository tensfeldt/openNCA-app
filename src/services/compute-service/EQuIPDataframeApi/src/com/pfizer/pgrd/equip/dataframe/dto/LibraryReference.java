package com.pfizer.pgrd.equip.dataframe.dto;

public class LibraryReference extends EquipObject {
	public static final String ENTITY_TYPE = "Library Reference";
	
	private String libraryRef;
	
	public LibraryReference() {
		this.setEntityType(LibraryReference.ENTITY_TYPE);
	}

	public String getLibraryRef() {
		return libraryRef;
	}

	public void setLibraryRef(String libraryRef) {
		this.libraryRef = libraryRef;
	}
}