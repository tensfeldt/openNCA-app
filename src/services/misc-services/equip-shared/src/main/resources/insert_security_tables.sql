prompt Uncomment delete lines to refresh
-- delete from security_role;
-- delete from security_group_access;
-- delete from security_group;
-- delete from security_role_priv;
-- delete from security_priv;

-- Create roles
-- Map to actual AD groups for dev, test, and prod for NCA
prompt Creating roles
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-Logon', 'LOGON');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-SystemAdmin', 'SYSADMIN');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-CAG', 'CAG');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-PKA', 'PKA');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-DataLoader', 'DATA_LOADER');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-CustomDataLoader', 'CUSTOM_DATA_LOADER');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-QCReviewer', 'QC_REVIEWER');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-Librarian', 'LIBRARIAN');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-SuperUser', 'SUPER_USER');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-DataAdminSupport', 'DATA_ADMIN');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-StandaloneDataValidator', 'DATA_VALIDATOR');
insert into security_role (system_key, external_group_name, role_name) values ('nca', 'EQUIP-NCA-DataReviewer', 'DATA_REVIEWER');

insert into  security_role (system_key, external_group_name, role_name)  values ('mas', 'COD_Users', 'LOGON');
insert into  security_role (system_key, external_group_name, role_name)  values ('mas', 'SPLUNKApp_Other_Group', 'CAG');
insert into  security_role (system_key, external_group_name, role_name)  values ('mas', 'SPLUNKApp_Netspeed_Group', 'PKA');
insert into  security_role (system_key, external_group_name, role_name) values ('mas', 'SPLUNKNoCloud_Group', 'LIBRARIAN');

-- Create groups
prompt Creating groups
insert into security_group (external_group_name, group_name) values ('SPLUNKApp_Netspeed_Group', 'A0011001_ADMIN');

-- Associate access to groups
prompt Mapping groups to access
insert into security_group_access (security_group_id, entity_id, entity_type, restricted_access_yn, blinded_access_yn) 
select security_group_id, 'A0011001', 'PROTOCOL', 'N', 'N' from security_group where group_name='A0011001';
insert into security_group_access (security_group_id, entity_id, entity_type, restricted_access_yn, blinded_access_yn) 
select security_group_id, 'A0011001', 'PROTOCOL', 'Y', 'Y' from security_group where group_name='A0011001_ADMIN';

-- Create privs
---- NCA
prompt Creating privs in NCA
insert into security_priv (system_key, security_priv_key) values ('nca', 'ADD_ANY_REPORT_ITEM');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ADD_PROTOCOL_CONTAINER');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ADMIN_VALIDATOR');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_ANY_SUBSCRIPTION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_BLINDING');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_OPMETA_REFERENCE');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_PROMOTION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_RESTRICTION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_SELECTION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_DATA_STATUS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_GROUP');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROGRAM');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_ADHOC');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_CONTAINER');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_CONTROL');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_ASSIGNMENT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROGRAM_ASSIGNMENT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_QC_STATUS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_QC_REVIEWER');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_QC_REVIEW_LOCK');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_REPORTING_EVENT_LOCK');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_REPORTING_EVENT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_REPORTING_EVENT_SELF');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_ROLE');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_ROLE_SUBSCRIPTION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ANNOTATE_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ANNOTATE_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'DROP_ANY_REPORT_ITEM');
insert into security_priv (system_key, security_priv_key) values ('nca', 'DROP_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'DROP_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'DROP_USER_ENTITY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'EXPORT_GLOBAL_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'EXPORT_USER_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'LOGON');
insert into security_priv (system_key, security_priv_key) values ('nca', 'PUBLISH_DATA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'QUERY_GLOBAL_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'QUERY_PODS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'QUERY_USER_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RENAME_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RENAME_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'REQUEST_QC_REVIEW');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RESTORE_ANY_ENTITY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RESTORE_USER_ENTITY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_ANALYSIS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_ANALYSIS_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_CONC_PLOTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_CONC_TABLES');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_DATALOAD');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_DATA_TRANSFORM');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_DATA_TRANSFORM_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PARAM_QC_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PK_PARAM_AND_CONC_TIME');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PK_PARAM_PLOTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PK_PARAM_TABLES');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PREQC_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_PUBLISH_DATA_AUDIT_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_SEARCH');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'RUN_VALIDATOR');
insert into security_priv (system_key, security_priv_key) values ('nca', 'SHARE_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'SHARE_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'SHARE_USER_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_ANALYSIS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_ANY_DEPENDENTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_ANY_PROVENANCE');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_AUDIT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_AUTOLOADER_STATUS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_BLINDED');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_COMPLETED_QC_REPORT');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_DATALOAD');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_DATA_TRANSFORM');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_GLOBAL_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_HELP');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_NAVIGATOR');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_NON_PROMOTED');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_OPERATIONAL_REPORTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_PUBLISH_DATA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_USER_DASHBOARD');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_USER_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'VIEW_VISUALIZATION');
insert into security_priv (system_key, security_priv_key) values ('nca', 'WRITE_ANY_CONFIG');
insert into security_priv (system_key, security_priv_key) values ('nca', 'WRITE_GLOBAL_LIBRARY');
insert into security_priv (system_key, security_priv_key) values ('nca', 'WRITE_SEARCH_CRITERIA');
insert into security_priv (system_key, security_priv_key) values ('nca', 'WRITE_SEARCH_RESULTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'WRITE_USER_LIBRARY');
-- additional
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_BLINDING');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_CRF_DATA_STATUS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_SD_COMMENTS');
insert into security_priv (system_key, security_priv_key) values ('nca', 'ALTER_PROTOCOL_ATTACHMENTS');

---- mas
prompt Creating privs in MAS
insert into security_priv (system_key, security_priv_key) values ('mas', 'LOGON');
insert into security_priv (system_key, security_priv_key) values ('mas', 'VIEW_BLINDED');
insert into security_priv (system_key, security_priv_key) values ('mas', 'VIEW_NON_PROMOTED');
insert into security_priv (system_key, security_priv_key) values ('mas', 'WRITE_GLOBAL_LIBRARY');

-- Associate privs to roles
-- LOGON
prompt Mapping roles to privs
prompt Mapping LOGON privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'LOGON' from security_role where role_name='LOGON' and system_key='nca'
);

-- SYSADMIN
prompt Mapping SYSADMIN privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ADD_PROTOCOL_CONTAINER' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ADMIN_VALIDATOR' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_SELECTION' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_OPMETA_REFERENCE' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_GROUP' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ADHOC' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CONTAINER' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CONTROL' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ASSIGNMENT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_BLINDING' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_RESTRICTION' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM_ASSIGNMENT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEW_LOCK' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_STATUS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEWER' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT_LOCK' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_ROLE' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'PUBLISH_DATA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_PODS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'REQUEST_QC_REVIEW' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_ANY_ENTITY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ADD_ANY_REPORT_ITEM' from security_role where role_name='SYSADMIN' and system_key='nca' union
   select system_key, security_role_id, 'DROP_ANY_REPORT_ITEM' from security_role where role_name='SYSADMIN' and system_key='nca' union
   select system_key, security_role_id, 'RUN_CONC_PLOTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_CONC_TABLES' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATALOAD' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PARAM_QC_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_AND_CONC_TIME' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_PLOTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_TABLES' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PREQC_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_VALIDATOR' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANALYSIS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUTOLOADER_STATUS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATALOAD' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_OPERATIONAL_REPORTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_ANY_CONFIG' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_GLOBAL_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='SYSADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='SYSADMIN' and system_key='nca'
);

-- CAG
prompt Mapping CAG privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='CAG' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_DATA_BLINDING' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_PROMOTION' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_STATUS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_OPMETA_REFERENCE' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ASSIGNMENT' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM_ASSIGNMENT' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_ROLE_SUBSCRIPTION' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_BLINDING' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CRF_DATA_STATUS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_PODS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='CAG' and system_key='nca' union
   select system_key, security_role_id, 'RUN_DATALOAD' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_VALIDATOR' from security_role where role_name='CAG' and system_key='nca' union
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUTOLOADER_STATUS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_BLINDED' from security_role where role_name='CAG' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATALOAD' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NON_PROMOTED' from security_role where role_name='CAG' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='CAG' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='CAG' and system_key='nca'
);

-- PKA
prompt Mapping PKA privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='PKA' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_DATA_SELECTION' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ADHOC' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ASSIGNMENT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CRF_DATA_STATUS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_SD_COMMENTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM_ASSIGNMENT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEW_LOCK' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_STATUS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEWER' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT_LOCK' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_ROLE_SUBSCRIPTION' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'PUBLISH_DATA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'REQUEST_QC_REVIEW' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'ADD_ANY_REPORT_ITEM' from security_role where role_name='PKA' and system_key='nca' union
   select system_key, security_role_id, 'DROP_ANY_REPORT_ITEM' from security_role where role_name='PKA' and system_key='nca' union
   select system_key, security_role_id, 'RUN_CONC_PLOTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_CONC_TABLES' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PARAM_QC_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_AND_CONC_TIME' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_PLOTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_TABLES' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PREQC_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANALYSIS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATALOAD' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='PKA' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='PKA' and system_key='nca'
);

-- DATA_LOADER
prompt Mapping DATA_LOADER privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'ADMIN_VALIDATOR' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CONTROL' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_PODS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_DATALOAD' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_VALIDATOR' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUTOLOADER_STATUS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATALOAD' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_NON_PROMOTED' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_BLINDED' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='DATA_LOADER' and system_key='nca'
);

-- CUSTOM_DATA_LOADER
prompt Mapping CUSTOM_DATA_LOADER privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'ADMIN_VALIDATOR' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CONTROL' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_PODS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_DATALOAD' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_VALIDATOR' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUTOLOADER_STATUS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATALOAD' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_NON_PROMOTED' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_BLINDED' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='CUSTOM_DATA_LOADER' and system_key='nca'
);

-- QC_REVIEWER
prompt Mapping QC_REVIEWER privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='QC_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_DATA_SELECTION' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_STATUS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEWER' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEW_LOCK' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT_LOCK' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'ADD_ANY_REPORT_ITEM' from security_role where role_name='QC_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'DROP_ANY_REPORT_ITEM' from security_role where role_name='QC_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_CONC_PLOTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_CONC_TABLES' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PARAM_QC_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_AND_CONC_TIME' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_PLOTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_TABLES' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PREQC_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANALYSIS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='QC_REVIEWER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='QC_REVIEWER' and system_key='nca'
);

-- LIBRARIAN
prompt Mapping LIBRARIAN privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='LIBRARIAN' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='LIBRARIAN' and system_key='nca' union
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_GLOBAL_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='LIBRARIAN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='nca'
);

-- SUPER_USER
prompt Mapping SUPER_USER privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_QC_REVIEWER' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_DASHBOARD' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_HELP' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_GROUP' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ASSIGNMENT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_RESTRICTION' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROGRAM_ASSIGNMENT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_CONTROL' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_ADHOC' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_SELECTION' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANALYSIS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'PUBLISH_DATA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_CONC_TABLES' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_CONC_PLOTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_TABLES' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_PLOTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PARAM_QC_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_DATA_TRANSFORM_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ADD_ANY_REPORT_ITEM' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_ANY_REPORT_ITEM' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_ANALYSIS_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PK_PARAM_AND_CONC_TIME' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_PREQC_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_OPERATIONAL_REPORTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'ANNOTATE_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RENAME_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RUN_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_CRITERIA' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_SEARCH_RESULTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='SUPER_USER' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='SUPER_USER' and system_key='nca'
);

-- DATA_ADMIN
prompt Mapping DATA_ADMIN privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='DATA_ADMIN' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_GROUP' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_DATA_RESTRICTION' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'ALTER_PROTOCOL_RESTRICTION' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_OPERATIONAL_REPORTS' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'SHARE_USER_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'DROP_USER_ENTITY' from security_role where role_name='DATA_ADMIN' and system_key='nca' union 
   select system_key, security_role_id, 'RESTORE_USER_ENTITY' from security_role where role_name='DATA_ADMIN' and system_key='nca'
);

-- DATA_VALIDATOR
prompt Mapping DATA_VALIDATOR privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='DATA_VALIDATOR' and system_key='nca' union
   select system_key, security_role_id, 'RUN_VALIDATOR' from security_role where role_name='DATA_VALIDATOR' and system_key='nca' union 
   select system_key, security_role_id, 'ADMIN_VALIDATOR' from security_role where role_name='DATA_VALIDATOR' and system_key='nca' union 
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='DATA_VALIDATOR' and system_key='nca' union 
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='DATA_VALIDATOR' and system_key='nca' union 
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='DATA_VALIDATOR' and system_key='nca'
);

-- DATA_REVIEWER
prompt Mapping DATA_REVIEWER privs
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'ALTER_ANY_SUBSCRIPTION' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_PROGRAM' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_PROTOCOL' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_PROTOCOL_ASSIGNMENT' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_PROTOCOL_ATTACHMENTS' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT_SELF' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'ALTER_REPORTING_EVENT_LOCK' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'DROP_SEARCH_CRITERIA' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'EXPORT_GLOBAL_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'EXPORT_USER_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'PUBLISH_DATA' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'QUERY_GLOBAL_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'QUERY_USER_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_PUBLISH_DATA_AUDIT_REPORT' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'RUN_SEARCH' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_ANALYSIS' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_ANY_DEPENDENTS' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_ANY_PROVENANCE' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_AUDIT' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_COMPLETED_QC_REPORT' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_DATA_TRANSFORM' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_GLOBAL_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_NAVIGATOR' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_PUBLISH_DATA' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_USER_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'VIEW_VISUALIZATION' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'WRITE_SEARCH_CRITERIA' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
   select system_key, security_role_id, 'WRITE_USER_LIBRARY' from security_role where role_name='DATA_REVIEWER' and system_key='nca' union
);

-- mas
insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'LOGON' from security_role where role_name='LOGON' and system_key='mas'
);

insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'VIEW_BLINDED' from security_role where role_name='CAG' and system_key='mas'
);

insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'VIEW_NON_PROMOTED' from security_role where role_name='CAG' and system_key='mas'
);

insert into security_role_priv (system_key, security_role_id, security_priv_key) select * from (
   select system_key, security_role_id, 'WRITE_GLOBAL_LIBRARY' from security_role where role_name='LIBRARIAN' and system_key='mas'
);

commit;

-- test queries
prompt Displaying results...
set lines 200 pages 5000
col system_key for a10
col role_name for a30
col security_priv_key for a30
col entity_id for a30
col entity_type for a15
col group_name for a30
col external_group_name for a30

prompt Query: Each role and each priv
select sr.system_key, sr.role_name, sr.external_group_name, srp.security_priv_key
from security_role sr, security_role_priv srp
where sr.security_role_id = srp.security_role_id
and sr.system_key         = srp.system_key;

prompt Query: Priv counts per role
select sr.system_key, sr.role_name, count(*) num_privs
from security_role sr, security_role_priv srp
where sr.security_role_id = srp.security_role_id
group by sr.role_name, sr.system_key
order by count(*);

prompt Query: Priv-role mapping table
select distinct * from (
select sp.system_key
, sp.security_priv_key
, sysadmin_privs.sysadmin
, cag_privs.cag
, pka_privs.pka
, data_loader_privs.data_loader
, custom_data_loader_privs.custom_data_loader
, qc_reviewer_privs.qc_reviewer
, librarian_privs.librarian
, super_user_privs.super_user
, data_admin_privs.data_admin
, data_validator_privs.data_validator
  from security_priv sp, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as SYSADMIN
            from security_role sr, security_role_priv srp
           where sr.security_role_id = srp.security_role_id
             and sr.system_key       = srp.system_key
             and sr.role_name        = 'SYSADMIN'
       ) sysadmin_privs,
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as CAG
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'CAG'
       ) cag_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as PKA
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'PKA'
       ) pka_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as DATA_LOADER
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'DATA_LOADER'
       ) data_loader_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as CUSTOM_DATA_LOADER
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'CUSTOM_DATA_LOADER'
       ) custom_data_loader_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as QC_REVIEWER
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'QC_REVIEWER'
       ) qc_reviewer_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as LIBRARIAN
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'LIBRARIAN'
       ) librarian_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as SUPER_USER
           from security_role sr, security_role_priv srp
          where sr.security_role_id = srp.security_role_id
            and sr.system_key       = srp.system_key
            and sr.role_name        = 'SUPER_USER'
       ) super_user_privs, 
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as DATA_ADMIN
            from security_role sr, security_role_priv srp
           where sr.security_role_id = srp.security_role_id
             and sr.system_key       = srp.system_key
             and sr.role_name        = 'DATA_ADMIN'
       ) data_admin_privs,
       ( 
         select sr.system_key, srp.security_priv_key, 'X' as DATA_VALIDATOR
            from security_role sr, security_role_priv srp
           where sr.security_role_id = srp.security_role_id
             and sr.system_key       = srp.system_key
             and sr.role_name        = 'DATA_VALIDATOR'
       ) data_validator_privs
 where sp.system_key        = sysadmin_privs.system_key (+)
   and sp.security_priv_key = sysadmin_privs.security_priv_key (+)
   and sp.system_key        = cag_privs.system_key (+)
   and sp.security_priv_key = cag_privs.security_priv_key (+)
   and sp.system_key        = pka_privs.system_key (+)
   and sp.security_priv_key = pka_privs.security_priv_key (+)
   and sp.system_key        = data_loader_privs.system_key (+)
   and sp.security_priv_key = data_loader_privs.security_priv_key (+)
   and sp.system_key        = custom_data_loader_privs.system_key (+)
   and sp.security_priv_key = custom_data_loader_privs.security_priv_key (+)
   and sp.system_key        = qc_reviewer_privs.system_key (+)
   and sp.security_priv_key = qc_reviewer_privs.security_priv_key (+)
   and sp.system_key        = librarian_privs.system_key (+)
   and sp.security_priv_key = librarian_privs.security_priv_key (+)
   and sp.system_key        = super_user_privs.system_key (+)
   and sp.security_priv_key = super_user_privs.security_priv_key (+)
   and sp.system_key        = data_admin_privs.system_key (+)
   and sp.security_priv_key = data_admin_privs.security_priv_key (+)
   and sp.system_key        = data_validator_privs.system_key (+)
   and sp.security_priv_key = data_validator_privs.security_priv_key (+)
) order by security_priv_key;

prompt Query: Group mapping
select sg.group_name, sg.external_group_name, sgp.entity_id, sgp.entity_type, sgp.blinded_access_yn, sgp.restricted_access_yn
from security_group sg, security_group_access sgp
where sg.security_group_id = sgp.security_group_id;
