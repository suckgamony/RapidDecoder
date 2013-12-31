package agu.bitmap.png.chunks;

import agu.bitmap.png.ImageInfo;
import agu.bitmap.png.PngHelperInternal;
import agu.bitmap.png.PngjException;

/**
 * bKGD Chunk.
 * <p>
 * see http://www.w3.org/TR/PNG/#11bKGD
 * <p>
 * this chunk structure depends on the image type
 */
public class PngChunkBKGD extends PngChunkSingle {
	public final static String ID = ChunkHelper.bKGD;
	// only one of these is meaningful
	private int gray;
	private int red, green, blue;
	private int paletteIndex;

	public PngChunkBKGD(ImageInfo info) {
		super(ChunkHelper.bKGD, info);
	}

	@Override
	public ChunkOrderingConstraint getOrderingConstraint() {
		return ChunkOrderingConstraint.AFTER_PLTE_BEFORE_IDAT;
	}

	@Override
	public void parseFromRaw(ChunkRaw c) {
		if (imgInfo.greyscale) {
			gray = PngHelperInternal.readInt2fromBytes(c.data, 0);
		} else if (imgInfo.indexed) {
			paletteIndex = (int) (c.data[0] & 0xff);
		} else {
			red = PngHelperInternal.readInt2fromBytes(c.data, 0);
			green = PngHelperInternal.readInt2fromBytes(c.data, 2);
			blue = PngHelperInternal.readInt2fromBytes(c.data, 4);
		}
	}

	@Override
	public PngChunk cloneForWrite(ImageInfo imgInfo) {
		PngChunkBKGD other = new PngChunkBKGD(imgInfo);
		other.gray = gray;
		other.red = red;
		other.green = green;
		other.blue = blue;
		other.paletteIndex = paletteIndex;
		return other;
	}

	/**
	 * Set gray value (0-255 if bitdept=8)
	 * 
	 * @param gray
	 */
	public void setGray(int gray) {
		invalidateRaw();
		if (!imgInfo.greyscale)
			throw new PngjException("only gray images support this");
		this.gray = gray;
	}

	public int getGray() {
		if (!imgInfo.greyscale)
			throw new PngjException("only gray images support this");
		return gray;
	}

	/**
	 * Set pallette index
	 * 
	 */
	public void setPaletteIndex(int i) {
		invalidateRaw();
		if (!imgInfo.indexed)
			throw new PngjException("only indexed (pallete) images support this");
		this.paletteIndex = i;
	}

	public int getPaletteIndex() {
		if (!imgInfo.indexed)
			throw new PngjException("only indexed (pallete) images support this");
		return paletteIndex;
	}

	/**
	 * Set rgb values
	 * 
	 */
	public void setRGB(int r, int g, int b) {
		invalidateRaw();
		if (imgInfo.greyscale || imgInfo.indexed)
			throw new PngjException("only rgb or rgba images support this");
		red = r;
		green = g;
		blue = b;
	}

	public int[] getRGB() {
		if (imgInfo.greyscale || imgInfo.indexed)
			throw new PngjException("only rgb or rgba images support this");
		return new int[] { red, green, blue };
	}

}