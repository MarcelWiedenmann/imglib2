
package bachelorprojekt.tiling;

import net.imglib2.Dimensions;
import net.imglib2.Interval;

public class TilingConfig {

	/**
	 * @see #getDimensions()
	 */
	public enum TilingType {
		FixedTileSize, FixedTilesPerDimension
	}

	private final Dimensions dimensions;
	private final TilingType tilingType;
	private final Dimensions overlap;

	/**
	 * dimensions.numDimensions() must equal overlap.numDimensions().
	 *
	 * @see #getDimensions()
	 * @see #getOverlap()
	 */
	public TilingConfig(final Dimensions dimensions, final TilingType tilingType, final Dimensions overlap) {
		assert dimensions.numDimensions() == overlap.numDimensions();

		this.dimensions = dimensions;
		this.tilingType = tilingType;
		this.overlap = overlap;
	}

	/**
	 * If {@link #getTilingType()} returns {@link TilingType#FixedTileSize}, the returned dimensions specify the default
	 * dimensions of a single tile (the dimensions of a border tile may differ). If {@link #getTilingType()} returns
	 * {@link TilingType#FixedTilesPerDimension}, the returned dimensions specify the number of tiles per tiling
	 * dimension.
	 */
	public Dimensions getDimensions() {
		return dimensions;
	}

	/**
	 * Returns the default tile size. If {@link #getTilingType()} returns {@link TilingType#FixedTileSize}, the argument
	 * may be null.
	 *
	 * @see #getDimensions()
	 */
	public Dimensions getDefaultTileSize(final Dimensions tilingDimensions) {
		if (tilingType == TilingType.FixedTileSize) {
			return dimensions;
		}
		// TODO: calc
	}

	/**
	 * Returns the number of tiles per dimensions. If {@link #getTilingType()} returns
	 * {@link TilingType#FixedTilesPerDimension}, the argument may be null.
	 *
	 * @see #getDimensions()
	 */
	public Dimensions getTilesPerDimension(final Dimensions tilingDimensions) {
		if (tilingType == TilingType.FixedTilesPerDimension) {
			return dimensions;
		}
		// TODO: calc
	}

	/**
	 * @see #getDimensions()
	 */
	public TilingType getTilingType() {
		return tilingType;
	}

	/**
	 * Returns the overlap dimensions. Each tile will be expanded by the overlap to enable neighborhood operations
	 * without running into boundary problems.
	 */
	public Dimensions getOverlap() {
		return overlap;
	}

	/**
	 * Expands a tile in a specified dimension given this configuration.
	 *
	 * @param tileIndex the index of the tile within its tiling
	 * @param tileMin the minimum coordinates of the tile
	 * @param tileMax the maximum coordinates of the tile
	 * @param d the dimension in which to expand the tile
	 * @param tilingInveral the interval of the entire tiling. This is required to clip the expansion of border tiles.
	 */
	public void expandByOverlap(final long[] tileIndex, final long[] tileMin, final long[] tileMax, final int d,
			final Interval tilingInterval) {
		if (tileIndex[d] > 0) {
			tileMin[d] -= overlap.dimension(d);
		}
		final long tilesInThisDim = getTilesPerDimension(tilingInterval).dimension(d);
		if (tileIndex[d] < tilesInThisDim - 1) {
			tileMax[d] += overlap.dimension(d);
		} else {
			// TODO: we have to clip here
		}
	}

	// // TODO: transform and transformBack differ in signature (and application)
	// public <T> List<RandomAccessibleInterval<T>> transformBack(final List<RandomAccessibleInterval<T>> tiles,
	// final TileIndexMapper mapper) {
	// final ArrayList<RandomAccessibleInterval<T>> transformedTiles = new ArrayList<>(tiles.size());
	// final Dimensions tilesPerDim = description.getTilesPerDim();
	// for (int i = 0; i < tiles.size(); i++) {
	// final RandomAccessibleInterval<T> tile = tiles.get(i);
	// final long[] min = new long[tile.numDimensions()];
	// final long[] max = new long[tile.numDimensions()];
	// tile.min(min);
	// tile.max(max);
	// final long[] tileIndex = new long[tile.numDimensions()];
	// mapper.getTileIndex(i, tileIndex);
	// for (int d = 0; d < tile.numDimensions(); d++) {
	// // Border checks
	// if (tileIndex[d] > 0) {
	// min[d] += overlap.dimension(d);
	// }
	// if (tileIndex[d] < tilesPerDim.dimension(d) - 1) {
	// max[d] -= overlap.dimension(d);
	// }
	// }
	// final FinalInterval innerTile = new FinalInterval(min, max);
	// final IntervalView<T> innerTileView = Views.interval(tile, innerTile);
	// transformedTiles.add(innerTileView);
	// }
	// return transformedTiles;
	// }
	//
	// public TilingStrategy getStatefulInstance(final Tiling desc) {
	// Dimensions matchingOverlap = overlap;
	// if (desc.numDimensions() > overlap.numDimensions()) {
	// final long[] dims = new long[desc.numDimensions()];
	// for (int d = 0; d < overlap.numDimensions(); d++) {
	// dims[d] = overlap.dimension(d);
	// }
	// matchingOverlap = FinalDimensions.wrap(dims);
	// }
	// return new TilingStrategy(matchingOverlap, desc);
	// }
}
