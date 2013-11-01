package org.maxgamer.maxbans.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class InputStreamWrapper extends InputStream{
	/** The character set used for transmitting messages. Should have 1;1 with byte[] to String. */
	public static final String CHARSET = "ISO-8859-1";
	
	private InputStream i;
	private int read = 0;
	/**
	 * Creates a new CachedInputStream with the given buffer.
	 * The buffer array is not copied.
	 * @param buf The buffer
	 */
	public InputStreamWrapper(InputStream in) {
		this.i = in;
	}
	public InputStreamWrapper(byte[] data){
		this(new ByteArrayInputStream(data));
	}
	
	public int getReadBytes(){
		return read;
	}
	
	/** Fetches the next string in the buffer */
	public String readString(){
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		
		byte b;
		while((b = this.readByte()) != 0){
			data.write(b);
		}
		try {
			return new String(data.toByteArray(), CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	/** Reads a byte */
	public byte readByte(){
		return (byte) this.read();
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
		return readShort() & 0xFFFF;
	}
	/** Reads a char (1 byte) */
	public char readChar(){
		return (char) readByte();
	}
	@Override
	public void close(){
		try {
			i.close();
		}
		catch (IOException e) {}
	}

	@Override
	public int read(){
		try{
			int n = i.read();
			if(n < 0){
				throw new RuntimeException("Socket is closed!");
			}
			read++;
			return n;
		}
		catch(IOException e){
			throw new RuntimeException(e.getMessage());
		}
	}
	
	@Override
	public int available(){
		try {
			return i.available();
		}
		catch(IOException e){
			throw new RuntimeException(e.getMessage());
		}
	}
	@Override
	public long skip(long n){
		try {
			return i.skip(n);
		}
		catch(IOException e){
			throw new RuntimeException(e.getMessage());
		}
	}
}