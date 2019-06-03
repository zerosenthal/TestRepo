package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

import java.nio.file.*;
import java.util.Iterator;


public class UndoIO {
    private Path baseDir;

    public UndoIO() {
        this.baseDir = Paths.get(System.getProperty("user.dir") + System.getProperty("file.separator") + "Undo");
        FileUtils.deleteQuietly(baseDir.toFile());
        baseDir.toFile().mkdirs();
    }


    protected Path getPathFromUri(URI uri) {
        String str = uri.toString();
        str = str.replaceAll("http://", "");
        str = str.replaceAll("https://", "");
        int x = whatEditionOfURI(Paths.get(str).toString());
        str += x + ".json";
        return this.baseDir.resolve(str);
    }

    protected int whatEditionOfURI(String str) {
        String[] listOfFilesSameURI = baseDir.toFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(str);
            }
        });
        if (listOfFilesSameURI == null) return 1;
        return listOfFilesSameURI.length + 1;
    }

    /**
     * @param doc to serialize
     * @return File object representing file on disk to which document was serialized
     */
    public void serialize(Document doc) {
        String json = GSONDocUtility.serialize(doc);
        URI uri = doc.getKey();
        Path path = getPathFromUri(uri);
        try {
            Files.deleteIfExists(path);
            path.toFile().getParentFile().mkdirs();
            Files.createFile(path);
        } catch (FileAlreadyExistsException x) {
            System.err.format("file named %s" + " already exists%n", path);
        } catch (IOException x) {
            System.err.format("createFile error: %s%n", x);
        }
        try {
            FileUtils.writeStringToFile(path.toFile(), json, "UTF-8");
        } catch (IOException x) {
            System.err.format("writeFile error: %s%n", x);
        }
    }

    /**
     * @param uri of doc to deserialize
     * @return deserialized document object
     */
    public Document deserialize(URI uri, int hashcode) {
        File file = findFileToDeserialize(uri, hashcode);
        String json = "";
        try {
            json = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return GSONDocUtility.deserialize(json);
    }

    private File findFileToDeserialize(URI uri, int hashcode) {
        String str = uri.toString();
        str = str.replaceAll("http://", "");
        str = str.replaceAll("https://", "");
        final String URIPathStr = Paths.get(str).toString();
        String[] extensions = {"json"};
        Iterator<File> iterator = FileUtils.iterateFiles(baseDir.toFile(), extensions, true);
        if (!iterator.hasNext()) {
            throw new RuntimeException("File not found in Undo dir that matches URI " + URIPathStr);
        }
        while (iterator.hasNext()) {
            File file = iterator.next();
            boolean isRegularFile = file.exists() & Files.isRegularFile(file.toPath()) & Files.isReadable(file.toPath());
            if (isRegularFile) {
                String json = "";
                try {
                    json = FileUtils.readFileToString(file, "UTF-8");
                } catch (IOException x) {
                    System.err.format("IOException: %s%n", x);
                }
                int fileHashcode = GSONDocUtility.getHashcode(json);
                if (fileHashcode == hashcode) {
                    return file;
                }
            }
        }
        throw new RuntimeException("File not found in Undo dir that matches Hashcode " + hashcode + " and URI " + URIPathStr);
    }
}
