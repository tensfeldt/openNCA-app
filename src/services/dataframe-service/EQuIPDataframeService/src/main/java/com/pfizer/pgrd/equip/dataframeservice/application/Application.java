package com.pfizer.pgrd.equip.dataframeservice.application;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframeservice.resource.BaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.CopyNodeResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityAttachmentResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityCommentResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityMetadataResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
//import com.pfizer.pgrd.equip.dataframeservice.resource.EquipIdCalculatorTestResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.analysis.AnalysisResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.analysis.LegacyParameters;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyListResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.batch.BatchResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeBulkResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeListResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframePromotionResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeRootResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeScriptResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataset.DataframeDatasetDataResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataset.DataframeDatasetResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.event.PublishEventResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.event.PublishItemResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.event.ReportingEventItemResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.event.ReportingEventResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.lineage.LineageRecreationResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.lineage.LineageResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.pims.PimsResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestChecklistIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestChecklistResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestItemIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestItemResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestSummaryIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestSummaryResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestWorkflowIdResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.qc.QCRequestWorkflowResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.report.ReportResource;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;

import spark.Spark;
import spark.servlet.SparkApplication;

@WebServlet(name = "Application", urlPatterns = { "/" })

@MultipartConfig(location = "/tmp", fileSizeThreshold = 1024 * 1024, maxFileSize = Integer.MAX_VALUE, maxRequestSize = Integer.MAX_VALUE)

public class Application extends HttpServlet implements SparkApplication {
	private static Logger log = LoggerFactory.getLogger(Application.class);	
	
	private static final long serialVersionUID = 1L;
	
	private static final String BASE_URI = "/:systemId";
	
	private static final String DATAFRAME_BASE_URI = BASE_URI + "/dataframes";
	private static final String DATAFRAME_BULK_URI = DATAFRAME_BASE_URI + "/bulk";
	private static final String DATAFRAME_SEARCH_URI = DATAFRAME_BASE_URI + "/search";
	private static final String DATAFRAME_LIST_URI = DATAFRAME_BASE_URI + "/list";
	
	private static final String DATAFRAME_ID_URI = DATAFRAME_BASE_URI + "/:id";
	private static final String DATAFRAME_DATASET_URI = DATAFRAME_ID_URI + "/data";
	private static final String DATAFRAME_DATASET_DATA_URI = DATAFRAME_BASE_URI + "/data/:id";
	private static final String DATAFRAME_PROMOTIONS_URI = DATAFRAME_ID_URI + "/promotions";
	private static final String DATAFRAME_SCRIPT_URI = DATAFRAME_ID_URI + "/script";
	private static final String DATAFRAME_REPORTING_EVENT_ITEMS_URI = DATAFRAME_ID_URI + "/reportingEventItems";
	//private static final String DATAFRAME_OPDATA_URI = DATAFRAME_ID_URI + "/operationalreferences";

	private static final String QCREQUEST_BASE_URI = BASE_URI + "/qcrequests";
	private static final String QCREQUEST_ID_URI = QCREQUEST_BASE_URI + "/:id";

	private static final String QCREQUEST_ITEM_URI = QCREQUEST_ID_URI + "/qcitems";
	private static final String QCREQUEST_ITEM_ID_URI = QCREQUEST_BASE_URI + "/qcitems/:id";
	
	private static final String QCREQUEST_WORKFLOW_URI = QCREQUEST_ID_URI + "/qcworkflowitems";
	private static final String QCREQUEST_WORKFLOW_ID_URI = QCREQUEST_BASE_URI + "/qcworkflowitems/:id";

	private static final String QCREQUEST_SUMMARY_URI = QCREQUEST_ID_URI + "/qcchecklistsummaryitems";
	private static final String QCREQUEST_SUMMARY_ID_URI = QCREQUEST_BASE_URI + "/qcchecklistsummaryitems/:id";

	private static final String QCREQUEST_CHECKLIST_URI = QCREQUEST_ID_URI + "/qcchecklistitems";
	private static final String QCREQUEST_CHECKLIST_ID_URI = QCREQUEST_BASE_URI + "/qcchecklistitems/:id";

	private static final String ASSEMBLY_BASE_URI = BASE_URI + "/assemblies";
	private static final String ASSEMBLY_ID_URI = ASSEMBLY_BASE_URI + "/:id";
	private static final String ASSEMBLY_EQUIP_ID_URI = ASSEMBLY_BASE_URI + "/equipId/:equipId";
	private static final String ASSEMBLY_LIST_URI = ASSEMBLY_BASE_URI + "/list";

	private static final String NODE_BASE_URI = BASE_URI + "/entities";
	private static final String NODE_ID_URI = NODE_BASE_URI + "/:id";
	private static final String NODE_REPORTING_EVENTS_URI = NODE_ID_URI + "/reportingEvents";
	private static final String NODE_COMMENTS_URI = NODE_ID_URI + "/comments";
	private static final String NODE_METADATA_URI = NODE_ID_URI + "/metadata";
	private static final String NODE_ATTACHMENTS_URI = NODE_ID_URI + "/attachments";
	private static final String NODE_COPY_URI = NODE_ID_URI + "/copy";
	//private static final String NODE_UPDATE_URI = NODE_ID_URI + "/update";
	private static final String NODE_VERSIONING_URI = NODE_ID_URI + "/:action";
	// private static final String NODE_UPDATE_URI = NODE_BASE_URI + "/update/:id";
	// This is just to Test the EquipIdCalculator as a stand-alone component/resource if you need to;
	//private static final String NODE_ACTION_NEW_EQUIP_ID = "/equipidcalculator/:objectType";
	private static final String NODE_ACTION_BY_EQUIP_ID = NODE_BASE_URI + "/equipid/:equipId/:action";
	private static final String NODE_VERSIONS_BY_EQUIP_ID = NODE_BASE_URI + "/equipid/:equipId/versions";
	private static final String MULTIPLE_DELETE = NODE_BASE_URI + "/delete";

	private static final String LINEAGE_BASE_URI = BASE_URI + "/lineage";
	private static final String LINEAGE_DATALOAD = LINEAGE_BASE_URI + "/dataload";
	private static final String LINEAGE_PROMOTION = LINEAGE_BASE_URI + "/promotion";
	private static final String LINEAGE_ANALYSIS_PREP = LINEAGE_BASE_URI + "/analysisprep";
	private static final String LINEAGE_EQUIP_ID = LINEAGE_BASE_URI + "/equipId/:equipId";
	private static final String LINEAGE_COPY = LINEAGE_BASE_URI + "/:startId/copy";
	//private static final String LINEAGE_DELETE = LINEAGE_DATALOAD + "/:startId/delete";
	private static final String LINEAGE_UPDATE = LINEAGE_DATALOAD + "/:startId/:action";

	private static final String LINEAGE_REEXECUTION_URI = LINEAGE_BASE_URI + "/re-execute";
	private static final String LINEAGE_VERSION_URI = LINEAGE_BASE_URI + "/:id/re-execute";

	private static final String REPORTING_EVENTS_BASE_URI = BASE_URI + "/reportingevents";
	private static final String REPORTING_EVENTS_LIST_URI = REPORTING_EVENTS_BASE_URI + "/list";
	private static final String REPORTING_EVENTS_ID_URI = REPORTING_EVENTS_BASE_URI + "/:id";
	private static final String REPORTING_EVENTS_RELEASE_STATUS_URI = REPORTING_EVENTS_BASE_URI + "/releasestatus";
	private static final String REPORTING_EVENTS_PUBLISH_URI = REPORTING_EVENTS_ID_URI + "/publish";

	private static final String REPORTING_EVENT_BY_STUDY_ID_URI = REPORTING_EVENTS_BASE_URI + "/studyids";

	private static final String REPORTING_EVENT_ITEMS_BASE_URI = BASE_URI + "/reportingeventitems";
	private static final String REPORTING_EVENT_ITEMS_BASE_LIST_URI = REPORTING_EVENT_ITEMS_BASE_URI + "/list";
	private static final String REPORTING_EVENT_ITEMS_ID_URI = REPORTING_EVENT_ITEMS_BASE_URI + "/:id";

	private static final String PUBLISHING_EVENTS_BASE_URI = BASE_URI + "/publishingevents";
	private static final String PUBLISHING_EVENTS_BASE_LIST_URI = PUBLISHING_EVENTS_BASE_URI + "/list";
	private static final String PUBLISHING_EVENTS_ID_URI = PUBLISHING_EVENTS_BASE_URI + "/:id";

	private static final String PUBLISHING_EVENT_ITEMS_BASE_URI = BASE_URI + "/publishitems";
	private static final String PUBLISHING_EVENT_ITEMS_BASE_LIST_URI = PUBLISHING_EVENT_ITEMS_BASE_URI + "/list";
	private static final String PUBLISHING_EVENT_ITEMS_ID_URI = PUBLISHING_EVENT_ITEMS_BASE_URI + "/:id";
	private static final String PUBLISHING_EVENT_ITEMS_PUBLISH_STATUS_URI = PUBLISHING_EVENT_ITEMS_BASE_URI
			+ "/publishstatus";
	private static final String BULK_PUBLISH_STATUS = PUBLISHING_EVENT_ITEMS_PUBLISH_STATUS_URI + "/bulk";

	private static final String ANALYSIS_BASE_URI = BASE_URI + "/analyses";
	private static final String ANALYSIS_ID_URI = ANALYSIS_BASE_URI + "/:id";
	private static final String ANALYSIS_COPY_MCT = ANALYSIS_ID_URI + "/copyMCT";
	
	private static final String ANALYSIS_SAVE_URI = BASE_URI + "/analysis/save";

	private static final String PIMS_BASE_URI = BASE_URI + "/pims";
	private static final String PIMS_DOSEP_VIA_STUDY_ID_URI = PIMS_BASE_URI + "/:id/planneddose";
	private static final String PIMS_DATA_LOAD_VIA_STUDY_ID_URI = PIMS_BASE_URI + "/:id/loaddata";
	private static final String PIMS_STATUS_FOR_STUDY = PIMS_BASE_URI + "/:id/studypimsstatus";
	
	private static final String AUDIT_TRAIL_REPORT_URI = BASE_URI + "/reports/atr/:reportingEventId";
	private static final String ANALYSIS_QC_REPORT_URI = BASE_URI + "/reports/analysisqc/:analysisId";
	
	private static final String DATAFRAME_SERVICE_PROPERTIES_FILE_PATH = "/app/3rdparty/equip/EquipDataframeService/";
	private static final String DATAFRAME_SERVICE_PROPERTIES_FILE = DATAFRAME_SERVICE_PROPERTIES_FILE_PATH + "application.properties";
	
	private static final String BATCH_BASE_URI = BASE_URI + "/batches";
	
	@Override
	public void init() {
		// Echo any OPTIONS requests
		Spark.options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}
			
			return "OK";
		});

		// Enable Cross Origin Resource Sharing (CORS)
		Spark.before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Methods", "*");
			response.header("Access-Control-Allow-Headers", "*");
			response.header(HTTPHeaders.CACHE_CONTROL, "no-cache,no-store");
		});
		
		loadApplicationProperties();

		this.initDataframeService();
	}

	public static void loadApplicationProperties(){
		InputStream pfis = null;

		/*try {
			if( pfis == null ) {
				pfis = Application.class.getClassLoader().getResourceAsStream( "application.properties" );
			}

			// Force failure if we don't have a properties file by now.
			// Otherwise, we get a less-than-helpful null pointer exception.
			if( pfis == null ) {
				throw new IllegalStateException( "Unable to load application.properties from classpath." );
			}

			Properties props = new Properties();
			props.load( pfis );
			Props.setProps( props ); //access static members of Props to get to properties.
			
			log.info(Props.toStrings());
		}
		catch( Exception ex ) {
			throw new RuntimeException( ex );
		}*/
		
		try {
			try {
				Properties props = new Properties();
				FileReader fr = new FileReader(DATAFRAME_SERVICE_PROPERTIES_FILE);
				props.load(fr);
				Props.setProps( props ); //access static members of Props to get to properties.
				
				log.info(Props.toStrings());
			} catch (IOException e) {
				throw new IllegalStateException( "Unable to load " + DATAFRAME_SERVICE_PROPERTIES_FILE);
			}
		}
		catch( Exception ex ) {
			throw new RuntimeException( ex );
		}

	}
	
	
	/**
	 * Initializes all URIs for the dataframe service.
	 */
	private void initDataframeService() {
		// =================================================================
		// !IMPORTANT!
		// Spark will match paths in the order that they are submitted!
		// For example, if a request for /entities/<id>/update is made, 
		// Spark will first check /entites/:id/:action before checking
		// /entites/:id/update if the latter was submitted first. In 
		// this case, we simply need to submit the /update URI first.
		// =================================================================
		
		Spark.options("/*", null);

		// allow static files so we can integrate swagger
		Spark.staticFiles.location("/static");
		// redirect base uri to swagger
		Spark.redirect.get("/", "/EQuIPDataframeService/DataframeSwagger/index.html");

		Spark.get(DATAFRAME_BASE_URI, DataframeRootResource.get);
		Spark.post(DATAFRAME_BASE_URI, DataframeRootResource.post);
		Spark.put(DATAFRAME_BASE_URI, DataframeRootResource.put);
		Spark.delete(DATAFRAME_BASE_URI, DataframeRootResource.delete);
		
		Spark.get(DATAFRAME_BULK_URI, DataframeBulkResource.get);
		Spark.post(DATAFRAME_BULK_URI, DataframeBulkResource.post);
		Spark.put(DATAFRAME_BULK_URI, DataframeBulkResource.put);
		Spark.delete(DATAFRAME_BULK_URI, DataframeBulkResource.delete);

		Spark.get(DATAFRAME_LIST_URI, DataframeListResource.get);
		Spark.post(DATAFRAME_LIST_URI, DataframeListResource.post);
		Spark.put(DATAFRAME_LIST_URI, DataframeListResource.put);
		Spark.delete(DATAFRAME_LIST_URI, DataframeListResource.delete);

		Spark.get(DATAFRAME_ID_URI, DataframeIdResource.get);
		Spark.post(DATAFRAME_ID_URI, DataframeIdResource.post);
		Spark.put(DATAFRAME_ID_URI, DataframeIdResource.put);
		Spark.delete(DATAFRAME_ID_URI, DataframeIdResource.delete);
		
		Spark.get(NODE_REPORTING_EVENTS_URI, EntityResource.getReportingEvents);
		Spark.post(NODE_REPORTING_EVENTS_URI, EntityResource.post);
		Spark.put(NODE_REPORTING_EVENTS_URI, EntityResource.put);
		Spark.delete(NODE_REPORTING_EVENTS_URI, EntityResource.delete);

		Spark.get(NODE_COMMENTS_URI, EntityCommentResource.get);
		Spark.post(NODE_COMMENTS_URI, EntityCommentResource.post);
		Spark.put(NODE_COMMENTS_URI, EntityCommentResource.put);
		Spark.delete(NODE_COMMENTS_URI, EntityCommentResource.delete);

		Spark.get(NODE_METADATA_URI, EntityMetadataResource.get);
		Spark.post(NODE_METADATA_URI, EntityMetadataResource.post);
		Spark.put(NODE_METADATA_URI, EntityMetadataResource.put);
		Spark.delete(NODE_METADATA_URI, EntityMetadataResource.delete);

		Spark.get(NODE_ATTACHMENTS_URI, EntityAttachmentResource.get);
		Spark.post(NODE_ATTACHMENTS_URI, BaseResource.post); //NOT IMPLEMENTED
		Spark.put(NODE_ATTACHMENTS_URI, BaseResource.put); //NOT IMPLEMENTED
		Spark.delete(NODE_ATTACHMENTS_URI, BaseResource.delete); //NOT IMPLEMENTED
		
		Spark.get(NODE_COPY_URI, CopyNodeResource.get);
		Spark.post(NODE_COPY_URI, CopyNodeResource.post);
		Spark.put(NODE_COPY_URI, CopyNodeResource.put);
		Spark.delete(NODE_COPY_URI, CopyNodeResource.delete);
		
		Spark.get(NODE_VERSIONING_URI, EntityVersioningResource.get);
		Spark.post(NODE_VERSIONING_URI, EntityVersioningResource.post);
		Spark.put(NODE_VERSIONING_URI, EntityVersioningResource.put);
		Spark.delete(NODE_VERSIONING_URI, EntityVersioningResource.delete);

		// delete once we confirm these are no longer needed (the versioning resource
		// picks up these calls)
		// Spark.get(NODE_UPDATE_URI, EntityUpdateResource.get);
		// Spark.post(NODE_UPDATE_URI, EntityUpdateResource.post);
		// Spark.put(NODE_UPDATE_URI, EntityUpdateResource.put);
		// Spark.delete(NODE_UPDATE_URI, EntityUpdateResource.delete);
		// left off here with error handling
		Spark.get(NODE_ACTION_BY_EQUIP_ID, EntityVersioningResource.get);
		Spark.post(NODE_ACTION_BY_EQUIP_ID, EntityVersioningResource.post);
		Spark.put(NODE_ACTION_BY_EQUIP_ID, EntityVersioningResource.putByEquipId);
		Spark.delete(NODE_ACTION_BY_EQUIP_ID, EntityVersioningResource.delete);
		
		// This is just to Test the EquipIdCalculator as a stand-alone component
		//Spark.get(NODE_ACTION_NEW_EQUIP_ID, EquipIdCalculatorTestResource.get);

		Spark.get(NODE_VERSIONS_BY_EQUIP_ID, EntityVersioningResource.get);
		Spark.post(NODE_VERSIONS_BY_EQUIP_ID, EntityVersioningResource.post);
		Spark.put(NODE_VERSIONS_BY_EQUIP_ID, EntityVersioningResource.put);
		Spark.delete(NODE_VERSIONS_BY_EQUIP_ID, EntityVersioningResource.delete);
		
		
		Spark.put(MULTIPLE_DELETE, EntityVersioningResource.deleteMultiple);
		

		Spark.get(DATAFRAME_PROMOTIONS_URI, DataframePromotionResource.get);
		Spark.post(DATAFRAME_PROMOTIONS_URI, DataframePromotionResource.post);
		Spark.put(DATAFRAME_PROMOTIONS_URI, DataframePromotionResource.put);
		Spark.delete(DATAFRAME_PROMOTIONS_URI, DataframePromotionResource.delete);

		Spark.get(DATAFRAME_SCRIPT_URI, DataframeScriptResource.get);
		Spark.post(DATAFRAME_SCRIPT_URI, DataframeScriptResource.post);
		Spark.put(DATAFRAME_SCRIPT_URI, DataframeScriptResource.put);
		Spark.delete(DATAFRAME_SCRIPT_URI, DataframeScriptResource.delete);
		
		Spark.get(DATAFRAME_REPORTING_EVENT_ITEMS_URI, ReportingEventItemResource.getByDataframeId);
		Spark.post(DATAFRAME_REPORTING_EVENT_ITEMS_URI, DataframeIdResource.post);
		Spark.put(DATAFRAME_REPORTING_EVENT_ITEMS_URI, DataframeIdResource.put);
		Spark.delete(DATAFRAME_REPORTING_EVENT_ITEMS_URI, DataframeIdResource.delete);

		//no longer used
		//Spark.get(DATAFRAME_OPDATA_URI, xDataframeOpdataResource.get);
		//Spark.post(DATAFRAME_OPDATA_URI, xDataframeOpdataResource.post);
		//Spark.put(DATAFRAME_OPDATA_URI, xDataframeOpdataResource.put);
		//Spark.delete(DATAFRAME_OPDATA_URI, xDataframeOpdataResource.delete);

		Spark.get(DATAFRAME_DATASET_URI, DataframeDatasetResource.get);
		Spark.post(DATAFRAME_DATASET_URI, DataframeDatasetResource.post);
		Spark.put(DATAFRAME_DATASET_URI, DataframeDatasetResource.put);
		Spark.delete(DATAFRAME_DATASET_URI, DataframeDatasetResource.delete);

		Spark.get(DATAFRAME_DATASET_DATA_URI, DataframeDatasetDataResource.get);
		Spark.post(DATAFRAME_DATASET_DATA_URI, DataframeDatasetDataResource.post);
		Spark.put(DATAFRAME_DATASET_DATA_URI, DataframeDatasetDataResource.put);
		Spark.delete(DATAFRAME_DATASET_DATA_URI, DataframeDatasetDataResource.delete);

		Spark.get(QCREQUEST_BASE_URI, QCRequestBaseResource.get);
		Spark.post(QCREQUEST_BASE_URI, QCRequestBaseResource.post);
		Spark.put(QCREQUEST_BASE_URI, QCRequestBaseResource.put);
		Spark.delete(QCREQUEST_BASE_URI, QCRequestBaseResource.delete);

		Spark.get(QCREQUEST_ITEM_URI, QCRequestItemResource.get);
		Spark.post(QCREQUEST_ITEM_URI, QCRequestItemResource.post);
		Spark.put(QCREQUEST_ITEM_URI, QCRequestItemResource.put);
		Spark.delete(QCREQUEST_ITEM_URI, QCRequestItemResource.delete);

		Spark.get(QCREQUEST_ITEM_ID_URI, QCRequestItemIdResource.get);
		Spark.post(QCREQUEST_ITEM_ID_URI, QCRequestItemIdResource.post);
		Spark.put(QCREQUEST_ITEM_ID_URI, QCRequestItemIdResource.put);
		Spark.delete(QCREQUEST_ITEM_ID_URI, QCRequestItemIdResource.delete);

		Spark.get(QCREQUEST_ID_URI, QCRequestIdResource.get);
		Spark.post(QCREQUEST_ID_URI, QCRequestIdResource.post);
		Spark.put(QCREQUEST_ID_URI, QCRequestIdResource.put);
		Spark.delete(QCREQUEST_ID_URI, QCRequestIdResource.delete);

		Spark.get(QCREQUEST_WORKFLOW_URI, QCRequestWorkflowResource.get);
		Spark.post(QCREQUEST_WORKFLOW_URI, QCRequestWorkflowResource.post);
		Spark.put(QCREQUEST_WORKFLOW_URI, QCRequestWorkflowResource.put);
		Spark.delete(QCREQUEST_WORKFLOW_URI, QCRequestWorkflowResource.delete);

		Spark.get(QCREQUEST_WORKFLOW_ID_URI, QCRequestWorkflowIdResource.get);
		Spark.post(QCREQUEST_WORKFLOW_ID_URI, QCRequestWorkflowIdResource.post);
		Spark.put(QCREQUEST_WORKFLOW_ID_URI, QCRequestWorkflowIdResource.put);
		Spark.delete(QCREQUEST_WORKFLOW_ID_URI, QCRequestWorkflowIdResource.delete);

		Spark.get(QCREQUEST_SUMMARY_URI, QCRequestSummaryResource.get);
		Spark.post(QCREQUEST_SUMMARY_URI, QCRequestSummaryResource.post);
		Spark.put(QCREQUEST_SUMMARY_URI, QCRequestSummaryResource.put);
		Spark.delete(QCREQUEST_SUMMARY_URI, QCRequestSummaryResource.delete);

		Spark.get(QCREQUEST_SUMMARY_ID_URI, QCRequestSummaryIdResource.get);
		Spark.post(QCREQUEST_SUMMARY_ID_URI, QCRequestSummaryIdResource.post);
		Spark.put(QCREQUEST_SUMMARY_ID_URI, QCRequestSummaryIdResource.put);
		Spark.delete(QCREQUEST_SUMMARY_ID_URI, QCRequestSummaryIdResource.delete);

		Spark.get(QCREQUEST_CHECKLIST_URI, QCRequestChecklistResource.get);
		Spark.post(QCREQUEST_CHECKLIST_URI, QCRequestChecklistResource.post);
		Spark.put(QCREQUEST_CHECKLIST_URI, QCRequestChecklistResource.put);
		Spark.delete(QCREQUEST_CHECKLIST_URI, QCRequestChecklistResource.delete);

		Spark.get(QCREQUEST_CHECKLIST_ID_URI, QCRequestChecklistIdResource.get);
		Spark.post(QCREQUEST_CHECKLIST_ID_URI, QCRequestChecklistIdResource.post);
		Spark.put(QCREQUEST_CHECKLIST_ID_URI, QCRequestChecklistIdResource.put);
		Spark.delete(QCREQUEST_CHECKLIST_ID_URI, QCRequestChecklistIdResource.delete);

		Spark.get(ASSEMBLY_LIST_URI, AssemblyListResource.get);
		Spark.post(ASSEMBLY_LIST_URI, AssemblyListResource.post);
		Spark.put(ASSEMBLY_LIST_URI, AssemblyListResource.put);
		Spark.delete(ASSEMBLY_LIST_URI, AssemblyListResource.delete);
		
		Spark.get(ASSEMBLY_EQUIP_ID_URI, AssemblyIdResource.getAssemblyByEquipId);
		Spark.post(ASSEMBLY_ID_URI, AssemblyIdResource.post);
		Spark.put(ASSEMBLY_ID_URI, AssemblyIdResource.put);
		Spark.delete(ASSEMBLY_ID_URI, AssemblyIdResource.delete);
		
		Spark.get(ASSEMBLY_ID_URI, AssemblyIdResource.get);
		Spark.post(ASSEMBLY_ID_URI, AssemblyIdResource.post);
		Spark.put(ASSEMBLY_ID_URI, AssemblyIdResource.put);
		Spark.delete(ASSEMBLY_ID_URI, AssemblyIdResource.delete);
		
		Spark.get(ASSEMBLY_BASE_URI, AssemblyBaseResource.get);
		Spark.post(ASSEMBLY_BASE_URI, AssemblyBaseResource.post);
		Spark.put(ASSEMBLY_BASE_URI, AssemblyBaseResource.put);
		Spark.delete(ASSEMBLY_BASE_URI, AssemblyBaseResource.delete);
		
		Spark.get(LINEAGE_DATALOAD, LineageResource.getDataloadLineage);
		Spark.post(LINEAGE_DATALOAD, LineageResource.post);
		Spark.put(LINEAGE_DATALOAD, LineageResource.put);
		Spark.delete(LINEAGE_DATALOAD, LineageResource.delete);

		Spark.get(LINEAGE_PROMOTION, LineageResource.getPromotionLineage);
		Spark.post(LINEAGE_PROMOTION, LineageResource.post);
		Spark.put(LINEAGE_PROMOTION, LineageResource.put);
		Spark.delete(LINEAGE_PROMOTION, LineageResource.delete);

		Spark.get(LINEAGE_ANALYSIS_PREP, LineageResource.getAnalysisPrepLineage);
		Spark.post(LINEAGE_ANALYSIS_PREP, LineageResource.post);
		Spark.put(LINEAGE_ANALYSIS_PREP, LineageResource.put);
		Spark.delete(LINEAGE_ANALYSIS_PREP, LineageResource.delete);

		Spark.get(LINEAGE_EQUIP_ID, LineageResource.getIdLineage);
		Spark.post(LINEAGE_EQUIP_ID, LineageResource.post);
		Spark.put(LINEAGE_EQUIP_ID, LineageResource.put);
		Spark.delete(LINEAGE_EQUIP_ID, LineageResource.delete);

		Spark.get(LINEAGE_UPDATE, LineageResource.get);
		Spark.post(LINEAGE_UPDATE, LineageResource.post);
		Spark.put(LINEAGE_UPDATE, LineageResource.updateLineage);
		Spark.delete(LINEAGE_UPDATE, LineageResource.delete);
		
		Spark.get(LINEAGE_VERSION_URI, LineageRecreationResource.get);
		Spark.post(LINEAGE_VERSION_URI, LineageRecreationResource.versionLineage);
		Spark.put(LINEAGE_VERSION_URI, LineageRecreationResource.put);
		Spark.delete(LINEAGE_VERSION_URI, LineageRecreationResource.delete);

		Spark.get(LINEAGE_REEXECUTION_URI, LineageRecreationResource.get);
		Spark.post(LINEAGE_REEXECUTION_URI, LineageRecreationResource.reExecute);
		Spark.put(LINEAGE_REEXECUTION_URI, LineageRecreationResource.put);
		Spark.delete(LINEAGE_REEXECUTION_URI, LineageRecreationResource.delete);
		
		Spark.get(LINEAGE_COPY, LineageRecreationResource.get);
		Spark.post(LINEAGE_COPY, LineageRecreationResource.copyLineage);
		Spark.put(LINEAGE_COPY, LineageRecreationResource.put);
		Spark.delete(LINEAGE_COPY, LineageRecreationResource.delete);
		
		Spark.get(ANALYSIS_COPY_MCT, AnalysisResource.get);
		Spark.post(ANALYSIS_COPY_MCT, AnalysisResource.copyMCT);
		Spark.put(ANALYSIS_COPY_MCT, AnalysisResource.put);
		Spark.delete(ANALYSIS_COPY_MCT, AnalysisResource.delete);

		Spark.get(ANALYSIS_BASE_URI, AnalysisResource.get);
		Spark.post(ANALYSIS_BASE_URI, AnalysisResource.createAnalysis);
		Spark.put(ANALYSIS_BASE_URI, AnalysisResource.put);
		Spark.delete(ANALYSIS_BASE_URI, AnalysisResource.delete);

		Spark.get(ANALYSIS_ID_URI, AnalysisResource.getById);
		Spark.post(ANALYSIS_ID_URI, AnalysisResource.post);
		Spark.put(ANALYSIS_ID_URI, AnalysisResource.put);
		Spark.delete(ANALYSIS_ID_URI, AnalysisResource.delete);
		
		Spark.get(ANALYSIS_SAVE_URI, AnalysisResource.get);
		Spark.post(ANALYSIS_SAVE_URI, AnalysisResource.saveAnalysis);
		Spark.put(ANALYSIS_SAVE_URI, AnalysisResource.put);
		Spark.delete(ANALYSIS_SAVE_URI, AnalysisResource.delete);
		
		// REPORTING EVENTS
		Spark.post(REPORTING_EVENT_BY_STUDY_ID_URI, ReportingEventResource.getReportingEventsByStudyIds);
		Spark.put(REPORTING_EVENTS_RELEASE_STATUS_URI, ReportingEventResource.putReleaseStatus); 
		Spark.post(REPORTING_EVENTS_LIST_URI, ReportingEventResource.getList);
		Spark.post(REPORTING_EVENTS_BASE_URI, ReportingEventResource.post);
		Spark.post(REPORTING_EVENTS_ID_URI, BaseResource.post); // NOT IMPLEMENTED
		Spark.put(REPORTING_EVENTS_BASE_URI, BaseResource.put); // NOT IMPLEMENTED
		Spark.put(REPORTING_EVENTS_PUBLISH_URI, ReportingEventResource.publish);

		// REPORTING EVENT ITEMS
		Spark.post(REPORTING_EVENT_ITEMS_BASE_LIST_URI, ReportingEventItemResource.getList);
		Spark.post(REPORTING_EVENT_ITEMS_BASE_URI, ReportingEventItemResource.post);
		//Spark.post(REPORTING_EVENT_ITEMS_ID_URI, BaseResource.post); // NOT IMPLEMENTED
		//Spark.put(REPORTING_EVENT_ITEMS_BASE_URI, BaseResource.put); // NOT IMPLEMENTED

		// PUBLISHING EVENTS
		Spark.post(PUBLISHING_EVENTS_BASE_LIST_URI, PublishEventResource.getList);
		Spark.post(PUBLISHING_EVENTS_BASE_URI, PublishEventResource.post);
		Spark.post(PUBLISHING_EVENTS_ID_URI, BaseResource.post); // NOT IMPLEMENTED
		Spark.put(PUBLISHING_EVENTS_BASE_URI, BaseResource.put); // NOT IMPLEMENTED

		// PUBLISHING EVENT ITEMS
		Spark.post(PUBLISHING_EVENT_ITEMS_BASE_LIST_URI, PublishItemResource.getList);
		Spark.post(PUBLISHING_EVENT_ITEMS_BASE_URI, PublishItemResource.post);
		Spark.post(PUBLISHING_EVENT_ITEMS_ID_URI, BaseResource.post); // NOT IMPLEMENTED
		Spark.put(PUBLISHING_EVENT_ITEMS_BASE_URI, BaseResource.post); // NOT IMPLEMENTED
		Spark.put(PUBLISHING_EVENT_ITEMS_PUBLISH_STATUS_URI, PublishItemResource.putPublishStatus); // (Publish/UnPublish)
																									// are the only two
																									// statuses for now
		Spark.put(BULK_PUBLISH_STATUS, PublishItemResource.putMultiplePublishStatus);
		// PIMS
		Spark.get(PIMS_DOSEP_VIA_STUDY_ID_URI, PimsResource.getDosepViaStudyId);
		Spark.post(PIMS_DATA_LOAD_VIA_STUDY_ID_URI, PimsResource.postDataLoad);
		Spark.get(PIMS_STATUS_FOR_STUDY, PimsResource.studyPIMSStatus);
		 
		Spark.get(AUDIT_TRAIL_REPORT_URI, ReportResource.createATR);
		Spark.get(ANALYSIS_QC_REPORT_URI, ReportResource.createAnalysisQCReport);
		
		Spark.get(BASE_URI + "/reportingEvent/:reId/reports", ReportResource.getReportingEventReports);
		Spark.get(BASE_URI + "/analysis/:analysisId/reports", ReportResource.getAnalysisReports);
		
		Spark.get(BASE_URI + "/legacyParameters/:paramsId/publishedViewFilterCriteria", LegacyParameters.getPublishViewFilterCriteria);
		
		Spark.post(BATCH_BASE_URI, BatchResource.createBatch);
		Spark.get(BATCH_BASE_URI + "/:id", BatchResource.getBatchById);
	}
}
