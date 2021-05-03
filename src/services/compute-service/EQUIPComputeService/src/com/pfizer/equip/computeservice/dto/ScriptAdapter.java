package com.pfizer.equip.computeservice.dto;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Script;

public class ScriptAdapter extends TypeAdapter<Script> {

	@Override
	public Script read(JsonReader jReader) throws IOException {
		CommentAdapter commentAdapter = new CommentAdapter();
		MetadataAdapter metadataAdapter = new MetadataAdapter();
		LibraryReferenceAdapter libraryReferenceAdapter = new LibraryReferenceAdapter();
		Script script = new Script();
		jReader.beginObject();
		while (jReader.hasNext()) {
			String name = jReader.nextName();
			switch (name) {
			case "scriptBody":
				LibraryReference scriptBody = libraryReferenceAdapter.read(jReader);
				script.setScriptBody(scriptBody);
				break;
			case "computeContainer":
				script.setComputeContainer(jReader.nextString());
				break;
			case "environment":
				script.setEnvironment(jReader.nextString());
				break;
			case "metadata":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Metadatum m = metadataAdapter.read(jReader);
					script.getMetadata().add(m);
				}
				jReader.endArray();
				break;
			case "comments":
				jReader.beginArray();
				while (jReader.hasNext()) {
					Comment c = commentAdapter.read(jReader);
					script.getComments().add(c);
				}
				jReader.endArray();
				break;
			case "created":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					script.setCreated(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "createdBy":
				script.setCreatedBy(jReader.nextString());
				break;
			case "modifiedDate":
				{
					OffsetDateTime odt = OffsetDateTime.parse(jReader.nextString());
					script.setModifiedDate(new Date(odt.toInstant().toEpochMilli()));
				}
				break;
			case "modifiedBy":
				script.setModifiedBy(jReader.nextString());
				break;
			case "id":
				script.setId(jReader.nextString());
				break;
			case "entityType":
				script.setEntityType(jReader.nextString());
				break;
			default:
				break;
			}
		}
		jReader.endObject();
		return script;
	}

	@Override
	public void write(JsonWriter jWriter, Script script) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
