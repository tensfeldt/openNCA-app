package com.pfizer.equip.computeservice.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;

import com.pfizer.equip.computeservice.dto.AsyncGetResponse;
import com.pfizer.equip.computeservice.dto.AsyncPostResponse;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.RequestBody;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class UserApi extends ComputeBaseResource {

	private static final String APPLICATION_JSON = "application/json";

	public static Route getParentData = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			response.header("Content-Type", APPLICATION_JSON);

			return "dummy";

		}
	};

	public static Route getServiceVersion = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String result = "";

			InputStream inputStream;
			Properties prop = new Properties();
			String propFileName = "config.properties";

			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath.");
			}
			result = prop.getProperty("version");
			return result;
		}
	};
	public static Route getAsync = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {

			Marshaller marshaller = null;
			JAXBContext jaxb_context = null;

			// ************************
			// TO-DO: use the RequestBody to call a get method from ComputeDAO
			// ************************
			List<AsyncGetResponse> agrList = new ArrayList<>();
			String responseValue = "";

			if (APPLICATION_JSON.equals(response.type())) {
				response.header("Content-Type", APPLICATION_JSON);
			}
			// Output Response
			StringWriter sw = new StringWriter();
			marshaller = jaxb_context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(agrList, sw);

			responseValue = sw.toString();

			return "Get Async Value URI";
		}
	};
	public static Route postCompute = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {

			Marshaller marshaller = null;
			Unmarshaller unmarshaller = null;
			JAXBContext jaxb_context = null;
			Map<String, Object> properties = new HashMap<>();

			properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			jaxb_context = JAXBContext.newInstance(new Class[] { RequestBody.class, ObjectFactory.class }, properties);

			String requestJson = "";
			unmarshaller = jaxb_context.createUnmarshaller();
			String contentType = request.headers("Content-Type");
			if (contentType != null) {
				if (contentType.equalsIgnoreCase("application/json")) {
					requestJson = request.body();
				}

				else {
					Spark.halt(400, "Content-Type must be application/json ");
				}
			} else {
				Spark.halt(400, "A Content-Type must be provided.");
			}

			StreamSource requestJsonStream = new StreamSource(requestJson);
			RequestBody rb = unmarshaller.unmarshal(requestJsonStream, RequestBody.class).getValue();

			// ************************
			// TO-DO: use the RequestBody to execute a computation using the
			// ComputeDAO
			// that returns a ComputeResponse object

			 // ComputeDAO computedao = getComputeDAO();
			 // List<Dataframe> dfList = computedao.getDataFrames(rb.getUser(), rb.getDataframes());

			// Gets Script from
			 // byte[] scriptBytes = computedao.getScript(rb.getUser(), rb.getScriptId());

			// ************************//
			String responseValue = "";
			if (APPLICATION_JSON.equals(response.type())) {
				response.header("Content-Type", "application/json");
			}
			ComputeResponse cr = null;

			// Output Response
			StringWriter sw = new StringWriter();
			marshaller = jaxb_context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(cr, sw);

			responseValue = sw.toString();

			return "Post Compute Value URI";
		}
	};
	public static Route postAsync = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {

			Marshaller marshaller = null;
			Unmarshaller unmarshaller = null;
			JAXBContext jaxb_context = null;
			Map<String, Object> properties = new HashMap<>();

			String responseJson = "";
			properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			jaxb_context = JAXBContext.newInstance(new Class[] { RequestBody.class, ObjectFactory.class }, properties);

			String requestJson = "";
			unmarshaller = jaxb_context.createUnmarshaller();
			String contentType = request.headers("Content-Type");
			if (contentType != null) {
				if (contentType.equalsIgnoreCase("application/json")) {
					requestJson = request.body();
				}

				else {
					Spark.halt(400, "Content-Type must be application/json ");
				}
			} else {
				Spark.halt(400, "A Content-Type must be provided.");
			}

			StreamSource requestJsonStream = new StreamSource(requestJson);
			RequestBody rb = unmarshaller.unmarshal(requestJsonStream, RequestBody.class).getValue();

			// ************************
			// TO-DO: use the RequestBody to execute a computation that returns
			// a
			// ComputeResponse object
			// ************************

			String responseValue = "";
			if (APPLICATION_JSON.equals(response.type())) {
				response.header("Content-Type", "application/json");
			}
			AsyncPostResponse apr = null;
			// Output Response
			StringWriter sw = new StringWriter();
			marshaller = jaxb_context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(apr, sw);

			responseValue = sw.toString();

			return "Post Async Value URI";
		}
	};

	private static void runRbatch() throws IOException {
		System.out.println("runRbatch:");
		String line;
		File dataPath = new File("data");
		Process p = Runtime.getRuntime()
				.exec("C:\\Progra~1\\Docker\\Docker\\Resources\\bin\\docker run -a STDERR -a STDOUT --rm -v "
						+ dataPath.getAbsolutePath()
						+ ":/home/docker -w /home/docker -u docker docker.pfizer.com:80/epharm/rhel7-equip-r-base R CMD BATCH helloworld.R");
		try (BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
			while ((line = errReader.readLine()) != null) {
				System.out.println(line);
			}
		}
		try (BufferedReader outputReader = new BufferedReader(new FileReader(".\\data\\helloworld.Rout"))) {
			while ((line = outputReader.readLine()) != null) {
				System.out.println(line);
			}
		}
		System.out.println();
	}

}
