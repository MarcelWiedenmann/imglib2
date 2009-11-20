/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * @author Stephan Preibisch & Stephan Saalfeld
 */
package mpi.imglib.outside;

import mpi.imglib.algorithm.math.MathLib;
import mpi.imglib.cursor.LocalizableByDimCursor;
import mpi.imglib.cursor.LocalizableCursor;
import mpi.imglib.type.NumericType;

public class OutsideStrategyMirrorExpWindowing<T extends NumericType<T>> extends OutsideStrategy<T>
{
	final LocalizableCursor<T> parentCursor;
	final LocalizableByDimCursor<T> mirrorCursor;
	final T type, mirrorType;
	final int numDimensions;
	final int[] dimension, position, mirroredPosition, currentDirection, tmp;
	
	final float[][] weights;
	final float cutOff = 0.0001f;
	
	public OutsideStrategyMirrorExpWindowing( final LocalizableCursor<T> parentCursor, final float[] relativeDistanceFadeOut )
	{
		super( parentCursor );
		
		this.parentCursor = parentCursor;
		this.mirrorCursor = parentCursor.getImage().createLocalizableByDimCursor();
		this.mirrorType = mirrorCursor.getType();
		this.type = mirrorType.createVariable();
			
		this.numDimensions = parentCursor.getImage().getNumDimensions();
		this.dimension = parentCursor.getImage().getDimensions();
		this.position = new int[ numDimensions ];
		this.mirroredPosition = new int[ numDimensions ];
		this.currentDirection = new int[ numDimensions ];
		this.tmp = new int[ numDimensions ];
		
		// create lookup table for the weights
		weights = new float[ numDimensions ][];
		
		for ( int d = 0; d < numDimensions; ++d )
			weights[ d ] = new float[ MathLib.round( dimension[ d ] * relativeDistanceFadeOut[ d ] ) ];
		
		final float a = 1000;

		for ( int d = 0; d < numDimensions; ++d )
			for ( int pos = 0; pos < dimension[ d ]; ++pos )
			{
				final float relPos = (float) pos / (float) ( dimension[ d ] - 1);
	
				if (relPos <= 0.5f)
					weights[ d ][ pos ] = (float) ( 1.0 - (1.0 / (Math.pow(a, (relPos * 2)))) );
				else
					weights[ d ][ pos ] = (float) ( 1.0 - (1.0 / (Math.pow(a, ((1 - relPos) * 2)))) );
			}		
	}
	

	@Override
	public T getType(){ return type; }
	
	@Override
	final public void notifyOutside()
	{
		parentCursor.getPosition( position );
		getMirrorCoordinate( position, mirroredPosition );		
		mirrorCursor.setPosition( mirroredPosition );

		type.set( mirrorType );
		type.mul( getWeight( position ) );

		// test current direction
		// where do we have to move when we move one forward in every dimension, respectively
		for ( int d = 0; d < numDimensions; ++d )
			tmp[ d ] = position[ d ] + 1;
		
		getMirrorCoordinate( tmp, currentDirection );		

		for ( int d = 0; d < numDimensions; ++d )
			currentDirection[ d ] = currentDirection[ d ] - mirroredPosition[ d ];
	}
	
	final protected float getWeight( final int[] position )
	{
		float weight = 1;
		
		for ( int d = 0; d < numDimensions; ++d )
			weight *= weights[ d ][ position[ d ] ];

		return weight;
	}

	@Override
	public void notifyOutside( final int steps, final int dim ) 
	{
		if ( Math.abs( steps ) > 10 )
		{
			notifyOutside();
		}
		else if ( steps > 0 )
		{
			for ( int i = 0; i < steps; ++i )
				notifyOutsideFwd( dim );
		}
		else
		{
			for ( int i = 0; i < -steps; ++i )
				notifyOutsideBck( dim );
		}
	}
	
	@Override
	public void notifyOutsideFwd( final int dim ) 
	{
		if ( currentDirection[ dim ] == 1 )
		{
			if ( mirrorCursor.getPosition( dim ) + 1 == dimension[ dim ] )
			{
				mirrorCursor.bck( dim );
				currentDirection[ dim ] = -1;				
			}
			else
			{
				mirrorCursor.fwd( dim );
			}			
		}
		else
		{
			if ( mirrorCursor.getPosition( dim ) == 0 )
			{
				currentDirection[ dim ] = 1;
				mirrorCursor.fwd( dim );
			}
			else
			{
				mirrorCursor.bck( dim );
			}
		}
		
		type.set( mirrorType );		
		parentCursor.getPosition( position );
		type.mul( getWeight( position ) );
	}

	@Override
	public void notifyOutsideBck( final int dim ) 
	{
		// current direction of the mirror cursor when going forward
		if ( currentDirection[ dim ] == 1 )
		{
			// so we have to move the mirror cursor back if we are not position 0
			if ( mirrorCursor.getPosition( dim ) == 0 )
			{
				// the mirror cursor is at position 0, so we have to go forward instead 
				mirrorCursor.fwd( dim );
				
				// that also means if we want to go 
				currentDirection[ dim ] = -1;				
			}
			else
			{
				mirrorCursor.bck( dim );
			}			
		}
		else
		{
			if ( mirrorCursor.getPosition( dim ) + 1 == dimension[ dim ] )
			{
				mirrorCursor.bck( dim );				
				currentDirection[ dim ] = 1;
				
			}
			else
			{
				mirrorCursor.fwd( dim );
			}
		}
		
		type.set( mirrorType );		
		parentCursor.getPosition( position );
		type.mul( getWeight( position ) );
	}
	
	/*
	 * For mirroring there is no difference between leaving the image and moving while 
	 * being outside of the image
	 * @see mpi.imglib.outside.OutsideStrategy#initOutside()
	 */
	@Override
	public void initOutside() { notifyOutside(); }
	
	protected void getMirrorCoordinate( final int[] position, final int mirroredPosition[] )
	{
		for ( int d = 0; d < numDimensions; d++ )
		{
			mirroredPosition[ d ] = position[ d ];
			
			if ( mirroredPosition[ d ] >= dimension[ d ])
				mirroredPosition[ d ] = dimension[ d ] - (mirroredPosition[ d ] - dimension[ d ] + 2);
	
			if ( mirroredPosition[ d ] < 0 )
			{
				int tmp = 0;
				int dir = 1;
	
				while ( mirroredPosition[ d ] < 0 )
				{
					tmp += dir;
					if (tmp == dimension[ d ] - 1 || tmp == 0)
						dir *= -1;
					mirroredPosition[ d ]++;
				}
				mirroredPosition[ d ] = tmp;
			}
		}
	}
	
	@Override
	public void close()
	{
		mirrorCursor.close();
	}
}
