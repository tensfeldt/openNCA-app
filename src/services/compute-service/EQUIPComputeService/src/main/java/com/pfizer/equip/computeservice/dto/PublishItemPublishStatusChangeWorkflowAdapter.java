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
import com.pfizer.pgrd.equip.dataframe.dto.PublishItemPublishStatusChangeWorkflow;

public class PublishItemPublishStatusChangeWorkflowAdapter extends TypeAdapter<PublishItemPublishStatusChangeWorkflow> {
	private static Logger log = LoggerFactory.getLogger(PublishItemPublishStatusChangeWorkflowAdapter.class);	

	@Override
	public PublishItemPublishStatusChangeWorkflow read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		PublishItemPublishStatusChangeWorkflow pipscw = new PublishItemPublishStatusChangeWorkflow();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "publishItemPublishStatusChangeDescription":
				pipscw.setPublishItemPublishStatusChangeDescription(jReader.nextString());
				break;
			case "publishStatus":
				pipscw.setPublishStatus(jReader.nextString());
				break;
			case "publishItemId":
				pipscw.setPublishItemId(jReader.nextString());
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					pipscw.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					pipscw.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pipscw.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				pipscw.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pipscw.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				pipscw.setModifiedBy(jReader.nextString());
				break;
			case "id":
				pipscw.setId(jReader.nextString());
				break;
			case "entityType":
				pipscw.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return pipscw;
	}

	@Override
	public void write(JsonWriter jWriter, PublishItemPublishStatusChangeWorkflow pipscw) throws IOException {
		// TODO Auto-generated method stub		
	}

}
