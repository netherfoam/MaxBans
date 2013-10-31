package org.maxgamer.maxbans.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class InputStreamBuffer extends ByteArrayInputStream{
	/**
	 * Creates a new CachedInputStream with the given buffer.
	 * The buffer array is not copied.
	 * @param buf The buffer
	 */
	public InputStreamBuffer(byte[] buf) {
		super(buf);
	}
	/**
	 * Creates a new CachedInputStream with the given buffer, and given offsets
	 * @param buff The data
	 * @param start The start position in the data
	 * @param end The end position in the data
	 */
	public InputStreamBuffer(byte[] buff, int start, int end){
		super(buff, start, end);
	}
	/**
	 * Returns true if the buffer has a string available
	 * @return true if the buffer has a string available
	 */
	public boolean hasNextString(){
		return pos + 1 <= super.count;
	}
	/**
	 * Returns true if the buffer has a byte or (signed or unsigned) available
	 * @return true if the buffer has a byte or (signed or unsigned) available
	 */
	public boolean hasNextByte(){
		return pos + 1 <= super.count;
	}
	/**
	 * Returns true if the buffer has a short or (signed or unsigned) available
	 * @return true if the buffer has a short or (signed or unsigned) available
	 */
	public boolean hasNextShort(){
		return pos + 2 <= super.count;
	}
	/**
	 * Returns true if the buffer has a int or (signed or unsigned) available
	 * @return true if the buffer has a int or (signed or unsigned) available
	 */
	public boolean hasNextInt(){
		return pos + 4 <= super.count;
	}
	/**
	 * Returns true if the buffer has a long available
	 * @return true if the buffer has a long available
	 */
	public boolean hasNextLong(){
		return pos + 8 <= super.count;
	}
	
	/** The number of valid bytes remaining */
	public int getRemaining(){
		return super.count - pos;
	}
	
	/** Fetches the next string in the buffer */
	public String readString(){
		StringBuilder sb = new StringBuilder();
		
		char c;
		try{
			while((c = this.readChar()) != 0){
				sb.append(c);
			}
		}
		catch(IndexOutOfBoundsException e){
			
		}
		return sb.toString();
	}
	/** Reads a byte */
	public byte readByte(){
		return (byte) super.read();
	}
	/** Reads an unsigned byte */
	public short readUnsignedByte(){
		return (short) (readByte() & 0xFF);
	}
	/** Reads a long (8 bytes) */
	public long readLong(){
		return (readInt() << 32) | (readInt() & 0xFFFFFF);
	}
	/** Reads an int (4 bytes) */
	public int readInt(){
		return (readShort() << 16) | (readShort() & 0xFFFF);
	}
	/** Reads an unsigned int (4 bytes) */
	public long readUnsignedInt(){
		return readInt() & 0xFFFFFFFF;
	}
	/** Reads a short (2 bytes) */
	public short readShort(){
		return (short) ((readByte() << 8) | (readByte() & 0xFF));
	}
	/** Reads an unsigned short (2 bytes) */
	public int readUnsignedShort(){
		return readInt() & 0xFFFF;
	}
	/** Reads a char (1 byte) */
	public char readChar(){
		return (char) readByte();
	}
	@Override
	public void close(){
		try{ super.close(); }
		catch(IOException e){} //No care
	}
}