package se.footballaddicts.strategicballs.multiplayer;

import java.io.IOException;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener.DefaultSocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.util.debug.Debug;

import se.footballaddicts.strategicballs.BallsConstants;
import se.footballaddicts.strategicballs.multiplayer.client.ClientMessageFlags;
import se.footballaddicts.strategicballs.multiplayer.client.ConnectionCloseClientMessage;
import se.footballaddicts.strategicballs.multiplayer.client.ConnectionEstablishClientMessage;
import se.footballaddicts.strategicballs.multiplayer.client.ConnectionPingClientMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ConnectionEstablishedServerMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ConnectionPongServerMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ConnectionRejectedProtocolMissmatchServerMessage;
import se.footballaddicts.strategicballs.multiplayer.server.ServerMessageFlags;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class BallsServer extends SocketServer<SocketConnectionClientConnector> implements BallsConstants, ServerMessageFlags, ClientMessageFlags, IUpdateHandler, ContactListener
{
    private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

    public BallsServer( final ISocketConnectionClientConnectorListener pSocketConnectionClientConnectorListener )
    {
        super( SERVER_PORT, pSocketConnectionClientConnectorListener, new DefaultSocketServerListener<SocketConnectionClientConnector>() );
    }

    @Override
    protected SocketConnectionClientConnector newClientConnector( SocketConnection pSocketConnection ) throws IOException
    {
        final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector( pSocketConnection );

        clientConnector.registerClientMessage( FLAG_MESSAGE_CLIENT_CONNECTION_CLOSE, ConnectionCloseClientMessage.class, new IClientMessageHandler<SocketConnection>()
        {
            @Override
            public void onHandleMessage( final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage ) throws IOException
            {
                pClientConnector.terminate();
            }
        } );

        clientConnector.registerClientMessage( FLAG_MESSAGE_CLIENT_CONNECTION_ESTABLISH, ConnectionEstablishClientMessage.class, new IClientMessageHandler<SocketConnection>()
        {
            @Override
            public void onHandleMessage( final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage ) throws IOException
            {
                final ConnectionEstablishClientMessage connectionEstablishClientMessage = (ConnectionEstablishClientMessage) pClientMessage;
                if( connectionEstablishClientMessage.getProtocolVersion() == MessageConstants.PROTOCOL_VERSION )
                {
                    final ConnectionEstablishedServerMessage connectionEstablishedServerMessage = (ConnectionEstablishedServerMessage) BallsServer.this.mMessagePool
                            .obtainMessage( FLAG_MESSAGE_SERVER_CONNECTION_ESTABLISHED );
                    try
                    {
                        pClientConnector.sendServerMessage( connectionEstablishedServerMessage );
                    }
                    catch( IOException e )
                    {
                        Debug.e( e );
                    }
                    
                    BallsServer.this.mMessagePool.recycleMessage( connectionEstablishedServerMessage );
                }
                else
                {
                    final ConnectionRejectedProtocolMissmatchServerMessage connectionRejectedProtocolMissmatchServerMessage = (ConnectionRejectedProtocolMissmatchServerMessage) BallsServer.this.mMessagePool
                            .obtainMessage( FLAG_MESSAGE_SERVER_CONNECTION_REJECTED_PROTOCOL_MISSMATCH );
                    connectionRejectedProtocolMissmatchServerMessage.setProtocolVersion( MessageConstants.PROTOCOL_VERSION );
                    try
                    {
                        pClientConnector.sendServerMessage( connectionRejectedProtocolMissmatchServerMessage );
                    }
                    catch( IOException e )
                    {
                        Debug.e( e );
                    }

                    BallsServer.this.mMessagePool.recycleMessage( connectionRejectedProtocolMissmatchServerMessage );
                }
            }
        } );

        clientConnector.registerClientMessage( FLAG_MESSAGE_CLIENT_CONNECTION_PING, ConnectionPingClientMessage.class, new IClientMessageHandler<SocketConnection>()
        {
            @Override
            public void onHandleMessage( final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage ) throws IOException
            {
                final ConnectionPongServerMessage connectionPongServerMessage = (ConnectionPongServerMessage) BallsServer.this.mMessagePool.obtainMessage( FLAG_MESSAGE_SERVER_CONNECTION_PONG );
                try
                {
                    pClientConnector.sendServerMessage( connectionPongServerMessage );
                }
                catch( IOException e )
                {
                    Debug.e( e );
                }

                BallsServer.this.mMessagePool.recycleMessage( connectionPongServerMessage );
            }
        } );

        clientConnector.sendServerMessage( new SetUserIDServerMessage( this.mClientConnectors.size() ) ); // TODO
                                                                                                          // should
                                                                                                          // not
                                                                                                          // be
                                                                                                          // size(),
                                                                                                          // as
                                                                                                          // it
                                                                                                          // only
                                                                                                          // works
                                                                                                          // properly
                                                                                                          // for
                                                                                                          // first
                                                                                                          // two
                                                                                                          // connections!
        return clientConnector;
    }

    @Override
    public void beginContact( Contact contact )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void endContact( Contact contact )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void preSolve( Contact contact, Manifold oldManifold )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void postSolve( Contact contact, ContactImpulse impulse )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate( float pSecondsElapsed )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void reset()
    {
        // TODO Auto-generated method stub

    }
}
