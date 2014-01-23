package se.footballaddicts.strategicballs;

import org.andengine.engine.handler.physics.PhysicsHandler;

import android.graphics.Point;

public class Ball extends BallsEntity
{
    protected final PhysicsHandler mPhysicsHandler;

    public Ball()
    {
        super( new Point( 0, 0 ) );
        
        this.mPhysicsHandler = new PhysicsHandler( this );
        this.registerUpdateHandler( this.mPhysicsHandler );
        // this.mPhysicsHandler.setVelocity( BALL_VELOCITY, 0 );
    }

    public Ball( int logicalX, int logicalY )
    {
        super( new Point( logicalX, logicalY ) );

        this.mPhysicsHandler = new PhysicsHandler( this );
        this.registerUpdateHandler( this.mPhysicsHandler );
        // this.mPhysicsHandler.setVelocity( BALL_VELOCITY, 0 );
    }

    @Override
    protected void onManagedUpdate( final float pSecondsElapsed )
    {
        super.onManagedUpdate( pSecondsElapsed );

        // if( this.mX < 0 )
        // {
        // this.mPhysicsHandler.setVelocityX( BALL_VELOCITY );
        // }
        // else if( this.mX + this.getWidth() > CAMERA_WIDTH )
        // {
        // this.mPhysicsHandler.setVelocityX( -BALL_VELOCITY );
        // }

        /*
         * if( this.mY < 0 ) { this.mPhysicsHandler.setVelocityY( BALL_VELOCITY
         * ); } else if( this.mY + this.getHeight() > CAMERA_HEIGHT ) {
         * this.mPhysicsHandler.setVelocityY( -BALL_VELOCITY ); }
         */

    }

}
