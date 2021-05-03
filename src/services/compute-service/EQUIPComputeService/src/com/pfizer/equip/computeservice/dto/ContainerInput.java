package com.pfizer.equip.computeservice.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.equip.computeservice.scripts.ScriptItem;
import com.pfizer.equip.utils.TypedValue;

public class ContainerInput {
	private String username;
	private String modeshapeServer;
	private String modeshapeUser;
	private String modeshapePassword;
	private Map<String, String> dataframes = new HashMap<>();
	private String scriptId;
	List<ScriptItem> subScriptItems = new ArrayList<>();
	Map<String, TypedValue> parameters = new HashMap<>();
	private String scriptsDestination;
	private String dataframesDestination;
	private Map<String, String> commandLineArgs = new HashMap<>();
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}	
	public String getModeshapeServer() {
		return modeshapeServer;
	}
	public void setModeshapeServer(String modeshapeServer) {
		this.modeshapeServer = modeshapeServer;
	}
	public String getModeshapeUser() {
		return modeshapeUser;
	}
	public void setModeshapeUser(String modeshapeUser) {
		this.modeshapeUser = modeshapeUser;
	}
	public String getModeshapePassword() {
		return modeshapePassword;
	}
	public void setModeshapePassword(String modeshapePassword) {
		this.modeshapePassword = modeshapePassword;
	}
	public Map<String, String> getDataframes() {
		return dataframes;
	}
	public void setDataframes(Map<String, String> dataframes) {
		this.dataframes = dataframes;
	}
	
	public String getScriptId() {
		return scriptId;
	}
	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}
	public List<ScriptItem> getSubScriptItems() {
		return subScriptItems;
	}
	public void setSubScriptItems(List<ScriptItem> subScriptItems) {
		this.subScriptItems = subScriptItems;
	}
	public Map<String, TypedValue> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, TypedValue> parameters) {
		this.parameters = parameters;
	}
	public String getScriptsDestination() {
		return scriptsDestination;
	}
	public void setScriptsDestination(String scriptsDestination) {
		this.scriptsDestination = scriptsDestination;
	}
	public String getDataframesDestination() {
		return dataframesDestination;
	}
	public void setDataframesDestination(String dataframesDestination) {
		this.dataframesDestination = dataframesDestination;
	}
	public Map<String, String> getCommandLineArgs() {
		return commandLineArgs;
	}
	public void setCommandLineArgs(Map<String, String> commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}
}
