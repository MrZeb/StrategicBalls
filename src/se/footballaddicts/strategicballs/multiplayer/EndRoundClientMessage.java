package se.footballaddicts.strategicballs.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

import se.footballaddicts.strategicballs.BallsConstants;
import se.footballaddicts.strategicballs.Player;

public class EndRoundClientMessage extends ClientMessage implements BallsConstants
{
    private Object      mUserID;
    private Set<Player> mPlayers;

    public EndRoundClientMessage( Object userID, Set<Player> players )
    {
        this.setmUserID( userID );
        this.setmPlayers( players );
    }

    @Override
    public short getFlag()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void read( DataInputStream pDataInputStream ) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void write( DataOutputStream pDataOutputStream ) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onReadTransmissionData( DataInputStream pDataInputStream ) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onWriteTransmissionData( DataOutputStream pDataOutputStream ) throws IOException
    {
        // TODO Auto-generated method stub

    }

    public Object getmUserID()
    {
        return mUserID;
    }

    public void setmUserID( Object mUserID )
    {
        this.mUserID = mUserID;
    }

    public Set<Player> getmPlayers()
    {
        return mPlayers;
    }

    public void setmPlayers( Set<Player> mPlayers )
    {
        this.mPlayers = mPlayers;
    }

}
