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
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;

public class ReportingEventStatusChangeWorkflowAdapter extends TypeAdapter<ReportingEventStatusChangeWorkflow> {
	private static Logger log = LoggerFactory.getLogger(ReportingEventStatusChangeWorkflowAdapter.class);	

	@Override
	public ReportingEventStatusChangeWorkflow read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		ReportingEventStatusChangeWorkflow rescw = new ReportingEventStatusChangeWorkflow();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "reportingEventId":
				rescw.setReportingEventId(jReader.nextString());
				break;
			case "reportingEventStatusWorkflowDescription":
				rescw.setReportingEventStatusWorkflowDescription(jReader.nextString());
				break;
			case "reportingEventReleaseStatus":
				rescw.setReportingEventReleaseStatus(jReader.nextString());
				break;
			case "reportingEventReopenReasonKey":
				rescw.setReportingEventReopenReasonKey(jReader.nextString());
				break;
			case "reportingEventReopenReasonAttachmentId":
				rescw.setReportingEventReopenReasonAttachmentId(jReader.nextString());
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					rescw.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					rescw.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					rescw.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				rescw.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					rescw.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				rescw.setModifiedBy(jReader.nextString());
				break;
			case "id":
				rescw.setId(jReader.nextString());
				break;
			case "entityType":
				rescw.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return rescw;
	}

	@Override
	public void write(JsonWriter jWriter, ReportingEventStatusChangeWorkflow rescw) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
