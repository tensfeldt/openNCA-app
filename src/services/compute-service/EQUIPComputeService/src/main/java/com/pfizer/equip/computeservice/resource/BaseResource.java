package com.pfizer.equip.computeservice.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.servlet.http.Part;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

/**
 * Contains default {@link Route} objects for resources. Contains methods for reading files from requests.
 * @author QUINTJ16
 *
 */
public abstract class BaseResource {
	private static final int METHOD_NOT_ALLOWED = 405;

	/**
	 * Default GET handling.
	 */
	public static Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default POST handling.
	 */
	public static Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default PUT handling.
	 */
	public static Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default DELETE handling.
	 */
	public static Route delete = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			Spark.halt(METHOD_NOT_ALLOWED);
			return "";
		}

	};
	
	/**
	 * Reads the provided {@link InputStream} into a {@link String}.
	 * @param stream {@link InputStream} the stream
	 * @return {@link String} the contents
	 * @throws IOException
	 */
	protected static String readStream(InputStream stream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer);
		
		String s = writer.toString();
		return s;
	}
	
	/**
	 * Returns the file within the {@link Part} object's input stream as a temporary file.
	 * @param part {@link Part} the part
	 * @return {@link File} the file
	 * @throws IOException
	 */
	protected static File readFile(Part part) throws IOException {
		String fileName = part.getSubmittedFileName();
		String[] fnParts = fileName.split("\\.");
		
		String ext = fnParts[fnParts.length - 1];
		String name = fileName.substring(0, fileName.indexOf("." + ext));
		
		File f = readFile(part.getInputStream(), name, "." + ext);
		return f;
	}
	
	/**
	 * Reads the provided {@link InputStream} into a temporary file.
	 * @param stream {@link InputStream} the stream
	 * @param fileName {@link String} the name of the file
	 * @param fileType {@link String} the file type (e.g. .txt, .docx, .csv, etc.)
	 * @return {@link File} the temporary file
	 * @throws IOException
	 */
	protected static File readFile(InputStream stream, String fileName, String fileType) throws IOException {
		File f = File.createTempFile(fileName, fileType);
		f.deleteOnExit();
		
		OutputStream os = new FileOutputStream(f);
		IOUtils.copy(stream, os);
		
		return f;
	}
	
	/**
	 * Returns the content of the provided {@link Part} object as a {@code byte[]}.
	 * @param part the part
	 * @return {@code byte[]}
	 * @throws IOException
	 */
	protected static byte[] readContent(Part part) throws IOException {
		byte[] content = readContent(part.getInputStream());
		return content;
	}
	
	/**
	 * Returns the content of the provided {@link InputStream} object as a {@code byte[]}.
	 * @param stream the stream
	 * @return {@code byte[]}
	 * @throws IOException
	 */
	protected static byte[] readContent(InputStream stream) throws IOException {
		byte[] content = IOUtils.toByteArray(stream);
		return content;
	}
}
