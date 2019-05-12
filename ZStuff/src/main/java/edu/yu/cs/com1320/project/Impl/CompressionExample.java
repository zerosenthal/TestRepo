package edu.yu.cs.com1320.project.Impl;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CompressionExample
{
    public static void main(String[] args)
    {
        new CompressionExample();
    }
    public CompressionExample()
    {
        gzipTest();
    }

    private void gzipTest()
    {
        try{
            //compress
            String str = "Hello this is a string Hello this is a string Hello this is a string Hello this is a string Hello this is a string Hello this is a string ";
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bos);
            gzOut.write(str.getBytes());
            gzOut.close();
            byte[] compressed = bos.toByteArray();
            System.out.println("raw data length: " + str.getBytes().length);
            System.out.print("raw data: ");
            System.out.write(str.getBytes());
            System.out.println();
            System.out.println("compressed data length: " + compressed.length);
            System.out.print("compressed data: ");
            System.out.write(compressed);
            System.out.println();

            //decompress
            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GzipCompressorInputStream gzIn = new GzipCompressorInputStream(bis);
            bos = new ByteArrayOutputStream();
            int n = 0;
            while (-1 != (n = gzIn.read())) {
                bos.write(n);
            }
            System.out.println("decompressed data:  " + bos.toString());
            gzIn.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
