package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;

/**
 * Implementing classes expose methods to retrieve, create, and modify assemblies
 * @author HIRSCM08
 *
 */
public interface AssemblyDAO {
	/**
	 * Returns the Assembly matching the provided Assembly ID.
	 * @param AssemblyId
	 * @return {@link Assembly} the Assembly
	 */
	public Assembly getAssembly(String assemblyId);
	
	/**
	 * Returns a collection of QCRequests matching the provided QCRequest IDs.
	 * @param QCRequestIds
	 * @return {@link List}<{@link QCRequest}> the QCRequests
	 */
	public List<Assembly> getAssembly(List<String> assemblyIds);
	
	/**
	 * Returns a collection of QCRequests matching the provided QCRequest IDs.
	 * @param QCRequestIds
	 * @return {@link List}<{@link QCRequest}> the QCRequests
	 */
	public List<Assembly> getAssembly(String[] assemblyIds);
	
	/**
	 * Returns a collection of Assembly matching the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link List}<{@link QCRequest}> the QCRequests
	 */
	public List<Assembly> getAssemblyByParentId(String parentId);
	
	public List<Assembly> getAssemblyByParentId(String[] parentIds);
	
	public List<Assembly> getAssemblyByParentId(List<String> parentIds);
	
	public List<Assembly> getAssemblyByMemberDataframeId(String dataframeId);
	
	public List<Assembly> getAssemblyByMemberDataframeId(String[] dataframeIds);
	
	public List<Assembly> getAssemblyByMemberDataframeId(List<String> dataframeIds);
	
	/**
	 * Inserts the provided {@link Assembly} object and returns the newly created object.
	 * @param assembly the assembly object
	 * @return {@link Assembly}
	 */
	public Assembly insertAssembly(Assembly assembly);
	
	public List<Assembly> getAssemblyByStudyId(String studyId);
	
	public List<Assembly> getAssembliesByStudyIds(List<String> studyIds);
	
	public List<Assembly> getAssemblyByStudyId(String[] studyIds);
	
	public List<Assembly> getAssemblyByProgramProtocol(String programId, String protocolId);
	public List<Assembly> getAssemblyByProgramProtocol(String programId, List<String> protocolIds);
	public List<Assembly> getAssemblyByProgramProtocol(String programId, String[] protocolIds);
	
	public List<Assembly> getAssemblyByUserId(String userId);
	
	public List<Assembly> getAssemblyByUserId(List<String> userIds);
	
	public List<Assembly> getAssemblyByUserId(String[] userIds);
	
	public List<Assembly> getAssemblyByMemberAssemblyId(String assemblyId);
	
	public List<Assembly> getAssemblyByMemberAssemblyId(String[] assemblyIds);
	
	public List<Assembly> getAssemblyByMemberAssemblyId(List<String> assemblyIds);
	
	public Assembly updateAssembly(String assemblyId, Assembly assembly);
	
	public List<Assembly> getAssemblysByEquipId(String equipId);
	
	public List<Assembly> getAssemblyByEquipId(List<String> equipIds);
	
	public List<Assembly> getAssemblyByEquipId(String[] equipIds);
	
	public Assembly getLatestAssemblyByEquipId(String equipId);
	public Assembly getLatestAssemblyByEquipId(String equipId, String userId);
	public Assembly getLatestAssemblyByEquipId(String equipId, boolean includeDeleted);
	public Assembly getLatestAssemblyByEquipId(String equipId, String userId, boolean includeDeleted);

	public Assembly updateAssembly(String assemblyId, Assembly assembly, boolean includeChildren);
	
	public List<Assembly> getRootDataLoads(String id);

	List<Assembly> getAssemblyByReportingEventItemId(String[] reids);

	List<Assembly> getAssemblyByReportingEventItemId(String reiId);
}
