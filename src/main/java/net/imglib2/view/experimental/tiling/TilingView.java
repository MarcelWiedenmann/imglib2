
package bachelorprojekt.view;

import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import bachelorprojekt.tiling.Tiling;

public class TilingView<T> extends TilesView<T, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {

	protected final Tiling description;

	public TilingView(final RandomAccessibleInterval<T> source, final Tiling description) {
		super(source, description.getTilesPerDim());
		this.description = description;

		assert size == description.getNumTiles();
		assert dimensionsEqual(tileSize, description.getDefaultTileSize());
	}

	// -- --

	@Override
	public RandomAccess<RandomAccessibleInterval<T>> randomAccess() {
		return new TilingRandomAccess<>(this, source, description);
	}

	private static boolean dimensionsEqual(final Dimensions d1, final Dimensions d2) {
		if (d1.numDimensions() != d2.numDimensions()) {
			return false;
		}
		for (int d = 0; d < d1.numDimensions(); d++) {
			if (d1.dimension(d) != d2.dimension(d)) {
				return false;
			}
		}
		return true;
	}

	private static final class TilingRandomAccess<T>
			extends TilesRandomAccess<T, RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> {

		protected final Tiling description;

		public TilingRandomAccess(final TilingView<T> view, final RandomAccessibleInterval<T> source,
				final Tiling description) {
			super(view, source, description.getDefaultTileSize());
			this.description = description;
		}

		protected TilingRandomAccess(final TilingRandomAccess<T> randomAccess) {
			super(randomAccess);
			this.description = randomAccess.description;
		}

		// -- --

		@Override
		public RandomAccessibleInterval<T> get() {
			final long[] min = new long[n];
			final long[] max = new long[n];
			for (int d = 0; d < n; d++) {
				min[d] = position[d] * tileSize.dimension(d);
				max[d] = min[d] + tileSize.dimension(d) - 1;
				description.getStrategy().transform(min, max, position, d);
			}
			return Views.interval(source, new FinalInterval(min, max));
		}
	}
}
