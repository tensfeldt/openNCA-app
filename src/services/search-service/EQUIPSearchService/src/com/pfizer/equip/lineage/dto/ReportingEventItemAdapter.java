package com.pfizer.equip.lineage.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

public class ReportingEventItemAdapter extends TypeAdapter<ReportingEventItem> {
	private String regexString = "([a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";
	private Pattern regexPattern = Pattern.compile(regexString, Pattern.MULTILINE);
	private ModeshapeClient msClient;
	
	public ReportingEventItemAdapter(ModeshapeClient msClient) {
		this.msClient = msClient;
	}


	@Override
	public ReportingEventItem read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		ReportingEventItem returnValue = new ReportingEventItem();
		reader.beginObject();
		String name = reader.peek().name();
		try {
			while (!name.equals("END_DOCUMENT") && reader.hasNext()) {
				name = reader.nextName();
				switch(name) {
				case "self":
				case "up":
				case "jcr:primaryType":
				case "jcr:lastModifiedBy":
				case "jcr:lastModified":
				case "jcr:createdBy":
				case "jcr:created":
					reader.skipValue();
					break;
				case "id":
					returnValue.setId(reader.nextString());
					break;
				case "equip:name":
					returnValue.setName(reader.nextString());
					break;
				case "equip:equipId":
					returnValue.setEquipId(reader.nextString());
					break;
				case "equip:deleteFlag":
					returnValue.setDeleteFlag(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:versionSuperSeded":
					returnValue.setVersionSuperSeded(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:versionCommitted":
					returnValue.setCommitted(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:versionNumber":
					returnValue.setVersionNumber(Long.parseLong(reader.nextString()));
					break;
				case "equip:created":
					{
						OffsetDateTime odt = OffsetDateTime.parse(reader.nextString());
						returnValue.setCreated(odt.toInstant());
					}
					break;
				case "equip:createdBy":
					returnValue.setCreatedBy(reader.nextString());
					break;
				case "equip:modified":
					{
						OffsetDateTime odt = OffsetDateTime.parse(reader.nextString());
						returnValue.setModifiedDate(odt.toInstant());
					}
					break;
				case "equip:modifiedBy":
					returnValue.setModifiedBy(reader.nextString());
					break;
				case "children":
					try {
						reader.beginObject();
						try {
							ReportingEventStatusChangeWorkflowAdapter statusChangeWorkflowAdapter = new ReportingEventStatusChangeWorkflowAdapter(msClient);
							PromotionAdapter promotionAdapter = new PromotionAdapter(msClient);
							MetadataAdapter metadataAdapter = new MetadataAdapter();
							CommentAdapter commentAdapter = new CommentAdapter(msClient);
							List<ReportingEventStatusChangeWorkflow> statusChangeWorkflows = new ArrayList<>();
							List<Promotion> promotions = new ArrayList<>();
							List<Metadata> metadata = new ArrayList<>();
							List<Comment> comments = new ArrayList<>();
							Script script = null;
							name = reader.peek().name();
							while (reader.hasNext()) {
								name = reader.nextName();
								if (name.startsWith("equip:publishedItem")) {
									reader.skipValue();
//									try {
//										promotions.add(retrievePromotion(reader, promotionAdapter));
//									} catch (ModeshapeClientException ex) {
//										throw new IOException(ex);
//									}
								} else if (name.startsWith("equip:reportingEventStatusChangeWorkflow")) {
									try {
										statusChangeWorkflows.add(retrieveStatusChangeWorkFlow(reader, statusChangeWorkflowAdapter));
									} catch (ModeshapeClientException ex) {
										throw new IOException(ex);
									}
								} else if (name.startsWith("equip:metadatum")) {
									try {
										metadata.add(retrieveMetadata(reader, metadataAdapter));
									} catch (ModeshapeClientException ex) {
										throw new IOException(ex);
									}
								} else if (name.startsWith("equip:comment")) {
									try {
										comments.add(retrieveComment(reader, commentAdapter));
									} catch (ModeshapeClientException ex) {
										throw new IOException(ex);
									}
								} else {
									reader.skipValue();
								}
							}
							returnValue.setReportingEventStatusChangeWorkflows(statusChangeWorkflows.toArray(new ReportingEventStatusChangeWorkflow[statusChangeWorkflows.size()]));
							returnValue.setMetadata(metadata.toArray(new Metadata[metadata.size()]));
							returnValue.setComments(comments.toArray(new Comment[comments.size()]));
						} finally {
							reader.endObject();
						}
					} finally {
						reader.endObject();
					}
					break;
				case "equip:obsoleteFlag":
					returnValue.setObsoleteFlag(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:included":
					returnValue.setIncluded(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:dataframeId":
					returnValue.setDataframeId(reader.nextString());
					break;
				case "equip:assemblyId":
					returnValue.setAssemblyId(reader.nextString());
					break;
				case "equip:parentReportingEventId":
					returnValue.setParentReportingEventId(reader.nextString());
					break;
				default:
					reader.skipValue();
					break;
				}
				name = reader.peek().name();
			}
		} finally {
			if (!name.equals("END_DOCUMENT")) {
				reader.endObject();
			}
		}
		return returnValue;
	}

	@Override
	public void write(JsonWriter writer, ReportingEventItem reportingEventItem) throws IOException {
		throw new UnsupportedOperationException();
	}

	private ReportingEventStatusChangeWorkflow retrieveStatusChangeWorkFlow(JsonReader reader,
			ReportingEventStatusChangeWorkflowAdapter statusChangeWorkflowAdapter)  throws IOException, ModeshapeClientException {
		ReportingEventStatusChangeWorkflow returnValue = null;
		reader.beginObject();
		try {
			returnValue = statusChangeWorkflowAdapter.read(reader);
		} finally {
			reader.endObject();
		}
		return returnValue;
	}

	private Metadata retrieveMetadata(JsonReader reader, MetadataAdapter metadataAdapter) throws IOException, ModeshapeClientException {
		Metadata returnValue = null;
		reader.beginObject();
		try {
			String name = reader.peek().name();
			while (reader.hasNext()) {
				name = reader.nextName();
				if (name.equals("id")) {
					String uuid = reader.nextString();
					String json = msClient.retrieveNodeById("equip", "nca", uuid);
					returnValue = metadataAdapter.fromJson(json.replaceAll("\\/", "/"));
				} else {
					reader.skipValue();
				}
			}
		} finally {
			reader.endObject();
		}
		return returnValue;
	}

	private Comment retrieveComment(JsonReader reader, CommentAdapter commentAdapter) throws IOException, ModeshapeClientException {
		Comment returnValue = null;
		reader.beginObject();
		try {
			String name = reader.peek().name();
			while (reader.hasNext()) {
				name = reader.nextName();
				if (name.equals("id")) {
					String uuid = reader.nextString();
					String json = msClient.retrieveNodeById("equip", "nca", uuid);
					returnValue = commentAdapter.fromJson(json.replaceAll("\\/", "/"));
				} else {
					reader.skipValue();
				}
			}
		} finally {
			reader.endObject();
		}
		return returnValue;
	}

	private List<String> fetchIds(ModeshapeClient msClient, List<String> uris) throws ModeshapeClientException {
		List<String> returnValue = new ArrayList<>();
		for (String uri : uris) {
			String uuid = fetchId(msClient, uri);
			if (uuid != null) {
				returnValue.add(uuid);
			}
		}
		return returnValue;
	}

	private String fetchId(ModeshapeClient msClient, String uri) throws ModeshapeClientException {
		String json = msClient.retrieveNodeByUri(String.format("%s/%s", uri, "jcr:uuid"));
		Matcher matcher = regexPattern.matcher(json);
		if (matcher.find()) {
			String returnValue = matcher.group();
			if (returnValue.startsWith("\"")) {
				returnValue = returnValue.substring(1);
			}
			return returnValue;
		}
		return null;
	}

}
