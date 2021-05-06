package com.pfizer.pgrd.equip.dataframeservice.reportutils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.atr.AnalysisData;
import com.pfizer.pgrd.equip.dataframe.dto.atr.AuditTrailReport;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.atr.DataLoad;
import com.pfizer.pgrd.equip.dataframe.dto.atr.DataTransformation;
import com.pfizer.pgrd.equip.dataframe.dto.atr.DataframeInfo;
import com.pfizer.pgrd.equip.dataframe.dto.atr.File;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Parameter;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Program;
import com.pfizer.pgrd.equip.dataframe.dto.atr.PromotedDataTransformation;
import com.pfizer.pgrd.equip.dataframe.dto.atr.Protocol;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ProtocolContact;
import com.pfizer.pgrd.equip.dataframe.dto.atr.PublishedParameter;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ReportingEventItemSummary;
import com.pfizer.pgrd.equip.dataframe.dto.atr.StatusChangeEvent;
import com.pfizer.pgrd.equip.dataframe.dto.atr.ReportingEventItemSummary.Item;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageSearcher;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponseProperties;
import com.pfizer.pgrd.equip.services.opmeta.client.AssignedUser;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;
import com.pfizer.pgrd.equip.services.opmeta.client.ProtocolAlias;

public class ATRUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ATRUtils.class);

	private static final int PROGRAM_CODE_INDEX = 0, PROTOCOL_CODE_INDEX = 1;
	private boolean isRaw = false;
	private Map<String, LineageItem> CURRENT_LINEAGE_MAPPING = new HashMap<>();
	private List<AssemblyLineageItem> CURRENT_LINEAGE = new ArrayList<>();
	private List<AssemblyLineageItem> ALL_DATA_LOADS = new ArrayList<>();
	private List<DataTransformationLink> DATA_TRANSFORMATIONS = new ArrayList<>();
	private List<ReportingEventItem> DT_EVENT_ITEMS = new ArrayList<>();
	private AuditTrailReport CURRENT_REPORT;
	
	private DataframeDAO DDAO = ModeShapeDAO.getDataframeDAO();
	private AssemblyDAO ADAO = ModeShapeDAO.getAssemblyDAO();
	
	private AuthorizationDAO authDAO = null;

	private String username;

	/**
	 * Returns an {@link AuditTrailReport} object created using the provided
	 * reporting event ID.
	 * 
	 * @param reportingEventId
	 * @return {@link AuditTrailReport}
	 */
	public AuditTrailReport createReport(String reportingEventId) {
		AuditTrailReport atr = null;
		if (reportingEventId != null) {
			AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
			Assembly reportingEvent = aDao.getAssembly(reportingEventId);
			return this.createReport(reportingEvent);
		}

		return atr;
	}

	/**
	 * Returns an {@link AuditTrailReport} object created using the provided
	 * reporting event.
	 * 
	 * @param reportingEvent
	 * @return {@link AuditTrailReport}
	 */
	public AuditTrailReport createReport(Assembly reportingEvent) {
		this.CURRENT_REPORT = null;
		if (reportingEvent != null) {
			try {
				this.authDAO = new AuthorizationDAO();
			}
			catch(Exception e) {
				LOGGER.error("Error when trying to instantiate authorization DAO.", e);
				this.authDAO = null;
			}
			
			this.CURRENT_REPORT = new AuditTrailReport();
			
			String studyId = reportingEvent.getStudyIds().get(0);
			String[] parts = studyId.split(":");
			String programCode = parts[PROGRAM_CODE_INDEX];
			String protocolCode = parts[PROTOCOL_CODE_INDEX];
			
			LineageDAO ldao = ModeShapeDAO.getLineageDAO();
			ldao.setAuthUserId(this.username);
			this.CURRENT_LINEAGE = ldao.getAnalysisPrepLineage(studyId, this.username);
			for (AssemblyLineageItem adl : this.CURRENT_LINEAGE) {
				this.ALL_DATA_LOADS.add(adl);
			}
			
			this.CURRENT_LINEAGE_MAPPING = LineageSearcher.createEquipIdTable(this.CURRENT_LINEAGE);
			this.CURRENT_REPORT.setProgram(this.getProgram(programCode, protocolCode));
			ReportingEvent re = ReportingEvent.fromAssembly(reportingEvent);
			for(ReportingEventStatusChangeWorkflow scw : reportingEvent.getReportingEventStatusChangeWorkflows()) {
				if(scw.getReportingEventReleaseStatus() != null && !scw.getReportingEventReleaseStatus().equalsIgnoreCase(ReportingEventStatusChangeWorkflow.UNRELEASED_STATUS)) {
					StatusChangeEvent sce = new StatusChangeEvent();
					sce.setComments(Comment.fromComment(scw.getComments()));
					sce.setEventDate(scw.getCreated());
					sce.setUserId(scw.getCreatedBy());
					if(scw.getReportingEventReopenReasonKey() != null && !scw.getReportingEventReopenReasonKey().trim().isEmpty()) {
						sce.setEventType("Reopen");
						sce.setReopenReason(scw.getReportingEventReopenReasonKey().trim());
					}
					else {
						sce.setEventType("Release");
					}
					
					if(scw.getReportingEventReopenReasonAttachmentId() != null) {
						String aId = scw.getReportingEventReopenReasonAttachmentId().trim();
						if(!aId.isEmpty()) {
							Dataframe attachment = DDAO.getDataframe(aId);
							
							if(attachment != null) {
								sce.setFileEquipId(attachment.getEquipId());
								sce.setFileId(attachment.getId());
								sce.setFileName(attachment.getFileName());
								sce.setFileVersion(attachment.getVersionNumber());
							}
						}
					}
					
					re.getReleaseReopenEvents().add(sce);
				}
			}
			
			List<ReportingEventItemSummary> summaries = this.getEventItemSummaries(reportingEvent);
			re.setEventItemSummaries(summaries);
			this.CURRENT_REPORT.setReportingEvent(re);

			List<String> handledIds = new ArrayList<>();
			for (Entry<String, LineageItem> e : this.CURRENT_LINEAGE_MAPPING.entrySet()) {
				LineageItem li = e.getValue();
				if (li instanceof DataframeLineageItem) {
					DataframeLineageItem dli = (DataframeLineageItem) li;
					if (!handledIds.contains(dli.getEquipId())
							&& dli.getDataframeType().equalsIgnoreCase(Dataframe.DATA_TRANSFORMATION_TYPE)) {
						handledIds.add(dli.getEquipId());
						for (ReportingEventItemSummary reis : this.CURRENT_REPORT.getReportingEvent()
								.getEventItemSummaries()) {
							if (reis.getLineage().contains(dli.getEquipId())) {
								DataTransformation dt = this.CURRENT_REPORT.getDataTransformation(dli.getEquipId());
								if (dt == null) {
									Dataframe df = this.DDAO.getDataframe(dli.getId());
									//this.addToDataframes(df);
									this.addToDataframesInfo(df);
									if (df != null) {
										ReportingEventItem rei = null;
										for (ReportingEventItem i : this.DT_EVENT_ITEMS) {
											if (i.getDataFrameId() != null && i.getDataFrameId().equals(df.getId())) {
												rei = i;
												break;
											}
										}

										dt = this.createDataTransformation(df, rei);
										if (dt instanceof PromotedDataTransformation) {
											//this.addToDataframes(df);
											this.addToDataframesInfo(df);
											this.CURRENT_REPORT.getPromotedDataTransformations()
													.add((PromotedDataTransformation) dt);
										} else {
											this.CURRENT_REPORT.getDataTransformations().add(dt);
										}
									}
								}
								
								if (!dt.getLineages().contains(reis.getLineage())) {
									dt.getLineages().add(reis.getLineage());
									
									if (reis.getLeagacyLineage() != null) {
										dt.getLegacyLineages().add(reis.getLeagacyLineage());
									}
								}
							}
						}
					}
				}
			}
		}

		return this.CURRENT_REPORT;
	}
	
	private void addToDTs(Dataframe df, ReportingEventItem rei) {
		if (df.getDataframeType().equalsIgnoreCase(Dataframe.DATA_TRANSFORMATION_TYPE)
				&& !this.linkExists(DATA_TRANSFORMATIONS, df)) {
			this.DT_EVENT_ITEMS.add(rei);
			DATA_TRANSFORMATIONS.add(new DataTransformationLink(df, rei));
		}
	}
	
	private void addToDataframes(Dataframe df) {
		if(df != null) {
			boolean add = true;
			for(DataframeInfo dfi : this.CURRENT_REPORT.getDataframes()) {
				if(dfi.getId().equals(df.getId())) {
					add = false;
					break;
				}
			}
			
			if(add) {
				if(this.authDAO != null) {
					boolean canView = false;
					try {
						canView = this.authDAO.canViewDataframe(df, this.username);
					}
					catch(Exception e) {
						LOGGER.error("Error when requesting view access for dataframe " + df.getId() + ".", e);
					}
					
					if(canView) {
						this.CURRENT_REPORT.getDataframes().add(new DataframeInfo(df.getId(), df.getEquipId(), df.getVersionNumber()));
						
						boolean addToInfo = true;
						for(DataframeInfo dfi : this.CURRENT_REPORT.getDataframesInfo()) {
							if(dfi.getId().equals(df.getId())) {
								addToInfo = false;
								break;
							}
						}
						
						if(addToInfo) {
							this.CURRENT_REPORT.getDataframesInfo().add(new DataframeInfo(df.getId(), df.getEquipId(), df.getVersionNumber()));
						}
					}
				}
			}
		}
	}
	
	private void addToDataframesInfo(Dataframe df) {
		if(df != null) {
			boolean add = true;
			for(DataframeInfo dfi : this.CURRENT_REPORT.getDataframesInfo()) {
				if(dfi.getId().equals(df.getId())) {
					add = false;
					break;
				}
			}
			
			if(add) {
				if(this.authDAO != null) {
					boolean canView = false;
					try {
						canView = this.authDAO.canViewDataframe(df, this.username);
					}
					catch(Exception e) {
						LOGGER.error("Error when requesting view access for dataframe " + df.getId() + ".", e);
					}
					
					if(canView) {
						this.CURRENT_REPORT.getDataframesInfo().add(new DataframeInfo(df.getId(), df.getEquipId(), df.getVersionNumber()));
					}
				}
			}
		}
	}

	/**
	 * Returns a {@link Program} object whose code matches the one provided.
	 * Populates the {@link Protocol} field with the protocol whose study ID matches
	 * the one provided.
	 * 
	 * @param programCode
	 * @param studyId
	 * @return {@link Program}
	 */
	private Program getProgram(String programCode, String studyId) {
		Program program = null;
		try {
			OpmetaServiceClient opClient = new OpmetaServiceClient();
			opClient.setHost(Props.getOpmetaServiceServer());
			opClient.setPort(Props.getOpmetaSerivcePort());
			opClient.setUser(this.username);

			com.pfizer.pgrd.equip.services.opmeta.client.Program opProgram = opClient.getProgram(programCode);
			if (opProgram != null) {
				program = new Program();
				program.setCompoundNumber(opProgram.getCompound());
				program.setProgram(programCode);
				program.setSetupBy(opProgram.getSetupBy());
				program.setSetupDate(opProgram.getSetupDate());
				program.setTradeName(opProgram.getTradeName());

				com.pfizer.pgrd.equip.services.opmeta.client.Protocol p = opProgram.getProtocol(studyId);
				if (p != null) {
					Protocol protocol = new Protocol();
					protocol.setProtocol(studyId);
					protocol.setSetupBy(p.getSetupBy());
					protocol.setSetupDate(p.getSetupDate());
					protocol.setTitle(p.getTitle());
					
					List<ProtocolAlias> aliases = opClient.getAliasListWithoutProgram(studyId);
					if(aliases != null) {
						for(ProtocolAlias pa : aliases) {
							protocol.getAliases().add(new Protocol.Alias(pa.getAliasType(), pa.getStudyAlias()));
						}
					}
					
					program.setProtocol(protocol);

					List<AssignedUser> assignedUsers = opClient.getAssignedUsers(programCode, studyId);
					for (AssignedUser user : assignedUsers) {
						ProtocolContact pc = new ProtocolContact(user.getFirstName(), user.getLastName(),
								user.getRole(), null);
						if (user.getEmailAddress() != null) {
							pc.setEmailAddress(user.getEmailAddress().toLowerCase());
						}

						protocol.getContacts().add(pc);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Call to OpMeta service to retrieve program '" + programCode + "' failed.", e);
			e.printStackTrace();
		}

		return program;
	}
	
	private List<ReportingEventItemSummary> getEventItemSummaries(Assembly reportingEvent) {
		List<ReportingEventItemSummary> summaries = new ArrayList<>();
		if (reportingEvent != null) {
			ReportingAndPublishingDAO rpDao = new ReportingAndPublishingDAO();
			List<ReportingEventItem> items = rpDao.getReportingItem(reportingEvent.getReportingItemIds());

			List<String> handledAssemblyIds = new ArrayList<>();
			List<String> handledDataframeIds = new ArrayList<>();
			
			List<ReportingEventItem> members = new ArrayList<>();
			for (ReportingEventItem item : items) {
				if (!item.isDeleteFlag() && item.getAssemblyId() != null) {
					String aId = item.getAssemblyId();
					if (aId != null && !aId.trim().isEmpty() && !handledAssemblyIds.contains(aId)) {
						handledAssemblyIds.add(aId);
						Assembly a = ADAO.getAssembly(aId);
						if (a != null && !a.isDeleteFlag()) {
							
							LineageItem li = this.CURRENT_LINEAGE_MAPPING.get(a.getEquipId());
							if (li != null) {
								if (a.getAssemblyType().equalsIgnoreCase(Assembly.ANALYSIS_TYPE)) {
									this.addToDataLoad(li);

									ReportingEventItemSummary summary = new ReportingEventItemSummary();
									summary.setLineage(li.getFullBreadcrumb());
									summary.setLeagacyLineage(li.getFullLegacyBreadcrumb());

									if (a instanceof Analysis) {
										Analysis an = (Analysis) a;
										Dataframe concentrationData = DDAO.getDataframe(an.getDataframeIds().get(0));
										if (concentrationData != null) {
											this.addToDataframes(concentrationData);
											handledDataframeIds.add(concentrationData.getId());
											ReportingEventItem rei = this.getReportingEventItem(items,
													concentrationData.getId());
											Item i = this.addItem(summary, concentrationData, rei);
											if (i != null) {
												i.setEventItemName("Concentration Data");
												this.addToDTs(concentrationData, rei);
											}
										}

										ReportingEventItem emptyRei = new ReportingEventItem();
										emptyRei.setIncluded(false);
										PublishItem empty = new PublishItem();
										empty.setPublishStatus("not published");
										emptyRei.setPublishItem(empty);

										Dataframe mct = DDAO.getDataframe(an.getModelConfigurationDataframeId());
										if (mct != null) {
											//this.addToDataframes(mct);
											this.addToDataframesInfo(mct);
											handledDataframeIds.add(mct.getId());
											ReportingEventItem rei = this.getReportingEventItem(items, mct.getId());
											if(rei == null) {
												rei = emptyRei;
											}
											
											Item i = this.addItem(summary, mct, rei);
											if (i != null) {
												i.setEventItemName("MCT");
												this.addToDTs(mct, rei);
											}
										}

										Dataframe kel = DDAO.getDataframe(an.getKelFlagsDataframeId());
										if (kel != null) {
											if(this.isRaw) {
												this.addToDataframes(kel);
											}
											else {
												this.addToDataframesInfo(kel);
											}
											
											handledDataframeIds.add(kel.getId());
											ReportingEventItem rei = this.getReportingEventItem(items, kel.getId());
											if(rei == null) {
												rei = emptyRei;
											}
											
											Item i = this.addItem(summary, kel, rei);
											if (i != null) {
												i.setEventItemName("KEL Flags");
												this.addToDTs(kel, rei);
											}
										}
										
										Dataframe ppm = DDAO.getDataframe(an.getParametersDataframeId());
										if (ppm != null) {
											//this.addToDataframes(ppm);
											this.addToDataframesInfo(ppm);
											handledDataframeIds.add(ppm.getId());
											ReportingEventItem rei = this.getReportingEventItem(items, ppm.getId());
											Item i = this.addItem(summary, ppm, rei);
											if (i != null) {
												i.setEventItemName("Primary Analysis");
												this.addToDTs(ppm, rei);
												if(i.isPublished()) {
													this.addToDataframes(ppm);
												}
											}
											
											com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis pa = this.CURRENT_REPORT
													.getPrimaryAnalysis(ppm.getEquipId());
											if (pa == null && rei != null) {
												pa = this.createAnalysis(ppm, rei.getPublishItem(), a);
												this.CURRENT_REPORT.getPrimaryAnalyses().add(pa);
												
												AnalysisData analysisData = new AnalysisData();
												analysisData.setAnalysisEquipId(an.getEquipId());
												analysisData.setAnalysisVersion(an.getVersionNumber());
												analysisData.setParametersEquipId(ppm.getEquipId());
												analysisData.setParametersVersion(ppm.getVersionNumber());
												analysisData.setParametersId(ppm.getId());
												if(concentrationData != null) {
													analysisData.setConcentrationDataId(concentrationData.getId());
													analysisData.setConcentrationVersion(concentrationData.getVersionNumber());
												}
												if(kel != null) {
													analysisData.setKelFlagsId(kel.getId());
													analysisData.setKelFlagsVersion(kel.getVersionNumber());
												}
												
												// get the first promoted data transformations
												List<AssemblyLineageItem> lineage = LineageSearcher.getContainingPathsByEquipId(this.CURRENT_LINEAGE, an.getEquipId());
												List<DataframeLineageItem> pdts = LineageSearcher.getFirstPromotedDataTransformations(lineage);
												for(DataframeLineageItem dli : pdts) {
													analysisData.getFirstPromotedDataframeIds().add(dli.getId());
													this.addToDataframes(DDAO.getDataframe(dli.getId()));
												}
												
												this.CURRENT_REPORT.getAnalysisData().add(analysisData);
											}
										}
									}

									summaries.add(summary);
								}
							}
						}
					}
				}
			}
			
			//items.addAll(members);
			for (ReportingEventItem item : items) {
				if (!item.isDeleteFlag() && item.getDataFrameId() != null
						&& !handledDataframeIds.contains(item.getDataFrameId())) {
					handledDataframeIds.add(item.getDataFrameId());
					Dataframe df = DDAO.getDataframe(item.getDataFrameId());
					
					if(df.getEquipId().equals("C-DT15685")) {
						String s = "";
					}
					
					if (df != null && this.isCorrectType(df.getDataframeType()) && !df.isDeleteFlag()) {
						LineageItem li = this.CURRENT_LINEAGE_MAPPING.get(df.getEquipId());
						if (li != null) {
							//this.addToDataframes(df);
							this.addToDataframesInfo(df);
							ReportingEventItemSummary summary = new ReportingEventItemSummary();
							String lineage = li.getFullBreadcrumb();
							String legacy = li.getFullLegacyBreadcrumb();
							
							boolean isLPKP = df.getEquipId().toUpperCase().startsWith("L-PKP");
							if (df.getDataframeType().equalsIgnoreCase(Dataframe.DERIVED_PARAMETERS_TYPE) || isLPKP) {
								String sep = Props.getLineageBreadcrumbSeparator();
								if (lineage != null) {
									lineage = lineage.substring(0, lineage.lastIndexOf(sep));
								}
								if (legacy != null) {
									legacy = legacy.substring(0, legacy.lastIndexOf(sep));
								}
								
								com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis sa = null;
								List<com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis> list = new ArrayList<>();
								if(isLPKP) {
									sa = this.CURRENT_REPORT.getLegacyAnalysis(df.getEquipId());
									list = this.CURRENT_REPORT.getLegacyAnalyses();
								}
								else {
									sa = this.CURRENT_REPORT.getSecondaryAnalysis(df.getEquipId());
									list = this.CURRENT_REPORT.getSecondaryAnalyses();
								}
								
								if (sa == null) {
									sa = this.createAnalysis(df, item.getPublishItem(), null);
									list.add(sa);
								}
							}
							
							summary.setLeagacyLineage(legacy);
							summary.setLineage(lineage);
							Item i = this.addItem(summary, df, item);
							if(i.isPublished()) {
								String equipId = df.getEquipId().trim().toUpperCase();
								String dfType = df.getDataframeType();
								
								boolean isRawScript = false;
								if(this.isRaw && !equipId.startsWith("L-")) {
									Script script = df.getScript();
									if(script != null) {
										LibraryReference lr = script.getScriptBody();
										if(lr != null) {
											String id = lr.getLibraryRef();
											
											try {
												LibraryServiceClient lsc = new LibraryServiceClient();
												lsc.setHost(Props.getLibraryServiceServer());
												lsc.setPort(Props.getLibraryServicePort());
												lsc.setSystemId("nca");
												lsc.setUser(Props.getServiceAccountName());
												
												LibraryResponse resp = lsc.getScriptById(id);
												if(resp != null && resp.getProperties() != null && resp.getProperties().getEquipName() != null) {
													String scriptName = resp.getProperties().getEquipName();
													if(scriptName.equalsIgnoreCase("blq-adjustments.r") || scriptName.equalsIgnoreCase("data-blinding.r")) {
														isRawScript = true;
													}
												}
											}
											catch(Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
								
								if(isRawScript || (equipId.startsWith("L-PAD") && this.isRaw) || equipId.startsWith("L-PARAM") || (equipId.startsWith("L-PKP") && !this.isRaw) || (this.isRaw && equipId.startsWith("L-PKC")) || dfType.equalsIgnoreCase(Dataframe.PRIMARY_PARAMETERS_TYPE) || dfType.equalsIgnoreCase(Dataframe.DERIVED_PARAMETERS_TYPE)) {
									this.addToDataframes(df);
								}
							}
							
							summaries.add(summary);
							this.addToDataLoad(li);
							
							this.addToDTs(df, item);
						}
					}
				}
			}

		}

		return summaries;
	}

	private boolean linkExists(List<DataTransformationLink> links, Dataframe df) {
		for (DataTransformationLink link : links) {
			if (link.dataTransformation.getEquipId().equalsIgnoreCase(df.getEquipId())) {
				return true;
			}
		}

		return false;
	}

	private com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis createAnalysis(Dataframe df, PublishItem publishItem, Assembly parentAnalysis) {
		com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis an = null;
		if (df != null) {
			an = new com.pfizer.pgrd.equip.dataframe.dto.atr.Analysis();
			an.setComments(Comment.fromComment(df.getComments()));
			an.setCreatedBy(df.getCreatedBy());
			an.setCreatedDate(df.getCreated());
			an.setCurrentStatus(df.getDataStatus());
			an.setEquipId(df.getEquipId());
			an.setVersion(df.getVersionNumber());
			an.setId(df.getId());
			an.setMethod("Created by script");
			
			LineageItem li = null;
			boolean trim = false;
			if (parentAnalysis != null) {
				li = this.CURRENT_LINEAGE_MAPPING.get(parentAnalysis.getEquipId());
			} else {
				li = this.CURRENT_LINEAGE_MAPPING.get(df.getEquipId());
				trim = true;
			}

			if (li != null) {
				String lineage = li.getFullBreadcrumb();
				String legacyLineage = li.getFullLegacyBreadcrumb();

				if (trim) {
					String sep = Props.getLineageBreadcrumbSeparator();
					if (lineage != null) {
						lineage = lineage.substring(0, lineage.lastIndexOf(sep));
					}
					if (legacyLineage != null) {
						legacyLineage = legacyLineage.substring(0, legacyLineage.lastIndexOf(sep));
					}
				}

				an.setLineage(lineage);
				an.setLegacyLineage(legacyLineage);
				an.setUserHasAccess(li.userHasAccess());
			}

			if (publishItem != null) {
				String filter = publishItem.getPublishedViewFilterCriteria();
				if (filter != null) {
					PublishedParameter pp = new PublishedParameter();
					PublishItemPublishStatusChangeWorkflow latest = publishItem.getMostRecentWorkflowItem("published");
					if (latest != null) {
						pp.setPublishedBy(latest.getCreatedBy());
						pp.setPublishedDate(publishItem.getPublishedDate());
					}

					Gson gson = new Gson();
					ViewFilters vf = gson.fromJson(filter, ViewFilters.class);
					if (vf.parameterFilter != null) {
						for (ParameterFilter pf : vf.parameterFilter) {
							if (pf.include) {
								String mappedName = pf.mappedName;
								if(mappedName != null) {
									mappedName = mappedName.trim();
									if(mappedName.isEmpty()) {
										mappedName = null;
									}
								}
								
								String manualMappedName = pf.manualMappedName;
								if(manualMappedName != null) {
									manualMappedName = manualMappedName.trim();
									if(manualMappedName.isEmpty()) {
										manualMappedName = null;
									}
								}
								
								Parameter p = new Parameter();
								p.setDecimalPlaces(pf.decimals);
								p.setName(pf.name);
								p.setPublishedName(mappedName);
								p.setManualName(manualMappedName);
								p.setSignificantFigures(pf.sigDigits);

								if (pf.units != null && !pf.units.trim().isEmpty()) {
									p.setUnits(pf.units);
								}
								
								pp.getParameters().add(p);
							}
						}
					}

					an.setPublishedParameters(pp);
				}
			}
		}

		return an;
	}

	private DataTransformation createDataTransformation(Dataframe df, ReportingEventItem rei) {
		DataTransformation dt = null;
		if (df != null) {
			if (df.getPromotionStatus().equalsIgnoreCase("promoted")) {
				PromotedDataTransformation pdt = new PromotedDataTransformation();
				if(!df.getPromotions().isEmpty()) {
					df.getPromotions().sort(new Comparator<Promotion>() {
	
						@Override
						public int compare(Promotion a, Promotion b) {
							Date ad = a.getCreated();
							Date bd = b.getCreated();
	
							if (ad.getTime() > bd.getTime()) {
								return -1;
							} else if (bd.getTime() > ad.getTime()) {
								return 1;
							}
							
							return 0;
						}
	
					});
	
					Promotion latest = df.getPromotions().get(0);
					if (!latest.getComments().isEmpty()) {
						pdt.setPromotionComment(Comment.fromComment(latest.getComments().get(0)));
					}
				}

				pdt.setCurrentStatus("Promoted");
				pdt.setLocked(df.isLocked());
				pdt.setDataBlindingStatus(df.getDataBlindingStatus());
				pdt.setDataStatus(df.getDataStatus());
				pdt.setDataReviewStatus(df.getQcStatus());

				if (rei != null) {
					PublishDetails pd = this.getPublishDetails(rei);
					if (pd != null) {
						pdt.setPublishedBy(pd.publishedBy);
						pdt.setPublishedDate(pd.publishedDate);
					}
				}

				dt = pdt;
			} else {
				dt = new DataTransformation();
			}
			
			DataframeLineageItem dli = (DataframeLineageItem) this.CURRENT_LINEAGE_MAPPING.get(df.getEquipId());
			if(dli != null) {
				dt.setUserHasAccess(dli.userHasAccess());
			}
			
			dt.setCreatedBy(df.getCreatedBy());
			dt.setCreatedDate(df.getCreated());
			dt.setEquipId(df.getEquipId());
			dt.setVersion(df.getVersionNumber());
			dt.setId(df.getId());
			dt.setMethod("Created by Script");

			for (ReportingEventItemSummary eis : this.CURRENT_REPORT.getReportingEvent().getEventItemSummaries()) {
				String breadcrumb = eis.getLineage();
				if (breadcrumb != null && breadcrumb.contains(dt.getEquipId())) {
					dt.getLineages().add(breadcrumb);

					if (eis.getLeagacyLineage() != null) {
						dt.getLegacyLineages().add(eis.getLeagacyLineage());
					}
				}
			}

			/*
			 * List<AssemblyLineageItem> paths =
			 * LineageSearcher.getContainingPathsByEquipId(this.CURRENT_LINEAGE,
			 * df.getEquipId()); for (AssemblyLineageItem path : paths) { List<LineageItem>
			 * leaves = LineageSearcher.getLeaves(path); for (LineageItem leaf : leaves) {
			 * if (leaf.getFullBreadcrumb() != null) {
			 * dt.getLineages().add(leaf.getFullBreadcrumb()); } if
			 * (leaf.getFullLegacyBreadcrumb() != null &&
			 * !leaf.getFullLegacyBreadcrumb().trim().isEmpty()) {
			 * dt.getLegacyLineages().add(leaf.getFullLegacyBreadcrumb()); } } }
			 */
			
			if (dt.userHasAccess() || !(dt instanceof PromotedDataTransformation)) {
				dt.setComments(Comment.fromComment(df.getComments()));
				if(df.getScript() != null) {
					Script s = df.getScript();
					LibraryReference ref = s.getScriptBody();
					if (ref != null) {
						try {
							LibraryServiceClient lc = new LibraryServiceClient();
							lc.setHost(Props.getLibraryServiceServer());
							lc.setPort(Props.getLibraryServicePort());
							lc.setUser(this.username);
	
							LibraryResponse lr = lc.getScriptById(ref.getLibraryRef());
							if (lr != null && lr.getProperties() != null) {
								LibraryResponseProperties props = lr.getProperties();
								dt.setScriptName(props.getEquipName());
	
								byte[] content = lc.getItemContent(ref.getLibraryRef());
								if (content != null) {
									String body = new String(content);
									dt.setScriptBody(body);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							LOGGER.error("Error when accessing script with ID " + ref.getId()
									+ " via the Library Service for user " + this.username, e);
						}
					}
				}
			}
		}

		return dt;
	}

	private void addToDataLoad(LineageItem item) {
		if (item != null && item.getBreadcrumb() != null) {
			String breadcrumb = item.getBreadcrumb();
			for (AssemblyLineageItem adl : this.ALL_DATA_LOADS) {
				String dlEquipId = adl.getEquipId();
				if (breadcrumb.contains(dlEquipId)) {
					LineageItem dlLi = this.CURRENT_LINEAGE_MAPPING.get(dlEquipId);
					if (dlLi != null) {
						DataLoad dl = this.CURRENT_REPORT.getDataLoad(dlEquipId);
						if (dl == null) {
							dl = new DataLoad();
							dl.setEquipId(adl.getEquipId());
							dl.setVersion(adl.getEquipVersion());
							dl.setId(adl.getId());
							if (dlLi.getLegacyBreadcrumb() != null) {
								dl.setSource(adl.getMetadatum("Study Source"));
							}
							
							//List<Dataframe> members = DDAO.getDataframe(adl.getDataframeIds());
							List<DataframeLineageItem> members = adl.getMemberDataframes();
							for (DataframeLineageItem df : members) {
								if (df.getDataframeType().equalsIgnoreCase(Dataframe.DATASET_TYPE)) {
									File f = new File();
									f.setUserHasAccess(df.userHasAccess());
									
									if(df.userHasAccess()) {
										f.setLocation(df.getMetadatum("Location Source Path"));
										if (f.getLocation().equalsIgnoreCase("null")) {
											f.setLocation(null);
										}
	
										f.setServer(df.getMetadatum("Location Server"));
										f.setSourceFile(df.getMetadatum("File Name Full"));
										f.setName(df.getMetadatum("Data Type"));
	
										String totalRows = df.getMetadatum("Data Load Row Count");
										if (totalRows != null) {
											f.setTotalRecords(Integer.parseInt(totalRows));
										}
										
										f.setComments(Comment.fromComment(df.getComments()));
									}
									else {
										// TEMP
										// This is only in place until a decision is made on how to handle access.
										Dataframe dataframe = DDAO.getDataframe(df.getId());
										if(dataframe != null) {
											f.setLocation(dataframe.getMetadatumValue("Location Source Path"));
											if (f.getLocation().equalsIgnoreCase("null")) {
												f.setLocation(null);
											}
		
											f.setServer(dataframe.getMetadatumValue("Location Server"));
											f.setSourceFile(dataframe.getMetadatumValue("File Name Full"));
											f.setName(dataframe.getMetadatumValue("Data Type"));
		
											String totalRows = dataframe.getMetadatumValue("Data Load Row Count");
											if (totalRows != null) {
												f.setTotalRecords(Integer.parseInt(totalRows));
											}
											
											f.setComments(Comment.fromComment(dataframe.getComments()));
										}
									}
									
									f.setEquipId(df.getEquipId());
									f.setId(df.getId());
									f.setLoadDate(df.getCreatedDate());
									f.setLoadedBy(df.getCreatedBy());

									dl.getFiles().add(f);
								}
							}

							this.CURRENT_REPORT.getDataLoads().add(dl);
						}

						if (dl != null) {
							if (item.getFullLegacyBreadcrumb() != null) {
								dl.getLegacyLineages().add(item.getFullLegacyBreadcrumb());
							}

							dl.getLineages().add(item.getFullBreadcrumb());
						}
					}
				}
			}
		}
	}

	private ReportingEventItem getReportingEventItem(List<ReportingEventItem> items, String objectId) {
		for (ReportingEventItem item : items) {
			if (!item.isDeleteFlag() && ((item.getDataFrameId() != null && item.getDataFrameId().equals(objectId))
					|| item.getAssemblyId() != null && item.getAssemblyId().equals(objectId))) {
				return item;
			}
		}
		
		return null;
	}

	private boolean isCorrectType(String type) {
		String[] allowedTypes = { Dataframe.DATA_TRANSFORMATION_TYPE, Dataframe.DERIVED_PARAMETERS_TYPE,
				Dataframe.REPORT_TYPE, Dataframe.REPORT_ITEM_TYPE };
		for (String allowedType : allowedTypes) {
			if (type.equalsIgnoreCase(allowedType)) {
				return true;
			}
		}

		return false;
	}

	private Item addItem(ReportingEventItemSummary summary, EquipObject eo, ReportingEventItem rei) {
		Item item = null;
		if (summary != null && eo != null && rei != null && !rei.isDeleteFlag()) {
			PublishItem pi = rei.getPublishItem();
			if (pi != null) {
				item = summary.new Item();
				
				PublishDetails pd = this.getPublishDetails(rei);
				item.setPublished(pd.isPublished);
				item.setSelected(pd.isSelected);
				if (pd.isPublished) {
					item.setPublishedBy(pd.publishedBy);
					item.setPublishedDate(pd.publishedDate);
				}
				
				item.setEquipId(((EquipID) eo).getEquipId());
				item.setId(eo.getId());
				String type = null;
				if (eo instanceof Dataframe) {
					Dataframe df = (Dataframe) eo;
					type = df.getItemType();
				} else if (eo instanceof Assembly) {
					Assembly a = (Assembly) eo;
					type = a.getItemType();
				}

				item.setEventItemName(type);

				summary.getItems().add(item);
			}
		}

		return item;
	}

	private PublishDetails getPublishDetails(ReportingEventItem rei) {
		PublishDetails pd = null;
		if (rei != null) {
			PublishItem pi = rei.getPublishItem();
			if (pi != null) {
				pd = new PublishDetails();
				pd.isPublished = pi.getPublishStatus().equalsIgnoreCase("published");
				pd.isSelected = rei.isIncluded();

				if (pd.isPublished) {
					PublishItemPublishStatusChangeWorkflow latest = pi.getMostRecentWorkflowItem("published");
					if (latest != null) {
						pd.publishedBy = latest.getCreatedBy();
						pd.publishedDate = pi.getPublishedDate();
					}
				}
			}
		}

		return pd;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isRaw() {
		return isRaw;
	}

	public void setRaw(boolean isRaw) {
		this.isRaw = isRaw;
	}
}

class DataTransformationLink {
	public Dataframe dataTransformation;
	public ReportingEventItem item;

	public DataTransformationLink() {
		this(null, null);
	}

	public DataTransformationLink(Dataframe dt, ReportingEventItem item) {
		this.dataTransformation = dt;
		this.item = item;
	}
}

class PublishDetails {
	public boolean isSelected;
	public boolean isPublished;
	public String publishedBy;
	public Date publishedDate;
}

class ViewFilters {
	public List<ParameterFilter> parameterFilter = new ArrayList<>();
}

class ParameterFilter {
	public boolean include;
	public String name;
	public String mappedName;
	public String manualMappedName;
	public int sigDigits;
	public int decimals;
	public String units;
}