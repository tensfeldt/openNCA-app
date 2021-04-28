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

public class CommentAdapter extends TypeAdapter<Comment> {
	private ModeshapeClient msClient;
	
	public CommentAdapter(ModeshapeClient msClient) {
		this.msClient = msClient;
	}

	@Override
	public Comment read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		Comment returnValue = new Comment();
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
			case "equip:commentType":
				returnValue.setCommentType(reader.nextString());
				break;
			case "equip:body":
				returnValue.setBody(reader.nextString());
				break;
			case "equip:deleteFlag":
				returnValue.setDeleted(Boolean.parseBoolean(reader.nextString()));
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
					MetadataAdapter metadataAdapter = new MetadataAdapter();
					List<Metadata> metadata = new ArrayList<>();
					reader.beginObject();
					name = reader.peek().name();
					while (reader.hasNext()) {
						name = reader.nextName();
						if (name.startsWith("equip:metadatum")) {
							try {
								metadata.add(retrieveMetadata(reader, metadataAdapter));
							} catch (ModeshapeClientException ex) {
								throw new IOException(ex);
							}
						} else {
							reader.skipValue();
						}
					}
					returnValue.setMetadata(metadata.toArray(new Metadata[metadata.size()]));
				} finally {
					reader.endObject();
				}
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
	public void write(JsonWriter writer, Comment comment) throws IOException {
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


}
