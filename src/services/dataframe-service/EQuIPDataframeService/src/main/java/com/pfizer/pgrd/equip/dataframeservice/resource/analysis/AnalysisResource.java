package com.pfizer.pgrd.equip.dataframeservice.resource.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionableListGetter;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyUtils;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.AnalysisMCTCopyParameters;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.dto.interfaces.EquipVersion;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeRootResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.exceptions.ErrorCodeException;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.computeservice.dto.Parameter;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class AnalysisResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisResource.class);

	public static final Route getById = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());

			try {
				String analysisId = request.params(":id");
				if (analysisId != null) {
					AnalysisDAO dao = ServiceBaseResource.getAnalysisDAO();
					Analysis a = dao.getAnalysis(analysisId);
					if (a != null) {
						String userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						
						ServiceBaseResource.handleUserAccess(a, userId);

						AuthorizationDAO auth = new AuthorizationDAO();
						boolean isOk = auth.checkPrivileges("analysis", "GET", userId);
						if (!isOk) {
							Spark.halt(HTTPStatusCodes.FORBIDDEN,
									"User " + userId + " does not have privileges to run this analysis");
						}

						if(Props.isAudit()){
							/*asc.logAuditEntry(	"Access of Analysis", 
												a.getEquipId(),
												a.getAnalysisType(),
												userId,
												Props.isAudit(),
												Const.AUDIT_SUCCESS,
												a.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Access of Analysis", a, userId);
							details.setContextEntity(a);
							asc.logAuditEntryAsync(details);
						}
						
						Gson gson = new Gson();
						json = gson.toJson(a);
					} else {
						Spark.halt(HTTPStatusCodes.NOT_FOUND, "No analysis with ID " + analysisId + " could be found.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No analysis ID was provided.");
				}
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}

	};

	public static final Route createAnalysis = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;
			String userId = null;
			Analysis analysis = null;
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try {
				String jsonBody = request.body();
				if (jsonBody != null && !jsonBody.isEmpty()) {
					Gson gson = new Gson();
					analysis = gson.fromJson(jsonBody, Analysis.class);
					if (analysis != null) {
						userId = request.headers("IAMPFIZERUSERCN");
						if (userId == null) {
							Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
						}
						if (analysis.getStudyIds() != null && !analysis.getStudyIds().isEmpty()) {
							if (analysis.getAssemblyType() == null || analysis.getAssemblyType().isEmpty()) {
								analysis.setAssemblyType("Analysis");
							}
							if (userId == null) {
								Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
							}

							AuthorizationDAO auth = new AuthorizationDAO();
							boolean isOk = auth.checkPrivileges("analysis", "POST", userId);
							if (!isOk) {
								Spark.halt(HTTPStatusCodes.FORBIDDEN,
										"User " + userId + " does not have privileges to run this analysis");
							}

							if (analysis.getCreated() == null) {
								analysis.setCreated(new Date());
							}
							if (analysis.getCreatedBy() == null) {
								analysis.setCreatedBy(userId);
							}

							AnalysisDAO adao = ServiceBaseResource.getAnalysisDAO();
							AnalysisResource.applyVersioning(analysis, adao);
							
							if(analysis.getId() != null && !analysis.getId().equals("")) {
								Analysis fromAnalysis = adao.getAnalysis(analysis.getId());
								ServiceBaseResource.handleUserAccess(fromAnalysis, userId);
								
								CopyUtils.copyMCTKEL(userId, analysis, fromAnalysis);
							}
							
							analysis.setId(null);
							
							if(AnalysisResource.checkProgramName(adao, analysis) == false ) {
								String programStudy = analysis.getStudyIds().get(0);
								Spark.halt(HTTPStatusCodes.BAD_REQUEST,
										"The analysis references an invalid or unknown program " + programStudy);
							}
							
							analysis = adao.insertAnalysis(analysis);

							if (analysis != null) {
								if(Props.isAudit()){
									/*asc.logAuditEntry(	"Creation of Analysis",
														analysis.getEquipId(),
														"Analysis",
														userId,
														Props.isAudit(),
														Const.AUDIT_SUCCESS,
														analysis.getVersionNumber());*/
									AuditDetails details = asc.new AuditDetails("Creation of Analysis", analysis, userId);
									details.setContextEntity(analysis);
									asc.logAuditEntryAsync(details);
								}
								if(analysis.getDataframeIds() != null && analysis.getDataframeIds().size()>0){
									DataframeDAO dfdao = ServiceBaseResource.getDataframeDAO();
									Dataframe df = dfdao.getDataframe(analysis.getDataframeIds().get(0));
									if(Props.isAudit()){
										/*asc.logAuditEntry(	"Node <Equipid=" + df.getEquipId() + ", version=" + df.getVersionNumber() + "> has been selected in " + analysis.getEquipId() + " Analysis.",
															analysis.getEquipId(),
															"ANALYSIS",
															userId,
															Props.isAudit(),
															Const.AUDIT_SUCCESS,
															analysis.getVersionNumber());*/
										AuditDetails details = asc.new AuditDetails("Node <Equipid=" + df.getEquipId() + ", version=" + df.getVersionNumber() + "> has been selected in " + analysis.getEquipId() + " Analysis.", df, userId);
										details.setContextEntity(analysis);
										asc.logAuditEntryAsync(details);
									}
								}
								//audit nominal data changes
								EntityVersioningResource.auditNominalDataChanges(analysis.getId(), userId, asc);
								
								
								
								// call opmeta service to update modification time on associated protocol
								try {
									OpmetaServiceClient osc = new OpmetaServiceClient();
									osc.setHost(Props.getOpmetaServiceServer());
									osc.setPort(Props.getOpmetaSerivcePort());
									List<String> studyIds = analysis.getStudyIds();
									for(String studyId: studyIds) {
										LOGGER.info("AnalysisResource: update protocl for study id=" + studyId);
										osc.updateProtocolModifiedDate(userId, studyId);
									}
								}
								catch(Exception err) {
									LOGGER.warn("AnalysisResource: Error updating protocol modification time for node " + analysis.id, err);
								}
								
								returnJson = gson.toJson(analysis);
								response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
								response.header(HTTPHeaders.LOCATION, "/analyses/" + analysis.getId());
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST,
										"The analysis could not be created using the input provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "At least one study ID must be provided.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST,
								"The provided body is not a JSON representation of an Analysis.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No body was provided in the request.");
				}
			} catch (Exception e) {
				try{
					if(userId != null && analysis != null){
						if(Props.isAudit()){
							/*asc.logAuditEntry(	"Attempt to create Analysis failed with exception " + e.getMessage(), 
												analysis.getEquipId(),
												"ANALYSIS",
												userId,
												Props.isAudit(),
												Const.AUDIT_FAILURE,
												analysis.getVersionNumber());*/
							AuditDetails details = asc.new AuditDetails("Attempt to create Analysis failed with exception " + e.getMessage(), analysis, userId);
							details.setContextEntity(analysis);
							details.setActionStatus(AuditDetails.FAILURE);
							asc.logAuditEntryAsync(details);
						}
					}
				}
				catch(Exception ex2){
					LOGGER.error("", ex2); //intentionally swallowing exception, we want the original exception to be reported.
				}
				
				ServiceExceptionHandler.handleException(e);
			}

			return returnJson;
		}
	};
	
	private static boolean checkProgramName(AnalysisDAO adao, Analysis analysis) {
		return AnalysisResource.checkProgramName(adao, analysis.getStudyIds());
	}
	
	private static boolean checkProgramName(AnalysisDAO adao, List<String> studyIds) {
		boolean result = false;
		if(studyIds != null && !studyIds.isEmpty()) {
			String programStudy = studyIds.get(0);
	
			if (programStudy != null) {
				String[] parts = programStudy.split(":");
				String programCode = parts[0].trim();
				
				result = adao.checkProgramPath(programCode);
			}
		}

		return result;
	}
	
	private static void applyVersioning(Analysis analysis, AnalysisDAO dao) {
		try {
			EquipVersionableListGetter assemblySiblingGetter = equipId -> {
				List<Analysis> analyses = dao.getAnalysisByEquipId(equipId);
				List<EquipVersionable> retVal = new ArrayList<>();
				for (Analysis a : analyses) {
					retVal.add(a);
				}

				return retVal;
			};

			new VersioningDAO().applyVersionIncrementingLogic(analysis, analysis.getAssemblyType(),
					assemblySiblingGetter);
		} catch (ErrorCodeException ex) {
			Spark.halt(ex.getErrorCode(), ex.getMessage());
		}
	}
	
	public static final Route copyMCT = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String success = "Unsuccessful";
			response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			
			try {
				String analysisId = request.params(":id");
				if(analysisId != null && request.body() != null) {
					Gson gson = new Gson();
					AnalysisMCTCopyParameters params = gson.fromJson(request.body(), AnalysisMCTCopyParameters.class);
					String userId = request.headers(Const.IAMPFIZERUSERCN);
					if (userId == null) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
					}
					if(params != null && params.getFromAnalysisId() != null) {
						AnalysisDAO adao = ServiceBaseResource.getAnalysisDAO();
						Analysis analysis = adao.getAnalysis(analysisId);
						if(analysis != null) {
							String fromAnalysisId = params.getFromAnalysisId();
							Analysis fromAnalysis = adao.getAnalysis(fromAnalysisId);
							if(fromAnalysis != null && fromAnalysis.getModelConfigurationDataframeId() != null) {
								DataframeDAO ddao = ServiceBaseResource.getDataframeDAO();
								Dataframe nmct = ddao.getDataframe(fromAnalysis.getModelConfigurationDataframeId());
								Dataframe omct = ddao.getDataframe(analysis.getModelConfigurationDataframeId());
								if(omct != null && nmct != null) {
									nmct.setEquipId(omct.getEquipId());
									nmct = CopyUtils.copyDataframe(nmct, true);
									analysis.setModelConfigurationDataframeId(nmct.getId());
									
									adao.updateAnalysis(analysis);

//audit happens on COMMIT action									
//									if(omct!=null){
//										String contentId = "" + nmct.getEquipId() + " v." + nmct.getVersionNumber();
//										asc.logAuditEntry(	"Analysis Copy MCT",
//															omct.getEquipId(),
//															omct.getDataframeType(),
//															userId,
//															Props.isAudit(),
//															Const.AUDIT_SUCCESS,
//															omct.getVersionNumber(),
//															nmct.getEquipId(),
//															contentId,
//															null);
//									}
									
									success = "Success";
								}
								else {
									Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No MCT dataframes could be found.");
								}
							}
							else {
								Spark.halt(HTTPStatusCodes.NOT_FOUND, "No analysis with ID '" + fromAnalysisId + "' could be found.");
							}
						}
						else {
							Spark.halt(HTTPStatusCodes.NOT_FOUND, "No analysis with ID '" + analysisId + "' could be found.");
						}
					}
					else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No from analysis ID was provided.");
					}
				}
				else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No analysis ID and/or body was provided.");
				}
			}
			catch(Exception e) {
				ServiceExceptionHandler.handleException(e);
			}
			
			return success;
		}
	};
	
	
	public static final Route saveAnalysis = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			// AUTHORIZATION & VALIDATION
			String json = null;
			String userId = request.headers("IAMPFIZERUSERCN");
			if (userId == null) {
				Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined.");
			}
			
			AuthorizationDAO auth = new AuthorizationDAO();
			boolean isOk = auth.checkPrivileges("analysis", "POST", userId);
			if (!isOk) {
				Spark.halt(HTTPStatusCodes.FORBIDDEN,
						"User " + userId + " does not have privileges to save analyses.");
			}
			
			String body = request.body();
			if(body == null || body.trim().isEmpty()) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No parameters were provided.");
			}
			
			List<AnalysisSaveParameters> list = AnalysisResource.unmarshalObject(body, AnalysisSaveParameters.class);
			if(list.size() != 1) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "A body was provided that did not contain exaclty one parameter object.");
			}
			
			AnalysisSaveParameters parameters = list.get(0);
			if(parameters.getConcentrationDataframeId() == null) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "A concentration data ID must be provided.");
			}
			// ------
			
			// THE ACTUAL SAVING
			Analysis an = AnalysisResource.saveAnalysis(userId, parameters);
			if(an != null) {
				json = AnalysisResource.returnJSON(an, response);
			}
			
			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(), Props.getExternalServicesPort());
			if(Props.isAudit()){
				//asc.logAuditEntryAsync("Saving analysis.");
			}
			
			return json;
		}
		
	};
	
	public static final Analysis saveAnalysis(String userId, AnalysisSaveParameters analysisSaveParameters) throws Exception {
		Analysis an = null;
		if(analysisSaveParameters != null) {
			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			AnalysisDAO aDao = ModeShapeDAO.getAnalysisDAO();
			
			AnalysisResource ar = new AnalysisResource();
			AnalysisSaveJournal journal = ar.new AnalysisSaveJournal();
			
			long fetchAnalysis = 0;
			long checkProgramName = 0;
			long fetchConc = 0;
			long mctInheritConc = 0;
			long fetchMct = 0;
			long kelInheritConc = 0;
			long fetchKel = 0;
			long computePPM = 0;
			long commitParameters = 0;
			long commitMCTKEL = 0;
			long supersedeOldAn = 0;
			long versionAn = 0;
			long insertAn = 0;
			long commitAn = 0;
			
			long totalTime = System.currentTimeMillis();
			try {
				Analysis analysis = null;
				List<String> studyIds = null;
				if(analysisSaveParameters.getStudyIds() != null && !analysisSaveParameters.getStudyIds().isEmpty()) {
					studyIds = analysisSaveParameters.getStudyIds();
				}
				
				fetchAnalysis = System.currentTimeMillis();
				if(analysisSaveParameters.getAnalysisId() != null) {
					analysis = aDao.getAnalysis(analysisSaveParameters.getAnalysisId());
					if(analysis.isLocked() && (analysis.getLockedByUser() == null || !analysis.getLockedByUser().equalsIgnoreCase(userId))) {
						Spark.halt(HTTPStatusCodes.FORBIDDEN, "Analysis " + analysis.getEquipId() + " v" + analysis.getVersionNumber() + " is locked and cannot be modified.");
					}
					
					if(studyIds == null) {
						studyIds = analysis.getStudyIds();
					}
				}
				fetchAnalysis = System.currentTimeMillis() - fetchAnalysis;
				
				if(studyIds == null || studyIds.isEmpty()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No study IDs were provided.");
				}
				
				checkProgramName = System.currentTimeMillis();
				if(AnalysisResource.checkProgramName(aDao, studyIds) == false ) {
					String programStudy = studyIds.get(0);
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							"The analysis references an invalid or unknown program " + programStudy);
				}
				checkProgramName = System.currentTimeMillis() - checkProgramName;
				
				// Keep track of the IDs we need to check for released reporting events.
				List<String> mctKelCestParamsIds = new ArrayList<>();
				
				fetchConc = System.currentTimeMillis();
				Dataframe concentrationData = dDao.getDataframe(analysisSaveParameters.getConcentrationDataframeId());
				if(concentrationData == null) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No concentration data ID was provided or refers to a non-exsitant dataframe.");
				}
				
				if(analysis != null && analysis.getParametersDataframeId() != null) {
					mctKelCestParamsIds.add(analysis.getParametersDataframeId());
				}
				
				fetchConc = System.currentTimeMillis() - fetchConc;
				
				boolean useEquipIds = (analysis != null && analysis.getParametersDataframeId() != null);
				
				// HANDLE MCT, KEL, & CEST
				Dataframe mct = null;
				Dataframe kel = null;
				Dataframe cest = null;
				
				// Get the existing MCT and KEL (if any)
				List<Thread> supThreads = new ArrayList<>();
				List<Dataframe> mctKelCest = new ArrayList<>();
				if(analysis != null && useEquipIds) {
					if(analysis.getModelConfigurationDataframeId() != null) {
						mct = dDao.getDataframe(analysis.getModelConfigurationDataframeId());
						mctKelCest.add(mct);
						mctKelCestParamsIds.add(mct.getId());
					}
					if(analysis.getKelFlagsDataframeId() != null) {
						kel = dDao.getDataframe(analysis.getKelFlagsDataframeId());
						mctKelCest.add(kel);
						mctKelCestParamsIds.add(kel.getId());
					}
					if(analysis.getEstimatedConcDataframeId() != null) {
						cest = dDao.getDataframe(analysis.getEstimatedConcDataframeId());
						mctKelCest.add(cest);
						mctKelCestParamsIds.add(cest.getId());
					}
				}
				
				// Need to check to see if the MCT, KEL, or PPRMS are part of any released reporting events.
				// If they are, we need to halt.
				// ------
				// CHECK RELEASED RE
				ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
				Map<String, List<Assembly>> map = rpDao.getReportingEventsByDataframeId(mctKelCestParamsIds);
				String errors = null;
				for(Entry<String, List<Assembly>> entry : map.entrySet()) {
					List<Assembly> reportingEvents = entry.getValue();
					for(Assembly reportingEvent : reportingEvents) {
						if(reportingEvent.isReleased()) {
							if(errors == null) {
								errors = "One or more nodes are referenced by released reporting event(s): ";
							}
							else {
								errors += ", ";
							}
							
							errors += reportingEvent.getEquipId() + " v" + reportingEvent.getVersionNumber();
						}
					}
				}
				
				if(errors != null) {
					errors += ".";
					Spark.halt(HTTPStatusCodes.CONFLICT, errors);
				}
				// ------
				
				
				// Supersede existing MCT, KEL, and CEST if necessary
				for(Dataframe df : mctKelCest) {
					if(!df.getVersionSuperSeded()) {
						Runnable r = new Runnable() {

							@Override
							public void run() {
								EntityVersioningResource.supersedeAction(df, userId);
								String m = "ANALYSIS SAVE: Superseded " + df.getEquipId() + " v" + df.getVersionNumber() + " (" + df.getId() + ")";
								LOGGER.info(m);
								System.out.println(m);
							}
							
						};
						Thread t = new Thread(r);
						t.start();
						supThreads.add(t);
					}
				}
				
				for(Thread t : supThreads) {
					t.join();
				}
				
				List<InheritConcentrationThread> inheritThreads = new ArrayList<>();
				if(analysisSaveParameters.getMct() != null) {
					InheritConcentrationThread mctRun = new InheritConcentrationThread();
					mctRun.concentrationData = concentrationData;
					mctRun.userId = userId;
					mctRun.studyIds = analysisSaveParameters.getStudyIds();
					mctRun.data = analysisSaveParameters.getMct();
					mctRun.type = Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE;
					mctRun.supersede = useEquipIds;
					if(mct != null && useEquipIds) {
						mctRun.equipId = mct.getEquipId();
					}
					
					mctRun.start();
					inheritThreads.add(mctRun);
				}
				
				if(analysisSaveParameters.getKelFlags() != null) {
					InheritConcentrationThread kelRun = new InheritConcentrationThread();
					kelRun.concentrationData = concentrationData;
					kelRun.userId = userId;
					kelRun.studyIds = analysisSaveParameters.getStudyIds();
					kelRun.data = analysisSaveParameters.getKelFlags();
					kelRun.type = Dataframe.KEL_FLAGS_TYPE;
					kelRun.supersede = useEquipIds;
					if(kel != null && useEquipIds) {
						kelRun.equipId = kel.getEquipId();
					}
					
					kelRun.start();
					inheritThreads.add(kelRun);
				}
				
				errors = "";
				for(InheritConcentrationThread t: inheritThreads) {
					t.join();
					if(t.getError() == null) {
						if(t.type == Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE) {
							mct = t.getResult();
						}
						else if(t.type == Dataframe.KEL_FLAGS_TYPE) {
							kel = t.getResult();
						}
					}
					else {
						Exception e = t.getError();
						String m = e.getMessage();
						if(e instanceof HaltException) {
							m = ((HaltException)e).body();
						}
						
						errors += " " + m;
					}
				}
				
				if(mct == null) {
					Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "MCT could not be found and could not be created." + errors);
				}
				if(kel == null) {
					Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "KEL flags were not found and could not be created." + errors);
				}
				
				// ------
				// COMPUTE PARAMETERS DATAFRAME
				computePPM = System.currentTimeMillis();
				String scriptName = Props.getParametersScriptName();
				LibraryServiceClient lsc = new LibraryServiceClient();
				lsc.setHost(Props.getLibraryServiceServer());
				lsc.setPort(Props.getLibraryServicePort());
				lsc.setUser(userId);
				
				LibraryResponse lr = lsc.getGlobalSystemScriptByName(scriptName);
				String scriptId = lr.getArtifactId();
				
				ComputeServiceClient csc = new ComputeServiceClient();
				csc.setHost(Props.getComputeServiceServer());
				csc.setPort(Props.getComputeServicePort());
				csc.setUser(userId);
				
				ComputeParameters cp = new ComputeParameters();
				cp.setUser(userId);
				cp.setScriptId(scriptId);
				cp.setComputeContainer("equip-opennca");
				// the dataframe IDs must be in this order: concentration, MCT, KEL
				cp.getDataframeIds().add(concentrationData.getId());
				cp.getDataframeIds().add(mct.getId());
				cp.getDataframeIds().add(kel.getId());
				cp.addDataframeType(Dataframe.PRIMARY_PARAMETERS_TYPE);
				cp.addDataframeType(Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE);
				cp.addDataframeType(Dataframe.KEL_FLAGS_TYPE);
				cp.getParameters().add(new Parameter("parameterset", analysisSaveParameters.getParameterSet(), "STRING"));
				
				// Prevent the compute service from making a batch.
				cp.setDontBatch(true);
				
				if(useEquipIds) {
					Dataframe parameters = dDao.getDataframe(analysis.getParametersDataframeId());
					EntityVersioningResource.supersedeAction(parameters, userId);
					cp.setEquipId(parameters.getEquipId());
					String cestEID = "";
					if(cest != null) {
						cestEID = cest.getEquipId();
					}
					cp.addEquipId(cestEID);
					cp.addEquipId(kel.getEquipId());
				}
				
				ComputeResult cr = csc.compute(cp);
				computePPM = System.currentTimeMillis() - computePPM;
				
				Dataframe parametersDataframe = null;
				Dataframe estimatedConcentration = null;
				Dataframe computedKel = null;
				
				// If there were not 3 dataframe IDs returned, then compute is considered to have failed 
				// and we leave PPM and estimated conc. null.
				if(cr.getDataframeIds().size() == 3) {
					List<Dataframe> dfs = dDao.getDataframe(cr.getDataframeIds());
					for(Dataframe df : dfs) {
						String outputFilename = df.getOutputFileName();
						//df.setBatchId(null);
						if(outputFilename.equalsIgnoreCase("output1")) {
							parametersDataframe = df;
						}
						else if(outputFilename.equalsIgnoreCase("cest.json")) {
							estimatedConcentration = df;
						}
						else if(outputFilename.equalsIgnoreCase("flags.json")) {
							computedKel = df;
							String m = "ANALYSIS SAVE: Compute created " + computedKel.getEquipId() + " v" + computedKel.getVersionNumber() + " (" + computedKel.getId() + ")";
							LOGGER.info(m);
							System.out.println(m);
						}
					}
				}
				
				// Check for any error messages.
				Comment errorComment = null;
				if(cr.getStdout() != null) {
					String stdOut = cr.getStdout().toLowerCase();
					
					String p = Props.getComputeErrorRegex();
					Pattern pattern = Pattern.compile(p);
					Matcher m = pattern.matcher(stdOut);
					if(m.find()) {
						String body = "Failed to generate parameters.";
						if(parametersDataframe != null) {
							body = "Finalized with errors.";
						}
						
						errorComment = new Comment();
						errorComment.setCommentType(Comment.ANALYSIS_SAVE_ERROR_TYPE);
						errorComment.setCreatedBy("System");
						errorComment.setCreated(new Date());
						errorComment.setBody(body);
					}
				}
				
				List<CommitThread> commitThreads = new ArrayList<>();
				if(parametersDataframe != null) {
					journal.parameters = parametersDataframe;
					if(estimatedConcentration != null) {
						journal.estimatedConcId = estimatedConcentration.getId();
						
						CommitThread t = new CommitThread();
						t.entityId = estimatedConcentration.getId();
						t.userId = userId;
						t.start();
						commitThreads.add(t);
					}
					
					CommitThread t = new CommitThread();
					t.entityId = journal.parameters.getId();
					t.userId = userId;
					t.start();
					commitThreads.add(t);
				}
				
				// ------
				// CREATE & COMMIT THE ANALYSIS
				if(analysis == null) {
					analysis = new Analysis();
				}
				else {
					// Remove any previous analysis save error comments
					List<Comment> comments = analysis.getComments();
					analysis.setComments(new ArrayList<>());
					for(Comment c : comments) {
						if(!c.getCommentType().equalsIgnoreCase(Comment.ANALYSIS_SAVE_ERROR_TYPE)) {
							c.setId(null);
							analysis.getComments().add(c);
						}
					}
					
					// Remove any previous stdout metadata
					List<Metadatum> metadata = analysis.getMetadata();
					analysis.setMetadata(new ArrayList<>());
					for(Metadatum md : metadata) {
						if(!md.getKey().equalsIgnoreCase(Metadatum.COMPUTE_STD_OUT_KEY)) {
							md.setId(null);
							analysis.getMetadata().add(md);
						}
					}
				}
				
				analysis.setModifiedDate(null);
				analysis.setModifiedBy(null);
				
				Date d = new Date();
				if(analysis.getCreatedBy() != null && analysis.getCreatedBy().isEmpty()) {
					analysis.setModifiedBy(userId);
					analysis.setModifiedDate(d);
				}
				
				analysis.setCreatedBy(userId);
				analysis.setCreated(d);
				analysis.setStudyIds(studyIds);
				analysis.setParentIds(analysisSaveParameters.getParentIds());
				analysis.setDataframeIds(Arrays.asList(analysisSaveParameters.getConcentrationDataframeId()));
				analysis.setProtocolIds(analysisSaveParameters.getProtocolIds());
				analysis.setProgramIds(analysisSaveParameters.getProgramIds());
				analysis.setProjectIds(analysisSaveParameters.getProjectIds());
				analysis.setAnalysisType(analysisSaveParameters.getAnalysisType());
				analysis.setConfigurationTemplateId(analysisSaveParameters.getConfigurationTemplateId());
				if(parametersDataframe != null) {
					analysis.setParametersDataframeId(parametersDataframe.getId());
				}
				else if(errorComment != null) {
					analysis.setParametersDataframeId(null);
				}
				
				if(estimatedConcentration != null) {
					analysis.setEstimatedConcDataframeId(estimatedConcentration.getId());
				}
				analysis.setName(null);
				analysis.setQcStatus("Not QC'd");
				analysis.setAssemblyType(Assembly.ANALYSIS_TYPE);
				analysis.setMetadata(analysisSaveParameters.getMetadata());
				//analysis.setLocked(false);
				//analysis.setLockedByUser(null);
				
				if(errorComment != null) {
					analysis.setSaveErrorComment(errorComment);
					if(parametersDataframe != null) {
						parametersDataframe.getComments().add(errorComment);
						
						CommentDAO cDao = AnalysisResource.getCommentDAO();
						cDao.insertComment(errorComment, parametersDataframe.getId());
					}
				}
				
				boolean optimizeKel = false;
				if(analysisSaveParameters.getMct() != null) {
					Gson gson = new Gson();
					MCTParameters kelParams = gson.fromJson(analysisSaveParameters.getMct(), MCTParameters[].class)[0];
					optimizeKel = kelParams.optimizeKEL();
				}
				
				String kelId = null;
				ModeShapeDAO msDao = new ModeShapeDAO();
				if(useEquipIds || optimizeKel) {
					if(computedKel != null) {
						kel = computedKel;
						kelId = kel.getId();
					}
					else if(kel != null) {
						// If we're here, then we wanted compute to override the KEL flags, but failed to do so.
						// We need to un-supersede the KEL flags we created.
						PropertiesPayload pp = new PropertiesPayload();
						pp.addProperty("equip:versionSuperSeded", false);
						msDao.updateNode(kel.getId(), pp);
						kelId = kel.getId();
					}
				}
				else if(kel != null) {
					kelId = kel.getId();
				}
				
				commitMCTKEL = System.currentTimeMillis();
				analysis.setKelFlagsDataframeId(kelId);
				if(kelId != null) {
					if(kel == null || !kel.isCommitted()) {
						CommitThread t = new CommitThread();
						t.entityId = kelId;
						t.userId = userId;
						t.start();
						commitThreads.add(t);
					}
				}
				
				if(mct != null) {
					analysis.setModelConfigurationDataframeId(mct.getId());
					if(!mct.isCommitted()) {
						CommitThread t = new CommitThread();
						t.entityId = mct.getId();
						t.userId = userId;
						t.start();
						commitThreads.add(t);
					}
				}
				
				commitMCTKEL = System.currentTimeMillis() - commitMCTKEL;
				
				Metadatum md = new Metadatum();
				md.setKey(Metadatum.COMPUTE_STD_OUT_KEY);
				md.addValue(cr.getStdout());
				analysis.getMetadata().add(md);
				
				if(analysis.getEquipId() != null) {
					supersedeOldAn = System.currentTimeMillis();
					EntityVersioningResource.supersedeAction(analysis, userId);
					analysis.setVersionSuperSeded(false);
					analysis.setCommitted(false);
					analysis.setVersionNumber(0);
					supersedeOldAn = System.currentTimeMillis() - supersedeOldAn;
				}
				
				versionAn = System.currentTimeMillis();
				AnalysisResource.applyVersioning(analysis, aDao);
				versionAn = System.currentTimeMillis() - versionAn;
				
				for(Metadatum meta : analysis.getMetadata()) {
					if(meta.getCreated() == null) {
						meta.setCreated(new Date());
					}
					if(meta.getCreatedBy() == null || meta.getCreatedBy().isEmpty()) {
						meta.setCreatedBy(userId);
					}
				}
				
				insertAn = System.currentTimeMillis();
				analysis.setId(null);
				analysis = aDao.insertAnalysis(analysis);
				an = analysis;
				insertAn = System.currentTimeMillis() - insertAn;
				
				commitAn = System.currentTimeMillis();
				EntityVersioningResource.put.handle(an.getId(), "commit", userId, null, null);
				String m = "ANALYSIS SAVE: Committed analysis " + an.getEquipId() + " v" + an.getVersionNumber() + " (" + an.getId() + ")";
				LOGGER.info(m);
				System.out.println(m);
				commitAn = System.currentTimeMillis();
				
				an = aDao.getAnalysis(an.getId());
				
				// Wait for the threads to finish before returning
				Exception e = null;
				for(CommitThread t : commitThreads) {
					t.join();
					if(t.getError() != null) {
						e = t.getError();
					}
				}
				
				if(e != null) {
					throw e;
				}
				// -----
				
				// Audit any nominal changes.
				// This is just an audit entry, so we don't care if it finishes or not; we don't need to join it.
				AuditNominalThread auditThread = new AuditNominalThread();
				auditThread.analysis = an;
				auditThread.userId = userId;
				auditThread.start();
			}
			catch(HaltException he) {
				throw he;
			}
			catch(Exception e) {
				e.printStackTrace();
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
			finally {
				if(analysisSaveParameters.shouldRollback()) {
					// go through the journal and see what needs to be deleted, uncommitted, and unsuperseded
					
					if(journal.mctId != null) {
						dDao.deleteDataframe(journal.mctId);
					}
					if(journal.kelId != null) {
						dDao.deleteDataframe(journal.kelId);
					}
					if(journal.parameters != null) {
						dDao.deleteDataframe(journal.parameters.getId());
					}
					if(journal.estimatedConcId != null) {
						dDao.deleteDataframe(journal.estimatedConcId);
					}
				}
			}
			
			totalTime = System.currentTimeMillis() - totalTime;
			System.out.println("Total Time: " + toSeconds(totalTime));
			printStat("Fetch Analysis", fetchAnalysis, totalTime);
			printStat("Check Program Name", checkProgramName, totalTime);
			printStat("Fetch Concentration", fetchConc, totalTime);
			printStat("MCT Inherit Concentration", mctInheritConc, totalTime);
			printStat("Fetch MCT", fetchMct, totalTime);
			printStat("KEL Inherit Concentration", kelInheritConc, totalTime);
			printStat("Fetch KEL", fetchKel, totalTime);
			printStat("Compute Primary Parameters", computePPM, totalTime);
			printStat("Commit Primary Parameters", commitParameters, totalTime);
			printStat("Commit MCT & KEL", commitMCTKEL, totalTime);
			printStat("Supersede Previous Analysis Version", supersedeOldAn, totalTime);
			printStat("Apply Versioning to Analysis", versionAn, totalTime);
			printStat("Insert Analysis", insertAn, totalTime);
			printStat("Commit Analysis", commitAn, totalTime);
		}
		
		return an;
	}
	
	private static void printStat(String name, long time, long totalTime) {
		System.out.println("\t" + name + ": " + toSeconds(time) + " (" + getPercent(time, totalTime) + "%)");
	}
	
	private static String toSeconds(long ms) {
		return ((double)ms / 1000.0) + "s";
	}
	
	private static double getPercent(long v, long t) {
		return ((double) v / (double) t) * 100.0;
	}
	
	private static final void insertData(String datasetId, String dataString) throws IOException {
		File f = null;
		try {
			DatasetDAO dsDao = ModeShapeDAO.getDatasetDAO();
			
			f = File.createTempFile("data", "txt");
			FileWriter fw = new FileWriter(f);
			fw.write(dataString);
			fw.flush();
			fw.close();
			
			byte[] data = new byte[(int)f.length()];
			FileInputStream fis = new FileInputStream(f);
			fis.read(data);
			fis.close();
			
			dsDao.insertData(datasetId, data);
		}
		catch(Exception e) {
			LOGGER.error("Error when saving KEL/MCT file data.", e);
			throw e;
		}
		finally {
			if(f != null) {
				try {
					boolean deleted = f.delete();
					if(!deleted) {
						LOGGER.info("Could not delete temporary file " + f.getAbsolutePath() + "\\" + f.getName() + ".");
					}
				}
				catch(Exception e) {
					LOGGER.error("Error when deleting temporary file " + f.getAbsolutePath() + "\\" + f.getName() + ".", e);
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final Dataframe inheritConcentrationData(String userId, String dataString, String dfType, Dataframe concentrationData, List<String> studyIds, String equipId, boolean supersede) throws IOException {
		Dataframe dataframe = new Dataframe();
		dataframe.setCreated(new Date());
		dataframe.setCreatedBy(userId);
		dataframe.setModifiedBy(dataframe.getCreatedBy());
		dataframe.setModifiedDate(dataframe.getCreated());
		dataframe.setStudyIds(studyIds);
		dataframe.setDataframeType(dfType);
		
		Dataset ds = new Dataset();
		ds.setDataSize(dataString.getBytes().length);
		ds.setMimeType("text/plain");
		dataframe.setDataset(ds);
		
		dataframe.setDataBlindingStatus(concentrationData.getDataBlindingStatus());
		dataframe.setDataStatus(concentrationData.getDataStatus());
		dataframe.setPromotionStatus(concentrationData.getPromotionStatus());
		dataframe.setRestrictionStatus(concentrationData.getRestrictionStatus());
		dataframe.getDataframeIds().add(concentrationData.getId());
		if(equipId != null) {
			dataframe.setEquipId(equipId);
		}
		
		dataframe.setVersionSuperSeded(supersede);
		
		/*if(dfType.equalsIgnoreCase(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE)) {
			dataframe.setCommitted(true);
		}*/
		
		Dataframe node = null;
		try {
			node = DataframeRootResource.insertDataframe(userId, dataframe);
			String m = "ANALYSIS SAVE: Created dataframe " + node.getEquipId() + " v" + node.getVersionNumber() + " (" + node.getId() + ")";
			LOGGER.info(m);
			System.out.println(m);
			
			if(supersede) {
				EntityVersioningResource.supersedeAction(dataframe, userId);
				m = "ANALYSIS SAVE: Superseded " + node.getEquipId() + " v" + node.getVersionNumber() + " (" + node.getId() + ")";
				LOGGER.info(m);
				System.out.println(m);
			}
		}
		catch(HaltException he) {
			Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, "Could not create " + dfType + ". " + he.body());
		}
		
		DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
		node = dDao.getDataframe(node.getId());
		if(node != null && node.getDataset() != null && node.getDataset().getId() != null) {
			AnalysisResource.insertData(node.getDataset().getId(), dataString);
		}
		
		return node;
	}
	
	private static class InheritConcentrationThread extends Thread {
		private Dataframe result = null;
		private Exception error = null;
		
		Dataframe concentrationData;
		
		List<String> studyIds = new ArrayList<>();
		String equipId = null;
		String userId = null;
		boolean supersede = false;
		
		String data = null;
		String dfId = null;
		String type = null;
		
		private long timeTaken = 0;
		
		@Override
		public void run() {
			DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
			if(this.data != null) {
				this.timeTaken = System.currentTimeMillis();
				try {
					this.result = AnalysisResource.inheritConcentrationData(this.userId, this.data, this.type, this.concentrationData, this.studyIds, this.equipId, supersede);
				}
				catch(Exception e) {
					this.error = e;
				}
				this.timeTaken = System.currentTimeMillis() - timeTaken;
			}
			else if(this.dfId != null) {
				this.result = dDao.getDataframe(this.dfId);
			}
		}
		
		public Dataframe getResult() {
			return this.result;
		}
		public Exception getError() {
			return this.error;
		}
		public long getTimeTaken() {
			return this.timeTaken;
		}
	}
	
	private static class AuditNominalThread extends Thread {
		public Analysis analysis;
		public String userId;
		
		@Override
		public void run() {
			try {
				EntityVersioningResource.auditNominalDataChanges(analysis.getId(), userId, null);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class CommitThread extends Thread {
		String entityId = null;
		String userId = null;
		
		private Exception error = null;
		private long timeTaken = 0;
		
		@Override
		public void run() {
			this.timeTaken = System.currentTimeMillis();
			try {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:versionSuperSeded", false);
				
				ModeShapeDAO msDao = new ModeShapeDAO();
				msDao.updateNode(this.entityId, pp);
				EntityVersioningResource.put.handle(this.entityId, "commit", this.userId, null, null);
				
				String m = "ANALYSIS SAVE: Committed " + this.entityId;
				LOGGER.info(m);
				System.out.println(m);
			}
			catch(Exception e) {
				this.error = e;
			}
			this.timeTaken = System.currentTimeMillis() - timeTaken;
		}
		
		public Exception getError() {
			return this.error;
		}
		public long getTimeTaken() {
			return this.timeTaken;
		}
	}
	
	class AnalysisSaveJournal {
		String mctId;
		String kelId;
		Dataframe parameters;
		String estimatedConcId;
	}
	
	class MCTParameters {
		private String OPTIMIZEKEL;
		public boolean optimizeKEL() {
			if(this.OPTIMIZEKEL != null && !this.OPTIMIZEKEL.equalsIgnoreCase("0")) {
				return true;
			}
			
			return false;
		}
	}
	
	public class AnalysisSaveParameters {
		private List<String> studyIds = new ArrayList<>();
		private List<String> parentIds = new ArrayList<>();
		private List<String> projectIds = new ArrayList<>();
		private List<String> programIds = new ArrayList<>();
		private List<String> protocolIds = new ArrayList<>();
		private String analysisType;
		private String configurationTemplateId;
		private String mct;
		private String kelFlags;
		private String concentrationDataframeId;
		private String analysisId;
		private String parameterSet;
		private List<Metadatum> metadata = new ArrayList<>();
		private List<Comment> comments = new ArrayList<>();
		private boolean rollback = false;
		private boolean optimizeKel = false;
		
		public List<String> getStudyIds() {
			return studyIds;
		}
		public void setStudyIds(List<String> studyIds) {
			this.studyIds = studyIds;
		}
		public List<String> getParentIds() {
			return parentIds;
		}
		public void setParentIds(List<String> parentIds) {
			this.parentIds = parentIds;
		}
		public List<String> getProjectIds() {
			return projectIds;
		}
		public void setProjectIds(List<String> projectIds) {
			this.projectIds = projectIds;
		}
		public List<String> getProgramIds() {
			return programIds;
		}
		public void setProgramIds(List<String> programIds) {
			this.programIds = programIds;
		}
		public String getAnalysisType() {
			return analysisType;
		}
		public void setAnalysisType(String analysisType) {
			this.analysisType = analysisType;
		}
		public String getConfigurationTemplateId() {
			return configurationTemplateId;
		}
		public void setConfigurationTemplateId(String configurationTemplateId) {
			this.configurationTemplateId = configurationTemplateId;
		}
		public String getConcentrationDataframeId() {
			return concentrationDataframeId;
		}
		public void setConcentrationDataframeId(String concentrationDataframeId) {
			this.concentrationDataframeId = concentrationDataframeId;
		}
		public String getAnalysisId() {
			return analysisId;
		}
		public void setAnalysisId(String analysisId) {
			this.analysisId = analysisId;
		}
		public String getParameterSet() {
			return parameterSet;
		}
		public void setParameterSet(String parameterSet) {
			this.parameterSet = parameterSet;
		}
		public List<Metadatum> getMetadata() {
			return metadata;
		}
		public void setMetadata(List<Metadatum> metadata) {
			this.metadata = metadata;
		}
		public List<Comment> getComments() {
			return comments;
		}
		public void setComments(List<Comment> comments) {
			this.comments = comments;
		}
		public boolean shouldRollback() {
			return rollback;
		}
		public void setRollback(boolean rollback) {
			this.rollback = rollback;
		}
		public List<String> getProtocolIds() {
			return protocolIds;
		}
		public void setProtocolIds(List<String> protocolIds) {
			this.protocolIds = protocolIds;
		}
		public String getMct() {
			return mct;
		}
		public void setMct(String mct) {
			this.mct = mct;
		}
		public String getKelFlags() {
			return kelFlags;
		}
		public void setKelFlags(String kelFlags) {
			this.kelFlags = kelFlags;
		}
		public void setOptimizeKel(boolean optimizeKel) {
			this.optimizeKel = optimizeKel;
		}
		public boolean optimizeKel() {
			return this.optimizeKel;
		}
	}
}
