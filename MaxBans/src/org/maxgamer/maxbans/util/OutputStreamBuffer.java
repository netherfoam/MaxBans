package org.maxgamer.maxbans.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutputStreamBuffer extends ByteArrayOutputStream{
	public OutputStreamBuffer(int capacity) {
		super(capacity);
	}
	
	public void writeByte(byte b){
		super.write(b);
	}
	public void writeShort(short s){
		this.writeByte((byte) (s >> 8));
		this.writeByte((byte) s);
	}
	public void writeInt(int i){
		this.writeShort((short) (i >> 16));
		this.writeShort((short) i);
	}
	public void writeLong(long l){
		this.writeInt((int) (l >> 32));
		this.writeInt((int) l);
	}
	public void writeChar(char c){
		this.writeByte((byte) c);
	}
	public void write(CharSequence s){
		for(int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			this.writeChar(c);
		}
		this.writeChar((char) 0);
	}
	@Override
	public void close(){
		try {
			super.close();
		} catch (IOException e) {
			//end
		}
	}

	public int position() {
		return super.count;
	}
}