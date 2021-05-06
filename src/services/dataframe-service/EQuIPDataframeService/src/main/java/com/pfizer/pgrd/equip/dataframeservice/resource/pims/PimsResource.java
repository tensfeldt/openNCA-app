package com.pfizer.pgrd.equip.dataframeservice.resource.pims;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsDose;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsPharmacokinetics;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsPkLabInfo;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsStudyLocations;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsTreatment;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.MetadataDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.NotificationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.VersioningDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PimsDataLoadInput;
import com.pfizer.pgrd.equip.dataframeservice.dto.PimsPkTermDosepMapping;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.pims.PimsDataTransComputeStdout;
import com.pfizer.pgrd.equip.dataframeservice.pims.PimsWrapper;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.computeservice.dto.Parameter;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;
import com.pfizer.pgrd.equip.services.notification.client.NotificationRequestBody;
import com.pfizer.pgrd.equip.services.notification.client.event_detail;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import com.pfizer.pgrd.equip.services.audit.AuditServiceClient.AuditDetails;

public class PimsResource extends ServiceBaseResource {
	private static Logger log = LoggerFactory.getLogger(PimsResource.class);

	private static final String LOCATION_FULL_PATH = "Location Full Path";
	private static final String LOCATION_SERVER = "Location Server";
	private static final String LOCATION_SOURCE_PATH = "Location Source Path";

	protected static final String LOCATION_NEW_HAVEN = "new-haven";
	protected static final String LOCATION_BRUSSELS = "brussels";

	protected static final String PDS_DATA_STANDARD = "pds";
	protected static final String CDISC_DATA_STANDARD = "cdisc";

	public static final Route studyPIMSStatus = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;

			try {
				String studyId = request.params(":id");

				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				PimsWrapper pw = new PimsWrapper();
				pw.setUserId(userId); // needed to allow call to opmeta service if necesseary
				String val = pw.isPIMSStudy(studyId);

				json = marshalObject(val);
				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

	public static final Route getDosepViaStudyId = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;

			AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
					Props.getExternalServicesPort());

			try {
				String studyId = request.params(":id");

				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				PimsWrapper pw = new PimsWrapper();
				pw.setUserId(userId); // needed to allow call to opmeta service if necesseary
				List<String> listDosep = pw.getDosepFromStudyId(studyId);

				json = marshalObject(listDosep);
				response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return json;
		}
	};

	public static final Route postDataLoad = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = null;

			try {
				String contentType = request.headers(HTTPHeaders.CONTENT_TYPE);
				String programProtocol = request.params(":id");
				String studyId = programProtocol.split(":")[1];

				if (contentType != null && contentType.equalsIgnoreCase(HTTPContentTypes.JSON)) {
					String jsonBody = request.body();

					if (jsonBody != null) {
						List<PimsDataLoadInput> inputs = unmarshalObject(jsonBody, PimsDataLoadInput.class);
						extractJSonDosepMappings(inputs, jsonBody);
						PimsDataLoadInput input = inputs.get(0);
						
						checkCreatedBy(request, input);
						
						PimsWrapper pw = new PimsWrapper();
						pw.setUserId(input.getCreatedBy());
						PimsStudyLocations locationsObj = pw.getPimsStudyLocations(studyId); // this will set the study
																								// alias in the pw
																								// object if one is
																								// being used, otherwise
																								// alias = studyId

						StringBuffer demographyVitalSignsCSV = new StringBuffer();
						int numCSVRows = pw.getPIMSDemographyAndVitalSignsData(pw.getStudyAlias(),
								demographyVitalSignsCSV, locationsObj);

						String demographyDataframeId = createDemographyDataframeAndDataSet(programProtocol,
								demographyVitalSignsCSV.toString(), numCSVRows, locationsObj,
								input.getPkDefDataframeId(), input.getCreatedBy(), input);
						updateAssemblyId(input, demographyDataframeId);

						// calls compute service with virtual call
						StringBuffer pkDataCSV = new StringBuffer();
						List<PimsPkLabInfo> listLabInfo = new ArrayList<PimsPkLabInfo>();
						String crfViewDataframeId = createCRFViewDataframe(programProtocol, pw.getStudyAlias(), pw,
								input, pkDataCSV, listLabInfo);
						updateAssemblyId(input, crfViewDataframeId);

						String studyCompletionDataframeId = createStudyCompletionDataframe(programProtocol,
								pw.getStudyAlias(), pw, input.getPkDefDataframeId(), locationsObj, input.getCreatedBy(),
								input);
						updateAssemblyId(input, studyCompletionDataframeId);

						// Calls compute service with Non-virtual call
						String dfId = createDataTransformationDataframe(demographyDataframeId, crfViewDataframeId,
								studyCompletionDataframeId, input.getPkDefDataframeId(), input.getCreatedBy(),
								input.getAssemblyId(), input.getDosepMapping(), locationsObj, input.getCreatedBy(),
								pkDataCSV, studyId, pw, listLabInfo);

						// call opmeta service to update modification time on associated protocol
						try {
							OpmetaServiceClient osc = new OpmetaServiceClient();
							osc.setHost(Props.getOpmetaServiceServer());
							osc.setPort(Props.getOpmetaSerivcePort());
							log.info("PimsResource: update protocol for study id=" + programProtocol);
							//osc.updateProtocolModifiedDate(input.getCreatedBy(), studyId);
							osc.updateProtocolModifiedDate(input.getCreatedBy(), programProtocol);
						} catch (Exception err) {
							log.warn("PimsResource: Error updating protocol modification time for study "
									+ programProtocol, err);
						}

						returnJson = marshalObject(dfId);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No PIMS dataloads were provided.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST,
							HTTPHeaders.CONTENT_TYPE + " must be " + HTTPContentTypes.JSON);
				}
			} catch (Exception ex) {
				ServiceExceptionHandler.handleException(ex);
			}

			return returnJson;
		}

		private void extractJSonDosepMappings(List<PimsDataLoadInput> inputs, String jsonBody) {
			PimsDataLoadInput input = inputs.get(0);
			String dosepJson = jsonBody.substring(jsonBody.indexOf("["), jsonBody.lastIndexOf("]") + 1)
					.replaceAll("\n", "").trim(); // .replace("\"", "\\\"");
			input.setDosepJson(dosepJson);
		}

		private void updateAssemblyId(PimsDataLoadInput input, String dataframeId) {
			AssemblyDAO aDao = new AssemblyDAOImpl();
			Assembly assembly = aDao.getAssembly(input.getAssemblyId());
			List<String> dataframeIds = assembly.getDataframeIds();

			dataframeIds.add(dataframeId);

			assembly.setDataframeIds(dataframeIds);
			aDao.updateAssembly(assembly.getId(), assembly);
		}

		private void commitDataframeLogic(String dataframeId, DataframeDAO dao) throws Exception {
			Dataframe df = dao.getDataframe(dataframeId);

			if (!df.isCommitted() && !df.getVersionSuperSeded() && !df.isDeleteFlag()) {
				VersioningDAO vdao = new VersioningDAO(df);
				boolean siblingsUpdated = vdao.deleteSiblings(df.getVersionNumber(), df.getId());

				if (siblingsUpdated) {
					df.setCommitted(true);
					df.setLockedByUser(null); // unlock when committing

					dao.updateDataframe(df, df.getId());
				} else {
					throw new Exception("Siblings couldn't be updated for dataframe id = " + df.getId());
				}
			}
		}

		private String createDataTransformationDataframe(String demographyDataframeId, String crfViewDataframeId,
				String studyCompletionDataframeId, String pkDefDataframeId, String createdBy, String assemblyId,
				List<PimsPkTermDosepMapping> dosepMappings, PimsStudyLocations locationsObj, String user,
				StringBuffer pkDataCSV, String studyId, PimsWrapper pw, List<PimsPkLabInfo> listLabInfo)
				throws Exception {
			ComputeServiceClient csc = new ComputeServiceClient();
			csc.setHost(Props.getComputeServiceServer());
			csc.setPort(Props.getComputeServicePort());
			csc.setUser(createdBy);

			ComputeParameters params = new ComputeParameters();
			String scriptId = getScriptIdFromLibraryService(Props.getDataTransformationComputeServiceScriptName(), user);
			
			// lines below for testing locally
			// LibraryServiceClient lsc = new LibraryServiceClient();
			/// lsc.setHost(Props.getLibraryServiceServer());
			// lsc.setPort(Props.getLibraryServicePort());
			// lsc.setUser("hirscm08");
			// LibraryResponse lr = lsc.getScriptByName("/users/hirscm08",
			// "pims-merge-pkdef.R");
			// String scriptId = lr.getArtifactId();
			// end lines for testing

			params.setScriptId(scriptId);
			params.setComputeContainer("equip-r-base");
			params.setEnvironment("Server");
			params.setUser(csc.getUser());
			
			// Added 10-24-2020 to fix prod deployment issue
			params.setDontBatch(true);
			
			List<String> dataframeIds = new ArrayList<String>();

			// THE ORDER OF THESE ITEMS MUST MATCH THE EXPECTED ORDER IN THE R SCRIPT
			dataframeIds.add(pkDefDataframeId);
			dataframeIds.add(demographyDataframeId);
			dataframeIds.add(crfViewDataframeId);
			dataframeIds.add(studyCompletionDataframeId);

			params.setDataframeIds(dataframeIds); // non virtual call which means we pass ids to objects that already
													// exist
			List<String> assemblyIds = new ArrayList<String>();
			assemblyIds.add(assemblyId);
			params.setAssemblyIds(assemblyIds);

			params.setDataframeType(Dataframe.DATA_TRANSFORMATION_TYPE);

			// listLabInfo.addAll(
			// pw.getLabInfoData(studyId,locationsObj.getLocations(),locationsObj.getDataStandard(),
			// PimsWrapper.PIMS_LAB_INFO_LABEL_TYPE_EQUAL_C ) );
			// params.getParameters().add(new Parameter("labinfo",
			// convertLabInfoListToCSV(listLabInfo), "string"));
			
			params.getParameters().add(new Parameter("dataStandard", locationsObj.getDataStandard(), "string"));
			
			// throw "forbidded" if datastandard is not in acceptable list
			checkDataStandard(locationsObj.getDataStandard(), createdBy, Dataframe.DATA_TRANSFORMATION_TYPE);
			
			params.getParameters().add(new Parameter("pkdata", pkDataCSV.toString(), "string"));
			
			Gson gson = new Gson();
			String computeJson = gson.toJson(params);
			log.info("PIMS compute payload: " + computeJson);
			
			ComputeResult result = csc.compute(params);
			
			if (result.getDataframeIds().isEmpty()) {
				//audit this
				DataframeDAO dfDao = new DataframeDAOImpl();
				Dataframe df = dfDao.getDataframe(pkDefDataframeId);
				
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());
				/*asc.logAuditEntry("Unable to load PIMS data. The subjects in the PKDef file are not found in PIMS database(s). If this study is PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.", df.getEquipId(), "PIMS Data Load", createdBy,
						Props.isAudit(), Const.AUDIT_SUCCESS, df.getVersionNumber());*/
				AuditDetails details = asc.new AuditDetails("Unable to load PIMS data. The subjects in the PKDef file are not found in PIMS database(s). If this study is PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.", df, createdBy);
				//details.setContextEntity(a);
				asc.logAuditEntryAsync(details);

					Spark.halt(HTTPStatusCodes.CONFLICT,
						"Unable to load PIMS data. The subjects in the PKDef file are not found in PIMS database(s).  If this study is PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.");
			}

			String dfId = null;
			if (Props.isAuditAndNotifyOnDataload()) {
				checkResultNotNull(assemblyId, result);

				dfId = postProcessComputeResult(dosepMappings, locationsObj, result, createdBy);
			} else {
				List<String> dfIds = result.getDataframeIds();

				DataframeDAO dfDao = new DataframeDAOImpl();
				Dataframe df = dfDao.getDataframe(dfIds.get(0));

				DatasetDAO ddao = new DatasetDAOImpl();
				ComplexData complexData = ddao.getData(df.getDataset().getComplexDataId());
				String csv = new String(complexData.getBytes());
				int numCSVRows = countRows(csv, "\n");

				setMetadatum(df, numCSVRows, locationsObj.getLocations(), "PIMS Data Transformation");

				addUserChosenDosepListToMetadata(df, dosepMappings);
				dfDao.updateDataframe(df, df.getId());
				commitDataframeLogic(df.getId(), dfDao);
			}

			return dfId;
		}

		private String postProcessComputeResult(List<PimsPkTermDosepMapping> dosepMappings,
				PimsStudyLocations locationsObj, ComputeResult result, String createdBy) throws Exception {
			String dfId = getDataframeId(result.getDataframeIds());
			DataframeDAO dfDao = new DataframeDAOImpl();
			Dataframe df = dfDao.getDataframe(dfId);

			PimsDataTransComputeStdout stdoutObj = get4kvpsErrorMessageAndJson(result.getStdout());

			if (stdoutObj.getMap().size() < 4) {
				throw new IllegalStateException("Unable to validate data load for dataframe id = " + df.getId()
						+ " only " + stdoutObj.getMap().size()
						+ " key value pairs were returned from the compute service");
			}

			List<Metadatum> metadata = setMetadatum(df, null, locationsObj.getLocations(), "PIMS Data Transformation");
			metadata.addAll(addToDataframeTheKeyValuePairsReturnedFromCompute(result, stdoutObj, df));
			auditAndNotifyDataTransformComputeResult(result, stdoutObj, df, createdBy, dosepMappings);

			metadata.addAll(addUserChosenDosepListToMetadata(df, dosepMappings));
			
			List<String> handled = new ArrayList<>();
			if(!metadata.isEmpty()) {
				MetadataDAO mdDao = ModeShapeDAO.getMetadataDAO();
				for(Metadatum m : metadata) {
					if(!handled.contains(m.getKey())) {
						mdDao.insertMetadata(m, df.getId());
						handled.add(m.getKey());
					}
				}
			}
			
			dfDao.updateDataframe(df, df.getId());
			commitDataframeLogic(dfId, dfDao);

			return dfId;
		}

//		NotificationDAO ndao = new NotificationDAO();
//		NotificationRequestBody body = new NotificationRequestBody();
//		event_detail detail = new event_detail();
//		body.setEvent_type("data_loading");
//		body.setEntity_id(df.getEquipId() + " v." + df.getVersionNumber());
//		detail.setUser_name(createdBy);
//		List<String> studyIds = df.getStudyIds();
//		body.setEntity_type(df.getDataframeType());
//		detail.setData_status(df.getDataStatus());
//		detail.setBlinding_status(df.getDataBlindingStatus());
//		List<String> notifComments = new ArrayList<String>();
//
//		for (Comment comment : df.getComments()) {
//			notifComments.add(comment.getBody());
//		}
//		detail.setComments(notifComments);
//		body.setEventDetail(detail);
//		boolean notifyFlag = ndao.notifyEvent(body, studyIds);
//		if (!notifyFlag) {
//			Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Dataframe promoted but notification failed");
//		}

		private void auditAndNotifyDataTransformComputeResult(ComputeResult result,
				PimsDataTransComputeStdout stdoutObj, Dataframe df, String createdBy,
				List<PimsPkTermDosepMapping> dosepMappings) throws Exception {
			if (result.getStdout() != null) {
				NotificationDAO ndao = new NotificationDAO();

				pimsDataloadNotification(df, createdBy, dosepMappings, ndao, "data_loading", stdoutObj);

				// audit data load completed
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());
				/*asc.logAuditEntry("Data load completed", df.getEquipId(), df.getDataframeType(), createdBy,
						Props.isAudit(), Const.AUDIT_SUCCESS, df.getVersionNumber());*/
				AuditDetails details = asc.new AuditDetails("Data load completed", df, createdBy);
				//details.setContextEntity(a);
				asc.logAuditEntryAsync(details);

				// success or failure will be indicated by whether or not there is an error
				// message.
				pimsDataloadNotification(df, createdBy, dosepMappings, ndao, "data_validation", stdoutObj);
			} else {
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
						"data load created, no stdout provided by compute service, so match and mismatch information is not available, unable to validate PIMS data load");
			}
		}

		private void pimsDataloadNotification(Dataframe df, String createdBy,
				List<PimsPkTermDosepMapping> dosepMappings, NotificationDAO ndao, String eventType,
				PimsDataTransComputeStdout stdoutObj) {
			NotificationRequestBody body = new NotificationRequestBody();
			event_detail detail = new event_detail();

			detail.setNumber_record_data_load(Long.parseLong(stdoutObj.getNumberRecordsLoaded()));
			detail.setNumber_skipped_records_data_load(Long.parseLong(stdoutObj.getNumberRecordsSkipped()));
			detail.setNumber_subjects_data_load(Long.parseLong(stdoutObj.getNumberSubjectsDataLoad()));
			String columns = "";
			if (stdoutObj.getJson() != null) {

				columns = stdoutObj.getJson().length() > 2000 ? stdoutObj.getJson().substring(0, 2000)
						: stdoutObj.getJson();
			}

			String errorMessage = "No validation error found";

			if (stdoutObj.getErrorMessage() != null) {
				errorMessage = "Found unmatched records: " + columns;

			}

			detail.setValidation_details(errorMessage);

			String pkTerms = "";
			String pkTermsTruncate = "";

			// Pfizer will only have one doseMapping but in an open source version they may
			// have
			// many
			for (int i = 0; i < dosepMappings.size(); i++) {
				pkTermsTruncate += dosepMappings.get(i).getPkTerm();

				if (pkTermsTruncate.length() >= 3999) {
					break;
				}
				pkTerms += dosepMappings.get(i).getPkTerm();
				if (i < dosepMappings.size() - 1) {
					pkTerms += ",";
				}
			}

			detail.setAnalyst_name(pkTerms);
			body.setEvent_type(eventType);
			body.setEntity_id(df.getEquipId() + " v." + df.getVersionNumber());
			detail.setUser_name(createdBy);
			List<String> studyIds = df.getStudyIds();
			body.setEntity_type(df.getDataframeType());
			detail.setData_status(df.getDataStatus());
			detail.setBlinding_status(df.getDataBlindingStatus());
			List<String> notifComments = new ArrayList<String>();

			for (Comment comment : df.getComments()) {
				notifComments.add(comment.getBody());
			}
			detail.setComments(notifComments);
			body.setEventDetail(detail);
			boolean notifyFlag = ndao.notifyEvent(body, studyIds);
			if (!notifyFlag) {
				Spark.halt(HTTPStatusCodes.BAD_REQUEST, "" + eventType + " successful but notification failed");
			}
		}

		private List<Metadatum> addToDataframeTheKeyValuePairsReturnedFromCompute(ComputeResult result,
				PimsDataTransComputeStdout stdoutObj, Dataframe df) throws Exception {
			List<Metadatum> list = new ArrayList<>();
			if (stdoutObj != null) {
				List<Metadatum> metadata = df.getMetadata();
				
				for (Object key : stdoutObj.getMap().keySet()) {
					Metadatum metadatum = new Metadatum((String) key, (String) stdoutObj.getMap().get(key));
					metadata.add(metadatum);
					list.add(metadatum);
				}
			}
			
			return list;
		}

		private String getDataframeId(List<String> dataframeIds) {
			if (dataframeIds.size() == 0) {
				throw new IllegalStateException(
						"Non dataframe ids returned from compute service on PIMS dataframe transformation creation");
			}

			return dataframeIds.get(0);
		}

		private void checkResultNotNull(String assemblyId, ComputeResult result) {
			if (result == null) {
				throw new IllegalStateException(
						"null value returned from Compute Service when creating transformation dataframe.  Assembly id = "
								+ assemblyId);
			}
			if (result.getDataframeIds() == null) {
				throw new IllegalStateException("Tranformation dataframe not created.  Assembly id = " + assemblyId);
			}
		}

		private void checkDataStandard(String dataStandard, String createdBy, String transformationType) throws ServiceCallerException {
			String allowedDataStandards = Props.getAllowedDataStandards();
			Long version = new Long(0);
			
			if (!allowedDataStandards.toLowerCase().contains(dataStandard.toLowerCase())) {
		
				AuditServiceClient asc = new AuditServiceClient(Props.getExternalServicesHost(),
						Props.getExternalServicesPort());

				asc.logAuditEntry("Unable to load PIMS data. This study is not supported. Only studies following "
								+ allowedDataStandards
								+ " Data Standard(s) are supported. To load PIMS data for a non-supported PIMS study or if this study is a PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.", dataStandard, transformationType, createdBy,
						Props.isAudit(), Const.AUDIT_SUCCESS, version);
				/*AuditDetails details = asc.new AuditDetails("Unable to load PIMS data. This study is not supported. Only studies following "
						+ allowedDataStandards
						+ " Data Standard(s) are supported. To load PIMS data for a non-supported PIMS study or if this study is a PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.", dataStandard, createdBy);
				//details.setContextEntity(a);
				asc.logAuditEntryAsync(details);*/

				Spark.halt(HTTPStatusCodes.CONFLICT,
						"Unable to load PIMS data. This study is not supported. Only studies following "
								+ allowedDataStandards
								+ " Data Standard(s) are supported. To load PIMS data for a non-supported PIMS study or if this study is a PIMS Hybrid and you are loading non-PIMS subjects, please reach out to the Programming team to generate ARD or LCD.");
			}
		}
		
		public PimsDataTransComputeStdout get4kvpsErrorMessageAndJson(String stdout) {
			PimsDataTransComputeStdout stdoutObj = new PimsDataTransComputeStdout();
			
			String beginString = "BEGIN_DATA_TRANSFORMATION_OUTPUT:\n";
			String endString = "\nEND_DATA_TRANSFORMATION_OUTPUT";
			stdout = stdout.substring(stdout.indexOf(beginString) + beginString.length(), stdout.indexOf(endString));

			String[] lines = stdout.split("\n");

			if (lines.length < 4) {
				throw new IllegalStateException("Only " + lines.length
						+ " lines returned from compute service, should have been at least four");
			}

			stdoutObj.put(lines[0].split(":")[0].trim(), lines[0].split(":")[1].trim());
			stdoutObj.put(lines[1].split(":")[0].trim(), lines[1].split(":")[1].trim());
			stdoutObj.put(lines[2].split(":")[0].trim(), lines[2].split(":")[1].trim());
			stdoutObj.put(lines[3].split(":")[0].trim(), lines[3].split(":")[1].trim());

			String errorMessage = null;

			if (lines.length > 4) {
				errorMessage = lines[4].replaceFirst("ERROR_MESSAGE:", "").trim();
			}

			stdoutObj.setErrorMessage(errorMessage);

			String json = null;

			if (lines.length >= 5) {
				json = lines[5];
			}

			stdoutObj.setJson(json);

			return stdoutObj;
		}

		private int countRows(String csv, String string) {
			String[] lines = csv.split(string);
			if (lines == null || lines.length == 0) {
				return 0;
			}
			return lines.length - 1;
		}

		private String createStudyCompletionDataframe(String programProtocol, String studyId, PimsWrapper pw,
				String pkDefDataframeId, PimsStudyLocations locationsObj, String createdBy, PimsDataLoadInput input)
				throws Exception {
			StringBuffer csv = new StringBuffer();
			int numCSVRows = pw.getStudyCompletionData(studyId, csv);

			DataframeDAO dDao = getDataframeDAO();

			Dataframe df = createDefaultDataframe(programProtocol, numCSVRows, locationsObj.getLocations(),
					pkDefDataframeId, dDao, createdBy, "PIMS Completion", input);
			df.setItemType("PIMS Study Completion");
			addPIMSDatabaseInfoAndViewsForStudyCompletion(df, locationsObj);
			Dataframe dfNode = dDao.insertDataframe(df);
			createDefaultDataset(csv.toString(), dfNode.getId());
			commitDataframeLogic(dfNode.getId(), dDao);

			return dfNode.getId();
		}

		private void addPIMSDatabaseInfoAndViewsForStudyCompletion(Dataframe df, PimsStudyLocations locations) {
			List<String> listLocations = new ArrayList<String>();
			List<String> listDatabaseNames = new ArrayList<String>();
			List<String> listViews = new ArrayList<String>();

			getPIMSDatabaseNamesAndLocationsList(locations, listLocations, listDatabaseNames);

			if (locations.getDataStandard().equals(PDS_DATA_STANDARD)) {
				listViews.add("PI_GRADES_DSP_P");
			} else if (locations.getDataStandard().equals(CDISC_DATA_STANDARD)) {
				listViews.add("PI_CDISC_DSP_P");
			}

			storeLocationInfoAsThreePiecesOfMetadata(df, listLocations, listDatabaseNames, listViews);
		}

		private String createCRFViewDataframe(String programProtocol, String studyId, PimsWrapper pw,
				PimsDataLoadInput input, StringBuffer pkDataCSVReturnValue, List<PimsPkLabInfo> listLabInfo)
				throws Exception {
			// example compute service URI for a virtual call
			// e. Call /compute?virtual to generate a merged CSV file
			// POST
			// http://amrvlp000005317.pfizer.com:8080/EQUIPComputeService/compute?virtual=true
			PimsStudyLocations locationsObj = pw.getPimsStudyLocations(studyId);

			List<PimsDose> listDosep = pw.getDosesFromStudyId(studyId);
			List<PimsTreatment> treatments = pw.getTreatmentData(studyId, locationsObj.getLocations(),
					locationsObj.getDataStandard());
			List<PimsPharmacokinetics> listPKData = pw.getPKData(studyId, locationsObj.getLocations(),
					locationsObj.getDataStandard());
			listLabInfo.addAll(pw.getLabInfoData(studyId, locationsObj.getLocations(), locationsObj.getDataStandard(),
					PimsWrapper.PIMS_LAB_INFO_LABEL_TYPE_EQUAL_S));

			ComputeServiceClient csc = new ComputeServiceClient();
			csc.setHost(Props.getComputeServiceServer());
			csc.setPort(Props.getComputeServicePort());
			csc.setUser(input.getCreatedBy());

			ComputeParameters params = new ComputeParameters();

			params.setUser(csc.getUser());
			params.setComputeContainer("equip-r-base");
			params.setEnvironment("Server");

			// String scriptId = Props.getCRFComputeServiceScriptValue();
			String scriptId = getScriptIdFromLibraryService(Props.getCRFComputeServiceScriptName(),
					input.getCreatedBy());
			params.setScriptId(scriptId);

			// params.setDataframeIds(); //for a virtual call to the compute service we
			// don't specify datafradosepme ids, we send the actual data across the wire.
			String dosepCSV = convertDosepListToCSV(listDosep);
			String treatmentsCSV = convertTreatmentListToCSV(treatments);
			String pkDataCSV = convertPkDataListToCSV(listPKData);
			pkDataCSVReturnValue.append(pkDataCSV);
			String labInfoCSV = convertLabInfoListToCSV(listLabInfo);

			params.getParameters().add(new Parameter("dose", dosepCSV, "string"));
			params.getParameters().add(new Parameter("treatment", treatmentsCSV, "string"));
			params.getParameters().add(new Parameter("pkdata", pkDataCSV, "string"));
			params.getParameters().add(new Parameter("labinfo", labInfoCSV, "string"));

			// MM todo - This was moved to another version. Query #2.
			// params.getParameters().add(new
			// Parameter("PIMS_PKBDFL_MAPPING",pkbdflMapping,"string"));

			params.getParameters().add(new Parameter("dosepMapping", input.getDosepJson(), "string"));
			// params.getParameters().add(new
			// Parameter("dosepMapping",convertUserSpecifiedDosepMappingsToCSV(input),"string"));

			params.setDataframeType(Dataframe.DATA_TRANSFORMATION_TYPE);

			ComputeResult result = csc.computeVirtual(params);
			List<String> list = result.getDatasetData();
			if (list.size() == 0) {
				throw new IllegalStateException("No data returned from compute service");
			}
			String computeServiceBase64String = list.get(0);

			if (!Base64.isBase64(computeServiceBase64String)) {
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR,
						"Compute service returned a non base 64 encoding on CRF call.");
			}

			String standardCSV = new String(Base64.decodeBase64(computeServiceBase64String), StandardCharsets.UTF_8);
			int numRows = countRows(standardCSV, "\n");

			DataframeDAO dDao = getDataframeDAO();

			Dataframe df = createDefaultDataframe(programProtocol, numRows, locationsObj.getLocations(),
					input.getPkDefDataframeId(), dDao, input.getCreatedBy(), "PIMS CRF DATASET", input);
			addPIMSDatabaseInfoAndViewsForCRF(df, locationsObj, pw, studyId);
			df.setItemType("CRF View");
			Dataframe dfNode = dDao.insertDataframe(df);
			createDefaultDataset(standardCSV, dfNode.getId());
			commitDataframeLogic(dfNode.getId(), dDao);

			return dfNode.getId();
		}

		private void addPIMSDatabaseInfoAndViewsForCRF(Dataframe df, PimsStudyLocations locations, PimsWrapper pw,
				String studyId) throws Exception {
			List<String> listLocations = new ArrayList<String>();
			List<String> listDatabaseNames = new ArrayList<String>();
			List<String> listViews = new ArrayList<String>();

			getPIMSDatabaseNamesAndLocationsList(locations, listLocations, listDatabaseNames);
			Boolean isLocked = null;

			// a study can only be at one location but we don't know which location it's at
			// so we have to check all of them.
			for (String location : locations.getLocations()) {
				isLocked = pw.getLockFlag(studyId, location);
				if (isLocked != null) {
					break;
				}
			}

			if (locations.getDataStandard().equals(PDS_DATA_STANDARD)) {
				if (isLocked) {
					listViews.add("PI_GRADES_DOS_P");
					listViews.add("PI_GRADES_TRT_P");
				} else {
					listViews.add("PI_GRADES_DOS_UNBLINDED_P");
					listViews.add("PI_GRADES_TRT_R");
				}

				listViews.add("PI_GRADES_PK_P");
			} else if (locations.getDataStandard().equals(CDISC_DATA_STANDARD)) {
				if (isLocked) {
					listViews.add("PI_CDISC_DOS_P");
					listViews.add("PI_CDISC_TRT_P");
				} else {
					listViews.add("PI_CDISC_DOS_UNBLINDED_P");
					listViews.add("PI_CDISC_TRT_R");
				}

				listViews.add("PI_CDISC_PK_P");
			}

			listViews.add("PIMS_#PK_LAB_INFO");

			storeLocationInfoAsThreePiecesOfMetadata(df, listLocations, listDatabaseNames, listViews);
		}

		public String getScriptIdFromLibraryService(String crfComputeServiceScriptName, String user)
				throws ServiceCallerException {
			LibraryServiceClient lsc = new LibraryServiceClient();
			lsc.setHost(Props.getLibraryServiceServer());
			lsc.setPort(Props.getLibraryServicePort());
			lsc.setUser(user);
			LibraryResponse lr = lsc.getGlobalSystemScriptByName(crfComputeServiceScriptName);
			return lr.getArtifactId();
		}

		private String convertUserSpecifiedDosepMappingsToCSV(PimsDataLoadInput input) {
			StringBuffer buf = new StringBuffer();
			int i = 0;

			for (PimsPkTermDosepMapping dosep : input.getDosepMapping()) {
				buf.append(dosep.getPkTerm());
				buf.append(",");
				buf.append(dosep.getDosep());
				if (i < input.getDosepMapping().size()) {
					buf.append("\r\n");
				}
				i++;
			}

			return buf.toString();
		}

		private List<Metadatum> addUserChosenDosepListToMetadata(Dataframe df, List<PimsPkTermDosepMapping> dosepMappings) {
			List<Metadatum> metadata = df.getMetadata();
			int ndx = 1;
			List<Metadatum> list = new ArrayList<>();
			for (PimsPkTermDosepMapping dosepMapping : dosepMappings) {
				Metadatum metadatumDosep = new Metadatum(ndx + "USER_CHOSEN_DOSEP_LIST", dosepMapping.getPkTerm() + "," + dosepMapping.getDosep());
				metadata.add(metadatumDosep);
				list.add(metadatumDosep);
				ndx++;
			}
			
			return list;
		}

		private String convertLabInfoListToCSV(List<PimsPkLabInfo> listLabInfo) {
			StringBuffer csv = new StringBuffer();
			createPimsPkLabInfoCSVHeader(csv);

			for (PimsPkLabInfo labInfo : listLabInfo) {
				nullCheck(csv, labInfo.getBarcode());
				csv.append(",");
				nullCheck(csv, labInfo.getLabelComment());
				csv.append(",");
				nullCheck(csv, labInfo.getLabelType());
				csv.append(",");
				nullCheck(csv, labInfo.getPkbdfld());
				csv.append(",");
				nullCheck(csv, labInfo.getPkcoll());
				csv.append(",");
				nullCheck(csv, labInfo.getPkptm());
				csv.append(",");
				nullCheck(csv, labInfo.getPkptmi());
				csv.append(",");
				nullCheck(csv, labInfo.getStudyid());
				csv.append(",");
				nullCheck(csv, labInfo.getSubjid());
				csv.append(",");
				nullCheck(csv, labInfo.getVisit());
				csv.append(",");
				nullCheckInt(csv, labInfo.getItem());
				csv.append(",");
				nullCheckLong(csv, labInfo.getSubjectId());
				csv.append(",");
				nullCheckLong(csv, labInfo.getSubjectNumber());

				csv.append("\r\n");
			}

			if (Props.isCreateCSV_FilesForPIMS_CRFDataframe()) {
				createFile("CRFLabInfo.csv", csv.toString());
			}

			return csv.toString();
		}

		private void createPimsPkLabInfoCSVHeader(StringBuffer csv) {
			csv.append("Barcode".toUpperCase());
			csv.append(",");
			csv.append("LabelComment".toUpperCase());
			csv.append(",");
			csv.append("LabelType".toUpperCase());
			csv.append(",");
			csv.append("Pkbdfld".toUpperCase());
			csv.append(",");
			csv.append("Pkcoll".toUpperCase());
			csv.append(",");
			csv.append("Pkptm".toUpperCase());
			csv.append(",");
			csv.append("Pkptmi".toUpperCase());
			csv.append(",");
			csv.append("Studyid".toUpperCase());
			csv.append(",");
			csv.append("Subjid".toUpperCase());
			csv.append(",");
			csv.append("Visit".toUpperCase());
			csv.append(",");
			csv.append("getItem".toUpperCase());
			csv.append(",");
			csv.append("SubjectId".toUpperCase());
			csv.append(",");
			csv.append("SubjectNumber".toUpperCase());

			csv.append("\r\n");
		}

		private String convertPkDataListToCSV(List<PimsPharmacokinetics> listPKData) {
			StringBuffer csv = new StringBuffer();
			createPimsPharmacokineticsCSVHeader(csv);

			for (PimsPharmacokinetics pk : listPKData) {
//				nullCheck(csv,pk.getCountry());
//				csv.append(",");
//				nullCheck(csv,pk.getDataStandard());
//				csv.append(",");
				nullCheck(csv, pk.getPeriod());
				csv.append(",");
				nullCheck(csv, pk.getPeriodc());
				csv.append(",");
				nullCheck(csv, pk.getPhase());
				csv.append(",");
				nullCheck(csv, pk.getPhasec());
				csv.append(",");
				nullCheck(csv, pk.getPkadtef());
				csv.append(",");
				nullCheck(csv, pk.getPkadtf());
				csv.append(",");
				nullCheck(csv, pk.getPkamt());
				csv.append(",");
				nullCheck(csv, pk.getPkamtu());
				csv.append(",");
				nullCheck(csv, pk.getPkatmef());
				csv.append(",");
				nullCheck(csv, pk.getPkatmf());
				csv.append(",");
				nullCheck(csv, pk.getPkbdfld());
				csv.append(",");
				nullCheck(csv, pk.getPkcnc());
				csv.append(",");
				nullCheck(csv, pk.getPkcncu());
				csv.append(",");
				nullCheck(csv, pk.getPkcode());
				csv.append(",");
				nullCheck(csv, pk.getPkcoll());
				csv.append(",");
				nullCheck(csv, pk.getPkcomc());
				csv.append(",");
				nullCheck(csv, pk.getPkcoml());
				csv.append(",");
				nullCheck(csv, pk.getPkdecod());
				csv.append(",");
				nullCheck(csv, pk.getPkloqf());
				csv.append(",");
				nullCheck(csv, pk.getPknd());
				csv.append(",");
				nullCheck(csv, pk.getPkptm());
				csv.append(",");
				nullCheck(csv, pk.getPkptmi());
				csv.append(",");
				nullCheck(csv, pk.getPksmms());
				csv.append(",");
				nullCheck(csv, pk.getPksmmsu());
				csv.append(",");
				nullCheck(csv, pk.getPksmnd());
				csv.append(",");
				nullCheck(csv, pk.getPksmvl());
				csv.append(",");
				nullCheck(csv, pk.getPksmvlu());
				csv.append(",");
				nullCheck(csv, pk.getPkterm());
				csv.append(",");
				nullCheck(csv, pk.getPkusmid());
				csv.append(",");
				nullCheck(csv, pk.getSiteid());
				csv.append(",");
				nullCheck(csv, pk.getSperiod());
				csv.append(",");
				nullCheck(csv, pk.getSperiodc());
				csv.append(",");
				nullCheck(csv, pk.getStudyid());
				csv.append(",");
				nullCheck(csv, pk.getSubevenf());
				csv.append(",");
				nullCheck(csv, pk.getSubjid());
				csv.append(",");
				nullCheck(csv, pk.getVisit());
				csv.append(",");
				nullCheck(csv, pk.getVisnumf());
				csv.append(",");
				csv.append(pk.getItem());
				csv.append(",");
				if (pk.getLastUpdateDate() != null) {
					nullCheck(csv, pk.getLastUpdateDate().toString());
				} else {
					nullCheck(csv, null);
				}

				csv.append("\r\n");
			}

			if (Props.isCreateCSV_FilesForPIMS_CRFDataframe()) {
				createFile("CRFPkData.csv", csv.toString());
			}

			return csv.toString();
		}

		private void createPimsPharmacokineticsCSVHeader(StringBuffer csv) {
//			csv.append("Country".toUpperCase());
//			csv.append(",");
//			csv.append("DataStandard".toUpperCase());
//			csv.append(",");
			csv.append("Period".toUpperCase());
			csv.append(",");
			csv.append("Periodc".toUpperCase());
			csv.append(",");
			csv.append("Phase".toUpperCase());
			csv.append(",");
			csv.append("Phasec".toUpperCase());
			csv.append(",");
			csv.append("Pkadtef".toUpperCase());
			csv.append(",");
			csv.append("Pkadtf".toUpperCase());
			csv.append(",");
			csv.append("Pkamt".toUpperCase());
			csv.append(",");
			csv.append("Pkamtu".toUpperCase());
			csv.append(",");
			csv.append("Pkatmef".toUpperCase());
			csv.append(",");
			csv.append("Pkatmf".toUpperCase());
			csv.append(",");
			csv.append("Pkbdfld".toUpperCase());
			csv.append(",");
			csv.append("Pkcnc".toUpperCase());
			csv.append(",");
			csv.append("Pkcncu".toUpperCase());
			csv.append(",");
			csv.append("Pkcode".toUpperCase());
			csv.append(",");
			csv.append("Pkcoll".toUpperCase());
			csv.append(",");
			csv.append("Pkcomc".toUpperCase());
			csv.append(",");
			csv.append("Pkcoml".toUpperCase());
			csv.append(",");
			csv.append("Pkdecod".toUpperCase());
			csv.append(",");
			csv.append("Pkloqf".toUpperCase());
			csv.append(",");
			csv.append("Pknd".toUpperCase());
			csv.append(",");
			csv.append("Pkptm".toUpperCase());
			csv.append(",");
			csv.append("Pkptmi".toUpperCase());
			csv.append(",");
			csv.append("Pksmms".toUpperCase());
			csv.append(",");
			csv.append("Pksmmsu".toUpperCase());
			csv.append(",");
			csv.append("Pksmnd".toUpperCase());
			csv.append(",");
			csv.append("Pksmvl".toUpperCase());
			csv.append(",");
			csv.append("Pksmvlu".toUpperCase());
			csv.append(",");
			csv.append("Pkterm".toUpperCase());
			csv.append(",");
			csv.append("Pkusmid".toUpperCase());
			csv.append(",");
			csv.append("Siteid".toUpperCase());
			csv.append(",");
			csv.append("Speriod".toUpperCase());
			csv.append(",");
			csv.append("Speriodc".toUpperCase());
			csv.append(",");
			csv.append("Studyid".toUpperCase());
			csv.append(",");
			csv.append("Subevenf".toUpperCase());
			csv.append(",");
			csv.append("Subjid".toUpperCase());
			csv.append(",");
			csv.append("Visit".toUpperCase());
			csv.append(",");
			csv.append("Visnumf".toUpperCase());
			csv.append(",");
			csv.append("Item".toUpperCase());
			csv.append(",");
			csv.append("LastUpdateDate".toUpperCase());

			csv.append("\r\n");
		}

		private String convertTreatmentListToCSV(List<PimsTreatment> treatments) {
			StringBuffer csv = new StringBuffer();
			createPimsTreatmentListCSVHeader(csv);

			for (PimsTreatment pt : treatments) {
				nullCheck(csv, pt.getActdtls());
				csv.append(",");
				nullCheck(csv, pt.getActtrt());
				csv.append(",");
				nullCheck(csv, pt.getActtrtc());
				csv.append(",");
				nullCheck(csv, pt.getCohort());
				csv.append(",");
				nullCheck(csv, pt.getComppctf());
				csv.append(",");
				nullCheck(csv, pt.getCompsat());
				csv.append(",");
				nullCheck(csv, pt.getCountry());
				csv.append(",");
//				nullCheck(csv,pt.getDataStandard());
//				csv.append(",");
				nullCheck(csv, pt.getEval());
				csv.append(",");
				nullCheck(csv, pt.getExdetls());
				csv.append(",");
				nullCheck(csv, pt.getExendtf());
				csv.append(",");
				nullCheck(csv, pt.getExentmf());
				csv.append(",");
				nullCheck(csv, pt.getExstdtf());
				csv.append(",");
				nullCheck(csv, pt.getExsttmf());
				csv.append(",");
				nullCheck(csv, pt.getItt());
				csv.append(",");
				nullCheck(csv, pt.getPeriod());
				csv.append(",");
				nullCheck(csv, pt.getPeriodc());
				csv.append(",");
				nullCheck(csv, pt.getPhase());
				csv.append(",");
				nullCheck(csv, pt.getPhasec());
				csv.append(",");
				nullCheck(csv, pt.getRandno());
				csv.append(",");
				nullCheck(csv, pt.getRandnof());
				csv.append(",");
				nullCheck(csv, pt.getRandodtf());
				csv.append(",");
				nullCheck(csv, pt.getSafety());
				csv.append(",");
				nullCheck(csv, pt.getSiteid());
				csv.append(",");
				nullCheck(csv, pt.getSperiod());
				csv.append(",");
				nullCheck(csv, pt.getSperiodc());
				csv.append(",");
				nullCheck(csv, pt.getStudyid());
				csv.append(",");
				nullCheck(csv, pt.getSubevenf());
				csv.append(",");
				nullCheck(csv, pt.getSubjid());
				csv.append(",");
				nullCheck(csv, pt.getTrtgrp());
				csv.append(",");
				nullCheck(csv, pt.getTrtgrpc());
				csv.append(",");
				nullCheck(csv, pt.getTrtnd());
				csv.append(",");
				nullCheck(csv, pt.getTrtseq());
				csv.append(",");
				nullCheck(csv, pt.getTrtseql());
				csv.append(",");
				nullCheck(csv, pt.getVisit());
				csv.append(",");
				nullCheck(csv, pt.getVisnumf());
				csv.append(",");
				csv.append(pt.getItem());
				csv.append(",");
				if (pt.getLastUpdateDate() != null) {
					nullCheck(csv, pt.getLastUpdateDate().toString());
				} else {
					nullCheck(csv, null);
				}

				csv.append("\r\n");
			}

			if (Props.isCreateCSV_FilesForPIMS_CRFDataframe()) {
				createFile("CRFTreatment.csv", csv.toString());
			}

			return csv.toString();
		}

		private void createPimsTreatmentListCSVHeader(StringBuffer csv) {
			csv.append("Actdtls".toUpperCase());
			csv.append(",");
			csv.append("Acttrt".toUpperCase());
			csv.append(",");
			csv.append("Acttrtc".toUpperCase());
			csv.append(",");
			csv.append("Cohort".toUpperCase());
			csv.append(",");
			csv.append("Comppctf".toUpperCase());
			csv.append(",");
			csv.append("Compsat".toUpperCase());
			csv.append(",");
			csv.append("Country".toUpperCase());
			csv.append(",");
//			csv.append("DataStandard".toUpperCase());
//			csv.append(",");
			csv.append("Eval".toUpperCase());
			csv.append(",");
			csv.append("Exdetls".toUpperCase());
			csv.append(",");
			csv.append("Exendtf".toUpperCase());
			csv.append(",");
			csv.append("Exentmf".toUpperCase());
			csv.append(",");
			csv.append("Exstdtf".toUpperCase());
			csv.append(",");
			csv.append("Exsttmf".toUpperCase());
			csv.append(",");
			csv.append("Itt".toUpperCase());
			csv.append(",");
			csv.append("Period".toUpperCase());
			csv.append(",");
			csv.append("Periodc".toUpperCase());
			csv.append(",");
			csv.append("Phase".toUpperCase());
			csv.append(",");
			csv.append("Phasec".toUpperCase());
			csv.append(",");
			csv.append("Randno".toUpperCase());
			csv.append(",");
			csv.append("Randnof".toUpperCase());
			csv.append(",");
			csv.append("Randodtf".toUpperCase());
			csv.append(",");
			csv.append("Safety".toUpperCase());
			csv.append(",");
			csv.append("Siteid".toUpperCase());
			csv.append(",");
			csv.append("Speriod".toUpperCase());
			csv.append(",");
			csv.append("Speriodc".toUpperCase());
			csv.append(",");
			csv.append("Studyid".toUpperCase());
			csv.append(",");
			csv.append("Subevenf".toUpperCase());
			csv.append(",");
			csv.append("Subjid".toUpperCase());
			csv.append(",");
			csv.append("Trtgrp".toUpperCase());
			csv.append(",");
			csv.append("Trtgrpc".toUpperCase());
			csv.append(",");
			csv.append("Trtnd".toUpperCase());
			csv.append(",");
			csv.append("Trtseq".toUpperCase());
			csv.append(",");
			csv.append("Trtseql".toUpperCase());
			csv.append(",");
			csv.append("Visit".toUpperCase());
			csv.append(",");
			csv.append("Visnumf".toUpperCase());
			csv.append(",");
			csv.append("Item".toUpperCase());
			csv.append(",");
			csv.append("LastUpdateDate".toUpperCase());

			csv.append("\r\n");
		}

		private void nullCheck(StringBuffer csv, String string) {
			if (string == null || string.toUpperCase().equals("NULL")) {
				csv.append("");
			} else {
				String s = string;
				try {
					Double.parseDouble(s);
				}
				catch(Exception e) {
					s = "\"" + s.replaceAll("\"", "\"\"") + "\"";
				}
				
				csv.append(s);
			}
		}

		private void nullCheckInt(StringBuffer csv, Integer intVal) {
			if (intVal == null) {
				csv.append("");
			} else {
				csv.append(intVal);
			}
		}

		private void nullCheckLong(StringBuffer csv, Long intVal) {
			if (intVal == null) {
				csv.append("");
			} else {
				csv.append(intVal);
			}
		}

		private String convertDosepListToCSV(List<PimsDose> list) {
			StringBuffer csv = new StringBuffer();
			createPimsDoseCSVHeader(csv);

			for (PimsDose pd : list) {
				nullCheck(csv, pd.getAcendtf());
				csv.append(",");
				nullCheck(csv, pd.getAcentmf());
				csv.append(",");
				nullCheck(csv, pd.getAcstdtf());
				csv.append(",");
				nullCheck(csv, pd.getAcsttmf());
				csv.append(",");
				nullCheck(csv, pd.getBodsit());
				csv.append(",");
				nullCheck(csv, pd.getColnof());
				csv.append(",");
				nullCheck(csv, pd.getCompde());
				csv.append(",");
				nullCheck(csv, pd.getCountry());
				csv.append(",");
//				nullCheck(csv,pd.getDataStandard());
//				csv.append(",");
				nullCheck(csv, pd.getDosedtf());
				csv.append(",");
				nullCheck(csv, pd.getDosep());
				csv.append(","); // fix added 6/5
				nullCheck(csv, pd.getDosetmf());
				csv.append(",");
				nullCheck(csv, pd.getDosfrm());
				csv.append(","); // fix added 6/5
				nullCheck(csv, pd.getDosmsf());
				csv.append(",");
				nullCheck(csv, pd.getDosnd());
				csv.append(",");
				nullCheck(csv, pd.getDospnd());
				csv.append(",");
				nullCheck(csv, pd.getDosuni());
				csv.append(",");
				nullCheck(csv, pd.getDosqtyf());
				csv.append(",");
				nullCheck(csv, pd.getDostotf());
				csv.append(",");
				nullCheck(csv, pd.getDosuse());
				csv.append(",");
				nullCheck(csv, pd.getDscoll());
				csv.append(",");
				nullCheck(csv, pd.getDsptm());
				csv.append(",");
				nullCheck(csv, pd.getEccat());
				csv.append(",");
				nullCheck(csv, pd.getEcrefid());
				csv.append(",");
				nullCheck(csv, pd.getEcscat());
				csv.append(",");
				nullCheck(csv, pd.getInfratf());
				csv.append(",");
				nullCheck(csv, pd.getInfrau());
				csv.append(",");
				nullCheck(csv, pd.getInfsp());
				csv.append(",");
				nullCheck(csv, pd.getInfspr());
				csv.append(",");
				nullCheck(csv, pd.getInfvlf());
				csv.append(",");
				nullCheck(csv, pd.getInfvlu());
				csv.append(",");
				nullCheck(csv, pd.getMedlbl());
				csv.append(",");
				nullCheck(csv, pd.getMendtf());
				csv.append(",");
				nullCheck(csv, pd.getMentmf());
				csv.append(",");
				nullCheck(csv, pd.getMisdos());
				csv.append(",");
				nullCheck(csv, pd.getMstdtf());
				csv.append(",");
				nullCheck(csv, pd.getMsttmf());
				csv.append(",");
				nullCheck(csv, pd.getPeriod());
				csv.append(",");
				nullCheck(csv, pd.getPeriodc());
				csv.append(",");
				nullCheck(csv, pd.getPhase());
				csv.append(",");
				nullCheck(csv, pd.getPhasec());
				csv.append(",");
				nullCheck(csv, pd.getRoute());
				csv.append(",");
				nullCheck(csv, pd.getRownof());
				csv.append(",");
				nullCheck(csv, pd.getSiteid());
				csv.append(",");
				nullCheck(csv, pd.getSolcncf());
				csv.append(",");
				nullCheck(csv, pd.getSolcnu());
				csv.append(",");
				nullCheck(csv, pd.getSolvlf());
				csv.append(",");
				nullCheck(csv, pd.getSolvlu());
				csv.append(",");
				nullCheck(csv, pd.getSperiod());
				csv.append(",");
				nullCheck(csv, pd.getSperiodc());
				csv.append(",");
				nullCheck(csv, pd.getStudyid());
				csv.append(",");
				nullCheck(csv, pd.getSubevenf());
				csv.append(",");
				nullCheck(csv, pd.getSubjid());
				csv.append(",");
				csv.append(pd.getVisit());
				csv.append(",");
				csv.append(pd.getVisnumf());
				csv.append(",");
				csv.append(pd.getItem());
				csv.append(",");
				if (pd.getLastUpdateDate() != null) {
					nullCheck(csv, pd.getLastUpdateDate().toString());
				} else {
					nullCheck(csv, null);
				}

				csv.append("\r\n");
			}

			if (Props.isCreateCSV_FilesForPIMS_CRFDataframe()) {
				createFile("CRFDoseP.csv", csv.toString());
			}

			return csv.toString();
		}

		private void createFile(String fileName, String string) {
			try {
				// NOTE!!!! Writing to c:\ won't work because you don't have admin privs
				RandomAccessFile raf = new RandomAccessFile("C:\\aTEMP\\" + fileName, "rw");
				raf.writeBytes(string);
				raf.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private void createPimsDoseCSVHeader(StringBuffer csv) {
			csv.append("Acendtf".toUpperCase());
			csv.append(",");
			csv.append("Acentmf".toUpperCase());
			csv.append(",");
			csv.append("Acstdtf".toUpperCase());
			csv.append(",");
			csv.append("Acsttmf".toUpperCase());
			csv.append(",");
			csv.append("Bodsit".toUpperCase());
			csv.append(",");
			csv.append("Colnof".toUpperCase());
			csv.append(",");
			csv.append("Compde".toUpperCase());
			csv.append(",");
			csv.append("Country".toUpperCase());
			csv.append(",");
//			csv.append("DataStandard".toUpperCase());
//			csv.append(",");
			csv.append("Dosedtf".toUpperCase());
			csv.append(",");
			csv.append("Dosep".toUpperCase());
			csv.append(",");
			csv.append("Dosetmf".toUpperCase());
			csv.append(",");
			csv.append("Dosfrm".toUpperCase());
			csv.append(",");
			csv.append("Dosmsf".toUpperCase());
			csv.append(",");
			csv.append("Dosnd".toUpperCase());
			csv.append(",");
			csv.append("Dospnd".toUpperCase());
			csv.append(",");
			csv.append("Dosuni".toUpperCase());
			csv.append(",");
			csv.append("Dosqtyf".toUpperCase());
			csv.append(",");
			csv.append("Dostotf".toUpperCase());
			csv.append(",");
			csv.append("Dosuse".toUpperCase());
			csv.append(",");
			csv.append("Dscoll".toUpperCase());
			csv.append(",");
			csv.append("Dsptm".toUpperCase());
			csv.append(",");
			csv.append("Eccat".toUpperCase());
			csv.append(",");
			csv.append("Ecrefid".toUpperCase());
			csv.append(",");
			csv.append("Ecscat".toUpperCase());
			csv.append(",");
			csv.append("Infratf".toUpperCase());
			csv.append(",");
			csv.append("Infrau".toUpperCase());
			csv.append(",");
			csv.append("Infsp".toUpperCase());
			csv.append(",");
			csv.append("Infspr".toUpperCase());
			csv.append(",");
			csv.append("Infvlf".toUpperCase());
			csv.append(",");
			csv.append("Infvlu".toUpperCase());
			csv.append(",");
			csv.append("Medlbl".toUpperCase());
			csv.append(",");
			csv.append("Mendtf".toUpperCase());
			csv.append(",");
			csv.append("Mentmf".toUpperCase());
			csv.append(",");
			csv.append("Misdos".toUpperCase());
			csv.append(",");
			csv.append("Mstdtf".toUpperCase());
			csv.append(",");
			csv.append("Msttmf".toUpperCase());
			csv.append(",");
			csv.append("Period".toUpperCase());
			csv.append(",");
			csv.append("Periodc".toUpperCase());
			csv.append(",");
			csv.append("Phase".toUpperCase());
			csv.append(",");
			csv.append("Phasec".toUpperCase());
			csv.append(",");
			csv.append("Route".toUpperCase());
			csv.append(",");
			csv.append("Rownof".toUpperCase());
			csv.append(",");
			csv.append("Siteid".toUpperCase());
			csv.append(",");
			csv.append("Solcncf".toUpperCase());
			csv.append(",");
			csv.append("Solcnu".toUpperCase());
			csv.append(",");
			csv.append("Solvlf".toUpperCase());
			csv.append(",");
			csv.append("Solvlu".toUpperCase());
			csv.append(",");
			csv.append("Speriod".toUpperCase());
			csv.append(",");
			csv.append("Speriodc".toUpperCase());
			csv.append(",");
			csv.append("Studyid".toUpperCase());
			csv.append(",");
			csv.append("Subevenf".toUpperCase());
			csv.append(",");
			csv.append("Subjid".toUpperCase());
			csv.append(",");
			csv.append("Visit".toUpperCase());
			csv.append(",");
			csv.append("Visnumf".toUpperCase());
			csv.append(",");
			csv.append("Item".toUpperCase());
			csv.append(",");
			csv.append("LastUpdateDate".toUpperCase());

			csv.append("\r\n");
		}

		private void checkCreatedBy(Request request, PimsDataLoadInput input) {
			if (input.getCreatedBy() == null || input.getCreatedBy().isEmpty()) {
				String userId = request.headers(Const.IAMPFIZERUSERCN);
				if (userId != null) {
					input.setCreatedBy(userId);
				} else {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
			}
		}

		private String createDemographyDataframeAndDataSet(String programProtocol, String csv, int numCSVRows,
				PimsStudyLocations locations, String pkdefDataframeId, String createdBy, PimsDataLoadInput input)
				throws Exception {
			DataframeDAO dDao = getDataframeDAO();

			Dataframe df = createDefaultDataframe(programProtocol, numCSVRows, locations.getLocations(),
					pkdefDataframeId, dDao, createdBy, "PIMS Demography", input);
			addPIMSDatabaseInfoAndViewsForDemography(df, locations);
			df.setItemType("PIMS Demography");
			Dataframe dfNode = dDao.insertDataframe(df);
			createDefaultDataset(csv, dfNode.getId());

			commitDataframeLogic(dfNode.getId(), dDao);
			return dfNode.getId();
		}

		private void addPIMSDatabaseInfoAndViewsForDemography(Dataframe df, PimsStudyLocations locations) {
			List<String> listLocations = new ArrayList<String>();
			List<String> listDatabaseNames = new ArrayList<String>();

			getPIMSDatabaseNamesAndLocationsList(locations, listLocations, listDatabaseNames);

			List<String> listViews = new ArrayList<String>();

			if (locations.getDataStandard().toLowerCase().equals(PDS_DATA_STANDARD)) {
				listViews.add("PI_GRADES_DEM_P");
				listViews.add("PI_GRADES_VS_P");
			} else if (locations.getDataStandard().equals(CDISC_DATA_STANDARD)) {
				listViews.add("PI_CDISC_DEM_P");
				listViews.add("PI_CDISC_VS_P");
			}

			storeLocationInfoAsThreePiecesOfMetadata(df, listLocations, listDatabaseNames, listViews);
		}

		private void storeLocationInfoAsThreePiecesOfMetadata(Dataframe df, List<String> listLocations,
				List<String> listDatabaseNames, List<String> listViews) {
			createMetadataItem(df.getMetadata(), LOCATION_SOURCE_PATH, listLocations.toString(), "string");
			createMetadataItem(df.getMetadata(), LOCATION_SERVER, listDatabaseNames.toString(), "string");
			createMetadataItem(df.getMetadata(), LOCATION_FULL_PATH, listViews.toString(), "string");
		}

		private void getPIMSDatabaseNamesAndLocationsList(PimsStudyLocations locations, List<String> listLocations,
				List<String> listDatabaseNames) {
			for (String location : locations.getLocations()) {
				if (location.equals(LOCATION_NEW_HAVEN)) {
					listLocations.add(LOCATION_NEW_HAVEN);
					listDatabaseNames.add(Props.getPimsNewHavenDatabaseName());
				} else if (location.equalsIgnoreCase(LOCATION_BRUSSELS)) {
					listLocations.add(LOCATION_BRUSSELS);
					listDatabaseNames.add(Props.getPimsBrusselsDatabaseName());
				}
			}
		}

		private void createDefaultDataset(String csv, String dataframeId) {
			DatasetDAO dsDao = new DatasetDAOImpl();
			Dataset dataset = new Dataset();

			dataset = dsDao.insertDataset(dataframeId, dataset);
			dsDao.insertData(dataset.getId(), csv.getBytes());
		}

		private Dataframe createDefaultDataframe(String studyId, int numCSVRows, List<String> locations,
				String pkdefDataframeId, DataframeDAO dDao, String createdBy, String dataType,
				PimsDataLoadInput input) {
			Dataframe df = new Dataframe();

			// TODO get this constant from Justin's newly created constants (I think I saw
			// them in the Lineage DAO and they were private so will have to be made public
			// or moved
			df.setDataframeType(Dataframe.DATASET_TYPE);

			List<String> studyIds = new ArrayList<String>();
			studyIds.add(studyId);
			df.setCreatedBy(createdBy);
			df.setStudyIds(studyIds);
			df.setItemType(dataType);

			df.setPromotionStatus("Pending Promotion");
			df.setRestrictionStatus("Not Restricted");
			df.setDataBlindingStatus("Unblinded");

			addUserChosenDosepListToMetadata(df, input.getDosepMapping());
			setMetadatum(df, numCSVRows, locations, dataType);

			Dataframe pkdefDataframe = dDao.getDataframe(pkdefDataframeId);

			df.setDataStatus(pkdefDataframe.getDataStatus());
			df.setProtocolIds(pkdefDataframe.getProtocolIds());
			df.setProjectIds(pkdefDataframe.getProjectIds());
			df.setProgramIds(pkdefDataframe.getProgramIds());
			df.setEquipId(EquipIdCalculator.calculate(df.getDataframeType()));

			return df;
		}

		private List<Metadatum> setMetadatum(Dataframe df, Integer numCSVRows, List<String> locations, String dataType) {
			List<Metadatum> metadatum = new ArrayList<Metadatum>();

			// this could have been hard coded theoretically. For PIMS it's always
			// "Production" at this point.
			createMetadataItem(metadatum, "Location Type", Props.getPIMSLocationType(), "string");

			createMetadataItem(metadatum, "Location Controlled YN", "Y", "string");
			createMetadataItem(metadatum, "Data Load Status", "Completed", "string");
			if (numCSVRows != null) {
				createMetadataItem(metadatum, "Data Load Row Count", "" + numCSVRows, "string");
			}
			createMetadataItem(metadatum, LOCATION_SOURCE_PATH, locations.toString(), "string");
			createMetadataItem(metadatum, "Data Type", dataType, "string");
			createMetadataItem(metadatum, "File Format", "CSV", "string");

			df.setMetadata(metadatum);
			return metadatum;
		}

		private void createMetadataItem(List<Metadatum> metadatum, String key, String value, String valueType) {
			Metadatum metadata = new Metadatum();
			metadata.setKey(key);
			List<String> values = new ArrayList<String>();
			values.add(value);
			metadata.setValue(values);
			metadata.setValueType("string");
			metadatum.add(metadata);
		}
	};
}
