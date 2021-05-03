package com.pfizer.pgrd.equip.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

public class UtilsSQL {
//	private static final Log log = LogFactory.getLog(UtilsSQL.class);

	private static Object		criticalSection_		= new Object();
	private static boolean		bIsOracleDriverLoaded_	= false;

	public static Connection getConnection( String strURL,
											String strDriverName,
											String strUser,
											String strPassword ) throws SQLException,
																ClassNotFoundException {
		Connection con = null;

		synchronized( criticalSection_ ) {
			//load the oracle driver once and only once for any given program
			if( ! bIsOracleDriverLoaded_ ) {
				Class.forName( strDriverName );
				bIsOracleDriverLoaded_ = true;
			}
		}

		con = DriverManager.getConnection(	strURL,
											strUser,
											strPassword );
		con.setAutoCommit( false );

		return con;
	}
	/**
	 * @param strURL - example: "jdbc:oracle:thin:@127.0.0.1:1521:ORC2"
	 * 					where 127.0.0.1 is the host, 1521, is the port,
	 *                  and ORC2 is the database name.
	 **/
	public static Connection getOracleThinConnection(	String strURL,
														String strUser,
														String strPassword ) throws SQLException,
																			ClassNotFoundException {
		return getConnection(	strURL,
								"oracle.jdbc.driver.OracleDriver",
								strUser,
								strPassword );
	}
	public static Connection getOracleThinConnection(	String strHost,
														String strPort,
														String strDatabase,
														String strUser,
														String strPassword ) throws SQLException,
																			ClassNotFoundException {
		Connection con = null;

		String strURL = "jdbc:oracle:thin:@" + strHost + ":" + strPort + ":" + strDatabase;

		con = getOracleThinConnection(	strURL,
										strUser,
										strPassword );
		return con;
	}
	
	
	public static String runQuery(	Connection conn,
	                            	String query ) throws Throwable {
		return runQuery(	conn,
		                	query,
		                	new Object[]{} );
	}
	
	public static String runQuery(	Connection conn,
									String query,
									Object ... args ) throws Throwable {
		StringBuffer strBuf = (new QueryRunner( true )).query(	conn,
																query,
																new ResultSetHandler<StringBuffer>() {
																    public StringBuffer handle(ResultSet rs) throws SQLException {
																    	StringBuffer buf = new StringBuffer();
																    	
																    	while( rs.next() ){
																    		for( int i=1; i<=rs.getMetaData().getColumnCount(); i++ ){
																    			buf.append( rs.getString( i ) );
																    			if( i <= rs.getMetaData().getColumnCount()-1 ){
																    				buf.append( ", " );
																    			}
																    			else if( i == rs.getMetaData().getColumnCount() ){
																    				buf.append( "\r\n" );
																    			}
																    		}
																    	}
																    	
																    	return buf;
																    }
																},
																args );
		return strBuf.toString();
	}
	
	public static void printQuery( Connection conn,
	                               String query ) throws Throwable {
		String val = runQuery( conn,
		                       query );
		System.out.println( val );
	}
	
	public static void printQuery(	Connection conn,
									String query,
									Object ... args ) throws Throwable {
		String val = runQuery( conn,
		                       query,
		                       args );
		System.out.println( val );		
	}

	public static long nextVal( Connection conn,
	                            String strSeqName ) throws SQLException {
		return nextVal( conn, 
		                strSeqName,
		                "",
		                "" );
	}
	public static long nextVal( Connection conn,
	                            String strSeqName,
	                            String strSchemaName,
	                            String strDBLinkIncludingAtSymbol ) throws SQLException {
		long seqVal = -1;
		
		Statement stmt = null;
		ResultSet rs = null;

		try{
			stmt = conn.createStatement();
			String strSQL = "SELECT ";
			if( strSchemaName != null && !strSchemaName.equals( "" ) ){
				strSQL += strSchemaName + "."; 
			}
			strSQL += strSeqName + ".nextVal" + strDBLinkIncludingAtSymbol + " FROM Dual";
			rs = stmt.executeQuery( strSQL );
			if( rs.next() ) {
				seqVal = rs.getLong( 1 );
			}
			else {
				throw new IllegalStateException( ""  + strSeqName + " sequence not found" );
			}
			
			if( seqVal == -1 ){
				throw new IllegalStateException( "seq val not set: was still -1 upon return" );
			}
		}
		finally {
			DbUtils.closeQuietly( rs );
			DbUtils.closeQuietly( stmt );
		}

		return seqVal;		
	}
	public static void rollbackQuietly( Connection conn ) {
		if( conn != null ){
			try{
				conn.rollback();
			}
			catch( Throwable th ){
				//log.error( "", th );
				UtilsGeneral.rethrowErrors( th );
			}
		}
	}
	public static void close( Connection conn ) {
		if( conn != null ){
			try{
				conn.close();
			}
			catch( Throwable th ){
				//log.error( "Unable to close db connection ", th );
			}
		}
	}

	public static String toOracleDatePrecisionSeconds( Date date ){
		SimpleDateFormat sdf = new SimpleDateFormat("M-dd-yyyy hh:mm:ss a"); // the "a" gives you the am/pm designation
        return sdf.format( date );				
	}
}
