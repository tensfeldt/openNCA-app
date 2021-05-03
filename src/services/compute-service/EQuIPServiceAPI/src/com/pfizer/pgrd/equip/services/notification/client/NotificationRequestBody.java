package com.pfizer.pgrd.equip.services.notification.client;


import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NotificationRequestBody {
	
//	{"component_name":"test", "entity_id":"232-2323-2323-23-4-", "entity_type":"Dataload", "event_type":"data_loading", "study_id":"A12312", "program_number": "", 
	//	 "event_detail":{"system_initiated": "false", "user_name": "leep45", "comments": "Approved", "requested_qc_due_date": "01/31/2018 15:18:00", "reporting_event_type": "Test_Event_Type",
	//	                "reporting_event_id": "123", "parameter_data_qc_status": "test", "concentration_data_status": "na", "publishing_event_expiration_date": "01/31/2018 15:18:00", "data_status": "test data status",
	//	                "blinding_status": "blinded", "analyst_name": "leep45", "qc_status": "qc status", "number_record_data_load": "123", "number_subjects_data_load": "23", "number_skipped_records_data_load": "5"}}

private String component_name = "Dataframe Service";
private String entity_id;
private String entity_type;
private String event_type;
private String study_id;
private String program_number;
private event_detail event_detail;

public String getComponent_name() {
	return component_name;
}
public void setComponent_name(String component_name) {
	this.component_name = component_name;
}
public String getEntity_id() {
	return entity_id;
}
public void setEntity_id(String entity_id) {
	this.entity_id = entity_id;
}
public String getEntity_type() {
	return entity_type;
}
public void setEntity_type(String entity_type) {
	this.entity_type = entity_type;
}
public String getEvent_type() {
	return event_type;
}
public void setEvent_type(String event_type) {
	this.event_type = event_type;
}
public String getStudy_id() {
	return study_id;
}
public void setStudy_id(String study_id) {
	this.study_id = study_id;
}
public String getProgram_number() {
	return program_number;
}
public void setProgram_number(String program_number) {
	this.program_number = program_number;
}

public event_detail getEventDetail() {
	return event_detail;
}
public void setEventDetail(event_detail eventDetail) {
	this.event_detail = eventDetail;
}



}




