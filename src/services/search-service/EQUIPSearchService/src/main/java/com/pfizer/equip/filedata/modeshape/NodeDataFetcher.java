package com.pfizer.equip.filedata.modeshape;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.pfizer.equip.searchable.dto.InstantDeserializer;
import com.pfizer.equip.searchable.dto.NtResource;
import com.pfizer.equip.searchable.dto.SearchableData;
import com.pfizer.equip.searchable.exceptions.InvalidCsvFileException;
import com.pfizer.equip.searchable.exceptions.SearchableDataException;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

/**
 * Retrieves nodes from Modeshape.
 * 
 * @author HeinemanWP
 *
 */
public class NodeDataFetcher {
	private static Logger log = LoggerFactory.getLogger(NodeDataFetcher.class);
	private static final String ES_INDEX_PREFIX = "162d9619a77200";
	private static String REPOSITORY = "equip"; 
	private static String WORKSPACE = "nca";
	private ModeshapeClient msc;
	private JsonParser jp = new JsonParser();
	// private static String ID_REGEX = "(\\\"id\\\":\\\")([a-f 0-9 -]+)";
	private static String ID_REGEX = "([a-f\\d]+(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";
	private static Pattern ID_PATTERN = Pattern.compile(ID_REGEX, Pattern.CASE_INSENSITIVE);
	
	public NodeDataFetcher(ModeshapeClient msc) {
		this.msc = msc;
	}

	public SearchableData fetchNode(String id) throws SearchableDataException {
		String node = fetchNodeEx(id);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		SearchableData returnValue = gson.fromJson(node, SearchableData.class);
		returnValue.setIndexKey(ES_INDEX_PREFIX + returnValue.getJcrUuid());
		if (id.length() > 36) {
			returnValue.setFileId(id.substring(14));
		} else {
			returnValue.setFileId(id);
		}
		return returnValue;
	}

	public SearchableData fetchNodeByPath(String path) throws SearchableDataException {
		String node = null;
		try {
			node = fetchNodeExByPath(path);
		} catch (SearchableDataException ex) {
			if (ex.getMessage().contains("java.io.FileNotFoundException")) {
				return null;
			}
			throw ex;
		}
		String id = getId(node);
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		SearchableData returnValue = gson.fromJson(node, SearchableData.class);
		returnValue.setJcrUuid(id);
		returnValue.setIndexKey(ES_INDEX_PREFIX + id);
		if (id.length() > 36) {
			returnValue.setFileId(id.substring(14));
		} else {
			returnValue.setFileId(id);
		}
		return returnValue;
	}

	public SearchableData fetchContentNode(String parentId) throws SearchableDataException {
		SearchableData parentNode = fetchNode(parentId);
		String parentPath = parentNode.getJcrPath();
		if (parentPath == null) {
			return null;
		}
		String parentPrimaryType = parentNode.getJcrPrimaryType();
		String childPath = null;
		switch (parentPrimaryType) {
			case "equip:assembly":
			case "equip:analysis":
			case "equip:batch":
				return null;
			case "equip:dataframe":
				childPath = String.format("%s/%s", parentPath, "equip%3adataset/equip%3acomplexData/jcr%3acontent");
				break;
			default:
				childPath = String.format("%s/%s", parentPath, "equip%3acomplexData/jcr%3acontent");
				break;
		}
		return fetchNodeByPath(childPath);
	}

	
	public SearchableData fetchParentNode(String id, String primaryType) throws SearchableDataException {
		try {
			String node = msc.retrieveNodeById(REPOSITORY, WORKSPACE, id);
			node = node.replace("\\/", "/");
			String parentNode = fetchParentNodeEx(node, primaryType);
			Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
			SearchableData fd = gson.fromJson(parentNode, SearchableData.class);
			fd.setIndexKey(ES_INDEX_PREFIX + fd.getJcrUuid());
			if (id.length() > 36) {
				fd.setFileId(id.substring(14));
			} else {
				fd.setFileId(id);
			}
			return fd;
		} catch (ModeshapeClientException e) {
			throw new SearchableDataException(e);
		}
	}
	
	public SearchableData fetchParentNode(String id, List<String> primaryTypes) throws SearchableDataException {
		try {
			if (id.length() > 36) {
				id = id.substring(14);
			}
			String node = msc.retrieveNodeById(REPOSITORY, WORKSPACE, id);
			node = node.replace("\\/", "/");
			String parentNode = fetchParentNodeEx(node, primaryTypes);
			Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
			SearchableData fd = gson.fromJson(parentNode, SearchableData.class);
			fd.setIndexKey(ES_INDEX_PREFIX + fd.getJcrUuid());
			if (id.length() > 36) {
				fd.setFileId(id.substring(14));
			} else {
				fd.setFileId(id);
			}
			return fd;
		} catch (ModeshapeClientException e) {
			throw new SearchableDataException(e);
		}
	}
	
	public SearchableData fetchParentNode(String path) throws ModeshapeClientException {
		String parentPath = extractParentPath(path);
		String parentNode = msc.retrieveNodeByPath(REPOSITORY, WORKSPACE, parentPath);
		parentNode = parentNode.replace("\\/", "/");
		Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
		SearchableData fd = gson.fromJson(parentNode, SearchableData.class);
		fd.setIndexKey(ES_INDEX_PREFIX + fd.getJcrUuid());
		// fd.setFileId(id.substring(14));
		return fd;
	}
	
	public CSVParser fetchCsv(String id) throws SearchableDataException, InvalidCsvFileException {
		try {
			String node = msc.retrieveNodeById(REPOSITORY, WORKSPACE, id);
			node = node.replace("\\/", "/");
			Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer()).create();
			NtResource ntResource = gson.fromJson(node, NtResource.class);
			if (!isCsv(msc.retrieveBinaryFromUri(ntResource.getJcrData()))) {
				throw new InvalidCsvFileException(String.format("File identified by id %s is not a valid CSV file", id));
			}
			Reader in = new InputStreamReader(msc.retrieveBinaryFromUri(ntResource.getJcrData()));
			return new CSVParser(in, CSVFormat.RFC4180.withHeader().withIgnoreEmptyLines().withIgnoreDuplicateHeaders());
		} catch (ModeshapeClientException | IOException e) {
			throw new SearchableDataException(e);
		}
	}
	
	public SearchableData fetchSearchableData(String id, SearchableData parentNode) throws SearchableDataException {
		try {
			SearchableData returnValue = parentNode.copy();
			try(CSVParser csvp = fetchCsv(id)) {
				Map<String, Integer> headerMap = csvp.getHeaderMap();
				for (CSVRecord record : csvp.getRecords()) {
					for (Entry<String, Integer> entry : headerMap.entrySet()) {
						String key = entry.getKey();
						// Elasticsearch doesn't like empty strings for field names
						// so we're going to skip columns that have empty strings for column names.
						if (key == null || key.isEmpty()) {
							continue;
						}
						// Elasticsearch doesn't handle periods in field names
						// the way I want so I replace periods in field names
						// with the ascii equivalent.
						String encodedKey = key.replace(".", "%2E");
						Integer value = entry.getValue();
						if (!returnValue.getRowValues().containsKey(encodedKey)) {
							returnValue.getRowValues().put(encodedKey, new ArrayList<>());
						}
						try {
							if (!record.get(value).isEmpty()) {
								if (!returnValue.getRowValues().get(encodedKey).contains(record.get(value))) {
									returnValue.getRowValues().get(encodedKey).add(record.get(value));
								}
							}
						} catch(ArrayIndexOutOfBoundsException ex) {
							// Ignore
						}
					}
				}
				returnValue.setJcrMimeType("text/csv");
			} catch(InvalidCsvFileException e) {
				// Ignore
			}
			return returnValue;
		} catch (IOException e) {
			throw new SearchableDataException(e);
		}
	}
	
	public void updateNodeMimeType(String id, String mimeType) throws ModeshapeClientException {
		String json = String.format("{\"jcr:mimeType\":\"%s\"}", mimeType);
		msc.updateNodeOrPropertyById(REPOSITORY, WORKSPACE, id, json);
	}

	private String fetchNodeEx(String id) throws SearchableDataException {
		try {
			String node = msc.retrieveNodeById(REPOSITORY, WORKSPACE, id);
			node = node.replaceAll("\\/", "/");
			return node;
		} catch (ModeshapeClientException e) {
			throw new SearchableDataException(e);
		}
	}
	
	private String fetchNodeExByPath(String path) throws SearchableDataException {
		try {
			if (path.contains("\\/")) {
				path = path.replace("\\/", "/");
			}
			if (path.contains("\\")) {
				log.info("path contains \\ : " + path);
			}
			String node = msc.retrieveNodeByPath(REPOSITORY, WORKSPACE, path);
			node = node.replace("\\/", "/");
			return node;
		} catch (ModeshapeClientException e) {
			throw new SearchableDataException(e);
		}
	}
	
	private String fetchParentNodeEx(String node, String primaryType) throws SearchableDataException {
		List<String> primaryTypes = new ArrayList<>();
		primaryTypes.add(primaryType);
		return fetchParentNodeEx(node, primaryTypes);
	}

	private String fetchParentNodeEx(String node, List<String> primaryTypes) throws SearchableDataException {
		String parentUri = extractParentUriFromNode(node);
		if ((parentUri == null) || (parentUri.endsWith("/equip/nca/items/"))) {
			return null;
		}
		try {
			String parentNode = msc.retrieveNodeByUri(parentUri);
			parentNode = parentNode.replace("\\/", "/");
			String parentPrimaryType = extractPrimaryType(parentNode);
			if (primaryTypes.contains(parentPrimaryType)) {
				return parentNode;
			}
			return fetchParentNodeEx(parentNode, primaryTypes);
		} catch (ModeshapeClientException e) {
			throw new SearchableDataException(e);
		}
	}

	private String extractPrimaryType(String node) {
		return jp.parse(node).getAsJsonObject().get("jcr:primaryType").getAsString();
	}

	private String extractParentUriFromNode(String node) {
		return jp.parse(node).getAsJsonObject().get("up").getAsString();
	}

	private String extractParentPath(String path) {
		String[] parts = path.split("/");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part.startsWith("equip:")) {
				break;
			}
			sb.append("/");
			sb.append(part);
		}
		return sb.toString();
	}

	private String getId(String nodeData) {
		Matcher m = ID_PATTERN.matcher(nodeData);
		String returnValue = null;
		if (m.find()) {
			returnValue = m.group();
			if (returnValue.startsWith("\"")) {
				returnValue = returnValue.substring(1);
			}
		}
		return returnValue;
	}
	
    private static boolean isCsv(InputStream inputStream ) {
        try (Reader in = new InputStreamReader(inputStream)) {
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withIgnoreEmptyLines().parse(in);
            for (CSVRecord record : records) {
                if (record.size() < 2) {
                    return false;
                }
                if (!record.isConsistent()) {
                    return false;
                }
                if (record.get(0).startsWith("Missing Report File")) {
                	return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
