package com.pfizer.equip.service.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Some utility methods for ServiceCaller
 * 
 * @author HeinemanWP
 *
 */
public class ServiceCallerUtils {
	private static int bufferSize = 65536;
	
	private ServiceCallerUtils() {}

	public static String getResponseDataAsString(ServiceResponse sr) throws IOException {
		return new String(getResponseDataAsByteArray(sr));
	}
	
	public static byte[] getResponseDataAsByteArray(ServiceResponse sr) throws IOException {
		InputStream is = sr.getInputStream();
		List<byte[]> buffers = new ArrayList<>();
		int count = 0;
		ByteBuffer bb = ByteBuffer.allocate(bufferSize);
		do {
			bb.clear();
			int offset = 0;
			int size = bufferSize;
			do {
				count = is.read(bb.array(), offset, size - offset);
				if (count > 0) {
					offset += count;
				}
			} while ((count > 0) && (offset < size));
			if (offset > 0) {
				byte[] buffer = new byte[offset];
				bb.get(buffer);
				buffers.add(buffer);
			}
		} while (count > -1);
		int arraySize = 0;
		for (byte[] buffer : buffers) {
			arraySize += buffer.length;
		}
		bb = ByteBuffer.allocate(arraySize);
		for (byte[] buffer : buffers) {
			bb.put(buffer, 0, buffer.length);
		}
		return bb.array();
	}

}
