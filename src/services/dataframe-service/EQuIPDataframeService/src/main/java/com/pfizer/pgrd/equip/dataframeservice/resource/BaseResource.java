package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframeservice.util.Const;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

/**
 * Contains default {@link Route} objects for resources. Contains methods for
 * reading files from requests.
 * 
 * @author QUINTJ16
 *
 */
public abstract class BaseResource {
	private static Logger log = LoggerFactory.getLogger(BaseResource.class);		

	/**
	 * Default GET handling.
	 */
	public static final Route get = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.error(Const.METHOD_NOT_ALLOWED);
			Spark.halt(HTTPStatusCodes.METHOD_NOT_ALLOWED);
			return "";
		}

	};

	/**
	 * Default POST handling.
	 */
	public static final Route post = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.error(Const.METHOD_NOT_ALLOWED);
			Spark.halt(HTTPStatusCodes.METHOD_NOT_ALLOWED);
			return null;
		}

	};

	/**
	 * Default PUT handling.
	 */
	public static final Route put = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.error(Const.METHOD_NOT_ALLOWED);
			Spark.halt(HTTPStatusCodes.METHOD_NOT_ALLOWED);
			return null;
		}

	};

	/**
	 * Default DELETE handling.
	 */
	public static final Route delete = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.error(Const.METHOD_NOT_ALLOWED);
			Spark.halt(HTTPStatusCodes.METHOD_NOT_ALLOWED);
			return null;
		}

	};

	/**
	 * Reads the provided {@link InputStream} into a {@link String}.
	 * 
	 * @param stream
	 *            {@link InputStream} the stream
	 * @return {@link String} the contents
	 * @throws IOException
	 */
	protected static String readStream(InputStream stream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer);

		return writer.toString();
	}

	/**
	 * Returns the file within the {@link Part} object's input stream as a temporary
	 * file.
	 * 
	 * @param part
	 *            {@link Part} the part
	 * @return {@link File} the file
	 * @throws IOException
	 */
	protected static File readFile(Part part) throws IOException {
		String fileName = part.getSubmittedFileName();
		String[] fnParts = fileName.split("\\.");

		String ext = fnParts[fnParts.length - 1];
		String name = fileName.substring(0, fileName.indexOf("." + ext));

		return readFile(part.getInputStream(), name, "." + ext);
	}

	/**
	 * Reads the provided {@link InputStream} into a temporary file.
	 * 
	 * @param stream
	 *            {@link InputStream} the stream
	 * @param fileName
	 *            {@link String} the name of the file
	 * @param fileType
	 *            {@link String} the file type (e.g. .txt, .docx, .csv, etc.)
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
	 * 
	 * @param part
	 *            the part
	 * @return {@code byte[]}
	 * @throws IOException
	 */
	protected static byte[] readContent(Part part) throws IOException {
		return readContent(part.getInputStream());
	}

	/**
	 * Returns the content of the provided {@link InputStream} object as a
	 * {@code byte[]}.
	 * 
	 * @param stream
	 *            the stream
	 * @return {@code byte[]}
	 * @throws IOException
	 */
	protected static byte[] readContent(InputStream stream) throws IOException {
		return IOUtils.toByteArray(stream);
	}
}
