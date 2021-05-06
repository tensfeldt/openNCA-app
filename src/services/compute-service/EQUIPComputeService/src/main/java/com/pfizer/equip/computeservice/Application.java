package com.pfizer.equip.computeservice;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.computeservice.containers.ContainerRunner;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.resource.ComputeLaunchResource;
import com.pfizer.equip.computeservice.resource.ComputeResource;
import com.pfizer.equip.utils.StackTraceDump;

import spark.Spark;
import spark.servlet.SparkApplication;

public class Application implements SparkApplication {
	private static Logger log = LoggerFactory.getLogger(Application.class);	
	private static Properties appProperties = new Properties();

	private static final String APPLICATION_PROPERTIES_FILE = "/app/3rdparty/equip/EquipComputeService/application.properties";
	private static final String COMPUTE_VERSION = "/version";
	private static final String COMPUTE_SYSTEM_BASE = "/:system/compute";
	private static final String COMPUTE_SYSTEM_LAUNCH = COMPUTE_SYSTEM_BASE + "/launch";
	private static final String COMPUTE_SYSTEM_LAUNCH_CLOSE = COMPUTE_SYSTEM_LAUNCH + "/close/:id";
	private static final String HTTP_ERROR_CODE_REGEX = "(?!.+HTTP response code: )([0-9]+)(?![.]+)";
	private static final Pattern HTTP_ERROR_CODE_PATTERN = Pattern.compile(HTTP_ERROR_CODE_REGEX, Pattern.CASE_INSENSITIVE);


	@Override
	public void init() {
		// This (staticFiles.location(..)) must be called before anything else in init.
		// The external location provided is in the WebContent folder.
		// Try {serviceroot}/public/README.txt from the browser.
		//allow static files so we can integrate swagger
		staticFiles.location("/static");

		// Exception handling
		Spark.exception(ComputeException.class, (exception, request, response) -> {
		    // Handle the exception here
			log.error("", exception);
			int status = 500;
			String msg = exception.getMessage();
			Matcher m = HTTP_ERROR_CODE_PATTERN.matcher(msg);
			if (m.find()) {
				status = Integer.parseInt(m.group());
			}
		    response.status(status);
		    try {
				response.body(StackTraceDump.dump(exception));
			} catch (IOException ex) {
				log.error("", ex);
			}
		});

		// Echo any OPTIONS requests
		Spark.options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}

			return "OK";
		});

		// Enable Cross Origin Resource Sharing (CORS)
		Spark.before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Allow-Methods", "*");
			response.header("Access-Control-Allow-Headers", "*");
		});

		// Load application properties
		try {
			appProperties.load(new FileReader(APPLICATION_PROPERTIES_FILE));
		} catch (IOException ex) {
			log.error(String.format("Failed to load application properties file %s", 
					APPLICATION_PROPERTIES_FILE), ex);
		}

		try {
			ContainerRunner.killAllPreExistingRunningContainers();
		} catch (IOException | GeneralSecurityException e) {
			log.error("", e);
		}
		
		initComputationService();
	}

	/**
	 * Initializes all URIs for the Compute service.
	 */
	private void initComputationService() {

		path("/", () -> {
			before("/*", (request, response) -> log
					.info(String.format("Received api call: %s from %s", request.pathInfo(), request.ip())));
			after("/*", (request, response) -> {
				String responseBody =  response.body();
				if (responseBody == null) {
					responseBody = "";
				} else if (responseBody.length() > 256) {
					responseBody = responseBody.substring(0, 256) + "...";
				}
				log.info(String.format("Returned: %s %s", response.status(), responseBody));
				});
			
			//redirect base uri to swagger
			Spark.redirect.get("/", "/EQUIPComputeService/ComputeSwagger/index.html");
			
			get(COMPUTE_VERSION, ComputeResource.getVersion);
			post(COMPUTE_SYSTEM_BASE, ComputeResource.postCompute);
			post(COMPUTE_SYSTEM_LAUNCH, ComputeLaunchResource.post);
			put(COMPUTE_SYSTEM_LAUNCH_CLOSE, ComputeLaunchResource.put);
			
		});
	}

	public static Properties getAppProperties() {
		return appProperties;
	}

}
