package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;

public class ScriptNode extends ModeShapeNode implements EquipCreatedMixin {
	public static final String PRIMARY_TYPE = "equip:script";
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:modified")
	private Date modified;
	
	@Expose
	@SerializedName("equip:modifiedBy")
	private String modifiedBy;
	
	@Expose
	@SerializedName("equip:computeContainer")
	private String computeContainer;
	
	@Expose
	@SerializedName("equip:environment")
	private String environment;
	
	@Expose
	@SerializedName("equip:scriptCriteria")
	private String scriptCriteria;
	
	public ScriptNode() {
		this(null);
	}
	
	public ScriptNode(Script script) {
		super();
		this.setPrimaryType(ScriptNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(script);
	}
	
	public static List<Script> toScript(List<ScriptNode> scripts) {
		List<Script> list = new ArrayList<>();
		if(scripts != null) {
			for(ScriptNode dto : scripts) {
				Script script = dto.toScript();
				list.add(script);
			}
		}
		
		return list;
	}

	@Override
	public EquipObject toEquipObject() {
		Script script = new Script();
		script.setCreated(this.getCreated());
		script.setCreatedBy(this.getCreatedBy());
		script.setId(this.getJcrId());
		script.setModifiedBy(this.getModifiedBy());
		script.setModifiedDate(this.getModified());
		script.setComputeContainer(this.getComputeContainer());
		script.setEnvironment(this.getEnvironment());
		script.setScriptCriteria(this.getScriptCriteria());
		
		List<Comment> comments = CommentNode.toComment(this.getComments());
		List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
		script.setComments(comments);
		script.setMetadata(metadata);
		
		LibraryReferenceNode body = this.getBody();
		if(body != null) {
			LibraryReference b = body.toLibraryReference();
			script.setScriptBody(b);
		}
		
		return script;
	}
	
	public Script toScript() {
		return (Script) this.toEquipObject();
	}
	
	public static List<ScriptNode> fromScript(List<Script> scripts) {
		List<ScriptNode> list = new ArrayList<>();
		if(scripts != null) {
			for(Script script : scripts) {
				ScriptNode dto = new ScriptNode(script);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Script script) {
		if(script != null) {
			this.setCreated(script.getCreated());
			this.setCreatedBy(script.getCreatedBy());
			this.setModified(script.getModifiedDate());
			this.setModifiedBy(script.getModifiedBy());
			this.setComputeContainer(script.getComputeContainer());
			this.setEnvironment(script.getEnvironment());
			this.setScriptCriteria(script.getScriptCriteria());
			
			List<CommentNode> comments = CommentNode.fromComment(script.getComments());
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(script.getMetadata());
			this.setComments(comments);
			this.setMetadata(metadata);
			
			if(script.getScriptBody() != null) {
				LibraryReferenceNode body = new LibraryReferenceNode(script.getScriptBody());
				this.setBody(body);
			}
		}
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<CommentNode> getComments() {
		return this.getChildren(CommentNode.class);
	}
	
	public void setComments(List<CommentNode> comments) {
		this.replaceChildren("equip:comment", comments);
	}
	
	public LibraryReferenceNode getBody() {
		ModeShapeNode child = this.getChild("equip:scriptBody");
		return (LibraryReferenceNode) child;
	}
	
	public void setBody(LibraryReferenceNode body) {
		this.replaceChild("equip:scriptBody", body);
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getComputeContainer() {
		return computeContainer;
	}

	public void setComputeContainer(String computeContainer) {
		this.computeContainer = computeContainer;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getScriptCriteria() {
		return scriptCriteria;
	}

	public void setScriptCriteria(String scriptCriteria) {
		this.scriptCriteria = scriptCriteria;
	}
}