package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import java.nio.file.*;


public class DocumentIOImpl extends edu.yu.cs.com1320.project.DocumentIO {
    private Path baseDir;

    public DocumentIOImpl(File dir) {
        this.baseDir = dir.toPath();
        FileUtils.deleteQuietly(baseDir.toFile());
        baseDir.toFile().mkdirs();
    }

    public DocumentIOImpl() {
        this.baseDir = Paths.get(System.getProperty("user.dir"));
    }

    protected void setBaseDir(File dir) {
        this.baseDir = dir.toPath();
        FileUtils.deleteQuietly(baseDir.toFile());
        baseDir.toFile().mkdirs();
    }

    protected Path getPathFromUri(URI uri) {
        String str = uri.toString();
        str = str.replaceAll("http://", "");
        str = str.replaceAll("https://", "");
        str += ".json";
        return this.baseDir.resolve(str);
    }

    /**
     * @param doc to serialize
     * @return File object representing file on disk to which document was serialized
     */
    @Override
    public File serialize(Document doc) {
        String json = GSONDocUtility.serialize(doc);
        URI uri = doc.getKey();
        Path path = getPathFromUri(uri);
        File jsonFile = null;
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" + " already exists%n", path);
        } catch (IOException x) {
            System.err.format("createFile error: %s%n", x);
        }
        try {
            jsonFile = path.toFile();
            FileUtils.forceMkdirParent(jsonFile);
            FileUtils.writeStringToFile(path.toFile(), json, "UTF-8");
        } catch (IOException x) {
            System.err.format("writeFile error: %s%n", x);
        }
        return jsonFile;
    }

    /**
     * @param uri of doc to deserialize
     * @return deserialized document object
     */
    @Override
    public Document deserialize(URI uri) {
        File file = getPathFromUri(uri).toFile();
        boolean isRegularFile = file.exists() & Files.isRegularFile(file.toPath()) & Files.isReadable(file.toPath());
        if (isRegularFile) {
            String json = "";
            try {
                json = FileUtils.readFileToString(file, "UTF-8");
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
            FileUtils.deleteQuietly(file);
            return GSONDocUtility.deserialize(json);
        }
        FileUtils.deleteQuietly(file);
        return null;
    }

}
