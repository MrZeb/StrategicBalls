package se.footballaddicts.strategicballs;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
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
import se.footballaddicts.strategicballs.multiplayer.Move;
import se.footballaddicts.strategicballs.multiplayer.Move.MoveType;
import se.footballaddicts.strategicballs.multiplayer.server.ServerMessageFlags;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Point;
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
    private Camera                      mCamera;

    private Rectangle                   mCollidingRectangle;
    protected int                       mLatestTouchEvent;

    private TextureRegion               mGoalLeftTextureRegion;
    private TextureRegion               mGoalRightTextureRegion;
    private TiledTextureRegion          mBallTextureRegion;
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
    private TextureRegion               mDefenderATextureRegion;
    private TextureRegion               mDefenderBTextureRegion;
    private TextureRegion               mAttackerATextureRegion;
    private TextureRegion               mAttackerBTextureRegion;
    private TextureRegion               mGoalkeeperATextureRegion;
    private TextureRegion               mGoalkeeperBTextureRegion;

    private Team                        mCurrentTeam;

    private Rectangle[][]               mPitchMatrix;
    private Entity[][]                  roundStartMatrix;

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

        createAnimatedSprites();

        createPlayerSprites();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset( this.mBitmapTextureAtlas, this, "ball.png", 0, 0, 1, 1 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mGoalWidth, mGoalHeight, TextureOptions.BILINEAR );
        this.mGoalLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goal_left.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mGoalWidth, mGoalHeight, TextureOptions.BILINEAR );
        this.mGoalRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goal_right.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mRoundButtonWidth, mRoundButtonWidth, TextureOptions.BILINEAR );
        this.mRoundCompleteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "round_complete.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas( this.getTextureManager(), CAMERA_WIDTH, CAMERA_HEIGHT );
        this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mAutoParallaxBackgroundTexture, this, "pitch_bg.jpg", 0, 0 );
        this.mAutoParallaxBackgroundTexture.load();
    }

    private void createPlayerSprites()
    {
        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mDefenderATextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "defender_team_a.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mDefenderBTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "defender_team_b.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mAttackerATextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "forward_team_a.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mAttackerBTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "forward_team_b.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mGoalkeeperATextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goalkeeper_team_a.png", 0, 0 );
        this.mBitmapTextureAtlas.load();

        this.mBitmapTextureAtlas = new BitmapTextureAtlas( this.getTextureManager(), mSpriteWidth, mSpriteWidth, TextureOptions.BILINEAR );
        this.mGoalkeeperBTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset( this.mBitmapTextureAtlas, this, "goalkeeper_team_b.png", 0, 0 );
        this.mBitmapTextureAtlas.load();
    }

    private void createAnimatedSprites()
    {
        this.mBitmapAnimatedTextureAtlas = new BuildableBitmapTextureAtlas( this.getTextureManager(), 140 * 10, 56 * 10, TextureOptions.NEAREST );

        // this.mBitmapTextureAtlas = new
        // BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 256,
        // TextureOptions.BILINEAR);

        this.mRoundActiveTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset( this.mBitmapAnimatedTextureAtlas, this, "round_active_sprite.png", 10, 4 );

        try
        {
            this.mBitmapAnimatedTextureAtlas.build( new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>( 0, 0, 1 ) );
            this.mBitmapAnimatedTextureAtlas.load();
        }
        catch( TextureAtlasBuilderException e )
        {
            Debug.e( e );
        }
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

        centerX = (CAMERA_WIDTH - mSpriteWidth) / 2;
        centerY = (CAMERA_HEIGHT - mSpriteWidth) / 2;

        initiatePitchMatrix( scene );

        final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground( 0, 0, 0, 5 );
        final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();

        autoParallaxBackground.attachParallaxEntity( new ParallaxEntity( 0.0f, new Sprite( 0, 0, this.mParallaxLayerBack, vertexBufferObjectManager ) ) );

        scene.setBackground( autoParallaxBackground );

        roundActiveButton = new Sprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundButtonWidth, mRoundButtonWidth, mRoundActiveTextureRegion, this.getVertexBufferObjectManager() );

        /* Animated round image. */
        final AnimatedSprite roundActive = new AnimatedSprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundActiveTextureRegion, this.getVertexBufferObjectManager() );
        roundActive.animate( 100 );
        // scene.attachChild( roundActive );

        roundCompleteButton = new Sprite( CAMERA_WIDTH - UI_WIDTH, 0, mRoundButtonWidth, mRoundButtonWidth, mRoundCompleteTextureRegion, this.getVertexBufferObjectManager() )
        {
            @Override
            public boolean onAreaTouched( TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY )
            {
                // scene.detachChild( this );

                if( pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP )
                {
                    if( mCurrentTeam == Team.A )
                    {
                        mCurrentTeam = Team.B;
                    }
                    else
                    {
                        mCurrentTeam = Team.A;
                    }

                    toast( "END ROUND! Team " + mCurrentTeam + "'s turn!" );

                    try
                    {
                        //TODO Remove dummy data
                        
                        Set<Move> moves = new HashSet<Move>();
                        
                        Move move1 = new Move( MoveType.PLAYER, new int[]{1,2}, new int[]{4,5} );
                        moves.add( move1 );
                        
                        Move move2 = new Move( MoveType.PLAYER, new int[]{3,3}, new int[]{5,4} );
                        moves.add( move2 );
                        
                        Move move3 = new Move( MoveType.PLAYER, new int[]{2,4}, new int[]{7,1} );
                        moves.add( move3 );
                        
                        BallsGameActivity.this.mServerConnector.sendClientMessage( new EndRoundClientMessage( BallsGameActivity.this.mUserID, moves ) );
                    }
                    catch( final IOException e )
                    {
                        Debug.e( e );
                    }

                    roundStartMatrix = mPitchMatrix;
                }

                return super.onAreaTouched( pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY );
            }
        };

        scene.attachChild( roundCompleteButton );
        scene.registerTouchArea( roundCompleteButton );

        final Ball ball = new Ball( centerX, centerY, this.mBallTextureRegion, this.getVertexBufferObjectManager() );

        scene.attachChild( ball );
        scene.registerTouchArea( ball );

        // Add players
        addPlayers( Team.B, scene );
        addPlayers( Team.A, scene );

        roundStartMatrix = mPitchMatrix;

        scene.setTouchAreaBindingOnActionDownEnabled( true );

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

    protected Set<Move> getMovesForRound()
    {

        return null;
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
            Position position = Position.getPositionForIndex( i );

            float xPosition = 0;
            float yPosition = 0;

            ITextureRegion textureRegion = null;

            switch( position )
            {
                case ATTACKER:

                    if( team == Team.A )
                    {
                        xPosition = mPitchMatrix[5][3].getX();

                        textureRegion = mAttackerATextureRegion;
                    }
                    else
                    {
                        xPosition = mPitchMatrix[mPitchMatrix[5].length - 5][3].getX();

                        textureRegion = mAttackerBTextureRegion;
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
                    break;

                case DEFENDER:

                    if( team == Team.A )
                    {
                        xPosition = mPitchMatrix[3][3].getX();
                        yPosition = i == 1 ? mPitchMatrix[3][3].getY() : mPitchMatrix[3][7].getY();

                        textureRegion = mDefenderATextureRegion;
                    }
                    else
                    {
                        xPosition = mPitchMatrix[mPitchMatrix[5].length - 3][3].getX();
                        yPosition = i == 1 ? mPitchMatrix[mPitchMatrix[5].length - 3][3].getY() : mPitchMatrix[mPitchMatrix[5].length - 3][7].getY();

                        textureRegion = mDefenderBTextureRegion;
                    }
                    break;

                case GOALKEEPER:

                    if( team == Team.A )
                    {
                        xPosition = mPitchMatrix[0][5].getX();
                        yPosition = mPitchMatrix[0][5].getY();

                        textureRegion = mGoalkeeperATextureRegion;
                    }
                    else
                    {
                        xPosition = mPitchMatrix[mPitchMatrix[5].length][5].getX();
                        yPosition = mPitchMatrix[mPitchMatrix[5].length][5].getY();

                        textureRegion = mGoalkeeperBTextureRegion;
                    }

                    break;

                default:
                    break;
            }

            final Player player = new Player( null, position, team );

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

    private void initiatePitchMatrix( Scene scene )
    {
        mPitchMatrix = new Rectangle[ 12 ][ 11 ];

        for( int i = 0; i < mPitchMatrix.length; i++ )
        {
            Rectangle[] row = mPitchMatrix[i];

            for( int p = 0; p < row.length; p++ )
            {
                int x = minX + i * (maxX / (row.length - 1));
                int y = minY + p * (maxY / (mPitchMatrix.length - 1));

                final Rectangle centerRectangle = new Rectangle( x, y, 0, 0, this.getVertexBufferObjectManager() );

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
