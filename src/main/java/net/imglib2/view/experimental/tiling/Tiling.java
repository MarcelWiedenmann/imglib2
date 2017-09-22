
package bachelorprojekt.tiling;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import bachelorprojekt.view.TilingView;

public class Tiling<T> {

	private final Interval interval;
	private final TilingConfig params;
	private final long numTiles;
	private final Dimensions tileSize;
	private final TilingStrategy strategy;

	public Tiling(final Interval interval, final TilingConfig params, final TilingStrategy strategy) {
		// NB: We do not want to keep a reference to the (probably) original Img/RAI here, only the views do that.
		this.interval = new FinalInterval(interval);

		assert interval.numDimensions() == params.getTilesPerDim().numDimensions();

		this.params = params;
		long num = 1;
		final long[] size = new long[interval.numDimensions()];
		for (int d = 0; d < interval.numDimensions(); d++) {
			num *= params.getTilesPerDim().dimension(d);
			size[d] = interval.dimension(d) / params.getTilesPerDim().dimension(d); // FIXME: How to handle borders?
		}
		numTiles = num;
		tileSize = FinalDimensions.wrap(size);
		this.strategy = strategy.getStatefulInstance(this);
	}

	public int numDimensions() {
		return interval.numDimensions();
	}

	public Interval getInterval() {
		return interval;
	}

	public Dimensions getTilesPerDim() {
		return params.getTilesPerDim();
	}

	public long getNumTiles() {
		return numTiles;
	}

	public Dimensions getDefaultTileSize() {
		return tileSize;
	}

	public TilingStrategy getStrategy() {
		return strategy;
	}

	public TilingView view() {

	}
}
