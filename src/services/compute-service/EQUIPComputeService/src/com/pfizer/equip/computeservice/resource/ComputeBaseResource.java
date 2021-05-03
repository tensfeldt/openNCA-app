package com.pfizer.equip.computeservice.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;

import com.pfizer.equip.computeservice.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;

import spark.utils.IOUtils;

public class ComputeBaseResource extends BaseResource {
	private static Marshaller marshaller = null;
	private static Unmarshaller unmarshaller = null;
	private static JAXBContext jaxb_context = null;
	
	
	/**
	 * Marshals the provided object or collection of objects to JSON.
	 * 
	 * @param object
	 *            the objects(s)
	 * @return {@link String} JSON representation
	 * @throws JAXBException
	 */
	private static String marshalObject(Object object) throws JAXBException {
		String json = null;
		if(object != null) {
			initMarshaller();
			StringWriter sw = new StringWriter();
			marshaller.marshal(object, sw);
			
			json = sw.toString();
		}
		
		return json;
	}

	/**
	 * Unmarshals the provided JSON string into a collection of dataframe objects.
	 * 
	 * @param json
	 *            {@link String} the JSON
	 * @return {@link List}<{@link Dataframe}>
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static List<Dataframe> unmarshalDataframe(String json) throws JAXBException, IOException {
		try(InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8.name()))) {
			return unmarshalDataframe(stream);
		}
	}

	/**
	 * Unmarshals the provided stream into a collection of dataframe objects.
	 * 
	 * @param stream
	 * @return {@link List}<{@link Dataframe}>
	 * @throws JAXBException
	 */
	public static List<Dataframe> unmarshalDataframe(InputStream stream) throws JAXBException {
		StreamSource source = new StreamSource(stream);
		return unmarshalDataframe(source);
	}

	/**
	 * Unmarshals the provided source into a collection of dataframe objects.
	 * @param source
	 * @return
	 * @throws JAXBException
	 */
	public static List<Dataframe> unmarshalDataframe(StreamSource source) throws JAXBException {
		initUnmarshaller();

		List<Dataframe> dataframes = new ArrayList<>();

		// We check to see if the JSON provided is for a dataframe object or a collection of
		// dataframe objects.
		Object o = unmarshaller.unmarshal(source, Dataframe.class).getValue();
		if(o.getClass() == Dataframe.class) {
			Dataframe df = (Dataframe)o;
			dataframes.add(df);
		}
		else {
			dataframes = (List<Dataframe>) o;
		}

		return dataframes;
	}

	/**
	 * Initializes the dataframe marshaller.
	 * 
	 * @throws JAXBException
	 */
	private static void initMarshaller() throws JAXBException {
		// We only want to initialize once.
		if (marshaller == null) {
			initJAXBContext();

			marshaller = jaxb_context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
	}

	/**
	 * Initializes the dataframe unmarshaller.
	 * 
	 * @throws JAXBException
	 */
	private static void initUnmarshaller() throws JAXBException {
		// We only want to initialize once.
		if (unmarshaller == null) {
			initJAXBContext();

			unmarshaller = jaxb_context.createUnmarshaller();
		}
	}

	/**
	 * Initializes the JAXB context used for marshalling and unmarshalling
	 * dataframes.
	 * 
	 * @throws JAXBException
	 */
	private static void initJAXBContext() throws JAXBException {
		// We only want to initialize it once
		if (jaxb_context == null) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
			properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

			// We must let the JAXB context know what classes it will be handling.
			// The ObjectFactory class will be used to make sure all Eclipse Link methods/classes are available.
			Class<?>[] classes = new Class<?>[] { Dataframe.class, Dataframe[].class, Metadatum.class, Dataset.class, ObjectFactory.class };
			jaxb_context = JAXBContext.newInstance(classes, properties);
		}
	}

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
}
