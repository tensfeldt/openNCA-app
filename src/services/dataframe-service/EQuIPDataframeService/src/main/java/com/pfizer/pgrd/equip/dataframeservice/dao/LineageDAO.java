package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.CopyException;

public interface LineageDAO {
	public String getAuthUserId();
	public void setAuthUserId(String authUserId);
	
	public List<AssemblyLineageItem> getFullStudyLineage(String studyId);
	public List<AssemblyLineageItem> getFullStudyLineage(List<String> studyIds);
	public List<AssemblyLineageItem> getFullStudyLineage(String[] studyIds);
	public List<AssemblyLineageItem> getFullStudyLineage(String studyId, boolean includeDeleted);
	public List<AssemblyLineageItem> getFullStudyLineage(List<String> studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getFullStudyLineage(String[] studyIds, boolean includeDeleted);
	
	public List<LineageItem> getLineage(String startId);
	public List<LineageItem> getLineage(List<String> startIds);
	public List<LineageItem> getLineage(String[] startIds);
	public List<LineageItem> getLineage(String startId, boolean includeDeleted);
	public List<LineageItem> getLineage(List<String> startIds, boolean includeDeleted);
	public List<LineageItem> getLineage(String[] startIds, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId);
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds);
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds);
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, String userId);
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, String userId);
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, String userId);
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineage(String studyId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineage(List<String> studyIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineage(String[] studyIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getPromotionLineage(String studyId);
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds);
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds);
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, String userId);
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, String userId);
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, String userId);
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineage(String studyId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineage(List<String> studyIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineage(String[] studyIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyId);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String studyId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(List<String> studyIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineage(String[] studyIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, String userId);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, String userId);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, String userId);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String equipId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(List<String> equipIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getDataLoadLineageByEquipId(String[] equipIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, String userId);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, String userId);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, String userId);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String equipId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(List<String> equipIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getPromotionLineageByEquipId(String[] equipIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, String userId);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String equipId, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(List<String> equipIds, String userId, boolean includeDeleted);
	public List<AssemblyLineageItem> getAnalysisPrepLineageByEquipId(String[] equipIds, String userId, boolean includeDeleted);
	
	public List<AssemblyLineageItem> getRawAnalysisPrepLineage(String nodeId, String userId);
	
	public LineageItem copyLineage(String startId) throws CopyException;
	public LineageItem copyLineage(String copier, String startId) throws CopyException;
	
	public LineageItem reExecuteLineage(String startId, List<String> newDataframeIds);
	public LineageItem reExecuteLineage(String copier, String startId, List<String> newDataframeIds);
}
