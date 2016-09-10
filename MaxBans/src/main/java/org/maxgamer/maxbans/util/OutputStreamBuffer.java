package org.maxgamer.maxbans.util;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class OutputStreamBuffer extends ByteArrayOutputStream
{
    public OutputStreamBuffer(final int capacity) {
        super(capacity);
    }
    
    public void writeByte(final byte b) {
        super.write(b);
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
    
    public void write(final CharSequence s) {
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            this.writeChar(c);
        }
        this.writeChar('\0');
    }
    
    public void close() {
        try {
            super.close();
        }
        catch (IOException ex) {}
    }
    
    public int position() {
        return super.count;
    }
}
