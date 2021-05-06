package com.pfizer.equip.dataframe.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pfizer.equip.computeservice.dto.AssemblyListAdapter;
import com.pfizer.equip.computeservice.dto.DataframeAdapter;
import com.pfizer.equip.computeservice.dto.DataframeCreationReportEntry;
import com.pfizer.equip.computeservice.dto.DataframeListAdapter;
import com.pfizer.equip.computeservice.dto.DatasetAdapter;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.services.client.MultipartUtility;
import com.pfizer.pgrd.equip.services.client.ServiceCaller;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.client.ServiceCallerUtils;
import com.pfizer.pgrd.equip.services.client.ServiceResponse;

public class DataframeServiceClient {
	private static final String IAMPFIZERUSERCN = "IAMPFIZERUSERCN";
	private static final String ACCEPT = "Accept";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final boolean USE_GSON = true;
	private String serviceUrl;
	private Gson dataframeGson;
	private Gson datasetGson;
	private DataframeListAdapter dataframeListAdapter = new DataframeListAdapter();
	private AssemblyListAdapter assemblyListAdapter = new AssemblyListAdapter();
	
	private DataframeServiceClient() {
		dataframeGson = new GsonBuilder()
				.registerTypeAdapter(Dataframe.class, new DataframeAdapter())
				.create();
		datasetGson = new GsonBuilder()
				.registerTypeAdapter(Dataset.class, new DatasetAdapter())
				.create();
	}
	
	public DataframeServiceClient(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	/**
	 * getDataframes - returns a list of Assembly objects for a passed list of assembly ids.
	 * 
	 * @param user
	 * @param assemblyListJson
	 * @return
	 * @throws JAXBException 
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public List<Assembly> getAssemblies(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String assemblyListJson) throws JAXBException, ServiceCallerException, IOException {
		if (USE_GSON) {
			String json = getAssembliesAsJson(system, user, requestHeaders, assemblyListJson);
			return assemblyListAdapter.fromJson(json);
		} else {
			return unmarshalAssemblyList(getAssembliesAsJson(system, user, requestHeaders, assemblyListJson), APPLICATION_JSON);
		}
	}
	
	
	/**
	 * getAssembliesAsJson - returns a list of assemblys as a JSON string for a passed list of dataframe ids.
	 * 
	 * @param user
	 * @param assemblyListJson
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public String getAssembliesAsJson(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String assemblyListJson) throws ServiceCallerException, IOException {
		String returnValue = "";
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put(IAMPFIZERUSERCN, user);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/assemblies/list", system);
		ServiceResponse sr = sc.post(url, headers, assemblyListJson);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			returnValue = ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
		return returnValue;
	}

	public String addDataframeAsJson(
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			String dataframeJson) throws ServiceCallerException, IOException {
		String returnValue = "";
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put(IAMPFIZERUSERCN, user);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/dataframes", system);
		ServiceResponse sr = sc.post(url, headers, dataframeJson);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			returnValue = ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
		return returnValue;
	}
	
	public Dataframe addDataframe(
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			Dataframe newDataframe) throws JAXBException, ServiceCallerException, IOException {
		String json = marshall(newDataframe, APPLICATION_JSON);
		json = json.replaceAll("\"equipId\" : \"UNKNOWN\",", "");
		String newId = addDataframeAsJson(system, user, requestHeaders, json);
		newDataframe.setId(newId);
		return newDataframe;
	}
	
	/**
	 * getDataframeAsJson - returns a dataframe as JSON string for a dataframe id
	 * 
	 * @param user
	 * @param dataframeId
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public String getDataframeAsJson(String system, String user, String dataframeId) throws ServiceCallerException, IOException {
		String returnValue = "";
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put(IAMPFIZERUSERCN, user);
		String url = serviceUrl + String.format("/%s/dataframes/", system) + dataframeId;
		ServiceResponse sr = sc.get(url, headers);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			returnValue = ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
		return returnValue;
	}

	/**
	 * getDataframe - returns a dataframe object for a dataframe id
	 * 
	 * @param user
	 * @param dataframeId
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws JAXBException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Dataframe getDataframe(String system, String user, String dataframeId) throws JAXBException, ServiceCallerException, IOException {
		if (USE_GSON) {
			String json = getDataframeAsJson(system, user, dataframeId);
			return dataframeGson.fromJson(json, Dataframe.class);
		} else {
			return unmarshalDataframe(getDataframeAsJson(system, user, dataframeId), APPLICATION_JSON);
		}
	}
	
	/**
	 * getDataframesAsJson - returns a list of dataframes as a JSON string for a passed list of dataframe ids.
	 * 
	 * @param user
	 * @param dataframeListJson
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public String getDataframesAsJson(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String dataframeListJson) throws ServiceCallerException, IOException {
		String returnValue = "";
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.put(IAMPFIZERUSERCN, user);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/dataframes/list", system);
		ServiceResponse sr = sc.post(url, headers, dataframeListJson);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			returnValue = ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
		return returnValue;
	}

	/**
	 * getDataframes - returns a list of dataframe objects for a passed list of dataframe ids.
	 * 
	 * @param user
	 * @param dataframeListJson
	 * @return
	 * @throws JAXBException 
	 * @throws ServiceCallerException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public List<Dataframe> getDataframes(
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			String dataframeListJson) throws JAXBException, ServiceCallerException, IOException {
		if (USE_GSON) {
			String json = getDataframesAsJson(system, user, requestHeaders, dataframeListJson);
			return dataframeListAdapter.fromJson(json);
		} else {
			return unmarshalDataframeList(getDataframesAsJson(system, user, requestHeaders, dataframeListJson), APPLICATION_JSON);
		}
	}
	
	
	/**
	 * getDataFrameData - returns the data associated with a dataframe.
	 * 
	 * @param user
	 * @param complexDataId
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public byte[] getDataFrameData(String system, String user, String complexDataId) throws ServiceCallerException, IOException {
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(IAMPFIZERUSERCN, user);
		String url = serviceUrl + String.format("/%s/dataframes/data/%s", system, complexDataId);
		ServiceResponse sr = sc.get(url, headers);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			return ServiceCallerUtils.getResponseDataAsByteArray(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
	}

	/**
	 * getDataFrameData - returns the data associated with a dataframe.
	 * 
	 * @param user
	 * @param complexDataId
	 * @return
	 * @throws ServiceCallerException
	 * @throws IOException
	 */
	public byte[] getDataFrameData(
			String system, 
			String user, 
			Map<String, String> requestHeaders, 
			String complexDataId) throws ServiceCallerException, IOException {
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(IAMPFIZERUSERCN, user);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/dataframes/data/%s", system, complexDataId);
		ServiceResponse sr = sc.get(url, headers);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			return ServiceCallerUtils.getResponseDataAsByteArray(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
	}

	public String addDataframeDatasetAsJson(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String dataframeId, 
			String datasetJson) throws ServiceCallerException, IOException {
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(IAMPFIZERUSERCN, user);
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/dataframes/%s/data", system, dataframeId);
		ServiceResponse sr = sc.post(url, headers, datasetJson);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			return ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
	}
	
	public String addDataframeDataset(
			String system, 
			String user,
			Map<String, String> requestHeaders,
			String dataframeId, 
			Dataset dataset) throws ServiceCallerException, IOException, JAXBException {
		return addDataframeDatasetAsJson(system, user, requestHeaders, dataframeId, marshall(dataset, APPLICATION_JSON));
	}

	public String getDataframeDatasetAsJson(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String dataframeId) throws ServiceCallerException, IOException {
		ServiceCaller sc = new ServiceCaller();
		Map<String, String> headers = new HashMap<>();
		headers.put(IAMPFIZERUSERCN, user);
		headers.put(CONTENT_TYPE, APPLICATION_JSON);
		headers.put(ACCEPT, APPLICATION_JSON);
		headers.putAll(requestHeaders);
		String url = serviceUrl + String.format("/%s/dataframes/%s/data", system, dataframeId);
		ServiceResponse sr = sc.get(url, headers);
		if (sr.getCode() >= 200 && sr.getCode() < 300) {
			return ServiceCallerUtils.getResponseDataAsString(sr);
		} else {
			String response = sr.getResponseAsString();
			String message = response.isEmpty() ? "" : " - " + response;
			throw new ServiceCallerException("Dataframe Service", sr.getCode(), message);
		}
	}

	public Dataset getDataframeDataset(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String dataframeId) throws JAXBException, ServiceCallerException, IOException {
		if (USE_GSON) {
			String json = getDataframeDatasetAsJson(system, user, requestHeaders, dataframeId);
			return this.datasetGson.fromJson(json, Dataset.class);
		} else {
			return unmarshalDataset(getDataframeDatasetAsJson(system, user, requestHeaders, dataframeId), APPLICATION_JSON);
		}
	}

	public String addDataToDataframeDataset(
			String system, 
			String user, 
			Map<String, String> requestHeaders,
			String datasetId, 
			String name, 
			InputStream inputStream) throws ServiceCallerException {
		// "multipart/form-data"
		String url = serviceUrl + String.format("/%s/dataframes/data/%s", system, datasetId);
        String charset = "UTF-8";
        try {
        	Map<String, String> headers = new HashMap<>();
        	headers.put(IAMPFIZERUSERCN, user);
        	headers.putAll(requestHeaders);
        	MultipartUtility mpu = new MultipartUtility(url, headers, charset);
			// mpu.addHeaderField(CONTENT_TYPE, "multipart/form-data");
			mpu.addFilePart("file", name, inputStream);
			List<String> response = mpu.finish();
			return String.join("\n", response);
        } catch (IOException ex) {
			throw new ServiceCallerException("Dataframe Service", ex); 	
        }
	}

	public String addDataToDataframeDataset(
			String system, 
			String user,
			Map<String, String> requestHeaders,
			String datasetId, 
			String name, 
			byte[] data) throws IOException, ServiceCallerException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			return addDataToDataframeDataset(system, user, requestHeaders, datasetId, name, bais);
		}
	}
	
	public List<DataframeCreationReportEntry> addDataframesInBulk(String system, String user, List<Dataframe> children, byte[] datasets) throws ServiceCallerException {
	    Gson gson = new Gson();
	    String json = gson.toJson(children);
		String url = serviceUrl + String.format("/%s/dataframes/bulk", system);
        String charset = "UTF-8";
        try {
        	Map<String, String> headers = new HashMap<>();
        	headers.put(IAMPFIZERUSERCN, user);
        	MultipartUtility mpu = new MultipartUtility(url, headers, charset);
        	try (ByteArrayInputStream jsonBais = new ByteArrayInputStream(json.getBytes())) {
        		try (ByteArrayInputStream filesBais = new ByteArrayInputStream(datasets)) {
					mpu.addFilePart("dataframes", "dataframes.json", jsonBais);
					mpu.addFilePart("files", "files.tar", filesBais);
					List<String> response = mpu.finish();
					return unmarshallBulkDataframeResponse(String.join("\n", response));
        		}
        	}
        } catch (IOException ex) {
			throw new ServiceCallerException("Dataframe Service", ex); 	
        }
	}

	private List<DataframeCreationReportEntry> unmarshallBulkDataframeResponse(String json) {
	    Type listOfDataframeCreationReportEntry = new TypeToken<ArrayList<DataframeCreationReportEntry>>(){}.getType();
	    Gson gson = new Gson();
	    return gson.fromJson(json, listOfDataframeCreationReportEntry);
	}
	
	static List<Assembly> unmarshalAssemblyList(String data, String contentType) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		try {
			Class.forName("com.pfizer.pgrd.equip.dataframe.dto.Assembly");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {Assembly.class, ObjectFactory.class}, properties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource stream = new StreamSource(new StringReader(data));
		Object obj = unmarshaller.unmarshal(stream, Assembly.class).getValue();
		@SuppressWarnings("unchecked")
		ArrayList<Assembly> returnValue = (ArrayList<Assembly>) obj;
		return returnValue;
	}

	private static String marshall(Dataframe dataframe, String contentType) throws JAXBException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        properties.put(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, true);
        if (APPLICATION_JSON.equals(contentType)) {
            properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
            properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
        }
        JAXBContext jc = JAXBContext.newInstance(new Class[] {Dataframe.class, Dataset.class, ObjectFactory.class}, properties);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Output
        StringWriter sw = new StringWriter();
        marshaller.marshal(dataframe, sw);
        return sw.toString();
	}

	static Dataframe unmarshalDataframe(String data, String contentType) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {Dataframe.class, Dataset.class, ObjectFactory.class}, properties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource stream = new StreamSource(new StringReader(data));
		return unmarshaller.unmarshal(stream, Dataframe.class).getValue();
	}

	static List<Dataframe> unmarshalDataframeList(String data, String contentType) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		try {
			Class.forName("com.pfizer.pgrd.equip.dataframe.dto.Dataframe");
			Class.forName("com.pfizer.pgrd.equip.dataframe.dto.Dataset");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {Dataframe.class, Dataset.class, ObjectFactory.class}, properties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource stream = new StreamSource(new StringReader(data));
		Object obj = unmarshaller.unmarshal(stream, Dataframe.class).getValue();
		@SuppressWarnings("unchecked")
		ArrayList<Dataframe> returnValue = (ArrayList<Dataframe>) obj;
		return returnValue;
	}

	private static String marshall(Dataset dataset, String contentType) throws JAXBException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        properties.put(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, true);
        if (APPLICATION_JSON.equals(contentType)) {
            properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
            properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
        }
        JAXBContext jc = JAXBContext.newInstance(new Class[] {Dataset.class, ObjectFactory.class}, properties);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Output
        StringWriter sw = new StringWriter();
        marshaller.marshal(dataset, sw);
        return sw.toString();
	}

	static Dataset unmarshalDataset(String data, String contentType) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, APPLICATION_JSON);
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		final JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {Dataset.class, ObjectFactory.class}, properties);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource stream = new StreamSource(new StringReader(data));
		return unmarshaller.unmarshal(stream, Dataset.class).getValue();
	}

}
