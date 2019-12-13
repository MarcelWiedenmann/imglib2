/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2019 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package net.imglib2.view;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Localizables;
import net.imglib2.view.TiledView.TiledViewRandomAccess;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcel Wiedenmann
 */
public class TiledViewTest
{
	@Test
	public void basicTest()
	{
		final long[] sourceDimensions = { 20, 20, 10 };
		final int numSourceDimensions = sourceDimensions.length;
		final long numSourcePixels = LongStream.of( sourceDimensions ).reduce( 1, ( x, y ) -> x * y );

		final int[] sourcePixels = IntStream.range( 0, ( int ) numSourcePixels ).toArray();
		final RandomAccessibleInterval< IntType > source = ArrayImgs.ints( sourcePixels, sourceDimensions );
		final long[] blockDimensions = { 5, 5, 2 };
		final TiledView< IntType > tiledView = new TiledView<>( source, blockDimensions );

		Assert.assertSame( source, tiledView.getSource() );
		final long[] tiledViewBlockDimensions = tiledView.getBlockSize();
		Assert.assertArrayEquals( blockDimensions, tiledViewBlockDimensions );
		Assert.assertNotSame( blockDimensions, tiledViewBlockDimensions );
		Assert.assertArrayEquals( new long[ blockDimensions.length ], tiledView.getOverlap() );

		Assert.assertEquals( numSourceDimensions, tiledView.numDimensions() );
		final long[] numberOfBlocks = { 4, 4, 5 };
		for ( int d = 0; d < numSourceDimensions; d++ )
		{
			Assert.assertEquals( 0, tiledView.min( d ) );
			Assert.assertEquals( numberOfBlocks[ d ] - 1, tiledView.max( d ) );
		}

		final TiledViewRandomAccess< IntType > tiledViewAccess = tiledView.randomAccess();
		Assert.assertEquals( tiledView.numDimensions(), tiledViewAccess.numDimensions() );
		tiledViewAccess.setPosition( new int[] { 3, 2, 1 } );
		final TiledViewRandomAccess< IntType > copy = tiledViewAccess.copy();
		Assert.assertTrue( Localizables.equals( tiledViewAccess, copy ) );
		Assert.assertNotSame( tiledViewAccess, copy );

		final Cursor< RandomAccessibleInterval< IntType > > tiledViewCursor = Views.flatIterable( tiledView ).localizingCursor();
		while ( tiledViewCursor.hasNext() )
		{
			final RandomAccessibleInterval< IntType > tile = tiledViewCursor.next();
			Assert.assertEquals( numSourceDimensions, tile.numDimensions() );
			for ( int d = 0; d < numSourceDimensions; d++ )
			{
				final long expectedTileMin = tiledViewCursor.getLongPosition( d ) * blockDimensions[ d ];
				Assert.assertEquals( expectedTileMin, tile.min( d ) );
				final long expectedTileMax = expectedTileMin + blockDimensions[ d ] - 1;
				Assert.assertEquals( expectedTileMax, tile.max( d ) );
			}

			final Cursor< IntType > tileCursor = Views.flatIterable( tile ).localizingCursor();
			while ( tileCursor.hasNext() )
			{
				tileCursor.fwd();
				final int expectedPixelValue = ( int ) IntervalIndexer.positionToIndex( Localizables.asLongArray( tileCursor ), sourceDimensions );
				Assert.assertEquals( expectedPixelValue, tileCursor.get().getInteger() );
			}
		}
	}

	// TODO: Check if basic test is sound and complete.
	// TODO: Test with smaller edge tiles.
	// TODO: Test with overlap.
	// TODO: Test createFromBlocksPerDim.
	// TODO: Check coverage.
}
