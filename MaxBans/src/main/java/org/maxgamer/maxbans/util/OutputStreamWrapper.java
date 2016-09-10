package org.maxgamer.maxbans.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;

public class OutputStreamWrapper extends OutputStream
{
    private OutputStream o;
    
    public OutputStreamWrapper(final OutputStream out) {
        super();
        this.o = out;
    }
    
    public void writeByte(final byte b) {
        this.write(b);
    }
    
    public void writeShort(final short s) {
        this.writeByte((byte)(s >> 8));
        this.writeByte((byte)s);
    }
    
    public void writeInt(final int i) {
        this.writeShort((short)(i >> 16));
        this.writeShort((short)i);
    }
    
    public void writeLong(final long l) {
        this.writeInt((int)(l >> 32));
        this.writeInt((int)l);
    }
    
    public void writeChar(final char c) {
        this.writeByte((byte)c);
    }
    
    public void write(final String s) {
        byte[] data;
        try {
            data = s.getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            data = new byte[0];
        }
        byte[] array;
        for (int length = (array = data).length, i = 0; i < length; ++i) {
            final byte b = array[i];
            this.write(b);
        }
        this.write(0);
    }
    
    public void close() {
        try {
            this.o.close();
        }
        catch (IOException ex) {}
    }
    
    public void write(final int b) {
        try {
            this.o.write(b);
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
