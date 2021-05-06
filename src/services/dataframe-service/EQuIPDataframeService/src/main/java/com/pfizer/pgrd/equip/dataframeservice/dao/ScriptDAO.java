package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Script;

public interface ScriptDAO {
	/**
	 * Returns the {@link Script} object matching the provided script ID.
	 * @param scriptId
	 * @return {@link Script}
	 */
	public Script getScriptById(String scriptId);
	
	/**
	 * Returns the {@link Script} objects matching the provided script IDs.
	 * @param scriptIds
	 * @return {@link List}<{@link Script}>
	 */
	public List<Script> getScriptById(List<String> scriptIds);
	
	/**
	 * Returns the {@link Script} objects matching the provided script IDs.
	 * @param scriptIds
	 * @return {@link List}<{@link Script}>
	 */
	public List<Script> getScriptById(String[] scriptIds);
	
	/**
	 * Returns the {@link Script} associated with the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link Script}
	 */
	public Script getScriptByDataframeId(String dataframeId);
	
	/**
	 * Returns the {@link Script} associated with the provided assembly ID.
	 * @param assemblyId
	 * @return {@link Script}
	 */
	public Script getScriptByAssemblyId(String assemblyId);
	
	/**
	 * Inserts the provided {@link Script} object as a child of the provided entity ID.
	 * @param script
	 * @param entityId
	 * @return {@link Script} the newly created Script object
	 */
	public Script insertScript(Script script, String entityId);
}