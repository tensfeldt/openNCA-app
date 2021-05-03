package com.pfizer.equip.computeservice.dao;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.dbutils.QueryLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pfizer.equip.computeservice.exception.ComputeDataAccessException;


public class DaoQueryLoader {

    // Log file initializations...
    private static final Log log = LogFactory.getLog(DaoQueryLoader.class);
    
    // This is how DbUtils needs the path to be formatted
    private final static String daoPath = "/com/pfizer/equip/computeservice/sql/";
    
    private static Map<String, String> queryMap = new TreeMap<String, String>();

	private DaoQueryLoader() {};
		
	public static void loadQueries() 
			throws ComputeDataAccessException {

		try {
			QueryLoader ql = QueryLoader.instance();
			//
			queryMap.putAll(ql.load(daoPath + "ComputeDataAccessSql.sql"));
			
			
			for(Map.Entry<String, String> entry : queryMap.entrySet()) {
				log.info(entry.getKey());
				log.info(entry.getValue());
				log.info("-------------------------");
			}
		} catch (IOException e) {
			log.error(e);
			throw new ComputeDataAccessException(e);
		}
		
	}
	
	/**
	 * 
	 * @param query	the query name you need to find
	 * @return	String	the SQL string mapped to the query name; 
	 * 					if not in the map, returns NULL
	 * @throws ComputeDataAccessException 
	 */
	public static String getQuery(String query) throws ComputeDataAccessException {
		DaoQueryLoader.loadQueries();
		return queryMap.get(query);
	}
	
	public static void main(String[] args) {
		try {
			DaoQueryLoader.loadQueries();
		} catch (ComputeDataAccessException e) {
			e.printStackTrace();
		}
		finally {
			System.exit(0);
		}
	}

}
