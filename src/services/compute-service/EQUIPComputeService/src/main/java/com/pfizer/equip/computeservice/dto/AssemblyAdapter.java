package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

public class AssemblyAdapter extends TypeAdapter<Assembly> {
	private static Logger log = LoggerFactory.getLogger(AssemblyAdapter.class);	
	
	
	@Override
	public Assembly read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		ScriptAdapter scriptAdapter = new ScriptAdapter();
		ReportingEventStatusChangeWorkflowAdapter rescwfAdapter = new ReportingEventStatusChangeWorkflowAdapter();
		ReportingEventItemAdapter reiAdapter = new ReportingEventItemAdapter();
		Assembly assembly = new Assembly();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "equipId":
				assembly.setEquipId(jReader.nextString());
				break;
			case "name":
				assembly.setName(jReader.nextString());
				break;
			case "assemblyType":
				assembly.setAssemblyType(jReader.nextString());
				break;
			case "itemType":
				assembly.setItemType(jReader.nextString());
				break;
			case "scripts":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Script s = scriptAdapter.read(jReader);
					assembly.getScripts().add(s);
				}
				jReader.endArray();
				break;
			case "dataframeIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getDataframeIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "assemblyIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getAssemblyIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "studyIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getStudyIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "parentIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getParentIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "reportingItemIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getReportingItemIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "publishItemIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getPublishItemIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "libraryReferences":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getLibraryReferences().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "reportingEventStatusChangeWorkflows":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getReportingEventStatusChangeWorkflows().add(rescwfAdapter.read(jReader));
				}
				jReader.endArray();
				break;
			case "reportingItems":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getReportingItems().add(reiAdapter.read(jReader));
				}
				jReader.endArray();
				break;
			case "atrIsCurrent":
				assembly.setAtrIsCurrent(jReader.nextBoolean());
				break;
			case "parentDataframeIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getParentDataframeIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "parentAssemblyIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getParentAssemblyIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "loadStatus":
				assembly.setLoadStatus(jReader.nextString());
				break;
			case "qcStatus":
				assembly.setQcStatus(jReader.nextString());
				break;
				
			case "versionNumber":
				assembly.setVersionNumber(jReader.nextLong());
				break;
			case "versionSuperSeded":
				assembly.setVersionSuperSeded(jReader.nextBoolean());
				break;
			case "deleteFlag":
				assembly.setDeleteFlag(jReader.nextBoolean());
				break;
			case "obsoleteFlag":
				assembly.setObsoleteFlag(jReader.nextBoolean());
				break;
			case "isCommitted":
				assembly.setCommitted(jReader.nextBoolean());
				break;
			case "published":
				assembly.setPublished(jReader.nextBoolean());
				break;
			case "released":
				assembly.setReleased(jReader.nextBoolean());
				break;
			case "isLocked":
				assembly.setLocked(jReader.nextBoolean());
				break;
			case "lockedByUser":
				assembly.setLockedByUser(jReader.nextString());
				break;
			case "protocolIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getProtocolIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "projectIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getProjectIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case"programIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					assembly.getProgramIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					assembly.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					assembly.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					assembly.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				assembly.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					assembly.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				assembly.setModifiedBy(jReader.nextString());
				break;
			case "id":
				assembly.setId(jReader.nextString());
				break;
			case "entityType":
				assembly.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return assembly;
	}

	@Override
	public void write(JsonWriter jWriter, Assembly assembly) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
