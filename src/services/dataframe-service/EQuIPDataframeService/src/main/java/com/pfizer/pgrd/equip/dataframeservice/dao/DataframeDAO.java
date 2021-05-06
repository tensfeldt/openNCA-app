package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.pfizer.equip.service.client.ServiceCallerException;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;

/**
 * Implementing classes expose methods to retrieve, created, and modify dataframes.
 * @author QUINTJ16
 *
 */
public interface DataframeDAO {
	/**
	 * Returns a collection of top-level dataframes.
	 * @return {@link List}<{@link Dataframe}> the nodes
	 */
	public List<Dataframe> getDataframe();
	
	/**
	 * Returns the dataframe matching the provided dataframe ID.
	 * @param dataframeId
	 * @return {@link Dataframe} the dataframe
	 */
	public Dataframe getDataframe(String dataframeId);
	
	/**
	 * Returns a collection of dataframes matching the provided dataframe IDs.
	 * @param dataframeIds
	 * @return {@link List}<{@link Dataframe}> the dataframes
	 */
	public List<Dataframe> getDataframe(List<String> dataframeIds);
	
	/**
	 * Returns a collection of dataframes matching the provided dataframe IDs.
	 * @param dataframeIds
	 * @return {@link List}<{@link Dataframe}> the dataframes
	 */
	public List<Dataframe> getDataframe(String[] dataframeIds);
	
	/**
	 * Inserts the provided dataframe into the repository. Returns the newly created dataframe.
	 * @param dataframe the dataframe
	 * @return {@link Dataframe} the newly created dataframe
	 * @return only the string
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public Dataframe insertDataframe(Dataframe dataframe);
	
	/**
	 * Inserts the provided {@link Dataframe} objects into the repository. Returns the newly created {@link Dataframe} objects.
	 * @param dataframes
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> insertDataframe(List<Dataframe> dataframes);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects matching the provided dataframe type.
	 * @param dataframeType the dataframe type
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByType(String dataframeType);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects matching the provided dataframe types.
	 * @param dataframeTypes the dataframe type
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByType(List<String> dataframeTypes);
	
	public List<Dataframe> getDataframeByType(String[] dataframeTypes);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects relating to the provided study ID.
	 * @param studyId the study ID
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByStudyId(String studyId);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects relating to the provided study IDs.
	 * @param studyIds the study IDs
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByStudyId(List<String> studyIds);
	
	public List<Dataframe> getDataframeByStudyId(String[] studyIds);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects relating to the provided user ID.
	 * @param userId
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByUserId(String userId);
	
	/**
	 * Returns a {@link List} of {@link Dataframe} objects relating to the provided user IDs.
	 * @param userIds
	 * @return {@link List}<{@link Dataframe}>
	 */
	public List<Dataframe> getDataframeByUserId(List<String> userIds);
	
	public List<Dataframe> getDataframeByUserId(String[] userIds);

	/**
	 * Updates the dataframe matching the provided dataframe ID to the values in the provided {@link Dataframe} object.
	 * @param dataframe
	 * @param id
	 * @return
	 */
	public Dataframe updateDataframe(Dataframe dataframe, String id);
	
	/**
	 * Inserts the provided {@link Promotion} object as a child of the dataframe matching the provided dataframe ID.
	 * @param promotion
	 * @param dataframeId
	 * @return {@link Promotion} the newly created promotion
	 */
	public Promotion addPromotion(Promotion promotion, String dataframeId);
	
	/**
	 * Hard deletes the {@link Dataframe} object whose UUID matches the one provided.
	 * @param id
	 * @return {@code true} if the delete was successful; {@code false} otherwise
	 */
	public boolean deleteDataframe(String id);
	
	public List<Dataframe> getDataframeByAssemblyId(String assemblyId);
	public List<Dataframe> getDataframeByAssemblyId(List<String> assemblyIds);
	public List<Dataframe> getDataframeByAssemblyId(String[] assemblyIds);
	
	public List<Dataframe> getDataframeByParentDataframeId(String dataframeId);
	public List<Dataframe> getDataframeByParentDataframeId(String[] dataframeIds);
	public List<Dataframe> getDataframeByParentDataframeId(List<String> dataframeIds);
	
	public List<Dataframe> getDataframeByEquipId(String equipId);
	public List<Dataframe> getDataframeByEquipId(List<String> equipIds);
	public List<Dataframe> getDataframeByEquipId(String[] equipIds);
	
	public Dataframe getLatestDataframeByEquipId(String equipId);
	public Dataframe getLatestDataframeByEquipId(String equipId, String userId);
	public Dataframe getLatestDataframeByEquipId(String equipId, boolean includeDeleted);
	public Dataframe getLatestDataframeByEquipId(String equipId, String userId, boolean includeDeleted);
	
	public List<Dataframe> getDataframeByProgramProtocol(String programId, String protocolId);
	public List<Dataframe> getDataframeByProgramProtocol(String programId, List<String> protocolIds);
	public List<Dataframe> getDataframeByProgramProtocol(String programId, String[] protocolIds);

	public boolean isPromotionRevokable(Dataframe df);

	public boolean copyGroupAccess(Dataframe dataframe, String userId);
	
	public Dataframe getDataframe(String dataframeId, boolean includeReportingMetadata);
	public List<Dataframe> getDataframe(List<String> dataframeIds, boolean includeReportingMetadata);
	public List<Dataframe> getDataframe(String[] dataframeIds, boolean includeReportingMetadata);

	public List<Dataframe> getDataframeAttachments(String nodeId, String userId);
	public List<Dataframe> getAssociatedAssemblyAttachments(String nodeId, String userId);
	
	public List<Dataframe> searchReports(ReportSearch searchCriteria);
}