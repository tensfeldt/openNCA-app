package com.pfizer.equip.computeservice.sql;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pfizer.equip.computeservice.exception.ComputeException;

/**
 * This is a base class for Sql classes wishing to use the DbUtils package
 * with a ResultSetHandler to retrieve Lists of objects.  This base class
 * also interacts with the CacheManager if the Sql sub-classes wish to put
 * the results into specific caches managed by Ehcache.
 * 
 * This class is parameterized <T> to allow the sub-classes to declare
 * the specific type of objects that can be put into the List.  The DbUtils
 * package requires us to use the *Impl classes, as they can be instantiated,
 * as opposed to the Interface.  For example, ContainerImpl as the declared
 * Type <T> in the ContainerSql sub-class, as opposed to the Container
 * interface.
 * 
 * @author MeccaRA
 *
 * @param <T>
 */
public class BaseResultSetHandlerSql<T> {
	
	protected static final Log log = LogFactory.getLog(BaseResultSetHandlerSql.class);
	
	protected final Connection conn;

	protected final ResultSetHandler<List<T>> listHandler;
			
	/**
	 * @param conn	the database Connection
	 * @param	handler	the ResultSetHandler naming the specific DTO "Impl" class; 
	 * 					under the hood, the	"Impl" class will be substituted for <T> 
	 * 					in the List<T> during operations of the class
	 */
	public BaseResultSetHandlerSql(Connection conn, ResultSetHandler<List<T>> handler) {
		this.conn = conn;
		this.listHandler = handler;
	}
					
  /**
   * Returns the list of objects
   * 
   * @param sql  the SQL statement to run
   * @return  List<U> the set of objects found; will return an empty list if the query does
   *          not return any rows, not a NULL reference
   * @throws ComputeException
   */
  protected List<T> getList(String sql) 
      throws ComputeException
  {
    return getList(sql, null);
  }
  	
	/**
	 * This method first checks the cache for the List if that has been requested.  
	 * If the Element is not in the cache, or the value is NULL, or the List is empty, 
	 * then we'll go to the database and initialize the cache; else, return the List 
	 * in the cache.
	 *
	 * @param sql	the SQL statement to run
	 * @param params	any substitution parameters that belong with the SQL statement
	 * @param cacheKey	the cache key value indicating which element the resulting list belongs to
	 * @param checkCacheFirst	TRUE to look in the cache first; else, go right to the database
	 * @return	List<U>	the set of objects found; will return an empty list if the query does
	 * 					not return any rows, not a NULL reference
	 * @throws ComputeException
	 */
	protected List<T> getList(String sql, Object[] params) 
			throws ComputeException {
		
		List<T> list = null;
				
		QueryRunner queryRunner = new QueryRunner(true);
		try {
			list = queryRunner.query(conn, sql, listHandler, params);
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
		return list;
	}

	/**
	 * Convenience method for running a query which returns a Long; the DbUtils
	 * ScalarHandler returns a BigDecimal, so the Long value needs to be extracted
	 * from it
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("num_rows", "id", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	Long	the Long value returned from the query
	 * @throws ComputeException
	 */
	public Long getLong(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {			
			
			BigDecimal bd = queryRunner.query(	conn, 
												sql,												
												new ScalarHandler<BigDecimal>(columnLabel),
												params );
			if(bd != null) {
				return bd.longValue();
			}
			return null;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
	}

	/**
	 * Convenience method for running a query which returns a collection of Longs for
	 * use as internal PK ids; can be used to generate 'n' number of ids; the DbUtils
	 * ScalarHandler returns a BigDecimal, so the Long value needs to be extracted
	 * from it
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query ("num_rows", "id", for example)
	 * @param params	the array of parameters supplied to the query
	 * @param	numberOfLongsToGet	the number of long values to retrieve
	 * @return	Long	the Long value returned from the query
	 * @throws ComputeException
	 */
	public Long[] getLongs(String sql, String columnLabel, Object[] params, int numberOfLongsToGet) 
			throws ComputeException {
		
		Long[] returnIds = new Long[numberOfLongsToGet];
					
		for(int i = 0; i < numberOfLongsToGet; i++) {
			Long l = getLong(sql, columnLabel, params);
			returnIds[i] = l;
		}

		return returnIds;
					
	}

	/**
	 * Convenience method for running a query which returns a Long to be used as 
	 * the PK for a AAA table; assumes no parameters for the query and a column
	 * label "id" as the alias for the "xxx_seq.nextval"
	 * 
	 * the DbUtils ScalarHandler returns a BigDecimal, so the Long value needs 
	 * to be extracted from it
	 * 
	 * @param sql	the SQL statement to run
	 * @return	Long	the Long value returned from the query
	 * @throws ComputeException
	 */
	public Long getId(String sql) 
			throws ComputeException {
		
		return this.getLong(sql, "id", null);
					
	}

	/**
	 * Convenience method for running a query which returns an array of Longs
	 * to be used as PKs for an AAA table; assumes no parameters for the query 
	 * and a column label "id" as the alias for the "xxx_seq.nextval"
	 * 
	 * the DbUtils ScalarHandler returns a BigDecimal, so the Long value needs 
	 * to be extracted from it
	 * 
	 * @param sql	the SQL statement to run
	 * @param	numberOfLongsToGet	the number of ids to retrieve
	 * @return	Long[]	the array of Long values returned from the query
	 * @throws ComputeException
	 */
	public Long[] getIds(String sql, int numberOfLongsToGet) 
			throws ComputeException {
		
		return this.getLongs(sql, "id", null, numberOfLongsToGet);
					
	}

	/**
	 * Convenience method for running a query which returns a collection of longs in
	 * a single SQL statement; the DbUtils ColumnListHandler can manage a collection
	 * of BigDecimals, so the Long values needs to be extracted from it
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("num_rows", "provider_id", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	Long[]	the array of Long values returned from the query
	 * @throws ComputeException
	 */
	public long[] getLongs(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {
			List<BigDecimal> ids = queryRunner.query(
									conn, sql,
									new ColumnListHandler<BigDecimal>(columnLabel),
									params);
			
			long[] returnIds = new long[ids.size()];
			for(int i = 0; i < ids.size(); i++) {
				returnIds[i] = ids.get(i).longValue();
			}
			return returnIds;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}					
	}

	/**
	 * Convenience method for running a query which returns 
	 * a String from a Clob column using the DbUtils ScalarHandler
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("my_string_column", "myString", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	String	the String value returned from the query
	 * @throws ComputeException
	 */
	public String getStringFromClobColumn(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {			
			
			String returnString = null;
			
			Clob c = queryRunner.query(	conn, 
																	sql,												
																	new ScalarHandler<Clob>(columnLabel),
																	params );
			if(c != null && c.length() > 0) {
				returnString = c.getSubString(1L, (int)c.length());
			}
			
			return returnString;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
	}

	/**
	 * Convenience method for running a query which returns 
	 * a list of String from a Clob column using the DbUtils 
	 * ColumnListHandler
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("my_string_column", "myString", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	List<String>	the list of String values returned from the query
	 * @throws ComputeException
	 */
	public List<String> getStringsFromClobColumn(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {			
			
			List<String> returnStrings = new ArrayList<String>();
			
			List<Clob> cList = queryRunner.query(	conn, 
																				sql,												
																				new ColumnListHandler<Clob>(columnLabel),
																				params );
			for(Clob c : cList)
			{
				if(c != null && c.length() > 0) {
					String s = c.getSubString(1L, (int)c.length());
					returnStrings.add(s);
				}				
			}
			
			return returnStrings;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
	}

	/**
	 * Convenience method for running a query which returns 
	 * a String from a column using the DbUtils ScalarHandler
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("my_string_column", "myString", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	String	the String value returned from the query
	 * @throws ComputeException
	 */
	public String getStringFromColumn(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {			
						
			String s = queryRunner.query(	conn, 
											sql,												
											new ScalarHandler<String>(columnLabel),
											params );			
			return s;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
	}

  /**
   * Convenience method for running a query which returns 
   * a String from a column using the DbUtils ScalarHandler
   * 
   * @param sql the SQL statement to run
   * @param columnLabel the name of the column returned by the query 
   *            ("my_string_column", "myString", for example)
   * @param params  the array of parameters supplied to the query
   * @return  List<String>  the list of Strings returned from the query
   * @throws ComputeException
   */
  public List<String> getStringsFromColumn(String sql, String columnLabel, Object[] params) 
      throws ComputeException {
    
    QueryRunner queryRunner = new QueryRunner(true);
    try {     
            
      List<String> s = queryRunner.query( conn, 
                      sql,                        
                      new ColumnListHandler<String>(columnLabel),
                      params );     
      return s;
      
    } catch (SQLException e) {
      log.error(e);
      throw new ComputeException(e);
    }
    
  }

	/**
	 * Convenience method for running a query which returns the attributes
	 * of a java.sql.Struct from a column using the DbUtils ScalarHandler
	 * 
	 * @param sql	the SQL statement to run
	 * @param columnLabel	the name of the column returned by the query 
	 * 						("my_string_column", "myString", for example)
	 * @param params	the array of parameters supplied to the query
	 * @return	Object[]	the array of Object values returned from the query;
	 * 						will return an empty array if no data was found
	 * @throws ComputeException
	 */
	public Object[] getStructureFromColumn(String sql, String columnLabel, Object[] params) 
			throws ComputeException {
		
		Object[] returnObjects = null;
		
		QueryRunner queryRunner = new QueryRunner(true);
		try {			
						
			java.sql.Struct s = queryRunner.query(	conn, 
													sql,												
													new ScalarHandler<java.sql.Struct>(columnLabel),
													params );
			
			if(s != null) {
				returnObjects = s.getAttributes();
			}
			else {
				returnObjects = new Object[0];
			}
			return returnObjects;
			
		} catch (SQLException e) {
			log.error(e);
			throw new ComputeException(e);
		}
		
	}
	
}
