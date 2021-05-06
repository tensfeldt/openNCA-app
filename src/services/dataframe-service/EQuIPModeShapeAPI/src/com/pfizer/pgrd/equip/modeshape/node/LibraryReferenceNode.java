package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;

public class LibraryReferenceNode extends ModeShapeNode {
	public static final String PRIMARY_TYPE = "equip:libraryReference";
	
	@Expose
	@SerializedName("equip:libraryRef")
	private String libraryReferenceId;
	
	public LibraryReferenceNode() {
		this(null);
	}
	
	public LibraryReferenceNode(LibraryReference libRef) {
		super();
		this.setPrimaryType(LibraryReferenceNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(libRef);
	}
	
	public static List<LibraryReference> toLibraryReference(List<LibraryReferenceNode> libRefs) {
		List<LibraryReference> list = new ArrayList<>();
		if(libRefs != null) {
			for(LibraryReferenceNode dto : libRefs) {
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
	
	public static List<LibraryReferenceNode> fromLibraryReference(List<LibraryReference> libRefs) {
		List<LibraryReferenceNode> list = new ArrayList<>();
		if(libRefs != null) {
			for(LibraryReference libRef : libRefs) {
				LibraryReferenceNode dto = new LibraryReferenceNode(libRef);
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