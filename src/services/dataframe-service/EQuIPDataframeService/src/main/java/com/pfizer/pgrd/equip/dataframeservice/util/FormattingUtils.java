package com.pfizer.pgrd.equip.dataframeservice.util;

import java.util.List;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;

public class FormattingUtils {
	private static TikaConfig TIKA_CONFIG = null;
	private FormattingUtils() {}
	
	public static final double percentage(double total, double piece) {
		double p = (piece / total) * 100.0;
		return Math.round(p * 10.0) / 10.0;
	}
	
	public static final <T extends Object> String asList(T[] items) {
		String list = "[";
		if(items != null) {
			for(T s : items) {
				list += s.toString() + ", ";
			}
		}
		list = list.substring(0, list.length() - 3); // remove the last ', '
		list += "]";
		
		return list;
	}
	
	public static final void logTime(String title, long ms, long total) {
		double p = 0.0;
		if(total != 0) {
			p = (((double)ms) / ((double)total)) * 100.0;
		}
		
		System.out.println(title + ": " + (((double)ms) / 1000.0) + "s (" + p + "%)");
	}
	
	/**
	 * Returns the file extension that correlates to the provided MIME type.
	 * @param mimeType
	 * @return {@link String}
	 */
	public static final String getExtension(String mimeType) {
		String extension = null;
		try {
			String results[] = mimeType.split("\\/");
			String type = results[0];
			String subType = results[1];
			MediaType mediaType = new MediaType(type, subType);
			
			FormattingUtils.initTikaConfig();
			MimeType tikaMimeType = TIKA_CONFIG.getMimeRepository().forName(mediaType.toString());
			extension = tikaMimeType.getExtension();
			
			if(extension.equalsIgnoreCase(".xhtml")) {
				extension = ".html";
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return extension;
	}
	
	private static final void initTikaConfig() {
		if(TIKA_CONFIG == null) {
			TIKA_CONFIG = TikaConfig.getDefaultConfig();
		}
	}
	
	/**
	 * Returns whether or not the provided {@link List} contains the provided {@link String} (case insensitive).
	 * @param value
	 * @param list
	 * @return
	 */
	public static final boolean caseInsensitiveContains(String value, List<String> list) {
		if(value != null && list != null) {
			for(String s : list) {
				if(value.equalsIgnoreCase(s)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
