package se.footballaddicts.strategicballs;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import se.footballaddicts.strategicballs.Player.Position;
import se.footballaddicts.strategicballs.Player.Team;
import se.footballaddicts.strategicballs.multiplayer.BallsServer;
import se.footballaddicts.strategicballs.multiplayer.EndRoundClientMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ServerMessageFlags;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class BallsGameActivity extends SimpleBaseGameActivity
{
    private static final int            CAMERA_WIDTH                      = 2048;
    private static final int            CAMERA_HEIGHT                     = 1536;

    private static final int            UI_WIDTH                          = 150;
    private static final int            UI_HEIGHT                         = CAMERA_WIDTH;
    private static final int            PITCH_WIDTH                       = CAMERA_WIDTH - 2 * UI_WIDTH;
    private static final int            PITCH_HEIGHT                      = CAMERA_HEIGHT;
    public static final float           BALL_VELOCITY                     = 500.0f;

    private int                         mSpriteWidth                      = 88;

    private final int                   mGoalWidth                        = 133;
    private final int                   mGoalHeight                       = 426;

    private int                         mRoundButtonWidth                 = 140;

    private final int                   minX                              = UI_WIDTH + 30;
    private final int                   maxX                              = PITCH_WIDTH - 288;
    private final int                   minY                              = 37;
    private final int                   maxY                              = PITCH_HEIGHT - 24;

    private static final int            DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
    private static final int            DIALOG_ENTER_SERVER_IP_ID         = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
    private static final int            DIALOG_SHOW_SERVER_IP_ID          = DIALOG_ENTER_SERVER_IP_ID + 1;

    // ===========================================================
    // Fields
    // ===========================================================

    private BitmapTextureAtlas          mBitmapTextureAtlas;
    private BitmapTextureAtlas          mAutoParallaxBackgroundTexture;

    private ITextureRegion              mParallaxLayerBack;
    private ITextureRegion              mParallaxLayerMid;
    private ITextureRegion              mParallaxLayerFront;
    private ITextureRegion              mBluePlayerTextureRegion;
    private Camera                      mCamera;

    private Rectangle                   mCollidingRectangle;
    protected int                       mLatestTouchEvent;
    private Rectangle[][]               mPitchMatrix;
    private TextureRegion               mGoalLeftTextureRegion;
    private TextureRegion               mGoalRightTextureRegion;
    private TiledTextureRegion          mBallTextureRegion;
    private TextureRegion               mPossesionTextureRegion;
    private TextureRegion               mRedPlayerTextureRegion;
    protected Player                    mLatestPlayer;

    private Set<Player>                 mPlayers                          = new HashSet<Player>();
    private float                       centerX;
    private float                       centerY;
    private BallsServer                 mServer;
    private BallsServerConnector        mServerConnector;
    private String                      mServerIP;
    protected Object                    mUserID;
    private TiledTextureRegion          mRoundActiveTextureRegion;
    private TextureRegion               mRoundCompleteTextureRegion;
    private Sprite                      roundActiveButton;
    private Sprite                      roundCompleteButton;
    private BuildableBitmapTextureAtlas mBitmapAnimatedTextureAtlas;

    @Override
    public EngineOptions onCreateEngineOptions()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy( policy );

        mCamera = new Camera( 0, 0, CAMERA_WIDTH, CAMERA_HEIGHT );

        return new EngineOptions( true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy( CAMERA_WIDTH, CAMERA_HEIGHT ), mCamera );
    }

    @Override
    protected void onCreateResources()
    {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath( "gfx/" );

        // ANIMATED PNGS

        this.mBitmapAnimatedTextureAtlas = new BuildableBitmapTextureAtlas( this.getTextureManager(), 140, 140, TextureOptions.NEAREST );

        // this.mBitmapTextureAtlas = new
        // BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 256,
        // TextureOptions.BILINEAR);

        this.mRoundActiveTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset( this.mBitmapAnimatedTextureAtlas, this, "banana_tiled.png", 4, 2 );

        try
        {
            this.mBitmapAnimatedTextureAtlas.build( new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>( 0, 0, 1 ) );
            this.mBitmapAnimatedTextureAtlas.load();
        }
        catch( TextureAtlasBuilderException e )
        {
            Debug.e( e );
        }

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mBluePlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "blue-player.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mRedPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "red-player.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset( this.mBitmapTextureAtlas, this, "loose-ball.png", 0, 0, 1, 1 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mPossesionTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "in-possession.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mGoalWidth, mGoalHeight, TextureOptions.BILINEAR );
        this.mGoalLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goal-left.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mGoalWidth, mGoalHeight, TextureOptions.BILINEAR );
        this.mGoalRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goal-right.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mRoundButtonWidth, mRoundButtonWidth, TextureOptions.BILINEAR );
        this.mRoundCompleteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "round-complete.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas( this.getTextureManager(), CAMERA_WIDTH, CAMERA_HEIGHT );
        this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mAutoParallaxBackgroundTexture, this, "pitch-bg.jpg", 0, 0 );
        this.mAutoParallaxBackgroundTexture.load();
    }

    @Override
    protected void onCreate( Bundle pSavedInstanceState )
    {
        super.onCreate( pSavedInstanceState );

        this.showDialog( DIALOG_CHOOSE_SERVER_OR_CLIENT_ID );
    }

    @Override
    protected Scene onCreateScene()
    {
        this.mEngine.registerUpdateHandler( new FPSLogger() );

        final Scene scene = new Scene();

        float imageWidth = this.mBluePlayerTextureRegion.getWidth();
        float imageHeight = this.mBluePlayerTextureRegion.getHeight();

        centerX = (CAMERA_WIDTH - imageWidth) / 2;
        centerY = (CAMERA_HEIGHT - imageHeight) / 2;

        setPlayerMatrix( scene );

        final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground( 0, 0, 0, 5 );
        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

        autoParallaxBackground.attachParallaxEntity( new ParallaxEntity( 0.0f, new Sprite( 0, 0, this.mParallaxLayerBack, vertexBufferObjectManager ) ) );

        scene.setBackground( autoParallaxBackground );

        roundActiveButton = new Sprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundButtonWidth, mRoundButtonWidth, mRoundActiveTextureRegion, this.getVertexBufferObjectManager() );

        /* Quickly twinkling face. */
        final AnimatedSprite roundActive = new AnimatedSprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundActiveTextureRegion, this.getVertexBufferObjectManager() );
        roundActive.animate( 100 );
        scene.attachChild( roundActive );

        roundCompleteButton = new Sprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundButtonWidth, mRoundButtonWidth, mRoundCompleteTextureRegion, this.getVertexBufferObjectManager() )
        {
            @Override
            public boolean onAreaTouched( TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY )
            {
                toast( "END ROUND!" );

                scene.detachChild( this );

                try
                {
                    BallsGameActivity.this.mServerConnector.sendClientMessage( new EndRoundClientMessage( BallsGameActivity.this.mUserID, BallsGameActivity.this.mPlayers ) );
                }
                catch( final IOException e )
                {
                    Debug.e( e );
                }

                return super.onAreaTouched( pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY );
            }
        };

        scene.attachChild( roundCompleteButton );
        scene.registerTouchArea( roundCompleteButton );

        final Ball ball = new Ball( centerX, centerY, this.mBallTextureRegion, this.getVertexBufferObjectManager() );

        scene.attachChild( ball );
        scene.registerTouchArea( ball );

        // Add blue players
        addPlayers( Team.BLUE, scene );
        addPlayers( Team.RED, scene );

        scene.setTouchAreaBindingOnActionDownEnabled( true );

        final Sprite inPossession = new Sprite( centerX, centerY, this.mPossesionTextureRegion, this.getVertexBufferObjectManager() );

        scene.attachChild( inPossession );

        inPossession.setAlpha( 0.0f );

        Sprite goalLeft = new Sprite( 0, CAMERA_HEIGHT / 2 - this.mGoalLeftTextureRegion.getHeight() / 2, this.mGoalLeftTextureRegion, this.getVertexBufferObjectManager() );
        scene.attachChild( goalLeft );

        Sprite goalRight = new Sprite( CAMERA_WIDTH - this.mGoalRightTextureRegion.getWidth(), CAMERA_HEIGHT / 2 - this.mGoalRightTextureRegion.getHeight() / 2, this.mGoalRightTextureRegion,
                this.getVertexBufferObjectManager() );
        scene.attachChild( goalRight );

        /* The actual collision-checking. */
        scene.registerUpdateHandler( new IUpdateHandler()
        {
            @Override
            public void reset()
            {
            }

            @Override
            public void onUpdate( final float pSecondsElapsed )
            {
                boolean collide = false;

                if( mLatestPlayer != null )
                {
                    for( Rectangle[] rectRow : mPitchMatrix )
                    {
                        for( Rectangle centerRectangle : rectRow )
                        {
                            if( centerRectangle.collidesWith( mLatestPlayer.getSprite() ) )
                            {
                                mCollidingRectangle = centerRectangle;
                                centerRectangle.setColor( 1, 0, 1 );
                                collide = true;
                            }
                            else
                            {
                                centerRectangle.setColor( 0, 0, 0 );
                            }
                        }
                    }

                    if( mLatestTouchEvent == TouchEvent.ACTION_UP && mCollidingRectangle != null )
                    {
                        mLatestPlayer.getSprite().setScale( 1.0f );

                        // SNAP
                        mLatestPlayer.getSprite().setX( mCollidingRectangle.getX() );
                        mLatestPlayer.getSprite().setY( mCollidingRectangle.getY() );

                        inPossession.setX( mCollidingRectangle.getX() );
                        inPossession.setY( mCollidingRectangle.getY() );

                        // inPossession.setAlpha( 1.0f );
                    }
                }

                if( !collide )
                {
                    mCollidingRectangle = null;
                }

            }
        } );

        return scene;
    }

    private void initServerAndClient()
    {
        BallsGameActivity.this.initServer();

        /*
         * Wait some time after the server has been started, so it actually can
         * start up.
         */
        try
        {
            Thread.sleep( 500 );
        }
        catch( final Throwable t )
        {
            Debug.e( t );
        }

        BallsGameActivity.this.initClient();
    }

    private void initServer()
    {
        this.mServer = new BallsServer( new ExampleClientConnectorListener() );

        this.mServer.start();

        this.mEngine.registerUpdateHandler( this.mServer );
    }

    private void initClient()
    {
        try
        {
            this.mServerConnector = new BallsServerConnector( this.mServerIP, new ExampleServerConnectorListener() );

            this.mServerConnector.getConnection().start();
        }
        catch( final Throwable t )
        {
            Debug.e( t );
        }
    }

    private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener
    {
        @Override
        public void onStarted( final ServerConnector<SocketConnection> pServerConnector )
        {
            BallsGameActivity.this.toast( "CLIENT: Connected to server." );
        }

        @Override
        public void onTerminated( final ServerConnector<SocketConnection> pServerConnector )
        {
            BallsGameActivity.this.toast( "CLIENT: Disconnected from Server." );
            BallsGameActivity.this.finish();
        }
    }

    private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener
    {
        @Override
        public void onStarted( final ClientConnector<SocketConnection> pClientConnector )
        {
            BallsGameActivity.this.toast( "SERVER: Client connected: " + pClientConnector.getConnection().getSocket().getInetAddress().getHostAddress() );
        }

        @Override
        public void onTerminated( final ClientConnector<SocketConnection> pClientConnector )
        {
            BallsGameActivity.this.toast( "SERVER: Client disconnected: " + pClientConnector.getConnection().getSocket().getInetAddress().getHostAddress() );
        }
    }

    private class BallsServerConnector extends ServerConnector<SocketConnection> implements BallsConstants, ServerMessageFlags
    {
        public BallsServerConnector( final String pServerIP, final ISocketConnectionServerConnectorListener pSocketConnectionServerConnectorListener ) throws IOException
        {
            super( new SocketConnection( new Socket( pServerIP, SERVER_PORT ) ), pSocketConnectionServerConnectorListener );
        }
    }

    private void addPlayers( Team team, Scene scene )
    {
        for( int i = 0; i < 6; i++ )
        {
            Position position;
            float xPosition = 0;
            float yPosition = 0;

            if( i == 0 )
            {
                position = Position.GOALKEEPER;

                if( team == Team.RED )
                {
                    xPosition = mPitchMatrix[0][5].getX();
                    yPosition = mPitchMatrix[0][5].getY();
                }
                else
                {
                    xPosition = mPitchMatrix[mPitchMatrix[5].length][5].getX();
                    yPosition = mPitchMatrix[mPitchMatrix[5].length][5].getY();
                }
            }
            else if( i == 1 || i == 2 )
            {
                position = Position.DEFENDER;

                if( team == Team.RED )
                {
                    xPosition = mPitchMatrix[3][3].getX();
                    yPosition = i == 1 ? mPitchMatrix[3][3].getY() : mPitchMatrix[3][7].getY();
                }
                else
                {
                    xPosition = mPitchMatrix[mPitchMatrix[5].length - 3][3].getX();
                    yPosition = i == 1 ? mPitchMatrix[mPitchMatrix[5].length - 3][3].getY() : mPitchMatrix[mPitchMatrix[5].length - 3][7].getY();
                }
            }
            else
            {
                position = Position.ATTACKER;

                if( team == Team.RED )
                {
                    xPosition = mPitchMatrix[5][3].getX();
                }
                else
                {
                    xPosition = mPitchMatrix[mPitchMatrix[5].length - 5][3].getX();
                }

                switch( i )
                {
                    case 3:
                        yPosition = mPitchMatrix[3][2].getY();
                        break;
                    case 4:
                        yPosition = mPitchMatrix[3][5].getY();
                        break;
                    case 5:
                        yPosition = mPitchMatrix[3][8].getY();
                        break;
                }
            }

            ITextureRegion textureRegion = team == Team.RED ? this.mRedPlayerTextureRegion : this.mBluePlayerTextureRegion;

            final Player player = new Player( i, position, team );

            Sprite sprite = new Sprite( xPosition, yPosition, textureRegion, this.getVertexBufferObjectManager() )
            {
                @Override
                public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY )
                {
                    float xPosition = pSceneTouchEvent.getX() - this.getWidth() / 2;
                    float yPosition = pSceneTouchEvent.getY() - this.getHeight() / 2;

                    this.setPosition( getProperX( xPosition ), getProperY( yPosition ) );

                    mLatestTouchEvent = pSceneTouchEvent.getAction();
                    mLatestPlayer = player;

                    this.setScale( 1.5f );

                    return true;
                }
            };

            player.setSprite( sprite );

            scene.attachChild( sprite );
            scene.registerTouchArea( sprite );

            mPlayers.add( player );
        }
    }

    private void setPlayerMatrix( Scene scene )
    {
        mPitchMatrix = new Rectangle[ 12 ][ 11 ];

        for( int i = 0; i < mPitchMatrix.length; i++ )
        {
            Rectangle[] row = mPitchMatrix[i];

            for( int p = 0; p < row.length; p++ )
            {
                int x = minX + i * (maxX / (row.length - 1));
                int y = minY + p * (maxY / (mPitchMatrix.length - 1));

                final Rectangle centerRectangle = new Rectangle( x, y, 10, 10, this.getVertexBufferObjectManager() );

                mPitchMatrix[i][p] = centerRectangle;

                scene.attachChild( centerRectangle );
            }
        }
    }

    protected float getProperY( float yPosition )
    {
        if( yPosition > maxY )
        {
            return maxY;
        }
        else if( yPosition < minY )
        {
            return minY;
        }

        return yPosition;
    }

    protected float getProperX( float xPosition )
    {
        return xPosition;
    }

    private static class Ball extends AnimatedSprite
    {
        protected final PhysicsHandler mPhysicsHandler;

        public Ball( final float pX, final float pY, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager )
        {
            super( pX, pY, pTextureRegion, pVertexBufferObjectManager );
            this.mPhysicsHandler = new PhysicsHandler( this );
            this.registerUpdateHandler( this.mPhysicsHandler );
            this.mPhysicsHandler.setVelocity( BALL_VELOCITY, 0 );
        }

        @Override
        protected void onManagedUpdate( final float pSecondsElapsed )
        {
            if( this.mX < 0 )
            {
                this.mPhysicsHandler.setVelocityX( BALL_VELOCITY );
            }
            else if( this.mX + this.getWidth() > CAMERA_WIDTH )
            {
                this.mPhysicsHandler.setVelocityX( -BALL_VELOCITY );
            }

            /*
             * if( this.mY < 0 ) { this.mPhysicsHandler.setVelocityY(
             * BALL_VELOCITY ); } else if( this.mY + this.getHeight() >
             * CAMERA_HEIGHT ) { this.mPhysicsHandler.setVelocityY(
             * -BALL_VELOCITY ); }
             */

            super.onManagedUpdate( pSecondsElapsed );
        }

        @Override
        public boolean onAreaTouched( TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY )
        {
            Log.d( "balltouch", "!" );

            this.mPhysicsHandler.setVelocity( -this.mPhysicsHandler.getVelocityX(), 0 );

            return super.onAreaTouched( pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY );
        }
    }

    private void toast( final String pMessage )
    {
        this.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText( BallsGameActivity.this, pMessage, Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    @Override
    protected Dialog onCreateDialog( final int pID )
    {
        switch( pID )
        {
            case DIALOG_SHOW_SERVER_IP_ID:
                try
                {
                    return new AlertDialog.Builder( this ).setIcon( android.R.drawable.ic_dialog_info ).setTitle( "Your Server-IP ..." ).setCancelable( false )
                            .setMessage( "The IP of your Server is:\n" + WifiUtils.getWifiIPv4Address( this ) ).setPositiveButton( android.R.string.ok, null ).create();
                }
                catch( final UnknownHostException e )
                {
                    return new AlertDialog.Builder( this ).setIcon( android.R.drawable.ic_dialog_alert ).setTitle( "Your Server-IP ..." ).setCancelable( false )
                            .setMessage( "Error retrieving IP of your Server: " + e ).setPositiveButton( android.R.string.ok, new OnClickListener()
                            {
                                @Override
                                public void onClick( final DialogInterface pDialog, final int pWhich )
                                {
                                    BallsGameActivity.this.finish();
                                }
                            } ).create();
                }
            case DIALOG_ENTER_SERVER_IP_ID:
                final EditText ipEditText = new EditText( this );
                return new AlertDialog.Builder( this ).setIcon( android.R.drawable.ic_dialog_info ).setTitle( "Enter Server-IP ..." ).setCancelable( false ).setView( ipEditText )
                        .setPositiveButton( "Connect", new OnClickListener()
                        {
                            @Override
                            public void onClick( final DialogInterface pDialog, final int pWhich )
                            {
                                BallsGameActivity.this.mServerIP = ipEditText.getText().toString();
                                BallsGameActivity.this.initClient();
                            }
                        } ).setNegativeButton( android.R.string.cancel, new OnClickListener()
                        {
                            @Override
                            public void onClick( final DialogInterface pDialog, final int pWhich )
                            {
                                BallsGameActivity.this.finish();
                            }
                        } ).create();
            case DIALOG_CHOOSE_SERVER_OR_CLIENT_ID:
                return new AlertDialog.Builder( this ).setIcon( android.R.drawable.ic_dialog_info ).setTitle( "Be Server or Client ..." ).setCancelable( false )
                        .setPositiveButton( "Client", new OnClickListener()
                        {
                            @Override
                            public void onClick( final DialogInterface pDialog, final int pWhich )
                            {
                                BallsGameActivity.this.showDialog( DIALOG_ENTER_SERVER_IP_ID );
                            }
                        } ).setNeutralButton( "Server", new OnClickListener()
                        {
                            @Override
                            public void onClick( final DialogInterface pDialog, final int pWhich )
                            {
                                BallsGameActivity.this.initServerAndClient();
                                BallsGameActivity.this.showDialog( DIALOG_SHOW_SERVER_IP_ID );
                            }
                        } ).create();
            default:
                return super.onCreateDialog( pID );
        }
    }

}