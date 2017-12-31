package org.kreal.FLVParser;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;

public class FLV implements Iterable<FLV.Tag>, Closeable {
	private FileChannel fChannel;

	private RandomAccessFile rFile;

	public FLVHeader flvHeader;

	public FLV(String file) throws IOException {
		rFile = new RandomAccessFile(file, "r");
		fChannel = rFile.getChannel();
		readFlvHeader();
	}

	private void readFlvHeader() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(9);
		fChannel.read(buffer);
		buffer.flip();
		flvHeader = new FLVHeader(buffer);
	}

	public class Tag {
		private TagHeader header;

		private Long postion;
		
		public TagHeader getHeader() {
			return header;
		}

		public ByteBuffer getdate() throws IOException {
			fChannel.position(postion + TagHeader.length);
			ByteBuffer rBuffer = ByteBuffer.allocate(header.getDataSize());
			fChannel.read(rBuffer);
			rBuffer.flip();
			return rBuffer;
		}

		public long writeTo(WritableByteChannel wChannel) throws IOException {
			return wChannel.write(header.getData()) + fChannel.transferTo(postion + 11, header.getDataSize(), wChannel)
					+ wChannel.write((ByteBuffer) ByteBuffer.allocate(4).putInt(header.getDataSize() + 11).flip());
		}
	}

	enum State {
		Erro, Prepared, Fetched, End
	}

	class TagIerator implements Iterator<FLV.Tag> {
		private long currentTagPostion = 13l;
		private Tag currentTag;
		private State state = State.Fetched;

		@Override
		public boolean hasNext() {
			switch (state) {
			case Prepared:
				return true;
			case Erro:
			case End:
				return false;
			default:
				break;
			}
			try {
				fChannel.position(currentTagPostion);
				TagHeader header = TagHeader.build(fChannel);
				if (header == null) {
					state = State.End;
					return false;
				}
				ByteBuffer tagsizebuf = ByteBuffer.allocate(4);
				if (fChannel.position(currentTagPostion + TagHeader.length + header.getDataSize()).read(tagsizebuf) < 4) {
					state = State.End;
					return false;
				}
				int tagsize = ((ByteBuffer) tagsizebuf.flip()).getInt();
				if ((TagHeader.length + header.getDataSize()) != tagsize) {
					state = State.Erro;
					return false;
				} else {
					currentTag = new Tag();
					currentTag.header = header;
					currentTag.postion = currentTagPostion;
					currentTagPostion = currentTagPostion + TagHeader.length + header.getDataSize() + 4;
					state = State.Prepared;
					return true;
				}
			} catch (IOException e) {
				state = State.Erro;
				// e.printStackTrace();
				return false;
			}
		}

		@Override
		public Tag next() {
			switch (state) {
			case Prepared:
				state = State.Fetched;
				return currentTag;
			default:
				return null;
			}
		}

	}

	@Override
	public Iterator<Tag> iterator() {
		return new TagIerator();
	}

	@Override
	public void close() throws IOException {
		rFile.close();
	}
}
