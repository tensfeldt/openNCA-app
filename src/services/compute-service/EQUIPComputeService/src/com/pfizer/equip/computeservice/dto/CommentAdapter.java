package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class CommentAdapter extends TypeAdapter<Comment> {
	private static Logger log = LoggerFactory.getLogger(CommentAdapter.class);	

	@Override
	public Comment read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		Comment comment = new Comment();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "body":
				comment.setBody(jReader.nextString());
				break;
			case "commentType":
				comment.setCommentType(jReader.nextString());
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					comment.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				comment.setCreatedBy(jReader.nextString());
				break;
			case "isDeleted":
				comment.setDeleted(jReader.nextBoolean());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					comment.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				comment.setModifiedBy(jReader.nextString());
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					comment.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "id":
				comment.setId(jReader.nextString());
				break;
			case "entityType":
				comment.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return comment;
	}

	@Override
	public void write(JsonWriter jWriter, Comment comment) throws IOException {
		// TODO Auto-generated method stub
	}

}
