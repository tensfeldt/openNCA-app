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
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

public class DataframeAdapter extends TypeAdapter<Dataframe> {
	private static Logger log = LoggerFactory.getLogger(DataframeAdapter.class);	

	@Override
	public Dataframe read(JsonReader jReader) throws IOException {
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		CommentAdapter commentAdapter = new CommentAdapter();
		ScriptAdapter scriptAdapter = new ScriptAdapter();
		DatasetAdapter datasetAdapter = new DatasetAdapter();
		PromotionAdapter promotionAdapter = new PromotionAdapter();
		Dataframe dataframe = new Dataframe();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "equipId":
				dataframe.setEquipId(jReader.nextString());
				break;
			case "name":
				dataframe.setName(jReader.nextString());
				break;
			case "dataframeType":
				dataframe.setDataframeType(jReader.nextString());
				break;
			case "itemType":
				dataframe.setItemType(jReader.nextString());
				break;
			case "script":
				Script s = scriptAdapter.read(jReader);
				dataframe.setScript(s);
				break;
			case "dataset":
				Dataset dataset = datasetAdapter.read(jReader);
				dataframe.setDataset(dataset);
				break;
			case "promotionStatus":
				dataframe.setPromotionStatus(jReader.nextString());
				break;
			case "promotions":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Promotion p = promotionAdapter.read(jReader);
					dataframe.getPromotions().add(p);
				}
				jReader.endArray();
				break;
			case "dataframeIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getDataframeIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "assemblyIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getAssemblyIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "batchId":
				dataframe.setBatchId(jReader.nextString());
				break;
			case "studyIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getStudyIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "restrictionStatus":
				dataframe.setRestrictionStatus(jReader.nextString());
				break;
			case "dataStatus":
				dataframe.setDataStatus(jReader.nextString());
				break;
			case "dataBlindingStatus":
				dataframe.setDataBlindingStatus(jReader.nextString());
				break;
			case "qcStatus":
				dataframe.setQcStatus(jReader.nextString());
				break;
			case "profileConfig":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getProfileConfig().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "versionNumber":
				dataframe.setVersionNumber(jReader.nextLong());
				break;
			case "versionSuperSeded":
				dataframe.setVersionSuperSeded(jReader.nextBoolean());
				break;
			case "deleteFlag":
				dataframe.setDeleteFlag(jReader.nextBoolean());
				break;
			case "obsoleteFlag":
				dataframe.setObsoleteFlag(jReader.nextBoolean());
				break;
			case "isCommitted":
				dataframe.setCommitted(jReader.nextBoolean());
				break;
			case "published":
				dataframe.setPublished(jReader.nextBoolean());
				break;
			case "released":
				dataframe.setReleased(jReader.nextBoolean());
				break;
			case "isLocked":
				dataframe.setLocked(jReader.nextBoolean());
				break;
			case "lockedByUser":
				dataframe.setLockedByUser(jReader.nextString());
				break;
			case "protocolIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getProtocolIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "projectIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getProjectIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case"programIds":
				jReader.beginArray();
				while (jReader.hasNext()) {
					dataframe.getProgramIds().add(jReader.nextString());
				}
				jReader.endArray();
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					dataframe.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					dataframe.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					dataframe.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				dataframe.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					dataframe.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				dataframe.setModifiedBy(jReader.nextString());
				break;
			case "id":
				dataframe.setId(jReader.nextString());
				break;
			case "entityType":
				dataframe.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return dataframe;
	}

	@Override
	public void write(JsonWriter jWriter, Dataframe dataframe) throws IOException {
		// TODO Auto-generated method stub
	}

}
