package com.pfizer.pgrd.equip.services.notification.client;

import java.util.Date;
import java.util.List;

public class event_detail {

	private boolean system_initiated = false;
	private String user_name;
	private Date requested_qc_dueDate;
	private Date publishing_event_expiration_date;
	private String reporting_event_type;
	private String reporting_event_id;
	private String parameter_data_qc_status;
	private String concentration_data_status;
	private String data_status;
	private String blinding_status;
	private String analyst_name;
	private String qc_status;
	private Long number_record_data_load;
	private Long number_subjects_data_load;
	private Long number_skipped_records_data_load;
	private String validation_warnings;
	private List<String> comments;
	private String validation_details;
	private List<String> error_message;
	

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public boolean isSystem_initiated() {
		return system_initiated;
	}

	public void setSystem_initiated(boolean system_initiated) {
		this.system_initiated = system_initiated;
	}

	public Date getRequested_qc_dueDate() {
		return requested_qc_dueDate;
	}

	public void setRequested_qc_dueDate(Date requested_qc_dueDate) {
		this.requested_qc_dueDate = requested_qc_dueDate;
	}

	public String getReporting_event_type() {
		return reporting_event_type;
	}

	public void setReporting_event_type(String reporting_event_type) {
		this.reporting_event_type = reporting_event_type;
	}

	public String getReporting_event_id() {
		return reporting_event_id;
	}

	public void setReporting_event_id(String reporting_event_id) {
		this.reporting_event_id = reporting_event_id;
	}

	public String getParameter_data_qc_status() {
		return parameter_data_qc_status;
	}

	public void setParameter_data_qc_status(String parameter_data_qc_status) {
		this.parameter_data_qc_status = parameter_data_qc_status;
	}

	public String getConcentration_data_status() {
		return concentration_data_status;
	}

	public void setConcentration_data_status(String concentration_data_status) {
		this.concentration_data_status = concentration_data_status;
	}

	public String getData_status() {
		return data_status;
	}

	public void setData_status(String data_status) {
		this.data_status = data_status;
	}

	public String getBlinding_status() {
		return blinding_status;
	}

	public void setBlinding_status(String blinding_status) {
		this.blinding_status = blinding_status;
	}

	public String getAnalyst_name() {
		return analyst_name;
	}

	public void setAnalyst_name(String analyst_name) {
		this.analyst_name = analyst_name;
	}

	public String getQc_status() {
		return qc_status;
	}

	public void setQc_status(String qc_status) {
		this.qc_status = qc_status;
	}

	public Long getNumber_record_data_load() {
		return number_record_data_load;
	}

	public void setNumber_record_data_load(Long number_record_data_load) {
		this.number_record_data_load = number_record_data_load;
	}

	public Long getNumber_subjects_data_load() {
		return number_subjects_data_load;
	}

	public void setNumber_subjects_data_load(Long number_subjects_data_load) {
		this.number_subjects_data_load = number_subjects_data_load;
	}

	public Long getNumber_skipped_records_data_load() {
		return number_skipped_records_data_load;
	}

	public void setNumber_skipped_records_data_load(Long number_skipped_records_data_load) {
		this.number_skipped_records_data_load = number_skipped_records_data_load;
	}

	public Date getPublishing_event_expiration_date() {
		return publishing_event_expiration_date;
	}

	public void setPublishing_event_expiration_date(Date publishing_event_expiration_date) {
		this.publishing_event_expiration_date = publishing_event_expiration_date;
	}

	public String getValidation_warnings() {
		return validation_warnings;
	}

	public void setValidation_warnings(String validation_warnings) {
		this.validation_warnings = validation_warnings;
	}

	public List<String> getComments() {
		return comments;
	}

	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	public String getValidation_details() {
		return validation_details;
	}

	public void setValidation_details(String validation_details) {
		this.validation_details = validation_details;
	}

	public List<String> getError_message() {
		return error_message;
	}

	public void setError_message(List<String> error_message) {
		this.error_message = error_message;
	}


	
}
