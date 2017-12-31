package org.kreal.FLVParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class TagHeader {
	static public int length = 11;

	private byte type;

	private int dataSize;

	private int timestamp;

	private byte[] streamID = new byte[3];

	private ByteBuffer data;

	public int getDataSize() {
		return dataSize;
	}

	public ByteBuffer getData() {
		return (ByteBuffer) data.position(0);
	}

	public void setDataSize(int dataSize) {
		data.position(1);
		data.put((byte) (dataSize >> 16)).put((byte) (dataSize >> 8)).put((byte) (dataSize));
		this.dataSize = dataSize;
	}

	public byte getType() {
		return type;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		data.position(4);
		data.put((byte) (timestamp >> 16 & 0xff)).put((byte) (timestamp >> 8 & 0xff)).put((byte) (timestamp & 0xff))
				.put((byte) (timestamp >> 24 & 0xff));
		this.timestamp = timestamp;
	}

	static TagHeader build(ReadableByteChannel rChannel) {
		ByteBuffer buffer = ByteBuffer.allocate(11);
		try {
			if (rChannel.read(buffer) < 11)
				return null;
		} catch (IOException e) {
			// e.printStackTrace();
			return null;
		}
		buffer.flip();
		TagHeader result = new TagHeader();
		result.data = buffer;
		result.type = buffer.get();
		result.dataSize = (buffer.get() << 24) >> 8 | ((buffer.get() << 8) & 0xff00) | (buffer.get() & 0xff);
		byte[] bytes = new byte[4];
		buffer.get(bytes);
		result.timestamp = TagHeader.getTimeStamp(bytes);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("Types:").append(type).append("\t DataSize:").append(dataSize).append("\t TimeStamp:")
				.append(timestamp);// .append("\t StreamID:").append(streamID);
		return sBuilder.toString();
	}

	static int getTimeStamp(byte[] res) {
		return (res[2] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[0] << 24) >>> 8) | (res[3] << 24);
	}
}
