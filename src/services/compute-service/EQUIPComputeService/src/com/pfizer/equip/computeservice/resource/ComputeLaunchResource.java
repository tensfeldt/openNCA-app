package com.pfizer.equip.computeservice.resource;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.computeservice.dao.ComputeLaunchDao;
import com.pfizer.equip.computeservice.dto.ComputeLaunchResponse;
import com.pfizer.equip.computeservice.dto.ComputeLaunchResponseAdapter;
import com.pfizer.equip.computeservice.dto.ComputeResponse;
import com.pfizer.equip.computeservice.dto.RequestBody;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class ComputeLaunchResource extends BaseComputeResource {
	private static Logger log = LoggerFactory.getLogger(ComputeLaunchResource.class);	
	private static final String NCA = "NCA";
	private static final String IAMPFIZERUSERCN = "IAMPFIZERUSERCN";
	private static final String APPLICATION_JSON = "application/json";

	private static ComputeLaunchDao dao = new ComputeLaunchDao();

	public static Route post = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String server = request.scheme() + "://" + request.host();
			String system = request.params(":system");
			if (system == null) {
				system = NCA;
			}
			
			log.info(request.body());
			
			RequestBody rb = unmarshallComputeRequest(request);
			log.info(String.format("IAMPFIZERUSERCN: %s", request.headers(IAMPFIZERUSERCN)));
			// Use the user specified in the header if available, 
			// otherwise use the user specified in the message.
			String user = request.headers(IAMPFIZERUSERCN) != null ? request.headers(IAMPFIZERUSERCN) : rb.getUser();
			Map<String, String> requestHeaders = new HashMap<>();
			for (String headerName : request.headers()) {
				if ((headerName.startsWith("EQUIP")) || headerName.equalsIgnoreCase("Client-Info")) {
					requestHeaders.put(headerName, request.headers(headerName));
				}
			}
			ComputeLaunchResponse clr = dao.doLaunch(server, system, user, requestHeaders, rb);
			return marshallComputeLaunchResponse(clr, request.headers("Content-Type"));
		}};
	
	public static Route put = new Route() {
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String server = request.scheme() + "://" + request.host();
			String system = request.params(":system");
			if (system == null) {
				system = NCA;
			}
			String id = request.params(":id");
			boolean save = true;
			if (request.queryParams().contains("save")) {
				save = Boolean.parseBoolean(request.queryParams("save"));
			}
			log.info(String.format("IAMPFIZERUSERCN: %s", request.headers(IAMPFIZERUSERCN)));
			String user = request.headers(IAMPFIZERUSERCN) != null ? request.headers(IAMPFIZERUSERCN) : "heinemanwp";
			ComputeResponse cr = dao.stopLaunched(server, system, user, id, save);
			response.header("Content-Type", request.headers("Content-Type"));
			response.body(marshallComputeResponse(cr, request.headers("Content-Type")));
			return response;
		}};


	protected static String marshallComputeLaunchResponse(ComputeLaunchResponse cr, String contentType) throws JAXBException {
		if (contentType != null) {
			if (contentType.equalsIgnoreCase(APPLICATION_JSON)) {
				ComputeLaunchResponseAdapter clra = new ComputeLaunchResponseAdapter();
				return clra.toJson(cr);
			} else {
				Spark.halt(400, "Content-Type must be application/json ");
			}
		} else {
			Spark.halt(400, "A Content-Type must be provided.");
		}
		return null;
	}

}
