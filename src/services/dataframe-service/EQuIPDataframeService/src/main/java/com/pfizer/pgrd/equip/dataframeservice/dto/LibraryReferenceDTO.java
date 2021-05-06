package com.pfizer.pgrd.equip.dataframeservice.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;

public class LibraryReferenceDTO extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "equip:libraryReference";
	
	@Expose
	@SerializedName("equip:libraryRef")
	private String libraryReferenceId;
	
	public LibraryReferenceDTO() {
		this(null);
	}
	
	public LibraryReferenceDTO(LibraryReference libRef) {
		super();
		this.setPrimaryType(LibraryReferenceDTO.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(libRef);
	}
	
	public static List<LibraryReference> toLibraryReference(List<LibraryReferenceDTO> libRefs) {
		List<LibraryReference> list = new ArrayList<>();
		if(libRefs != null) {
			for(LibraryReferenceDTO dto : libRefs) {
				LibraryReference libRef = dto.toLibraryReference();
				list.add(libRef);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		LibraryReference libRef = new LibraryReference();
		libRef.setId(this.getJcrId());
		libRef.setLibraryRef(this.getLibraryReferenceId());
		
		return libRef;
	}
	
	public LibraryReference toLibraryReference() {
		return (LibraryReference) this.toEquipObject();
	}
	
	public static List<LibraryReferenceDTO> fromLibraryReference(List<LibraryReference> libRefs) {
		List<LibraryReferenceDTO> list = new ArrayList<>();
		if(libRefs != null) {
			for(LibraryReference libRef : libRefs) {
				LibraryReferenceDTO dto = new LibraryReferenceDTO(libRef);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(LibraryReference libRef) {
		if(libRef != null) {
			this.setLibraryReferenceId(libRef.getLibraryRef());
		}
	}
	
	public String getLibraryReferenceId() {
		return libraryReferenceId;
	}

	public void setLibraryReferenceId(String libraryReferenceId) {
		this.libraryReferenceId = libraryReferenceId;
	}
}