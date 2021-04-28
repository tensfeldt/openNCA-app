package com.pfizer.equip.lineage.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.pfizer.modeshape.api.client.ModeshapeClient;
import com.pfizer.modeshape.api.client.ModeshapeClientException;

public class ReportingEventStatusChangeWorkflowAdapter extends TypeAdapter<ReportingEventStatusChangeWorkflow> {
	private ModeshapeClient msClient;
	
	public ReportingEventStatusChangeWorkflowAdapter(ModeshapeClient msClient) {
		this.msClient = msClient;
	}

	@Override
	public ReportingEventStatusChangeWorkflow read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		ReportingEventStatusChangeWorkflow returnValue = new ReportingEventStatusChangeWorkflow();
		reader.beginObject();
		String name = reader.peek().name();
		while (reader.hasNext()) {
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
			case "equip:equipId":
				returnValue.setEquipId(reader.nextString());
				break;
			case "equip:reportingEventStatusChangeDescription":
				returnValue.setReportingEventStatusWorkflowDescription(reader.nextString());
				break;
			case "equip:reportingEventReleaseStatus":
				returnValue.setReportingEventReleaseStatus(reader.nextString());
				break;
			case "equip:reportingEventReopenReason":
				returnValue.setReportingEventReopenReasonKey(reader.nextString());
				break;
			case "equip:reportingEventReopenReasonAttachmentId":
				returnValue.setReportingEventReopenReasonAttachmentId(reader.nextString());
				break;
			case "children":
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
			default:
				reader.skipValue();
				break;
			}
		}
		reader.endObject();
		return returnValue;
	}

	@Override
	public void write(JsonWriter writer, ReportingEventStatusChangeWorkflow rescw) throws IOException {
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

}
