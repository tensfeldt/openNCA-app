package com.pfizer.pgrd.equip.dataframeservice.resource;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.xmlmodel.ObjectFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.PublishEvent;
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;
import com.pfizer.pgrd.equip.dataframe.dto.PublishStatus;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.ReleaseStatus;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCommentable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipCreatable;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DatasetDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.EquipIDDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.EquipIDDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.MetadataDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.MetadataDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.QCRequestDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dao.ReportingAndPublishingDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ScriptDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ScriptDAOImpl;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.util.DateUtils;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * An abstract base class for dataframe service resources. It provides methods
 * to marshal and unmarshal Dataframe objects.
 * 
 * @author QUINTJ16
 *
 */
public abstract class ServiceBaseResource extends BaseResource {
	private static JAXBContext JAXB_CONTEXT = null;
	private static Marshaller MARSHALLER = null;
	private static Unmarshaller UNMARSHALLER = null;
	private static AuthorizationDAO AUTH_DAO = null;
	public static final String AUTH_HEADER = "IAMPFIZERUSERCN";

	protected static DataframeDAO getDataframeDAO() {
		return new DataframeDAOImpl();
	}
	
	protected static DatasetDAO getDatasetDAO() {
		return new DatasetDAOImpl();
	}

	protected static CommentDAO getCommentDAO() {
		return new CommentDAOImpl();
	}

	protected static MetadataDAO getMetadataDAO() {
		return new MetadataDAOImpl();
	}

	protected static AssemblyDAO getAssemblyDAO() {
		return new AssemblyDAOImpl();
	}

	protected static QCRequestDAO getQCRequestDAO() {
		return new QCRequestDAOImpl();
	}

	protected static ScriptDAO getScriptDAO() {
		return new ScriptDAOImpl();
	}
	
	protected static EquipIDDAO getEquipIDDAO() {
		return new EquipIDDAOImpl();
	}
	
	protected static LineageDAO getLineageDAO() {
		return new LineageDAOImpl();
	}
	
	protected static AnalysisDAO getAnalysisDAO() {
		return new AnalysisDAOImpl();
	}
	
	protected static String getSystemId(Request request) {
		return request.params(":systemId");
	}
	
	/**
	 * Returns a {@link Gson} instance customized to handle dates and other formatting.
	 * @return {@link Gson} customized to handle special formatting
	 */
	protected static Gson getGson() {
		return ServiceBaseResource.getGson(false);
	}
	
	/**
	 * Returns a {@link Gson} instance customized to handle dates and other formatting.
	 * @return {@link Gson} customized to handle special formatting
	 */
	protected static Gson getGson(boolean includeNulls) {
		return ServiceBaseResource.getGson(includeNulls, true);
	}
	
	/**
	 * Returns a {@link Gson} instance customized to handle dates and other formatting.
	 * @param includeNulls
	 * @param escapeHTML
	 * @return {@link Gson} customized to handle special formatting
	 */
	protected static Gson getGson(boolean includeNulls, boolean escapeHTML) {
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		gb.registerTypeHierarchyAdapter(Date.class, new DateUtils());
		if(includeNulls) {
			gb.serializeNulls();
		}
		if(!escapeHTML) {
			gb.disableHtmlEscaping();
		}
		
		//gb.excludeFieldsWithoutExposeAnnotation();
		return gb.create();
	}
	
	/**
	 * Returns the JSON representation of the provided object.
	 * 
	 * @param object
	 *            the object
	 * @return {@link String}
	 * @throws JAXBException
	 */
	protected static <T extends Object> String marshalObject(T object) throws JAXBException {
		String json = null;
		if (object != null) {
			Gson gson = getGson();
			json = gson.toJson(object);
			//initMarshaller();
			//StringWriter sw = new StringWriter();
			//MARSHALLER.marshal(object, sw);
			//json = sw.toString();
		}

		return json;
	}

	/**
	 * Returns a {@link List} of {@code T} objects unmarshalled from the provided
	 * JSON string.
	 * 
	 * @param json
	 *            the JSON
	 * @param c
	 *            the type
	 * @return {@link List}<{@code T}>
	 * @throws JAXBException
	 * @throws UnsupportedEncodingException
	 */
	protected static <T extends Object> List<T> unmarshalObject(String json, Class<T> c) {
		List<T> objects = new ArrayList<>();
		Gson gson = getGson();
		boolean tryAsArray = false;
		try {
			T t = gson.fromJson(json, c);
			if(t != null) {
				objects.add(t);
			}
		}
		catch(Exception e) {
			tryAsArray = true;
		}
		
		if(tryAsArray) {
			Object o = Array.newInstance(c, 0);
			o = gson.fromJson(json, o.getClass());
			if(o != null) {
				T[] a = (T[])o;
				for(T i : a) {
					objects.add(i);
				}
			}
		}
		
		//List<T> objects = new ArrayList<>();
		//if (json != null) {
		//	InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8.name()));
		//	objects = unmarshalObject(stream, c);
		//}
		
		return objects;
	}

	/**
	 * Returns a {@link List} of {@code T} objects unmarshalled from the provided
	 * stream.
	 * 
	 * @param stream
	 *            the stream
	 * @param c
	 *            the type
	 * @return {@link List}<{@code T}>
	 * @throws JAXBException
	 */
	/*protected static <T extends Object> List<T> unmarshalObject(InputStream stream, Class<T> c) throws JAXBException {
		List<T> objects = new ArrayList<>();
		if (stream != null) {
			StreamSource source = new StreamSource(stream);
			objects = unmarshalObject(source, c);
		}
		return objects;
	}*/

	/**
	 * Returns a {@link List} of {@code T} objects unmarshalled from the provided source.
	 * @param source the source
	 * @param c the type
	 * @return {@link List}<{@code T}>
	 * @throws JAXBException
	 */


	@SuppressWarnings("unchecked")
	/*protected static <T extends Object> List<T> unmarshalObject(StreamSource source, Class<T> c) throws JAXBException {
	List<T> objects = new ArrayList<>();

		if(source != null) {
			initUnmarshaller();
			Object o = UNMARSHALLER.unmarshal(source, c).getValue();
			if(o.getClass() == c) {
				objects.add((T) o);
			}
			else {
				objects = (List<T>)o;
			}
		}

		return objects;
	}*/

	/**
	 * Initializes the JAXB context.
	 * 
	 * @throws JAXBException
	 */
	private static void initJaxbContext() throws JAXBException {
		if (JAXB_CONTEXT == null) {
			Map<String, Object> properties = new HashMap<>();
			properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);

			Class[] classes = new Class[] { Dataframe.class, Dataframe[].class, Comment.class, Comment[].class,
					LineageItem.class, AssemblyLineageItem.class, DataframeLineageItem.class, LineageItem[].class,
					AssemblyLineageItem[].class, DataframeLineageItem[].class, Dataset.class, Dataset[].class,
					ObjectFactory.class, QCRequest.class, QCRequest[].class, Assembly.class, Assembly[].class,
					EquipObject.class, EquipObject[].class, ReportingEvent.class, PublishEvent.class, PublishItem.class,
					ReportingEventItem.class, PublishStatus.class, ReleaseStatus.class };
			JAXB_CONTEXT = JAXBContext.newInstance(classes, properties);
		}
	}

	/**
	 * Initializes the dataframe marshaller.
	 * 
	 * @throws JAXBException
	 */
	private static void initMarshaller() throws JAXBException {
		if (MARSHALLER == null) {
			initJaxbContext();

			MARSHALLER = JAXB_CONTEXT.createMarshaller();
			MARSHALLER.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
	}

	/**
	 * Initializes the dataframe unmarshaller.
	 * 
	 * @throws JAXBException
	 * 
	 */
	private static void initUnmarshaller() throws JAXBException {
		if (UNMARSHALLER == null) {
			initJaxbContext();

			UNMARSHALLER = JAXB_CONTEXT.createUnmarshaller();
		}
	}

	protected static ReportingAndPublishingDAO getReportingAndPublishingDAO() {
		return new ReportingAndPublishingDAO();
	}
	
	protected static final void returnJson(Response response) {
		returnJSON(null, response, false, true);
	}
	
	protected static final String returnJSON(Object o, Response response) {
		return ServiceBaseResource.returnJSON(o, response, false);
	}
	
	protected static final String returnJSON(Object o, Response response, boolean includeNulls) {
		return ServiceBaseResource.returnJSON(o, response, false, true);
	}
	
	protected static final String returnJSON(Object o, Response response, boolean includeNulls, boolean escapeHTML) {
		String json = null;
		if(o != null) {
			Gson gson = ServiceBaseResource.getGson(includeNulls, escapeHTML);
			json = gson.toJson(o);
		}
		
		if(response != null) {
			response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
		}
		
		return json;
	}
	
	protected static final boolean userHasAccess(ModeShapeNode node, String userId) {
		if(node != null) {
			return ServiceBaseResource.userHasAccess(node.toEquipObject(), userId);
		}
		
		return false;
	}
	
	protected static final boolean userHasAccess(EquipObject eo, String userId) {
		return ServiceBaseResource.userHasAccess(eo, userId, new HashMap<>());
	}
	
	protected static final boolean userHasAccess(EquipObject eo, String userId, Map<String, EquipObject> dataMap) {
		boolean canView = false;
		if(eo != null) {
			if(dataMap == null) {
				dataMap = new HashMap<>();
			}
			
			if(dataMap.get(eo.getId()) == null) {
				dataMap.put(eo.getId(), eo);
			}
			
			try {
				if(AUTH_DAO == null) {
					AUTH_DAO = new AuthorizationDAO();
				}
				
				if(eo instanceof Assembly) {
					Assembly a = (Assembly) eo;
					canView = AUTH_DAO.canViewAssembly(a, userId, dataMap);
				}
				else if(eo instanceof Dataframe) {
					Dataframe dataframe = (Dataframe) eo;
					canView = AUTH_DAO.canViewDataframe(dataframe, userId);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return canView;
	}
	
	protected static final void  handleUserAccess(ModeShapeNode node, String userId) {
		if(node != null) {
			ServiceBaseResource.handleUserAccess(node.toEquipObject(), userId);
		}
	}
	
	protected static final void handleUserAccess(EquipObject eo, String userId) {
		if(eo != null) {
			try {
				if(eo instanceof Assembly) {
					Assembly a = (Assembly) eo;
					boolean canView = ServiceBaseResource.userHasAccess(a, userId);
					if(!canView) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User does not have authorization to view one or more dataframes or assemblies within " + a.getAssemblyType() + " " + a.getEquipId() + ".");
					}
				}
				else if(eo instanceof Dataframe) {
					Dataframe dataframe = (Dataframe) eo;
					boolean canView = ServiceBaseResource.userHasAccess(dataframe, userId);
					if(!canView) {
						Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User does not have authorization to view " + dataframe.getDataframeType() + " " + dataframe.getEquipId() + ".");
					}
				}
			}
			catch(HaltException he) {
				throw he;
			}
			
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected static void setSubInfo(EquipObject eo, String user) {
		if(eo instanceof EquipCommentable) {
			List<Comment> comments = ((EquipCommentable) eo).getComments();
			if(comments != null) {
				for(Comment c : comments) {
					ServiceBaseResource.setCreatedInfo(c, user);
				}
			}
		}
		
		if(eo instanceof EquipMetadatable) {
			List<Metadatum> metadata = ((EquipMetadatable) eo).getMetadata();
			if(metadata != null) {
				for(Metadatum md : metadata) {
					ServiceBaseResource.setCreatedInfo(md, user);
				}
			}
		}
	}
	
	protected static void setCreatedInfo(EquipObject eo, String user) {
		if(eo instanceof EquipCreatable) {
			EquipCreatable ec = (EquipCreatable) eo;
			if(ec.getCreated() == null) {
				ec.setCreated(new Date());
			}
			if(ec.getCreatedBy() == null || ec.getCreatedBy().isEmpty()) {
				ec.setCreatedBy(user);
			}
		}
	}
}