package com.pfizer.pgrd.equip.utils;

import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;

public class UtilsGeneral {
//	private static final Log	log	= LogFactory.getLog( UtilsGeneral.class );
	
	public static String makeCommaSeparatedList( Object[] columns,
	                                             boolean bSingleQuoteItems ) {
		StringBuffer strbufRetVal = new StringBuffer();

		for( int i = 0; i < columns.length; i++ ) {
			if( bSingleQuoteItems ){
				strbufRetVal.append( "'" );
			}
			strbufRetVal.append( columns[i].toString() );
			if( bSingleQuoteItems ){
				strbufRetVal.append( "'" );
			}
			if( i < columns.length - 1 ) {
				strbufRetVal.append( ", " );
			}
		}

		return strbufRetVal.toString();
	}
	public static void rethrowErrors( Throwable th ){
		if( th instanceof Error ){
			throw (Error)th;
		}
	}
	public static void rethrowAll( Throwable th ) {
		rethrowErrors( th );
		throw new RuntimeException( th );
	}

	//DBUtils.closeQuietly() performs a commit!!!! No good!!!  The default behavior should be to not commit and when the transaction times out Oracle will roll it back.
	public static void close( Connection conn ) {
		if( conn != null ){
			try{
				conn.close();
			}
			catch( Throwable th ){
				//log.error( "", th );
				//rethrowErrors( th );  //if we call this function inside of a finally this may cause other resources to not be closed, catch 22 but I prefer this route.
			}
		}
	}
	public static String getStackTrace( Throwable th ){
		return getStackTrace( th, -1 );
	}
	public static String getStackTrace( Throwable th, int numLines ){
		StackTraceElement[] elements = th.getStackTrace();
		String str = "" + th.getMessage() + "\r\n";
		
		for( int i=0; i<elements.length; i++ ){
			str += "" + elements[i].toString() + "\r\n";
			if( numLines != -1 ){
				if( i+1 % numLines == 0 ){
					break;
				}
			}
		}
		
		return str;
	}
	public static void Assert(	boolean assertion,
								String errorMsg ) {
		if( !assertion ){
			throw new RuntimeException( errorMsg );
		}
	}

	public static void close( RandomAccessFile raf ){
		try{
			if( raf != null ){
				raf.close();
			}
		}
		catch( Throwable th ){
			//log.error( "", th );
			//rethrowErrors( th );  //if we call this function inside of a finally this may cause other resources to not be closed, catch 22 but I prefer this route.
		}		
	}
	
	public static boolean isNumeric( String postalCode ) {
		try{
			Long.parseLong(  postalCode );
		}
		catch( NumberFormatException ex ){
			return false;
		}
		
		return true;
	}

	public static String getLocalHost() {
		String returnValue = "";

		try {
			returnValue = InetAddress.getLocalHost().getHostName();
		} 
		catch( Throwable e ) {
			//generally this function is only called so that the host can be printed to a log file
			//so no need to worry about exceptions
		}

		return returnValue;
	}
	
	public static final String jsonToCSV(ComplexData cd) {
		if(cd != null && cd.getBytes() != null && cd.getBytes().length > 0) {
			byte[] b = UtilsGeneral.jsonToCSV(cd.getBytes());
			return new String(b);
		}
		
		return null;
	}
	
	public static final byte[] jsonToCSV(byte[] data) {
		if(data != null) {
			String csv = UtilsGeneral.jsonToCSV(new String(data));
			return csv.getBytes();
		}
		
		return null;
	}
	
	public static final String jsonToCSV(String json) {
		String csv = "";
		if(json != null) {
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(json);
			
			List<JsonObject> objects = new ArrayList<>();
			if(element.isJsonArray()) {
				JsonArray array = element.getAsJsonArray();
				for(int i = 0; i < array.size(); i++) {
					JsonElement e = array.get(i);
					if(!e.isJsonNull() && e.isJsonObject()) {
						objects.add(e.getAsJsonObject());
					}
				}
			}
			else if(!element.isJsonNull() && element.isJsonObject()) {
				objects.add(element.getAsJsonObject());
			}
			
			List<String> headers = new ArrayList<>();
			for(JsonObject object : objects) {
				for(String s : object.keySet()) {
					if(!headers.contains(s)) {
						headers.add(s);
						if(!csv.isEmpty()) {
							csv += ",";
						}
						
						csv += s;
					}
				}
			}
			
			for(JsonObject object : objects) {
				csv += "\r\n";
				for(int i = 0; i < headers.size(); i++) {
					if(i > 0) {
						csv += ",";
					}
					
					String header = headers.get(i);
					JsonElement e = object.get(header);
					if(e != null && !e.isJsonNull()) {
						String v = e.getAsString();
						try {
							Double.parseDouble(v);
						}
						catch(Exception ex) {
							v = "\"" + v.replaceAll("\"", "\"\"") + "\"";
						}
						
						csv += v;
					}
				}
			}
		}
		
		return csv;
	}
}
