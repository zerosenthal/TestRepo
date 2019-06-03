package edu.yu.cs.com1320.project.Impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;


public class GSONDocUtility {

    public GSONDocUtility() {
    }

    /**
     * @param doc to serialize
     * @return String JSON file of serialized document
     */
    public static String serialize(Document doc) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<Document> serializer = new JsonSerializer<>() {
            @Override
            public JsonElement serialize(Document src, Type type, JsonSerializationContext jsonSerializationContext) {
                JsonObject jsonDoc = new JsonObject();
                jsonDoc.addProperty("Hashcode", src.getDocumentHashCode());
                jsonDoc.addProperty("ByteArray", Base64.encodeBase64String(src.getDocument()));
                jsonDoc.addProperty("CompressionFormat", src.getCompressionFormat().name());
                jsonDoc.addProperty("URI", src.getKey().toString());
                jsonDoc.add("HashMap", new Gson().toJsonTree(src.getWordMap()));
                return jsonDoc;
            }
        };
        gsonBuilder.registerTypeHierarchyAdapter(Document.class, serializer);
        Gson customGson = gsonBuilder.create();
        String json = customGson.toJson(doc);
        return json;
    }

    /**
     * @param userJson JSON String of doc to deserialize
     * @return deserialized document object
     */
    public static Document deserialize(String userJson) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<DocumentImpl> deserializer = new JsonDeserializer<>() {
            @Override
            public DocumentImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                byte[] byteArray = Base64.decodeBase64(jsonObject.get("ByteArray").toString());
                int hashcode = jsonObject.get("Hashcode").getAsInt();
                DocumentStore.CompressionFormat format = new Gson().fromJson(jsonObject.get("CompressionFormat"), DocumentStore.CompressionFormat.class);
                HashMap<String, Integer> hashMap = new Gson().fromJson(jsonObject.get("HashMap").toString(), new TypeToken<HashMap<String, Integer>>() {
                }.getType());
                URI uri = new Gson().fromJson(jsonObject.get("URI"),URI.class);
                return new DocumentImpl(byteArray, hashcode, uri, format, hashMap);
            }
        };
        gsonBuilder.registerTypeHierarchyAdapter(DocumentImpl.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(userJson, DocumentImpl.class);
    }

    public static int getHashcode(String userJson) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonDeserializer<Integer> deserializer = new JsonDeserializer<>() {
            @Override
            public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                return jsonObject.get("Hashcode").getAsInt();
            }
        };
        gsonBuilder.registerTypeHierarchyAdapter(Integer.class, deserializer);
        Gson customGson = gsonBuilder.create();
        return customGson.fromJson(userJson, Integer.class);
    }
}
