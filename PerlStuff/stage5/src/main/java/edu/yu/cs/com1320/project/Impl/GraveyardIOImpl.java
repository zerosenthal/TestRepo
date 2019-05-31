package edu.yu.cs.com1320.project.Impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

/**
 * Project: stage5
 * GraveyardIOImpl.java - class representing Document IO for ejected Docs
 * Created 5/30/2019
 *
 * @author Elimelekh Perl
 */
public class GraveyardIOImpl extends DocumentIO
{
    protected File baseDir;
    protected Map<URI, Integer> versionCounter;

    public GraveyardIOImpl(Map<URI, Integer> versionCounter, File baseDir)
    {
        this.versionCounter = versionCounter;
        this.baseDir = baseDir;
    }
    public GraveyardIOImpl(Map<URI, Integer> versionCounter)
    {
        this.versionCounter = versionCounter;
    }

    protected class CustomJsonSerializer implements JsonSerializer<Document>
    {
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext)
        {
            DocumentImpl doc = (DocumentImpl)document;

            Gson gson = new GsonBuilder().create();
            JsonElement element;
            JsonObject jsonDoc = new JsonObject();

            element = new JsonPrimitive(Base64.encodeBase64String(doc.contents));
            jsonDoc.add("Contents", element);

            element = gson.toJsonTree(doc.getKey(), URI.class);
            jsonDoc.add("URI", element);

            element = gson.toJsonTree(doc.getCompressionFormat(), DocumentStore.CompressionFormat.class);
            jsonDoc.add("Compression Format", element);

            jsonDoc.addProperty("Hash Code", doc.getDocumentHashCode());

            element = gson.toJsonTree(doc.getWordMap(), Map.class);
            jsonDoc.add("Word Map", element);


            return jsonDoc;
        }
    }

    protected class CustomJsonDeserializer implements JsonDeserializer<Document>
    {

        @Override
        public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
        {
            Gson gson = new GsonBuilder().create();
            JsonObject jsonDoc = jsonElement.getAsJsonObject();

            byte[] contents = Base64.decodeBase64((jsonDoc.get("Contents")).getAsString());
            URI uri = gson.fromJson(jsonDoc.get("URI"), URI.class);
            DocumentStore.CompressionFormat compForm = gson.fromJson(jsonDoc.get("Compression Format"), DocumentStore.CompressionFormat.class);
            int hashCode = jsonDoc.get("Hash Code").getAsInt();
            Map wordMap = gson.fromJson(jsonDoc.get("Word Map"), Map.class);

            DocumentImpl doc = new DocumentImpl(null, uri, compForm);
            doc.contents = contents;
            doc.hashCode = hashCode;
            doc.setWordMap(wordMap);

            return (Document) doc;
        }
    }

    /**
     * @param doc to serialize
     * @return File object representing file on disk to which document was serialized
     */
    public File serialize(Document doc, Integer versionChange)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        CustomJsonSerializer customSerializer = new CustomJsonSerializer();
        gsonBuilder.registerTypeHierarchyAdapter(Document.class, customSerializer);
        Gson customGson = gsonBuilder.create();
        String customJson = customGson.toJson(doc);

        String dir;
        if (this.baseDir == null)
        {
            dir = System.getProperty("user.dir");
        }
        else
        {
            dir = this.baseDir.getAbsolutePath();
        }

        String fileSeparator = System.getProperty("file.separator");
        String uriPath = doc.getKey().getHost() + doc.getKey().getPath();
        int version = (this.versionCounter.get(doc.getKey())) + versionChange;
        String fullPath = dir + fileSeparator + "graveyard" + fileSeparator + uriPath + fileSeparator + "v" + version + ".json";

        File jsonFile = new File(fullPath);
        jsonFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(jsonFile))
        {
            writer.write(customJson);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return jsonFile;
    }

    /**
     10
     Requirements Version: May 13, 2019
     * @param uri of doc to deserialize
     * @return deserialized document object
     */
    public Document deserialize(URI uri, Integer versionChange)
    {
        String dir;
        if (this.baseDir == null)
        {
            dir = System.getProperty("user.dir");
        }
        else
        {
            dir = this.baseDir.getAbsolutePath();
        }

        String fileSeparator = System.getProperty("file.separator");
        String uriPath = uri.getHost() + uri.getPath();
        int version = (this.versionCounter.get(uri)) + versionChange;
        this.versionCounter.put(uri, version);
        String fullPath = dir + fileSeparator + "graveyard" + fileSeparator + uriPath + fileSeparator + "v" + version + ".json";

        String customJson = null;

        try (BufferedReader br = new BufferedReader(new FileReader(fullPath)))
        {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null)
            {
                sb.append(line);
                line = br.readLine();
            }
            customJson = sb.toString();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


        File jsonFile = new File(fullPath);
        jsonFile.delete();
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeHierarchyAdapter(Document.class, new GraveyardIOImpl.CustomJsonDeserializer());
        Gson customGson = gsonBuilder.create();
        Document doc = customGson.fromJson(customJson, Document.class);

        return doc;
    }
}
