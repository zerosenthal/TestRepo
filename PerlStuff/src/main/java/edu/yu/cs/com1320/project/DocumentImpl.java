package edu.yu.cs.com1320.project;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.net.URI;

/**
 * Project: stage1
 * DocumentImpl.java - class representing a Document object
 * Created 3/13/2019
 * @see DocumentStoreImpl
 *
 * @author Elimelekh Perl
 */
public class DocumentImpl implements Document
{
    protected byte[] contents;
    protected URI uri;
    protected int hashCode;
    protected DocumentStore.CompressionFormat compForm;

    /**
     * constructor
     * @param input of type InputStream
     * @param newURI of type URI
     * @param newCompForm of type CompressionFormat
     * @throws IOException
     */
    public DocumentImpl(InputStream input, URI newURI, DocumentStore.CompressionFormat newCompForm)
    {
        StringBuilder stringBuilder = new StringBuilder();

        int read = 0;
        while (true)
        {
            try
            {
                if (!((read = input.read()) != -1)) break;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            stringBuilder.append((char) read);
        }
        String string = stringBuilder.toString();

        this.contents = string.getBytes();
        this.uri = newURI;
        this.hashCode = string.hashCode();
        this.compForm = newCompForm;
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }

        if (that == null || getClass() != that.getClass())
        {
            return false;
        }

        DocumentImpl document = (DocumentImpl) that;

        return (this.hashCode == document.hashCode);
    }

    @Override
    public int hashCode()
    {
        return this.getDocumentHashCode();
    }

    /**
     * converts decompressed contents field into compressed byte array
     * @return compressed contents byte[]
     * @throws IOException
     */
    protected byte[] compress() throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        switch (this.compForm)
        {
            case ZIP:
                ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output);
                zipOutput.putArchiveEntry(new ZipArchiveEntry("zipArchive"));
                zipOutput.write(this.contents);
                zipOutput.closeArchiveEntry();
                zipOutput.close();
                break;

            case JAR:
                JarArchiveOutputStream jarOutput = new JarArchiveOutputStream(output);
                jarOutput.putArchiveEntry(new JarArchiveEntry("jarArchive"));
                jarOutput.write(this.contents);
                jarOutput.closeArchiveEntry();
                jarOutput.close();
                break;

            case GZIP:
                GzipCompressorOutputStream gzipOutput = new GzipCompressorOutputStream(output);
                gzipOutput.write(this.contents);
                gzipOutput.close();
                break;

            case BZIP2:
                BZip2CompressorOutputStream bzipOutput = new BZip2CompressorOutputStream(output);
                bzipOutput.write(this.contents);
                bzipOutput.close();
                break;

            case SEVENZ:
                File inputFile = new File("InputFile.7z");
                SevenZOutputFile sevenZOutput = new SevenZOutputFile(new File("SevenZFile.7z"));
                SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(inputFile, "SevenZFile");
                sevenZOutput.putArchiveEntry(entry);
                sevenZOutput.write(this.contents);
                sevenZOutput.closeArchiveEntry();
                sevenZOutput.close();
                InputStream fileIS = new FileInputStream("SevenZFile.7z");
                int read;
                while((read = fileIS.read()) != -1)
                {
                    output.write(read);
                }
                fileIS.close();
                break;

            default:
                return null;
        }
        return output.toByteArray();
    }

    /**
     *converts compressed contents field into readable String
     * @return decompressed contents String
     * @throws IOException
     */
    protected String decompress() throws IOException
    {
        ByteArrayInputStream byteInput = new ByteArrayInputStream(this.contents);
        BufferedInputStream bufInput = new BufferedInputStream(byteInput);

        switch (this.compForm)
        {
            case ZIP:
                ZipArchiveInputStream zipInput = new ZipArchiveInputStream(bufInput);
                zipInput.getNextZipEntry();

                return writeString(zipInput);

            case JAR:
                JarArchiveInputStream jarInput = new JarArchiveInputStream(bufInput);
                jarInput.getNextJarEntry();

                return writeString(jarInput);

            case GZIP:
                InputStream gzipInput = new GzipCompressorInputStream(bufInput);

                return writeString(gzipInput);

            case BZIP2:
                BZip2CompressorInputStream bzip2Input = new BZip2CompressorInputStream(bufInput);

                return writeString(bzip2Input);

            case SEVENZ:
                SevenZFile sevenZOutput = new SevenZFile(new File("SevenZFile.7z"));
                sevenZOutput.getNextEntry();
                OutputStream fileOS = new ByteArrayOutputStream();
                int read;
                while((read = sevenZOutput.read()) != -1)
                {
                    fileOS.write(read);
                }
                return fileOS.toString();

            default:
                return null;
        }
    }

    private String writeString(InputStream compISRef)
    {
        OutputStream stringOS = new ByteArrayOutputStream();

        try
        {
            int read;

            while ((read = compISRef.read()) != -1)
            {
                stringOS.write(read);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return stringOS.toString();
    }


    @Override
    public byte[] getDocument()
    {
        return this.contents;
    }

    @Override
    public int getDocumentHashCode()
    {
        return this.hashCode;
    }

    @Override
    public URI getKey()
    {
        return this.uri;
    }

    @Override
    public DocumentStore.CompressionFormat getCompressionFormat()
    {
        return this.compForm;
    }
}
