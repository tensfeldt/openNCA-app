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
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventItem;

public class ReportingEventItemAdapter extends TypeAdapter<ReportingEventItem> {
	private static Logger log = LoggerFactory.getLogger(ReportingEventItemAdapter.class);	

	@Override
	public ReportingEventItem read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		PublishItemAdapter publishItemAdapter = new PublishItemAdapter();
		ReportingEventItem rei = new ReportingEventItem();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "name":
				rei.setName(jReader.nextString());
				break;
			case "equipId":
				rei.setEquipId(jReader.nextString());
				break;
			case "reportingEventId":
				rei.setReportingEventId(jReader.nextString());
				break;
			case "dataframeId":
				rei.setDataFrameId(jReader.nextString());
				break;
			case "assemblyId":
				rei.setAssemblyId(jReader.nextString());
				break;
			case "included":
				rei.setIncluded(jReader.nextBoolean());
				break;
			case "publishItem":
				rei.setPublishItem(publishItemAdapter.read(jReader));
			case "versionNumber":
				rei.setVersionNumber(jReader.nextLong());
				break;
			case "obsoleteFlag":
				rei.setObsoleteFlag(jReader.nextBoolean());
				break;
			case "isCommitted":
				rei.setCommitted(jReader.nextBoolean());
				break;
			case "versionSuperSeded":
				rei.setVersionSuperSeded(jReader.nextBoolean());
				break;
			case "deleteFlag":
				rei.setDeleteFlag(jReader.nextBoolean());
				break;
				
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					rei.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					rei.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					rei.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				rei.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					rei.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				rei.setModifiedBy(jReader.nextString());
				break;
			case "id":
				rei.setId(jReader.nextString());
				break;
			case "entityType":
				rei.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return rei;
	}

	@Override
	public void write(JsonWriter jWriter, ReportingEventItem reportingEventItem) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
