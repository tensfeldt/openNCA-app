package com.pfizer.pgrd.equip.dataframeservice.resource.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryMCT;
import com.pfizer.pgrd.equip.dataframe.dto.analysisqc.AnalysisQCLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.analysisqc.AnalysisQCReport;
import com.pfizer.pgrd.equip.dataframe.dto.analysisqc.MCT;
import com.pfizer.pgrd.equip.dataframe.dto.atr.AuditTrailReport;
import com.pfizer.pgrd.equip.dataframe.dto.atr.DataTransformation;
import com.pfizer.pgrd.equip.dataframe.dto.atr.DataframeInfo;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Lineage;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ATRLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.atr.AnalysisData;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Program;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Protocol;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ProtocolContact;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageSearcher;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportSearch;
import com.pfizer.pgrd.equip.dataframeservice.dto.CommentDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.LibraryMCTDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.reportutils.ATRUtils;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.lineage.LineageResource;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class ReportResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportResource.class);

	public static final Route createATR = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId != null) {
					AuthorizationDAO auth = new AuthorizationDAO();
					boolean isOk = auth.checkPrivileges("ATR", "GET", userId);
					isOk = true;
					if (isOk) {
						String reportingEventId = request.params(":reportingEventId");
						AssemblyDAO aDao = ServiceBaseResource.getAssemblyDAO();
						Assembly reportingEvent = aDao.getAssembly(reportingEventId);
						if (reportingEvent != null) {
							if (!reportingEvent.isDeleteFlag()) {
								boolean isRaw = false;
								String rawp = request.queryParams("raw");
								if(rawp != null) {
									isRaw = Boolean.parseBoolean(rawp);
								}
								
								ATRUtils atrUtils = new ATRUtils();
								atrUtils.setUsername(userId);
								atrUtils.setRaw(isRaw);

								AuditTrailReport atr = atrUtils.createReport(reportingEvent);
								
								// Remove unpromoted dataframes
								List<DataframeInfo> remove = new ArrayList<>();
								for(DataTransformation dt : atr.getDataTransformations()) {
									for(DataframeInfo di : atr.getDataframes()) {
										if(di.getId() == dt.getId()) {
											remove.add(di);
											break;
										}
									}
								}
								atr.getDataframes().removeAll(remove);
								//atr.setDataTransformations(new ArrayList<>());
								
								json = ServiceBaseResource.returnJSON(atr, response, true, false);
							} else {
								Spark.halt(HTTPStatusCodes.CONFLICT,
										"Reporting event '" + reportingEvent.getEquipId() + "' is deleted.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No reporting event with ID '" + reportingEventId + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "User does not have privileges to generate ATR.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
			} catch (HaltException he) {
				throw he;
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}

	};

	public static final Route createAnalysisQCReport = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId != null) {
					AuthorizationDAO auth = new AuthorizationDAO();
					boolean isOk = auth.checkPrivileges("AQC", "GET", userId);
					isOk = true;
					if (isOk) {
						String analysisId = request.params(":analysisId");
						AnalysisDAO aDao = ServiceBaseResource.getAnalysisDAO();
						Analysis analysis = aDao.getAnalysis(analysisId);
						if (analysis != null) {
							AnalysisQCReport aqcr = new AnalysisQCReport();
							aqcr.setAnalysis(analysis);
							aqcr.setAnalysisEquipId(analysis.getEquipId());
							aqcr.setAnalysisVersion(analysis.getVersionNumber());
							aqcr.setCreatedBy(analysis.getCreatedBy());
							aqcr.setCreatedDate(analysis.getCreated());
							aqcr.setAnalysisSaveError(analysis.getAnalysisSaveError());
							
							DataframeDAO dDao = ServiceBaseResource.getDataframeDAO();
							if(analysis.getModelConfigurationDataframeId() != null) {
								Dataframe df = dDao.getDataframe(analysis.getModelConfigurationDataframeId());
								if(df != null) {
									MCT mct = new MCT(df);
									if(analysis.getConfigurationTemplateId() != null) {
										ModeShapeDAO msDao = new ModeShapeDAO();
										ModeShapeNode n = msDao.getNode(analysis.getConfigurationTemplateId());
										if(n != null) {
											LibraryMCT lmct = (LibraryMCT) n.toEquipObject();
											mct.setConfigurationTemplateDetails(lmct);
										}
									}
									
									aqcr.setMct(mct);
								}
							}
							
							if (analysis.getDataframeIds() != null && analysis.getDataframeIds().size() > 0) {
								String concentrationId = analysis.getDataframeIds().get(0);
								Dataframe concentrationData = dDao.getDataframe(concentrationId);
								if (concentrationData != null) {
									aqcr.setProfileConfig(concentrationData.getProfileConfig());
								}
							}
							
							for (String study : analysis.getStudyIds()) {
								String[] parts = study.split(":");
								aqcr.getProtocols().add(parts[1]);
							}

							LineageDAO lDao = ServiceBaseResource.getLineageDAO();
							lDao.setAuthUserId(userId);
							List<AssemblyLineageItem> lineage = lDao.getRawAnalysisPrepLineage(analysis.getId(), userId);
							aqcr.setLineage(ReportResource.createAQCLineage(lineage, analysis.getId()));
							
							json = ServiceBaseResource.returnJSON(aqcr, response, true, false);
						} else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND,
									"No analysis with ID '" + analysisId + "' could be found.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "User does not have privileges to generate ATR.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
			} catch (HaltException he) {
				throw he;
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}
	};

	public static final Route getReportingEventReports = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return ReportResource.getReports(":reId", request, response);
		}
	};

	public static final Route getAnalysisReports = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			return ReportResource.getReports(":analysisId", request, response);
		}
	};
	
	private static final String getReports(String idName, Request request, Response response) throws Exception {
		String json = null;
		try {
			String userId = request.headers("IAMPFIZERUSERCN");
			if (userId != null) {
				AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
				String assemblyId = request.params(idName);
				Assembly assembly = aDao.getAssembly(assemblyId);
				if (assembly != null) {
					ReportSearch search = new ReportSearch();
					search.setSubType(request.queryParams("subType"));
					search.setParentAssemblyId(assemblyId);
					
					String committedqp = request.queryParams("includeUncommitted");
					if(committedqp != null) {
						try {
							search.setIncludeUncommitted(Boolean.parseBoolean(committedqp));
						}
						catch(Exception e) { }
					}
					
					String supersededqp = request.queryParams("includeSuperseded");
					if(supersededqp != null) {
						try {
							search.setIncludeSuperseded(Boolean.parseBoolean(supersededqp));
						}
						catch(Exception e) { }
					}
					
					String deletedqp = request.queryParams("includeDeleted");
					if(deletedqp != null) {
						try {
							search.setIncludeDeleted(Boolean.parseBoolean(deletedqp));
						}
						catch(Exception e) { }
					}

					DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
					List<Dataframe> matches = dDao.searchReports(search);
					json = ServiceBaseResource.returnJSON(matches, response, false, false);
				} else {
					Spark.halt(HTTPStatusCodes.NOT_FOUND,
							"No assembly with ID '" + assemblyId + "' could be found.");
				}
			} else {
				Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
			}
		} catch (HaltException he) {
			throw he;
		} catch (Exception e) {
			ServiceExceptionHandler.handleException(e);
		}

		return json;
	}

	private static final List<AnalysisQCLineageItem> createAQCLineage(List<AssemblyLineageItem> lineage,
			String nodeId) {
		List<AnalysisQCLineageItem> items = new ArrayList<>();
		List<AssemblyLineageItem> paths = lineage;
		// List<AssemblyLineageItem> paths = LineageSearcher.getPaths(lineage, nodeId);
		for (AssemblyLineageItem aItem : paths) {
			AnalysisQCLineageItem item = AnalysisQCLineageItem.fromLineageItem(aItem);
			items.add(item);
		}

		for (AssemblyLineageItem aItem : paths) {
			ReportResource.down(items, aItem);
		}

		return items;
	}

	private static final void down(List<AnalysisQCLineageItem> items, LineageItem lineageItem) {
		for (LineageItem childAssembly : lineageItem.getChildAssemblies()) {
			if (!ReportResource.lineageContainsEquipId(items, childAssembly.getEquipId())) {
				AnalysisQCLineageItem item = AnalysisQCLineageItem.fromLineageItem(childAssembly);
				items.add(item);
			}
		}
		for (LineageItem childDataframe : lineageItem.getChildDataframes()) {
			DataframeLineageItem dli = (DataframeLineageItem) childDataframe;
			if (!dli.getDataframeType().equalsIgnoreCase(Dataframe.REPORT_TYPE) && !ReportResource.lineageContainsEquipId(items, childDataframe.getEquipId())) {
				AnalysisQCLineageItem item = AnalysisQCLineageItem.fromLineageItem(childDataframe);
				items.add(item);
			}
		}

		for (LineageItem childAssembly : lineageItem.getChildAssemblies()) {
			ReportResource.down(items, childAssembly);
		}
		for (LineageItem childDataframe : lineageItem.getChildDataframes()) {
			ReportResource.down(items, childDataframe);
		}
	}

	private static final boolean lineageContainsEquipId(List<AnalysisQCLineageItem> items, String equipId) {
		for (AnalysisQCLineageItem item : items) {
			if (item.getEquipId() != null && item.getEquipId().equals(equipId)) {
				return true;
			}
		}
		
		return false;
	}
}
