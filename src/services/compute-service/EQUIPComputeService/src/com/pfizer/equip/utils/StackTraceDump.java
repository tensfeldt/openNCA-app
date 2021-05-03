package com.pfizer.equip.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceDump {
	
	private StackTraceDump() {}
	
	public static String dump(Exception e) throws IOException {
		try (StringWriter sw = new StringWriter()) {
			try (PrintWriter pw = new PrintWriter(sw)) {
				e.printStackTrace(pw);
				pw.flush();
				return sw.toString();
			}
		}
	}
	
}