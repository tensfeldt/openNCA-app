package com.pfizer.pgrd.equip.utils;

import java.sql.Connection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;

public abstract class EquipIdCalculator {

	private static final Hashtable<String, String> hashtable = new Hashtable<String, String>() {
		{
			put(Dataframe.DATASET_TYPE.toLowerCase(), "DS");
			put(Dataframe.DATA_TRANSFORMATION_TYPE.toLowerCase(), "DT");
			put(Assembly.DATA_LOAD_TYPE.toLowerCase(), "DL");
			put(Assembly.REPORTING_EVENT_TYPE.toLowerCase(), "RE");
			put("publish item", "PI");
			put("publishing event", "PE");
			put("promotion", "PRM");
			put("qc request", "QC");
			put("qc checklist summary item", "QCSI");
			put("qc workflow item", "QCW");
			put("qc checklist item", "QCCI");
			put("qc request item", "QCI");
			put("reporting event item", "REI");
			put("publishing event item", "PEI");
			put(Dataframe.REPORT_ITEM_TYPE.toLowerCase(), "RI");
			put(Dataframe.REPORT_TYPE.toLowerCase(), "RPT");
			put(Dataframe.SUBSET_TYPE.toLowerCase(), "SUB");
			put(Dataframe.KEL_FLAGS_TYPE.toLowerCase(), "KEL");
			put(Dataframe.PRIMARY_PARAMETERS_TYPE.toLowerCase(),"PPRM");
			put(Dataframe.DERIVED_PARAMETERS_TYPE.toLowerCase(), "DPRM");
			put(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE.toLowerCase(), "MCT");
			put(Dataframe.SECONDARY_CONFIGURATION_TEMPLATE_TYPE.toLowerCase(), "SCT");
			put("analysis dataframe", "ADF");
			put("primary analysis", "PA");
			put("seconardy analysis", "SA");
			put("analysis", "AN");
			put(Dataframe.PROFILE_SETTINGS_TYPE.toLowerCase(), "PS");
		}
	};

	public static String calculate(String objectType) {
		Connection conn = null;
		String returnVal = null;
		
		try{
			String value = hashtable.get(objectType.toLowerCase());
			String prefix = (value == null) ? "" : value;
			
			//"jdbc:oracle:thin:@AMRVOP000005044.pfizer.com:63000:IA005044" 
			Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup("java:/MYEQUIP");
            conn = ds.getConnection("EQUIP_OWNER","EQUIP#01");
			
			returnVal = prefix + UtilsSQL.nextVal(	conn, 
													"DATAFRAME_EQUIPID_SEQ");
		}
		catch( Exception ex ){
			System.out.println(ex);
			throw new RuntimeException( ex );
		}
		finally{
			UtilsSQL.close(conn);
		}
		
		return returnVal;
	}

}