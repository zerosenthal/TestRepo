package edu.yu.cs.com1320.project.Impl;

import edu.yu.cs.com1320.project.Document;
import edu.yu.cs.com1320.project.DocumentStore;
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
import java.util.Map;

/**
 * Project: stage5
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
    protected Map<String, Integer> wordCounter;
    protected long lastUseTime;

    /**
     * constructor
     * @param input of type InputStream
     * @param newURI of type URI
     * @param newCompForm of type CompressionFormat
     * @throws IOException
     */
    public DocumentImpl(InputStream input, URI newURI, DocumentStore.CompressionFormat newCompForm)
    {
        if (input == null)
        {
            this.uri = newURI;
            this.compForm = newCompForm;
        }

        else
        {
            String string = buildString(input);

            this.contents = string.getBytes();
            this.uri = newURI;
            this.hashCode = string.hashCode();
            this.compForm = newCompForm;
        }
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
                File inputFile = new File("InputFile.txt");
                File outputFile = new File("SevenZFile.txt");
                SevenZOutputFile sevenZOutput = new SevenZOutputFile(outputFile);
                SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(inputFile, "SevenZFile");
                sevenZOutput.putArchiveEntry(entry);
                sevenZOutput.write(this.contents);
                sevenZOutput.closeArchiveEntry();
                sevenZOutput.close();
                InputStream fileIS = new FileInputStream("SevenZFile.txt");
                inputFile.delete();
                outputFile.delete();
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
                File sevenZFile = new File("SevenZFile.txt");
                SevenZFile sevenZOutput = new SevenZFile(sevenZFile);
                sevenZOutput.getNextEntry();
                OutputStream fileOS = new ByteArrayOutputStream();
                int read;
                while((read = sevenZOutput.read()) != -1)
                {
                    fileOS.write(read);
                }
                sevenZFile.delete();
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

    private String buildString(InputStream is)
    {
        StringBuilder stringBuilder = new StringBuilder();

        int read = 0;
        while (true)
        {
            try
            {
                if (!((read = is.read()) != -1)) break;
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            stringBuilder.append((char) read);
        }
        return stringBuilder.toString();
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

    /**
     * how many times does the given word appear in the document?
     *
     * @param word
     * @return
     */
    @Override
    public int wordCount(String word)
    {
        return this.wordCounter.get(word);
    }

    @Override
    public long getLastUseTime()
    {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInMilliseconds)
    {
        this.lastUseTime = timeInMilliseconds;
    }

    @Override
    public Map<String, Integer> getWordMap()
    {
        return wordCounter;
    }

    @Override
    public void setWordMap(Map<String, Integer> wordMap)
    {
        this.wordCounter = wordMap;
    }


    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Document o)
    {
        DocumentImpl oDoc = (DocumentImpl) o;
        if (this.lastUseTime > oDoc.lastUseTime)
        {
            return 1;
        }
        else if (this.lastUseTime < oDoc.lastUseTime)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
