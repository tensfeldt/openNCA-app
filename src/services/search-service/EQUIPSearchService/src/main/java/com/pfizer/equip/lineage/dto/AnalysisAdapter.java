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

public class AnalysisAdapter extends TypeAdapter<Analysis> {
	private String regexString = "([a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}?)";
	private Pattern regexPattern = Pattern.compile(regexString, Pattern.MULTILINE);
	private ModeshapeClient msClient;
	
	public AnalysisAdapter(ModeshapeClient msClient) {
		this.msClient = msClient;
	}

	@Override
	public Analysis read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		Analysis returnValue = new Analysis();
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
				case "equip:assemblyType":
					returnValue.setAssemblyType(reader.nextString());
					break;
				case "equip:name":
					returnValue.setName(reader.nextString());
					break;
				case "equip:studyId":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						returnValue.setStudyIds(values.toArray(new String[values.size()]));
					} finally {
						reader.endArray();
					}
					break;
				case "equip:equipId":
					returnValue.setEquipId(reader.nextString());
					break;
				case "equip:parentIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setParentIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:dataframeIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setDataframeIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:assemblyIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setAssemblyIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:lockFlag":
					returnValue.setLocked(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:lockedByUser":
					returnValue.setLockedByUser(reader.nextString());
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
				case "equip:dataStatus":
					returnValue.setDataStatus(reader.nextString());
					break;
				case "equip:promotionStatus":
					returnValue.setPromotionStatus(reader.nextString());
					break;
				case "equip:qcStatus":
					returnValue.setQcStatus(reader.nextString());
					break;
				case "equip:published":
					returnValue.setPublished(Boolean.parseBoolean(reader.nextString()));
					break;
				case "equip:released":
					returnValue.setReleased(Boolean.parseBoolean(reader.nextString()));
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
							MetadataAdapter metadataAdapter = new MetadataAdapter();
							CommentAdapter commentAdapter = new CommentAdapter(msClient);
							List<Metadata> metadata = new ArrayList<>();
							List<Comment> comments = new ArrayList<>();
							name = reader.peek().name();
							while (reader.hasNext()) {
								name = reader.nextName();
								if (name.startsWith("equip:metadatum")) {
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
				case "equip:itemType":
					returnValue.setItemType(reader.nextString());
					break;
				case "equip:subType":
					returnValue.setSubType(reader.nextString());
					break;
				case "equip:description":
					returnValue.setDescription(reader.nextString());
					break;
				case "equip:protocolIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setProtocolIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:projectIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setProjectIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:programIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setProgramIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:publishedItemIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setPublishedItemIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:reportingEventItemIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setReportingItemIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:loadStatus":
					returnValue.setLoadStatus(reader.nextString());
					break;
				case "equip:analysisType":
					returnValue.setAnalysisType(reader.nextString());
					break;
				case "equip:subsetDataframeIds":
					reader.beginArray();
					try {
						List<String> values = new ArrayList<>();
						while (reader.hasNext()) {
							values.add(reader.nextString());
						}
						try {
							List<String> ids = fetchIds(msClient, values);
							returnValue.setSubsetDataframeIds(ids.toArray(new String[ids.size()]));
						} catch (ModeshapeClientException ex) {
							throw new IOException(ex);
						}
					} finally {
						reader.endArray();
					}
					break;
				case "equip:kelFlagsDataframeId":
					try {
						returnValue.setKelFlagsDataframeId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
					break;
				case "equip:parametersDataframeId":
					try {
						returnValue.setParametersDataframeId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
					break;
				case "equip:modelConfigurationDataframeId":
					try {
						returnValue.setModelConfigurationDataframeId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
					break;
				case "equip:configurationTemplateId":
					try {
						returnValue.setConfigurationTemplateId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
					break;
				case "equip:profileSettingsDataframeId":
					try {
						returnValue.setProfileSettingsDataframeId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
					break;
				case "equip:estimatedConcDataframeId":
					try {
						returnValue.setEstimatedConcDataframeId(fetchId(msClient, reader.nextString()));
					} catch (ModeshapeClientException ex) {
						throw new IOException(ex);
					}
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
	public void write(JsonWriter writer, Analysis analysis) throws IOException {
		throw new UnsupportedOperationException();
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
