package com.pfizer.pgrd.equip.services.opmeta.client;

import java.util.Date;
import java.util.List;

public class Program {
	private String source;
	private Date sourceCreationTimestamp;
	private String programCode;
	private String compound;
	private String compoundDiscoveryTherapeuticArea;
	private String compoundMechanismOfAction;
	private String compoundName;
	private String compoundSource;
	private String genericName;
	private String studyProduct;
	private String tradeName;
	private Date setupDate;
	private String setupBy;
	
	private List<Protocol> protocols;
	
	public Protocol getProtocol(String studyId) {
		for(Protocol p : this.protocols) {
			if(p != null) {
				if(p.getStudyId().equalsIgnoreCase(studyId)) {
					return p;
				}
			}
		}
		
		return null;
	}
	
	public List<Protocol> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<Protocol> protocols) {
		this.protocols = protocols;
	}

	public String getProgramCode() {
		return programCode;
	}

	public void setProgramCode(String programCode) {
		this.programCode = programCode;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getSourceCreationTimestamp() {
		return sourceCreationTimestamp;
	}

	public void setSourceCreationTimestamp(Date sourceCreationTimestamp) {
		this.sourceCreationTimestamp = sourceCreationTimestamp;
	}

	public String getCompound() {
		return compound;
	}

	public void setCompound(String compound) {
		this.compound = compound;
	}

	public String getCompoundDiscoveryTherapeuticArea() {
		return compoundDiscoveryTherapeuticArea;
	}

	public void setCompoundDiscoveryTherapeuticArea(String compoundDiscoveryTherapeuticArea) {
		this.compoundDiscoveryTherapeuticArea = compoundDiscoveryTherapeuticArea;
	}

	public String getCompoundMechanismOfAction() {
		return compoundMechanismOfAction;
	}

	public void setCompoundMechanismOfAction(String compoundMechanismOfAction) {
		this.compoundMechanismOfAction = compoundMechanismOfAction;
	}

	public String getCompoundName() {
		return compoundName;
	}

	public void setCompoundName(String compoundName) {
		this.compoundName = compoundName;
	}

	public String getCompoundSource() {
		return compoundSource;
	}

	public void setCompoundSource(String compoundSource) {
		this.compoundSource = compoundSource;
	}

	public String getGenericName() {
		return genericName;
	}

	public void setGenericName(String genericName) {
		this.genericName = genericName;
	}

	public String getStudyProduct() {
		return studyProduct;
	}

	public void setStudyProduct(String studyProduct) {
		this.studyProduct = studyProduct;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

	public Date getSetupDate() {
		return setupDate;
	}

	public void setSetupDate(Date setupDate) {
		this.setupDate = setupDate;
	}

	public String getSetupBy() {
		return setupBy;
	}

	public void setSetupBy(String setupBy) {
		this.setupBy = setupBy;
	}
	
}
