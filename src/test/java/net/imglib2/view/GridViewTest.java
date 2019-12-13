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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Localizables;
import net.imglib2.view.GridView.GridViewCursor;
import net.imglib2.view.GridView.GridViewRandomAccess;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marcel Wiedenmann
 */
public class GridViewTest
{
	@Test
	public void basicTest()
	{
		final long[] cellDimensions = { 5, 5, 2 };
		final long numCellPixels = LongStream.of( cellDimensions ).reduce( 1, ( x, y ) -> x * y );
		final long[] gridDimensions = { 4, 4, 5 };
		final long numCells = LongStream.of( gridDimensions ).reduce( 1, ( x, y ) -> x * y );

		final List< RandomAccessibleInterval< IntType > > cells = new ArrayList<>( ( int ) numCells );
		for ( int i = 0; i < numCells; i++ )
		{
			final int[] cellPixels = IntStream.range( i, ( int ) numCellPixels + i ).toArray();
			final RandomAccessibleInterval< IntType > cell = ArrayImgs.ints( cellPixels, cellDimensions );
			cells.add( cell );
		}
		final RandomAccessibleInterval< RandomAccessibleInterval< IntType > > source = Views.arrange( cells, gridDimensions );
		final int numSourceDimensions = source.numDimensions();
		final GridView< IntType > gridView = new GridView<>( source );

		Assert.assertSame( source, gridView.getSource() );
		Assert.assertEquals( numCellPixels * numCells, gridView.size() );
		Assert.assertEquals( Views.flatIterable( cells.get( 0 ) ).firstElement(), gridView.firstElement() );

		Assert.assertEquals( numSourceDimensions, gridView.numDimensions() );
		for ( int d = 0; d < numSourceDimensions; d++ )
		{
			Assert.assertEquals( 0, gridView.min( d ) );
			Assert.assertEquals( cellDimensions[ d ] * gridDimensions[ d ] - 1, gridView.max( d ) );
		}

		final GridViewRandomAccess< IntType > gridViewAccess = gridView.randomAccess();
		Assert.assertEquals( gridView.numDimensions(), gridViewAccess.numDimensions() );
		gridViewAccess.setPosition( new int[] { 3, 2, 1 } );
		final GridViewRandomAccess< IntType > copy = gridViewAccess.copy();
		Assert.assertTrue( Localizables.equals( gridViewAccess, copy ) );
		Assert.assertNotSame( gridViewAccess, copy );

		final Cursor< IntType > gridViewFlatCursor = Views.flatIterable( gridView ).localizingCursor();
		while ( gridViewFlatCursor.hasNext() )
		{
			gridViewFlatCursor.fwd();
			gridViewFlatCursor.next();
			// TODO: Check pixel value
		}

		final GridViewCursor< IntType > gridViewCursor = gridView.localizingCursor();
		while ( gridViewCursor.hasNext() )
		{
			gridViewCursor.fwd();
			gridViewCursor.next();
			// TODO: Check pixel value
		}
	}
}
