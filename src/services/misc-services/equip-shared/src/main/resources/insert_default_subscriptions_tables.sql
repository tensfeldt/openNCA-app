insert into security_role_event_type (security_role_id,event_type_id)
select * from (
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='user_disablement') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='user_enablement') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='scripts_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SYSADMIN' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request_result') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CAG' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request_result') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='qc_request_sent')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='qc_assigned_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='qc_complete')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='qc_report_generated')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='reporting_event_published')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='release')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='reopen')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='parameter_data_change')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='analysis_change')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='review_ready')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='scripts_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='PKA' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='CUSTOM_DATA_LOADER' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='qc_request_sent')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='qc_assigned_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='qc_complete')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='qc_report_generated')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='scripts_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='QC_REVIEWER' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='scripts_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='LIBRARIAN' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='user_disablement') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='user_enablement') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='data_loading') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='global_library_promotion_request_result') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='qc_request_sent')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='qc_assigned_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='qc_complete')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='qc_report_generated')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='reporting_event_published')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='release')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='reopen')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='parameter_data_change')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='analysis_change')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='review_ready')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='reporting_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='qc_templates_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='scripts_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='SUPER_USER' and sec.system_key='nca' and et.event_type_name ='documents_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_ADMIN' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_ADMIN' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_ADMIN' and sec.system_key='nca' and et.event_type_name ='add_remove_data_authorization_self')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_ADMIN' and sec.system_key='nca' and et.event_type_name ='global_library_additions')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_VALIDATOR' and sec.system_key='nca' and et.event_type_name ='outage') union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_VALIDATOR' and sec.system_key='nca' and et.event_type_name ='data_validation')union
(select sec.security_role_id,et.event_type_id from security_role sec,event_type et where sec.role_name='DATA_VALIDATOR' and sec.system_key='nca' and et.event_type_name ='global_library_additions')
);

commit;


