package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Column;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;

public class PromotionAdapter extends TypeAdapter<Promotion> {
	private static Logger log = LoggerFactory.getLogger(PromotionAdapter.class);	

	@Override
	public Promotion read(JsonReader jReader) throws IOException {
		CommentAdapter commentAdapter = new CommentAdapter();
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		Promotion promotion = new Promotion();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "equipId":
				promotion.setEquipId(jReader.nextString());
				break;
			case "promotionStatus":
				promotion.setPromotionStatus(jReader.nextString());
				break;
			case "restrictionStatus":
				promotion.setRestrictionStatus(jReader.nextString());
				break;
			case "dataStatus":
				promotion.setDataStatus(jReader.nextString());
				break;
			case "dataBlindingStatus":
				promotion.setDataBlindingStatus(jReader.nextString());
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					promotion.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					promotion.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					promotion.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				promotion.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					promotion.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				promotion.setModifiedBy(jReader.nextString());
				break;
			case "id":
				promotion.setId(jReader.nextString());
				break;
			case "entityType":
				promotion.setEntityType(jReader.nextString());
				break;
			default:
				jReader.skipValue();
				log.info("Adapter did not parse the field: " + name);
				break;
			}
		}
		jReader.endObject();
		return promotion;
	}

	@Override
	public void write(JsonWriter jWriter, Promotion promotion) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
