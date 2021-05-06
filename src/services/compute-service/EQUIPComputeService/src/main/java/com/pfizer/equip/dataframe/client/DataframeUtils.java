package com.pfizer.equip.dataframe.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.equip.computeservice.dao.ComputeDAOImpl;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;

public class DataframeUtils {
	private static Logger log = LoggerFactory.getLogger(DataframeUtils.class);	
	private static final String DATA_TRANSFORMATION = "data transformation";
	private static String COMPLEX_DATA_REGEX = "(\\\"equip:complexData\\\":\\{)(.+)(\\}.+)";
	private static Pattern COMPLEX_DATA_PATTERN = Pattern.compile(COMPLEX_DATA_REGEX, Pattern.CASE_INSENSITIVE);
	private static String COMPLEX_DATA_URI_REGEX = "(\\\"self\\\":\\\")(.+/equip%3adataset/equip%3acomplexData)(\\\")(.+)";
	private static Pattern COMPLEX_DATA_URI_PATTERN = Pattern.compile(COMPLEX_DATA_URI_REGEX, Pattern.CASE_INSENSITIVE);
	private static String EQUIP_ID_REGEX = "(\\\"equip:equipId\\\":)\\\"([A-Z,a-z,0-9,\\-]+)\\\"";
	private static Pattern EQUIP_ID_PATTERN = Pattern.compile(EQUIP_ID_REGEX, Pattern.CASE_INSENSITIVE);
	
	private DataframeUtils() {}

	public static Dataframe createChildFrom(
			String dataframeType, 
			List<Assembly> assemblyParents, 
			List<Dataframe> dataframeParents,
			boolean isSystemReport) {
		Dataframe returnValue = setChildCommonValues();
		if (dataframeType == null) {
			dataframeType = "Data Transformation";
		}
		List<Metadatum> parentMetadata = DataframeUtils.getMetadataForChild(assemblyParents, dataframeParents, false);
		returnValue.setMetadata(parentMetadata);
//		List<Comment> parentComments = DataframeUtils.getCommentsForChild(assemblyParents, dataframeParents);
//		returnValue.setComments(parentComments);
		if (returnValue.getProfileConfig().isEmpty()) {
			for (Metadatum metadatum : parentMetadata) {
				if (metadatum.getKey().equalsIgnoreCase("Profile Configuration")
						&& !metadatum.getValue().isEmpty())
				{
					returnValue.getProfileConfig().add(metadatum.getValue().get(0));
					break;
				}
			}
		}

		return returnValue;
	}

	public static List<Comment> getCommentsForChild(
			List<Assembly> assemblyParents, 
			List<Dataframe> dataframeParents) {
		List<Comment> unfilteredComments = new ArrayList<>();
		List<Comment> comments = new ArrayList<>();
		if (assemblyParents != null) {
			unfilteredComments = getCommentsFromAssemblies(assemblyParents, unfilteredComments);
		}
		if (dataframeParents != null) {
			unfilteredComments = getCommentsFromDataframes(dataframeParents, unfilteredComments);
		}

		Collections.sort(unfilteredComments, (c1, c2) -> c2.getCreated().compareTo(c1.getCreated()));

		for (Comment comment : unfilteredComments) {
			if (!comment.isDeleted() && !comments.contains(comment)) {
				comments.add(comment);
			}
		}
		return comments;
	}

	public static List<Metadatum> getMetadataForChild(
			List<Assembly> assemblyParents, 
			List<Dataframe> dataframeParents,
			boolean isSystemReport) {
		List<Metadatum> unfilteredMetadata = new ArrayList<>();
		List<Metadatum> metadata = new ArrayList<>();
		if (assemblyParents != null) {
			unfilteredMetadata = getMetadataFromAssemblies(assemblyParents, unfilteredMetadata);
		}
		if (dataframeParents != null) {
			unfilteredMetadata = getMetadataFromDataframes(dataframeParents, unfilteredMetadata);
		}

		unfilteredMetadata.removeIf(m -> m.getKey().equalsIgnoreCase("output filename"));
		unfilteredMetadata.removeIf(m -> m.getKey().equalsIgnoreCase("Reporting Events"));

		for (Metadatum metadatum : unfilteredMetadata) {
			if (!metadatum.isDeleted() && !metadata.contains(metadatum)) {
				metadata.add(metadatum);
			}
		}
		
		return metadata;
	}

    public static void setDatasetFileMimeType(ModeshapeClient msc, String datasetId, String mimeType) throws ServiceCallerException {
    	try {
	    	// Get the dataset URI
	    	String json = msc.retrieveNodeById("equip", "nca", datasetId);
	    	json = json.replace("\\/", "/");
			Matcher m1 = COMPLEX_DATA_PATTERN.matcher(json);
			String datasetComplexData = null;
			String datasetComplexDataUri = null;
			if (m1.find()) {
				datasetComplexData = m1.group(2);
				Matcher m2 = COMPLEX_DATA_URI_PATTERN.matcher(datasetComplexData);
				if (m2.find()) {
					datasetComplexDataUri = m2.group(2);
				} else {
					String errMsg = String.format("Unable to match complex data pattern against %s",  json);
					throw new Exception(errMsg);
				}
			} else {
				String errMsg = String.format("Unable to match complex data pattern against %s",  json);
				throw new Exception(errMsg);
			}
			String datasetComplexDataPath = datasetComplexDataUri.substring(datasetComplexDataUri.indexOf("Programs"));
			String datasetContentPath = datasetComplexDataPath + "/jcr%3acontent";
	    	// set the dataset file mimetype
			String updateJson = String.format("{\"jcr:mimeType\":\"%s\"}", mimeType);
 			msc.updateNodeOrProperty("equip", "nca", datasetContentPath, updateJson);
    	} catch (Exception ex) {
    		log.error("", ex);
    		throw new ServiceCallerException(ex);
    	}
	}
	
	private static List<Comment> getCommentsFromAssembly(Assembly parent, List<Comment> unfilteredComments) {
		if (parent.getComments() != null) {
			for (Comment comment : parent.getComments()) {
				if (!comment.isDeleted()) {
					unfilteredComments.add(comment);
				}
			}
		}
		return unfilteredComments;
	}
	
	private static List<Comment> getCommentsFromAssemblies(List<Assembly> parents, List<Comment> unfilteredComments) {
		for (Assembly parent : parents) {
			getCommentsFromAssembly(parent, unfilteredComments);
		}
		return unfilteredComments;
	}
	
	private static List<Comment> getCommentsFromDataframe(Dataframe parent, List<Comment> unfilteredComments) {
		if (parent.getComments() != null) {
			for (Comment comment : parent.getComments()) {
				if (!comment.isDeleted()) {
					unfilteredComments.add(comment);
				}
			}
		}
		return unfilteredComments;
	}
	
	private static List<Comment> getCommentsFromDataframes(List<Dataframe> parents, List<Comment> unfilteredComments) {
		for (Dataframe parent : parents) {
			getCommentsFromDataframe(parent, unfilteredComments);
		}
		return unfilteredComments;
	}
	
	private static List<Metadatum> getMetadataFromAssembly(Assembly parent, List<Metadatum> unfilteredMetadata) {
		if (parent.getMetadata() != null) {
			for (Metadatum metadatum : parent.getMetadata()) {
				if (!metadatum.isDeleted()) {
					unfilteredMetadata.add(metadatum);
				}
			}
		}
		return unfilteredMetadata;
	}
	
	private static List<Metadatum> getMetadataFromAssemblies(List<Assembly> parents, List<Metadatum> unfilteredMetadata) {
		for (Assembly parent : parents) {
			getMetadataFromAssembly(parent, unfilteredMetadata);
		}
		return unfilteredMetadata;
	}
	
	private static List<Metadatum> getMetadataFromDataframe(Dataframe parent, List<Metadatum> unfilteredMetadata) {
		if (parent.getMetadata() != null) {
			for (Metadatum metadatum : parent.getMetadata()) {
				if (!metadatum.isDeleted()) {
					unfilteredMetadata.add(metadatum);
				}
			}
		}
		return unfilteredMetadata;
	}
	
	private static List<Metadatum> getMetadataFromDataframes(List<Dataframe> parents, List<Metadatum> unfilteredMetadata) {
		for (Dataframe parent : parents) {
			getMetadataFromDataframe(parent, unfilteredMetadata);
		}
		return unfilteredMetadata;
	}
	
	private static Dataframe setChildCommonValues() {
		Dataframe returnValue = new Dataframe();
		returnValue.setDataBlindingStatus("Unblinded");
		returnValue.setRestrictionStatus("Not Restricted");
		returnValue.setPromotionStatus("Not Promoted");
		returnValue.setDataStatus("Draft");
		returnValue.setDataframeType("Data Transformation");
		return returnValue;
	}
	
	public static String getEquipIdForNode(ModeshapeClient msc, String uuid) throws ServiceCallerException {
		try {
			String json = msc.retrieveNodeById("equip", "nca", uuid);
			Matcher m1 = EQUIP_ID_PATTERN.matcher(json);
			String equipId = null;
			if (m1.find()) {
				equipId = m1.group(2);
			}
			// log.info("getEquipIdForNode returns: " + equipId);
			return equipId;
    	} catch (Exception ex) {
    		log.error("", ex);
    		throw new ServiceCallerException(ex);
    	}
	}
	
	public static List<String> getEquipIdsForNodes(ModeshapeClient msc, List<String> uuids) throws ServiceCallerException {
		List<String> returnValue = new ArrayList<>();
		for (String uuid : uuids) {
			returnValue.add(getEquipIdForNode(msc, uuid));
		}
		return returnValue;
	}

}
