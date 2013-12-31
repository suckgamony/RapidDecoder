package agu.bitmap.png.chunks;

import agu.bitmap.png.ImageInfo;

/**
 * tEXt chunk.
 * <p>
 * see http://www.w3.org/TR/PNG/#11tEXt
 */
public class PngChunkTEXT extends PngChunkTextVar {
	public final static String ID = ChunkHelper.tEXt;

	public PngChunkTEXT(ImageInfo info) {
		super(ID, info);
	}

	@Override
	public void parseFromRaw(ChunkRaw c) {
		int i;
		for (i = 0; i < c.data.length; i++)
			if (c.data[i] == 0)
				break;
		key = ChunkHelper.toString(c.data, 0, i);
		i++;
		val = i < c.data.length ? ChunkHelper.toString(c.data, i, c.data.length - i) : "";
	}

	@Override
	public PngChunk cloneForWrite(ImageInfo imgInfo) {
		PngChunkTEXT other = new PngChunkTEXT(imgInfo);
		other.key = key;
		other.val = val;
		return other;
	}
}