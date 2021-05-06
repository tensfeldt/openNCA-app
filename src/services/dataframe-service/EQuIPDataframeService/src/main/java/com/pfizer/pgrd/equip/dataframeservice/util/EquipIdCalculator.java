package com.pfizer.pgrd.equip.dataframeservice.util;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;

public abstract class EquipIdCalculator {

	private static final Logger LOGGER = LoggerFactory.getLogger(EquipIdCalculator.class);

	private static final Map<String, String> hashtable = new HashMap<String, String>() {
		{
			put(Dataframe.DATASET_TYPE.toLowerCase(), "DS");
			put(Dataframe.DATA_TRANSFORMATION_TYPE.toLowerCase(), "DT");
			put(Assembly.DATA_LOAD_TYPE.toLowerCase(), "DL");
			put(Assembly.REPORTING_EVENT_TYPE.toLowerCase(), "RE");
			put(Assembly.BATCH_TYPE.toLowerCase(), "BAT");
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
			put(Dataframe.PRIMARY_PARAMETERS_TYPE.toLowerCase(), "PPRM");
			put(Dataframe.DERIVED_PARAMETERS_TYPE.toLowerCase(), "DPRM");
			put(Dataframe.MODEL_CONFIGURATION_TEMPLATE_TYPE.toLowerCase(), "MCT");
			put(Dataframe.SECONDARY_CONFIGURATION_TEMPLATE_TYPE.toLowerCase(), "SCT");
			put(Dataframe.CONCENTRATION_DATA_TYPE.toLowerCase(), "CONCD");
			put(Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE.toLowerCase(), "CEST");
			put("analysis dataframe", "ADF");
			put("primary analysis", "PA");
			put("seconardy analysis", "SA");
			put("analysis", "AN");
			put(Dataframe.ATTACHMENT_TYPE.toLowerCase(), "ATT");
		}
	};
	
	private EquipIdCalculator() {
	}
	
	public static String calculate(String objectType) {
		return EquipIdCalculator.calculate(objectType, false);
	}

	public static String calculate(String objectType, boolean isCopy) {
		Connection conn = null;
		String returnVal = null;

		try {
			String value = hashtable.get(objectType.toLowerCase());
			String prefix = (value == null) ? "" : value;
			if(isCopy) {
				prefix = "C-" + prefix;
			}
			
			returnVal = prefix + System.currentTimeMillis();
			
			
			// Check for a NULL up front, just in case
			if (Props.getEquipIdDatasourceName() == null) {
				String msg = "Could not create the EquipId because the datasource property is null.";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}
			
			// These steps work for either Oracle or SQLite (or any database)
			Context context = new InitialContext();
			DataSource ds = (DataSource) context.lookup(Props.getEquipIdDatasourceName());
			
			// Most likely we will have an Exception thrown before this would be evaluated
			// to TRUE, but just in case...
			if (ds == null) {
				String msg = "Could not create the EquipId because the datasource [" + Props.getEquipIdDatasourceName()
						+ "] could not be found.";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}

			conn = ds.getConnection();
			// Most likely we will have an Exception thrown before this would be evaluated
			// to TRUE, but just in case...
			if (conn == null) {
				String msg = "Could not create the EquipId because a connection from the ["
						+ Props.getEquipIdDatasourceName() + "] datasource could not be provided.";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}

			// Extract the database "flavor" from the connection string
			// The string will be something like "jdbc:oracle:..." or "jdbc:sqlite:..."
			String dbUrl = conn.getMetaData().getURL();
			LOGGER.debug(dbUrl);
			// Unlikely if we made it this far, but just in case...
			if (dbUrl == null) {
				String msg = "Could not create the EquipId because the database type was not available from the connection.";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}

			// Now fetch the "nextVal" based on the specific database "flavor"
			QueryRunner qr = new QueryRunner(true);
			// This will successfully capture an Integer and a Long in DbUtils
			Number nextVal = null;

			// Note for possible future implementation:
			//
			// In a subsequent iteration of the service, it might be worthwhile to have a
			// common implementation
			// at the database layer so that a re-usable method for fetching the "nextVal"
			// can be used across
			// all database platforms.
			//
			// For example, Oracle and SQLite do things differently with regard to
			// sequences: in Oracle, you
			// can have a stand-alone sequence and select the nextVal from it; in SQLite, a
			// sequence managed
			// by the database can only be attached to a table.
			//
			// Theoretically, the least common denominator across Oracle, SQLite, and other
			// embedded databases
			// would be something like this: a table with an "ID" column controlled by a
			// Sequence (in Oracle,
			// that's a Sequence *and* a Trigger), and a dummy, 1-character null-able column
			// ("EMPTY_FIELD").
			// This way, a common SQL statement such as "insert into dataframe_equip_id
			// (empty_field) values (null)"
			// could work across all databases, as they all would return the PK value. (It
			// turns out that not every
			// database accommodates the following syntax: "insert into dataframe_equip_id
			// default values".)
			//
			// In order to do this in Oracle, we need a Table, Sequence, and Trigger.
			//
			// nextVal = qr.insert(conn, "insert into dataframe_equip_id (empty_field)
			// values (null)", new ScalarHandler<Number>());
			//
			// If we actually implemented it with the database as the common denominator, we
			// would not need to
			// fetch the dbUrl above nor do the follow-on test for the database "flavor".
			//
			if (dbUrl.toLowerCase().contains("oracle")) {
				// For Oracle, grab the nextVal from the Sequence
				nextVal = qr.query(conn, "select EQUIP_OWNER.DATAFRAME_EQUIPID_SEQ.nextval from dual",
						new ScalarHandler<Number>());
			} else if (dbUrl.toLowerCase().contains("sqlite")) {
				// For SQLite, we have a table with only a PK column defined as AUTOINCREMENT,
				// so SQLite will manage the Sequence for us; we just INSERT a row and use the
				// generated Key returned to us
				nextVal = qr.insert(conn, "insert into dataframe_equip_id default values", new ScalarHandler<Number>());
			} else {
				String msg = "Could not create the EquipId because the database type was not provided or is invalid ["
						+ dbUrl + "].";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}

			if (nextVal != null) {
				returnVal = prefix + nextVal.longValue();
			} else {
				String msg = "Could not create the EquipId because the next value could not be found.";
				LOGGER.error(msg);
				throw new RuntimeException(msg);
			}
			
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage());
			throw new RuntimeException(ex);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		
		return returnVal;
	}

}