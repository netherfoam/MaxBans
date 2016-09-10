package org.maxgamer.maxbans.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream
{
    public static final String CHARSET = "ISO-8859-1";
    private InputStream i;
    private int read;
    
    public InputStreamWrapper(final InputStream in) {
        super();
        this.read = 0;
        this.i = in;
    }
    
    public InputStreamWrapper(final byte[] data) {
        this(new ByteArrayInputStream(data));
    }
    
    public int getReadBytes() {
        return this.read;
    }
    
    public String readString() {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte b;
        while ((b = this.readByte()) != 0) {
            data.write(b);
        }
        try {
            return new String(data.toByteArray(), "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public byte readByte() {
        return (byte)this.read();
    }
    
    public short readUnsignedByte() {
        return (short)(this.readByte() & 0xFF);
    }
    
    public long readLong() {
        return this.readInt() << 32 | (this.readInt() & 0xFFFFFF);
    }
    
    public int readInt() {
        return this.readShort() << 16 | (this.readShort() & 0xFFFF);
    }
    
    public long readUnsignedInt() {
        return this.readInt() & -1;
    }
    
    public short readShort() {
        return (short)(this.readByte() << 8 | (this.readByte() & 0xFF));
    }
    
    public int readUnsignedShort() {
        return this.readShort() & 0xFFFF;
    }
    
    public char readChar() {
        return (char)this.readByte();
    }
    
    public void close() {
        try {
            this.i.close();
        }
        catch (IOException ex) {}
    }
    
    public int read() {
        try {
            final int n = this.i.read();
            if (n < 0) {
                throw new RuntimeException("Socket is closed!");
            }
            ++this.read;
            return n;
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public int available() {
        try {
            return this.i.available();
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public long skip(final long n) {
        try {
            return this.i.skip(n);
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
