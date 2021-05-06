package com.pfizer.pgrd.equip.dataframeservice.pims;

import java.util.HashMap;
import java.util.Map;

import com.pfizer.pgrd.equip.dataframeservice.util.Const;

public class PimsDataTransComputeStdout {
	private Map<String,String> map = new HashMap<String,String>();
	private String errorMessage;
	private String json;
	private String numberRecordsPkDef;
	private String numberRecordsLoaded;
	private String numberRecordsSkipped;
	private String numberSubjectsDataLoad;
	
	public String toString(){
		return "" + map.toString() + ", errorMessage=" + errorMessage + ", json=" + json; 
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	
	public void put( String key, String value ){
		switch( key ){
			case "Number of records in PK Def" : 
				key = Const.PIMS_DATA_LOAD_TOTAL_RECORDS_PROCESSED; //"Total records processed";
				setNumberRecordsPkDef(value);
				break;
			case "Number of records matched" : 
				key = Const.PIMS_DATA_LOAD_TOTAL_RECORDS_LOADED; //"Total records loaded";
				setNumberRecordsLoaded(value);
				break;
			case "Number of records unmatched" : 
				key = Const.PIMS_DATA_LOAD_TOTAL_RECORDS_SKIPPED; //"totalRecordsSkipped";
				setNumberRecordsSkipped(value);
				break;
			case "Number of subjects in PK Def" : 
				key = Const.PIMS_DATA_LOAD_TOTAL_SUBJECTS_PROCESSED; //"Total subjects processed";
				setNumberSubjectsDataLoad(value);
				break;
			default: throw new IllegalStateException("invalid key " + key + " loaded into PIMSDataTransComputStdout.put()");
		}

		map.put(key, value);
	}
	
	public Map<String,String> getMap(){
		return map;
	}

	public String getNumberRecordsPkDef() {
		return numberRecordsPkDef;
	}

	public void setNumberRecordsPkDef(String numberRecordsPkDef) {
		this.numberRecordsPkDef = numberRecordsPkDef;
	}

	public String getNumberRecordsLoaded() {
		return numberRecordsLoaded;
	}

	public void setNumberRecordsLoaded(String numberRecordsLoaded) {
		this.numberRecordsLoaded = numberRecordsLoaded;
	}

	public String getNumberRecordsSkipped() {
		return numberRecordsSkipped;
	}

	public void setNumberRecordsSkipped(String numberRecordsSkipped) {
		this.numberRecordsSkipped = numberRecordsSkipped;
	}

	public String getNumberSubjectsDataLoad() {
		return numberSubjectsDataLoad;
	}

	public void setNumberSubjectsDataLoad(String numberSubjectsDataLoad) {
		this.numberSubjectsDataLoad = numberSubjectsDataLoad;
	}
}
