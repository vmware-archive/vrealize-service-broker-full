package org.cloudfoundry.community.servicebroker.vrealize;

import java.io.IOException;
import java.lang.reflect.Type;

import org.cloudfoundry.community.servicebroker.model.Catalog;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CatalogTranslator implements JsonDeserializer<Catalog> {//,
//		JsonSerializer<Catalog> {
	
	private Gson gson;

	public Catalog deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
//		JsonObject jobj = json.getAsJsonObject();
		Catalog catalog;
		try {
			catalog = new Catalog(TransUtils.getSDs(getGson()));
		} catch (IOException e) {
			throw new JsonParseException(e);
		}
		
//		catalog.setAuthor(gson.fromJson(jobj.get("writer"), Author.class));
		return catalog;
	}

//	public JsonElement serialize(Catalog src, Type typeOfSrc,
//			JsonSerializationContext context) {
//		JsonObject jobj = new JsonObject();
//		jobj.addProperty("name", src.getName());
//		jobj.add("tags", src.getTagsAsJsonArray());
//		jobj.addProperty("price", src.getPrice());
//		jobj.add("writer", gson.toJson(src.getAuthor()));
//		return jobj;
//	}

	public Gson getGson() {
		return gson;
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}
}
