package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;

public interface AnalysisDAO {
	/**
	 * Returns the {@link Analysis} object matching the provided analysis ID.
	 * @param analysisId
	 * @return {@link Analysis}
	 */
	public Analysis getAnalysis(String analysisId);
	
	/**
	 * Returns the {@link Analysis} objects matching the provided analysis IDs.
	 * @param analysisIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysis(List<String> analysisIds);
	
	/**
	 * Returns the {@link Analysis} objects matching the provided analysis IDs.
	 * @param analysisIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysis(String[] analysisIds);
	
	/**
	 * Returns the {@link Analysis} objects related to the provided study ID.
	 * @param studyId
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByStudyId(String studyId);
	
	/**
	 * Returns the {@link Analysis} objects related to the provided study IDs.
	 * @param studyIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByStudyId(List<String> studyIds);
	
	/**
	 * Returns the {@link Analysis} objects related to the provided study IDs.
	 * @param studyIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByStudyId(String[] studyIds);
	
	/**
	 * validates the program code by looking up the node
	 * @param programCode
	 * @return boolean - true if path exists
	 */
	public boolean checkProgramPath(String programCode);

	/**
	 * Inserts the provided {@link Analysis} object into the repository. Returns the newly created {@link Analysis} object.
	 * @param analysis the analysis
	 * @return {@link Analysis}
	 */
	public Analysis insertAnalysis(Analysis analysis);
	
	/**
	 * Updates the {@link Analysis} object in the repository matching the ID of the provided {@link Analysis} object.
	 * @param analysis
	 */
	public void updateAnalysis(Analysis analysis);
	
	/**
	 * Returns a {@link List} of {@link Analysis} objects matching the provided EQUIP ID.
	 * @param equipId
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByEquipId(String equipId);
	
	/**
	 * Returns a {@link List} of {@link Analysis} objects matching the provided EQUIP IDs.
	 * @param equipIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByEquipId(List<String> equipIds);
	
	/**
	 * Returns a {@link List} of {@link Analysis} objects matching the provided EQUIP IDs.
	 * @param equipIds
	 * @return {@link List}<{@link Analysis}>
	 */
	public List<Analysis> getAnalysisByEquipId(String[] equipIds);
}