package org.maxgamer.maxbans.util;

import java.io.IOException;
import java.io.ByteArrayInputStream;

public class InputStreamBuffer extends ByteArrayInputStream
{
    public InputStreamBuffer(final byte[] buf) {
        super(buf);
    }
    
    public InputStreamBuffer(final byte[] buff, final int start, final int end) {
        super(buff, start, end);
    }
    
    public boolean hasNextString() {
        return this.pos + 1 <= super.count;
    }
    
    public boolean hasNextByte() {
        return this.pos + 1 <= super.count;
    }
    
    public boolean hasNextShort() {
        return this.pos + 2 <= super.count;
    }
    
    public boolean hasNextInt() {
        return this.pos + 4 <= super.count;
    }
    
    public boolean hasNextLong() {
        return this.pos + 8 <= super.count;
    }
    
    public int getRemaining() {
        return super.count - this.pos;
    }
    
    public String readString() {
        final StringBuilder sb = new StringBuilder();
        try {
            char c;
            while ((c = this.readChar()) != '\0') {
                sb.append(c);
            }
        }
        catch (IndexOutOfBoundsException ex) {}
        return sb.toString();
    }
    
    public byte readByte() {
        return (byte)super.read();
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
        return this.readInt() & 0xFFFF;
    }
    
    public char readChar() {
        return (char)this.readByte();
    }
    
    public void close() {
        try {
            super.close();
        }
        catch (IOException ex) {}
    }
}
