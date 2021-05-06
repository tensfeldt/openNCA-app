package com.pfizer.equip.computeservice.resource;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.computeservice.dao.ComputeDAO;
import com.pfizer.equip.computeservice.dao.ComputeDAOImpl;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.RequestBody;

import spark.Request;
import spark.Response;
import spark.Route;

public class ComputeResource extends BaseComputeResource {
	private static Logger log = LoggerFactory.getLogger(ComputeResource.class);	
	private static final String NCA = "NCA";
	private static final String IAMPFIZERUSERCN = "IAMPFIZERUSERCN";
	private static final String APPLICATION_JSON = "application/json";

	private static ComputeDAO dao = new ComputeDAOImpl();
	
	private ComputeResource() {}
	
	public static final Route getVersion = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			log.info("IAMPFIZERUSERCN: " + request.headers("IAMPFIZERUSERCN"));
//			InputStream inputStream;
//			Properties prop = new Properties();
//			String propFileName = "config.properties";
//
//			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
//
//			if (inputStream != null) {
//				prop.load(inputStream);
//			} else {
//				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath.");
//			}
//			return prop.getProperty("version");
			return "1";
		}
	};
		
	public static final Route postCompute = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String server = request.scheme() + "://" + request.host();
			String system = request.params(":system");
			if (system == null) {
				system = NCA;
			}
			boolean isVirtual = Boolean.parseBoolean(request.queryParams("virtual"));
			if (!isVirtual) {
				log.info(request.body());
			}
			RequestBody rb = unmarshallComputeRequest(request);
			log.info(String.format("IAMPFIZERUSERCN: %s", request.headers(IAMPFIZERUSERCN)));
			// Use the user specified in the header if available, 
			// otherwise use the user specified in the message.
			String user = request.headers(IAMPFIZERUSERCN) != null ? request.headers(IAMPFIZERUSERCN) : rb.getUser();
			Map<String, String> requestHeaders = new HashMap<>();
			for (String headerName : request.headers()) {
				if ((headerName.startsWith("EQUIP")) || headerName.equalsIgnoreCase("Client-Info")) {
					requestHeaders.put(headerName, request.headers(headerName));
					log.info(String.format("Header: %s - %s", headerName, request.headers(headerName)));
				}
			}
			ComputeResponse cr = dao.doCompute(server, system, user, requestHeaders, rb, isVirtual);
			response.header("Content-Type", request.headers("Content-Type"));
			response.body(marshallComputeResponse(cr, request.headers("Content-Type")));
			return response;
		}		
	};
	
}
