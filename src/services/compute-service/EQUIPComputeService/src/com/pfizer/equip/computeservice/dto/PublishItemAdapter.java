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
import com.pfizer.pgrd.equip.dataframe.dto.PublishItem;

public class PublishItemAdapter extends TypeAdapter<PublishItem> {
	private static Logger log = LoggerFactory.getLogger(PublishItemAdapter.class);	

	@Override
	public PublishItem read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		PublishItemPublishStatusChangeWorkflowAdapter pipscwfa = new PublishItemPublishStatusChangeWorkflowAdapter();
		PublishItem pia = new PublishItem();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "name":
				pia.setName(jReader.nextString());
				break;
			case "equipId":
				pia.setEquipId(jReader.nextString());
				break;
			case "publishItemTemplateId":
				pia.setPublishItemTemplateId(jReader.nextString());
				break;
			case "reportingEventItemId":
				pia.setReportingEventItemId(jReader.nextString());
				break;
			case "expirationDate":	// a Date object
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pia.setExpirationDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "publishedViewTemplateId":
				pia.setPublishedViewTemplateId(jReader.nextString());
				break;
			case "publishedViewFilterCriteria":
				pia.setPublishedViewFilterCriteria(jReader.nextString());
				break;
			case "publishedViewSubFilter":
				pia.setPublishedViewSubFilter(jReader.nextString());
				break;
			case "publishStatus":
				pia.setPublishStatus(jReader.nextString());
				break;
			case "publishedDate":	// a Date object
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pia.setPublishedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "publishEventId":
				pia.setPublishEventId(jReader.nextString());
				break;
			case "publishedTags":	// a list of strings
				jReader.beginArray();
				while (jReader.hasNext()) {
					pia.getPublishedTags().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "workflowItems":	// a list of objects
				jReader.beginArray();
				while (jReader.hasNext()) {
					pia.getWorkflowItems().add(pipscwfa.read(jReader));
				}
				jReader.endArray();
				break;
			case "versionNumber":
				pia.setVersionNumber(jReader.nextLong());
				break;
			case "obsoleteFlag":
				pia.setObsoleteFlag(jReader.nextBoolean());
				break;
			case "isCommitted":
				pia.setCommitted(jReader.nextBoolean());
				break;
			case "versionSuperSeded":
				pia.setVersionSuperSeded(jReader.nextBoolean());
				break;
			case "deleteFlag":
				pia.setDeleteFlag(jReader.nextBoolean());
				break;
				
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					pia.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					pia.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pia.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				pia.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					pia.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				pia.setModifiedBy(jReader.nextString());
				break;
			case "id":
				pia.setId(jReader.nextString());
				break;
			case "entityType":
				pia.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return pia;
	}

	@Override
	public void write(JsonWriter jWriter, PublishItem publishItem) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
