package com.pfizer.pgrd.equip.dataframeservice.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Props {
	private static Properties props = new Properties();
	public static final String LINEAGE_DATA_SOURCE_ELASTIC = "ELASTIC", LINEAGE_DATA_SOURCE_MODESHAPE = "MODESHAPE";

	public static void setProps( Properties props ){ Props.props = props; }	
	public static void addProperties( Map<String, String> map ){ props.putAll( map ); }
	
	public static String getPIMSServerNameAndPort(){ return props.getProperty( "pims_server_name_and_port" ); }
	public static String getServiceAccountUser2(){ return props.getProperty( "service_account_user2" ); }
	public static String getServiceAccountPassword2(){ return props.getProperty( "service_account_password2" ); }
	public static String getPIMSLocationType(){ return props.getProperty( "pims_location_type", "Production" ); }
	public static String getCDSServiceName(){ return props.getProperty( "cds_service_name" ); }
	public static String getComputeServiceServer(){ return props.getProperty( "compute_service_server" ); }
	public static String getCRFComputeServiceScriptValue(){ return props.getProperty( "crf_compute_service_script_value" ); }
	public static String getPIMSOAuthEndpoint(){ return props.getProperty( "pims_oauth_endpoint" ); }
	public static String getPIMSDataTransformationComputeServiceScriptId(){ return props.getProperty( "pims_data_transformation_compute_service_script_id" ); }
	public static String getOAuthUser(){ return props.getProperty( "oauth_user" ); }
	public static String getOAuthPassword(){ return props.getProperty( "oauth_password" ); }
	public static String getCRFComputeServiceScriptName() { return props.getProperty( "crf_compute_service_script_name", "pims-merge-crfdata.R" ); }
	public static String getDataTransformationComputeServiceScriptName(){ return props.getProperty( "data_transformation_compute_service_script_name", "pims-merge-pkdef.R" ); }	
	public static String getAuditNominalDataChangesScriptName(){ return props.getProperty( "audit_nominal_data_change_script_name", "audit-nominal-data-change.R" ); }	
	public static String getAllowedDataStandards(){return props.getProperty("allowed_pims_data_standards");}
	
	public static int getComputeServicePort(){ return Integer.parseInt( props.getProperty( "compute_service_port" ) ); }
	public static int getAuthorizationSerivcePort(){ return Integer.parseInt( props.getProperty("auth_service_port" ));}
	public static int getOpmetaSerivcePort(){return Integer.parseInt(props.getProperty("opmeta_service_port"));}
	public static int getNotifSerivcePort(){return Integer.parseInt(props.getProperty("notif_service_port"));}
	
	public static String getAuthorizationServiceServer(){ return props.getProperty( "auth_service_server" ); }
	public static String getOpmetaServiceServer(){ return props.getProperty( "opmeta_service_server" ); }
	public static String getNotifServiceServer(){ return props.getProperty( "notif_service_server" ); }
	
	//system ids
	public static String getAuthorizationServiceSystemId(){ return props.getProperty( "auth_service_system_id" ); }
	public static String getNotificationServiceSystemId(){ return props.getProperty( "notif_service_system_id" ); }
	
	public static List<String> getAllowedUpdates() {return Arrays.asList(props.getProperty("allowedupdates").split(","));}
	//order in these two strings must match.  That is item 1 in the first must match item 1 in the second etc.
	public static List<String> getPimsDBLocations(){ return Arrays.asList(props.getProperty( "pims_db_locations", "new-haven,brussels" ).split(","));}

	public static String getPimsNewHavenDatabaseName(){ return props.getProperty( "pims_new_haven_database_name", "pimsnhp.pfizer.com" ); }
	public static String getPimsBrusselsDatabaseName(){ return props.getProperty( "pims_brussels_database_name", "pimsbrup.pfizer.com" ); }
	
	//order in the first string here must match order in the rest of them.  That is item 1 in the data standard must match item 1 in the rest of them.
	public static List<String> getDataStandards(){ return Arrays.asList(props.getProperty( "pims_data_standard", "pds:cdisc" ).split(":"));}
	public static List<String> getDemographyAndVitalSignsDatabaseViews(){ return Arrays.asList(props.getProperty( "pims_demography_and_vital_signs_views", "PI_GRADES_DEM_P,PI_GRADES_VS_P:PI_CDISC_DEM_P,PI_CDISC_VS_P" ).split(":"));}
	public static List<String> getCRFDatabaseViews(){ return Arrays.asList(props.getProperty( "pims_crf_views", "PI_GRADES_DOS_P,PI_GRADES_TRT_P,PI_GRADES_TRT_R,PI_GRADES_PK_P,PIMS_#PK_LAB_INFO:PI_CDISC_DOS_P,PI_CDISC_TRT_P,PI_CDISC_TRT_R,PI_CDISC_PK_P,PIMS_#PK_LAB_INFO" ).split(":"));}
	public static List<String> getCompletionDataViews(){ return Arrays.asList(props.getProperty( "pims_completion_data_views", "PI_GRADES_DSP_P:PI_CDISC_DSP_P" ).split(":"));}
	public static List<String> getDataTransformationViews(){ return Arrays.asList(props.getProperty( "pims_data_transformation_views", "PI_GRADES_DEM_P,PI_GRADES_VS_P,PI_GRADES_DOS_P,PI_GRADES_TRT_P,PI_GRADES_TRT_R,PI_GRADES_PK_P,PIMS_#PK_LAB_INFO,PI_GRADES_DSP_P:PI_CDISC_DEM_P,PI_CDISC_VS_P,PI_CDISC_DOS_P,PI_CDISC_TRT_P,PI_CDISC_TRT_R,PI_CDISC_PK_P,PIMS_#PK_LAB_INFO,PI_CDISC_DSP_P" ).split(":"));}
	//end order
	
	public static boolean isCreateCSV_FilesForPIMS_CRFDataframe() { return Boolean.parseBoolean( props.getProperty( "isCreateCSV_FilesForPIMS_CRFDataframe", "false" ) ); }
	
	public static String getModeShapeServer(){ return props.getProperty( "modeshape_server" ); }
	public static int getModeShapePort(){return Integer.parseInt(props.getProperty("modeshape_port"));}
	public static String getModeShapeUser(){ return props.getProperty( "modeshape_account_user" ); }
	public static String getModeShapePassword(){ return props.getProperty( "modeshape_account_password" ); }
	public static String getModeShapeContext(){ return props.getProperty( "modeshape_context" ); }
	public static String getModeShapeRespository(){ return props.getProperty( "modeshape_repository" ); }
	public static String getModeShapeWorkspace(){ return props.getProperty( "modeshape_workspace" ); }

	/*
	//This flag should always be true in production
	public static boolean isAudit() { return Boolean.parseBoolean( props.getProperty( "is_audit", "true" ) ); }
	public static boolean isAuditParametersPublished() { return Boolean.parseBoolean( props.getProperty( "is_audit_parameters_published", "true" ) ); }
	public static boolean isAuditReportCreationAuditInContextOfAnalysis() {return Boolean.parseBoolean( props.getProperty( "is_audit_report_creation_in_context_of_analysis", "true" ) );}
	
	//This is tested and should always be true now
	public static boolean isAuditAndNotifyOnDataload() { return Boolean.parseBoolean( props.getProperty( "is_audit_and_notify_on_dataload", "true" ) ); }
	public static boolean isAuditOnEntry() { return Boolean.parseBoolean( props.getProperty( "is_audit_on_entry", "true" ) ); }
	*/
	
	public static boolean isAudit() { return true; }
	public static boolean isAuditParametersPublished() { return true; }
	public static boolean isAuditReportCreationAuditInContextOfAnalysis() { return true; }
	
	//This is tested and should always be true now
	public static boolean isAuditAndNotifyOnDataload() { return true; }
	public static boolean isAuditOnEntry() { return true; }
	
	
	public static String getLibraryServiceServer() { return props.getProperty("library_service_server"); }
	public static int getLibraryServicePort() { return Integer.parseInt(props.getProperty("library_service_port")); }
	
	public static String getExternalServicesHost() { return props.getProperty("external_services_host"); }
	public static int getExternalServicesPort() { return Integer.parseInt(props.getProperty("external_services_port")); }

	// Properties for fetching the "nextVal" portion of the EquipId
	public static String getEquipIdDatasourceName() { return props.getProperty("equip_id_datasource_name"); }
	
	public static String getServiceAccountName() { return props.getProperty("service_account_name"); }
	public static String getLineageBreadcrumbSeparator() { return props.getProperty("lineage_breadcrumb_separator"); }
	
	public static String getLineageDataSource() { return props.getProperty("lineage_data_source", Props.LINEAGE_DATA_SOURCE_ELASTIC); }
	
	public static String getParametersScriptName() { return props.getProperty("parameters_script_name", "opennca-kel-calc.R"); }
	
	public static String getComputeErrorRegex() { return props.getProperty("compute_error_regex", "(error|execution halted|execution failed)"); }
	
	public static List<String> getExcludedBreadcrumbScriptNames() {
		String p = props.getProperty("excluded_breadcrumb_script_names");
		List<String> scripts = new ArrayList<>();
		if(p != null) {
			String[] names = p.split(",");
			for(String n : names) {
				scripts.add(n.trim());
			}
		}
		
		return scripts;
	}
	
	public static String toStrings(){
		StringBuffer buf = new StringBuffer();
		for( Object key : props.keySet() ){
			Object value = props.get( key );
			String entry = key.toString() + " = " + value.toString() + "\r\n";
			if( entry.toUpperCase().indexOf( "PASSWORD" ) == -1 ){
				buf.append( entry );
			}
		}
		
		return buf.toString();
	}
}