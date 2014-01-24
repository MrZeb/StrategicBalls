package se.footballaddicts.strategicballs.multiplayer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import se.footballaddicts.strategicballs.Player.TeamType;

public class CoinTossServerMessage extends ServerMessage
{
    public static final short FLAG_COIN_TOSS = 110;

    public TeamType teamInPossesion;
    
    public CoinTossServerMessage()
    {
        // TODO Auto-generated constructor stub
    }
    
    public CoinTossServerMessage( TeamType teamInPossesion )
    {
        this.teamInPossesion = teamInPossesion;
    }

    @Override
    public short getFlag()
    {
        return FLAG_COIN_TOSS;
    }

    @Override
    protected void onReadTransmissionData( DataInputStream pDataInputStream ) throws IOException
    {
        teamInPossesion = TeamType.fromServer( pDataInputStream.readInt() );
    }

    @Override
    protected void onWriteTransmissionData( DataOutputStream pDataOutputStream ) throws IOException
    {
        pDataOutputStream.writeInt( teamInPossesion.ordinal() );
    }
    
    
    
}
