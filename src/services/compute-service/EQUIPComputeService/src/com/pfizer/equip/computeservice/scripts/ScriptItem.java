package com.pfizer.equip.computeservice.scripts;

public class ScriptItem {
	private String id;
	private String name;
	private String reference;
	private byte[] script;
	private String path;
	
	public ScriptItem() {}
	
	public ScriptItem(String id, String name, String reference, byte[] script, String path) {
		this.id = id;
		this.name = name;
		this.reference = reference;
		this.script = script;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public byte[] getScript() {
		return script;
	}

	public void setScript(byte[] script) {
		this.script = script;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean scriptContains(String s) {
		return (new String(script)).contains(s);
	}
	
	public boolean isEmpty() {
		return id == null;
	}
}
