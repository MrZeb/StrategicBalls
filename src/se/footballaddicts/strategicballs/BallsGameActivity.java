package se.footballaddicts.strategicballs;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.shape.RectangularShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageReader.ServerMessageReader;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
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
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import se.footballaddicts.strategicballs.Player.GeneralPlayerType;
import se.footballaddicts.strategicballs.Player.PlayerType;
import se.footballaddicts.strategicballs.Player.TeamType;
import se.footballaddicts.strategicballs.multiplayer.BallsServer;
import se.footballaddicts.strategicballs.multiplayer.EndRoundClientMessage;
import se.footballaddicts.strategicballs.multiplayer.EndRoundServerMessage;
import se.footballaddicts.strategicballs.multiplayer.Move;
import se.footballaddicts.strategicballs.multiplayer.Move.MoveType;
import se.footballaddicts.strategicballs.multiplayer.SetUserIDServerMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ServerMessageFlags;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Point;
import android.graphics.Typeface;
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

    private Cell                        mCollidingRectangle;
    protected boolean                   mDropSelection;

    private TextureRegion               mGoalLeftTextureRegion;
    private TextureRegion               mGoalRightTextureRegion;
    private TiledTextureRegion          mBallTextureRegion;
    protected BallsEntity               mSelectedEntity;

    private ArrayList<Player>           mPlayers                          = new ArrayList<Player>();
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

    private TeamType                    mCurrentTeam;

    private Entity[][]                  mPitchMatrix;
    protected boolean                   isServer;
    private Font                        mFont;
    private ProgressDialog              waitForOtherUserMessage;

    private Toast                       currentToast;
    private Ball                        mBall;

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

        this.mFont = FontFactory.create( this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create( Typeface.DEFAULT, Typeface.BOLD ), 32, android.graphics.Color.WHITE );
        this.mFont.load();

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
    }

    @Override
    public synchronized void onResumeGame()
    {
        super.onResumeGame();

        Log.d( "SERVER", "HIYA" );
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
                    endRound();
                }

                return super.onAreaTouched( pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY );
            }
        };

        scene.attachChild( roundCompleteButton );
        scene.registerTouchArea( roundCompleteButton );

        mBall = new Ball();

        mBall.setSprite( new AnimatedSprite( 0, 0, this.mBallTextureRegion, this.getVertexBufferObjectManager() )
        {
            @Override
            public boolean onAreaTouched( TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY )
            {
                float xPosition = pSceneTouchEvent.getX() - this.getWidth() / 2;
                float yPosition = pSceneTouchEvent.getY() - this.getHeight() / 2;

                this.setPosition( getProperX( xPosition ), getProperY( yPosition ) );

                mDropSelection = pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP;
                mSelectedEntity = mBall;

                this.setScale( 1.5f );

                return true;
            }
        } );

        scene.registerTouchArea( mBall.getSprite() );

        // Add players
        addPlayers( TeamType.RIGHT, scene );
        addPlayers( TeamType.LEFT, scene );

        // updateEntityPositions( getMovesForRound() );

        coinTossForBall();

        mBall.getSprite().setY( mPitchMatrix[5][5].getY() );

        if( mCurrentTeam == TeamType.LEFT )
        {
            mBall.setRoundStartCoordinates( new Point( 5, 5 ) );
            mBall.getSprite().setX( mPitchMatrix[5][5].getX() );
        }
        else
        {
            mBall.setRoundStartCoordinates( new Point( 6, 5 ) );
            mBall.getSprite().setX( mPitchMatrix[6][5].getX() );
        }

        scene.attachChild( mBall.getSprite() );

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

                if( isServer )
                {
                    Text leftText = null;

                    try
                    {
                        leftText = new Text( 0, 0, BallsGameActivity.this.mFont, WifiUtils.getWifiIPv4Address( BallsGameActivity.this ), new TextOptions( HorizontalAlign.LEFT ),
                                vertexBufferObjectManager );

                        isServer = false;
                    }
                    catch( UnknownHostException e )
                    {
                        e.printStackTrace();
                    }

                    scene.attachChild( leftText );
                }

                if( mSelectedEntity != null )
                {
                    for( Entity[] rectRow : mPitchMatrix )
                    {
                        for( Entity centerRectangle : rectRow )
                        {
                            if( ((RectangularShape) centerRectangle).collidesWith( mSelectedEntity.getSprite() ) )
                            {
                                mCollidingRectangle = (Cell) centerRectangle;
                                centerRectangle.setColor( 1, 0, 1 );
                                collide = true;
                            }
                            else
                            {
                                centerRectangle.setColor( 0, 0, 0 );
                            }
                        }
                    }

                    if( mDropSelection && mCollidingRectangle != null )
                    {
                        mSelectedEntity.getSprite().setScale( 1.0f );

                        // SNAP
                        mSelectedEntity.getSprite().setX( mCollidingRectangle.getX() );
                        mSelectedEntity.getSprite().setY( mCollidingRectangle.getY() );

                        mSelectedEntity.setCurrentCoordinates( mCollidingRectangle.getCoordinates() );
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

    private void coinTossForBall()
    {
        if( new Random().nextInt( 2 ) == 1 )
        {
            setCurrentTeam( TeamType.LEFT );
        }
        else
        {
            setCurrentTeam( TeamType.RIGHT );
        }
    }

    protected void endRound()
    {
        if( mCurrentTeam == TeamType.LEFT )
        {
            setCurrentTeam( TeamType.RIGHT );
        }
        else
        {
            setCurrentTeam( TeamType.LEFT );
        }

        try
        {
            BallsGameActivity.this.mServerConnector.sendClientMessage( new EndRoundClientMessage( BallsGameActivity.this.mUserID, getMovesForRound() ) );
        }
        catch( final IOException e )
        {
            Debug.e( e );
        }

        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                waitForOtherUserMessage = ProgressDialog.show( BallsGameActivity.this, null, "Waiting for the other player to end its turn", true );
            }
        } );
    }

    private void setCurrentTeam( TeamType team )
    {
        mCurrentTeam = team;
        toast( "END ROUND! Team " + mCurrentTeam + "'s turn!" );
    }

    protected Set<Move> getMovesForRound()
    {
        Set<Move> moves = new HashSet<Move>();

        for( Player player : mPlayers )
        {
            if( player.getCurrentCoordinates() == null )
            {
                player.setCurrentCoordinates( player.getRoundStartCoordinates() );
            }

            moves.add( new Move( MoveType.PLAYER, player.getTeam(), player.getType(), player.getRoundStartCoordinates(), player.getCurrentCoordinates() ) );
            Log.d( "playermoves", player.getType() + " start: " + player.getRoundStartCoordinates() + " current: " + player.getCurrentCoordinates() );
            player.setRoundStartCoordinates( player.getCurrentCoordinates() );
        }

        if( mBall.getCurrentCoordinates() == null )
        {
            mBall.setCurrentCoordinates( mBall.getRoundStartCoordinates() );
        }

        moves.add( new Move( MoveType.BALL, null, null, mBall.getRoundStartCoordinates(), mBall.getCurrentCoordinates() ) );
        mBall.setRoundStartCoordinates( mBall.getCurrentCoordinates() );

        return moves;
    }

    private void updateEntityPositions( Set<Move> moves )
    {
        Log.d( "opponent", "init" );

        for( Move move : moves )
        {
            if( move.getType() == MoveType.PLAYER )
            {
                for( Player player : mPlayers )
                {
                    if( player.getTeam() == move.getTeam() && player.getType() == move.getPlayerType() )
                    {
                        Log.d( "opponent", player.getType() + " " + player.getTeam() + " start: " + player.getRoundStartCoordinates() + " current: " + player.getCurrentCoordinates() + " from: "
                                + move.getFrom() + " to: " + move.getTo() );

                        move.setTo( new Point( move.getTo().x, move.getTo().y - 1 ) );

                        moveEntity( player, move );

                        break;
                    }
                }
            }
            else if( move.getType() == MoveType.BALL )
            {
                moveEntity( mBall, move );
            }
        }
    }

    private void moveEntity( BallsEntity entity, Move move )
    {
        Log.d( "opponentmove", entity.getClass() + " " + move.getType() + " " + move.getTo() );

        entity.getSprite().setX( mPitchMatrix[move.getTo().x][move.getTo().y].getX() );
        entity.getSprite().setY( mPitchMatrix[move.getTo().x][move.getTo().y].getY() );
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
            this.mServerConnector = new BallsServerConnector( this.mServerIP, new ExampleServerConnectorListener(), new ExampleServerMessageReader() );

            this.mServerConnector.registerServerMessage( EndRoundServerMessage.FLAG_END_ROUND_MESSAGE, EndRoundServerMessage.class );

            this.mServerConnector.registerServerMessage( SetUserIDServerMessage.FLAG_SET_ID_MESSAGE, SetUserIDServerMessage.class );

            // this.mServerConnector

            // this.mServerConnector

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

    public class ExampleServerMessageReader extends ServerMessageReader<SocketConnection>
    {
        @Override
        public void handleMessage( ServerConnector<SocketConnection> pConnector, IServerMessage pMessage ) throws IOException
        {
            super.handleMessage( pConnector, pMessage );

            if( pMessage instanceof EndRoundServerMessage )
            {
                updateEntityPositions( ((EndRoundServerMessage) pMessage).getMoves() );
            }
        }
    }

    private class BallsServerConnector extends ServerConnector<SocketConnection> implements BallsConstants, ServerMessageFlags
    {
        public BallsServerConnector( final String pServerIP, final ISocketConnectionServerConnectorListener pSocketConnectionServerConnectorListener, ExampleServerMessageReader pServerMessageReader )
                                                                                                                                                                                                       throws IOException
        {
            super( new SocketConnection( new Socket( pServerIP, SERVER_PORT ) ), pServerMessageReader, pSocketConnectionServerConnectorListener );
        }

        @Override
        public void read( DataInputStream pDataInputStream ) throws IOException
        {
            super.read( pDataInputStream );
        }

    }

    private void addPlayers( TeamType team, Scene scene )
    {
        for( int i = 0; i < 6; i++ )
        {
            PlayerType type = PlayerType.getTypeForIndex( i );

            float xPosition = 0;
            float yPosition = 0;

            ITextureRegion textureRegion = null;

            Point logicalCoordinates = new Point();

            GeneralPlayerType generalType = type.getGeneralType();

            switch( generalType )
            {
                case ATTACKER:

                    logicalCoordinates = type.getLogicalCoordinates( team, mPitchMatrix[0].length );

                    if( team == TeamType.LEFT )
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

                    logicalCoordinates = type.getLogicalCoordinates( team, mPitchMatrix[0].length );

                    if( team == TeamType.LEFT )
                    {
                        xPosition = mPitchMatrix[logicalCoordinates.x][0].getX();

                        yPosition = mPitchMatrix[0][logicalCoordinates.y].getY();

                        textureRegion = mDefenderATextureRegion;
                    }
                    else
                    {
                        xPosition = mPitchMatrix[logicalCoordinates.x][0].getX();

                        yPosition = mPitchMatrix[0][logicalCoordinates.y].getY();

                        textureRegion = mDefenderBTextureRegion;
                    }

                    break;

                case GOALKEEPER:

                    logicalCoordinates = type.getLogicalCoordinates( team, mPitchMatrix[0].length );

                    if( team == TeamType.LEFT )
                    {
                        xPosition = mPitchMatrix[logicalCoordinates.x][0].getX();
                        yPosition = mPitchMatrix[0][logicalCoordinates.y].getY();

                        textureRegion = mGoalkeeperATextureRegion;
                    }
                    else
                    {
                        xPosition = mPitchMatrix[logicalCoordinates.x][0].getX();
                        yPosition = mPitchMatrix[0][logicalCoordinates.y].getY();

                        textureRegion = mGoalkeeperBTextureRegion;
                    }

                    break;

                default:
                    break;
            }

            final Player player = new Player( logicalCoordinates, type, team );

            Sprite sprite = new Sprite( xPosition, yPosition, textureRegion, this.getVertexBufferObjectManager() )
            {
                @Override
                public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY )
                {
                    float xPosition = pSceneTouchEvent.getX() - this.getWidth() / 2;
                    float yPosition = pSceneTouchEvent.getY() - this.getHeight() / 2;

                    this.setPosition( getProperX( xPosition ), getProperY( yPosition ) );

                    mDropSelection = pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP;
                    mSelectedEntity = player;

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
        mPitchMatrix = new Cell[ 12 ][ 11 ];

        for( int i = 0; i < mPitchMatrix.length; i++ )
        {
            Entity[] row = mPitchMatrix[i];

            for( int p = 0; p < row.length; p++ )
            {
                int x = minX + i * (maxX / (row.length - 1));
                int y = minY + p * (maxY / (mPitchMatrix.length - 1));

                final Cell cell = new Cell( x, y, 0, 0, this.getVertexBufferObjectManager() );

                cell.setCoordinates( new Point( i, p ) );

                mPitchMatrix[i][p] = cell;

                scene.attachChild( cell );
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

    private void toast( final String pMessage )
    {
        this.runOnUiThread( new Runnable()
        {

            @Override
            public void run()
            {
                if( currentToast == null )
                {
                    currentToast = Toast.makeText( BallsGameActivity.this, pMessage, Toast.LENGTH_SHORT );
                }
                else
                {
                    currentToast.setText( pMessage );
                }

                currentToast.show();
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
                                if( ipEditText.getText().toString().length() > 0 )
                                {
                                    BallsGameActivity.this.mServerIP = ipEditText.getText().toString();
                                }
                                else
                                {
                                    BallsGameActivity.this.mServerIP = "192.168.11.158";
                                }

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
                                isServer = false;
                                BallsGameActivity.this.showDialog( DIALOG_ENTER_SERVER_IP_ID );
                            }
                        } ).setNeutralButton( "Server", new OnClickListener()
                        {
                            @Override
                            public void onClick( final DialogInterface pDialog, final int pWhich )
                            {
                                isServer = true;
                                BallsGameActivity.this.initServerAndClient();
                                BallsGameActivity.this.showDialog( DIALOG_SHOW_SERVER_IP_ID );
                            }
                        } ).create();
            default:
                return super.onCreateDialog( pID );
        }
    }
}
