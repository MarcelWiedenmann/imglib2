
package bachelorprojekt.view;

import java.util.List;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;

// ###
// TODO: REMOVE this class as most of its functionality is also provided by net.imglib2.util.IntervalIndexer.
// ###

// Note: Derivatives of this class must NOT maintain a current position, i.e. should stay "state-less".
// This is because they can and will be shared between multiple cursors, random accesses and even entire tilings.
public class TileIndexMapper {

	// FIXME: fix for new TilingStrategy and TilingDescription layouts (e.g. by a new "TilingIndexMapper" class (extends
	// this) that accepts a TilingDescription).

	protected final int n;
	protected final Interval interval;
	protected final Dimensions tileSize;
	protected final long[] tilesPerDim;
	protected final int[] mappingOrder;

	public TileIndexMapper(final Interval interval, final Dimensions tileSize, final Dimensions tilesPerDim,
			final int[] mappingOrder) {
		n = interval.numDimensions();

		assert tileSize.numDimensions() == n;
		assert tilesPerDim.numDimensions() == n;
		assert mappingOrder.length == n;

		this.interval = interval;
		this.tileSize = tileSize;
		this.tilesPerDim = new long[n];
		tilesPerDim.dimensions(this.tilesPerDim);
		this.mappingOrder = mappingOrder;
	}

	public Interval getInterval() {
		return interval;
	}

	// TODO: more friendly/concise method naming

	public long getFlatTileIndex(final long[] index) {
		assert index.length == n;

		long flatIndex = 0;
		for (int k = n - 1; k >= 0; k--) {
			if (index[mappingOrder[k]] < 0 || tilesPerDim[mappingOrder[k]] <= index[mappingOrder[k]]) {
				throw new IndexOutOfBoundsException("'index' exceeds 'tilesPerDim'");
			}
			flatIndex = flatIndex * tilesPerDim[mappingOrder[k]] + index[mappingOrder[k]];
		}
		return flatIndex;
	}

	public void getTileIndex(long flatIndex, final long[] index) {
		assert index.length == n;

		for (final int d : mappingOrder) {
			final long i = flatIndex / tilesPerDim[d];
			index[d] = flatIndex - i * tilesPerDim[d];
			if (index[d] < 0 || tilesPerDim[d] <= index[d]) {
				throw new IndexOutOfBoundsException("'index' exceeds 'tilesPerDim'");
			}
			flatIndex = i;
		}
	}

	public void getTileIndexAndLocalPosition(final long[] position, final long[] tileIndex,
			final long[] localPosition /* , final boolean fillBorder */) {
		assert position.length == n;
		assert tileIndex.length == n;
		assert localPosition.length == n;

		for (int d = 0; d < n; d++) {
			if (position[d] < interval.min(d) || interval.max(d) < position[d]) {
				throw new IndexOutOfBoundsException("'position' exceeds defined interval");
			}
			tileIndex[d] = position[d] / tileSize.dimension(d);
			localPosition[d] = position[d] % tileSize.dimension(d);
			if (tileIndex[d] >= tilesPerDim[d]) {
				// Enlarge border tile to fill dimension until position is reached?
				// if (fillBorder) {
				tileIndex[d]--;
				localPosition[d] += tileSize.dimension(d);
				// }
				// else {
				// throw new IndexOutOfBoundsException("'position' maps to an index that exceeds 'tilesPerDim'");
				// }
			}
		}
	}

	// Note: This method intentionally contains duplicate code (see getFlatTileIndex and getTileIndexAndLocalPosition)
	// for performance reasons.
	public long getFlatTileIndexAndLocalPosition(final long[] position,
			final long[] localPosition /* , final boolean fillBorder */) {
		// TODO: stub - implement higher performance mapping by merging the D-loops of the two other methods.
		final long[] tileIndex = new long[position.length];
		getTileIndexAndLocalPosition(position, tileIndex, localPosition /* , fillBorder */);
		return getFlatTileIndex(tileIndex);
	}

	// -- Static --

	public static <T> TileIndexMapper createFromTiles(final List<RandomAccessibleInterval<T>> tiles,
			final long[] tilesPerDim) {
		final int[] combinationOrder = getDefaultMappingOrder(tilesPerDim.length);
		return createFromTiles(tiles, tilesPerDim, combinationOrder);
	}

	public static <T> TileIndexMapper createFromTiles(final List<RandomAccessibleInterval<T>> tiles,
			final long[] tilesPerDim, final int[] combinationOrder) {
		final Interval interval = getDefaultInterval(tiles, tilesPerDim);
		final Dimensions tileSize = tiles.get(0);
		return new TileIndexMapper(interval, tileSize, new FinalDimensions(tilesPerDim), combinationOrder);
	}

	public static int[] getDefaultMappingOrder(final int n) {
		final int[] combinationOrder = new int[n];
		for (int d = 0; d < n; d++) {
			combinationOrder[d] = d;
		}
		return combinationOrder;
	}

	public static <T> Interval getDefaultInterval(final List<RandomAccessibleInterval<T>> tiles,
			final long[] tilesPerDim) {
		final RandomAccessibleInterval<T> tile = tiles.get(0);
		final int n = tilesPerDim.length;

		// FIXME: tile.numDimensions() != n should also be allowed (or preprocessed at a central stage:
		// tile.numDimensions() > n --> "pad with 1"
		// tile.numDimensions() < n --> "use subset of n"
		assert tile.numDimensions() == n;

		final long[] imageSize = new long[n];
		for (int d = 0; d < n; d++) {
			imageSize[d] = tilesPerDim[d] * tile.dimension(d);
		}
		return new FinalInterval(imageSize);
	}
}
