package org.kreal.FLVParser;

import java.nio.ByteBuffer;

public class FLVHeader {
	static byte[] Signature = { 'F', 'L', 'V' };

	static boolean isFlv(byte[] bytes) {
		return bytes[0] == Signature[0] && bytes[1] == Signature[1] && bytes[2] == Signature[2];
	}
	
	private ByteBuffer data;

	public FLVHeader(ByteBuffer byteBuffer) {
		data = byteBuffer;
		byte[] bytes = new byte[3];
		isflv = isFlv(bytes);
		version = data.get();
		flags = data.get();
		headerSize = data.getInt();
	}
	
	private boolean isflv;
	
	private	byte version;

	private byte flags;

	private int headerSize;
	
	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		data.put(4, flags);
		this.flags = flags;
	}

	public boolean isIsflv() {
		return isflv;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public ByteBuffer getData() {
		return (ByteBuffer) data.position(0);
	}
}
