package se.footballaddicts.strategicballs.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import se.footballaddicts.strategicballs.BallsConstants;
import se.footballaddicts.strategicballs.Player.TeamType;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 19:48:32 - 28.02.2011
 */
public class SetUserIDServerMessage extends ServerMessage implements BallsConstants
{
    // ===========================================================
    // Constants
    // ===========================================================
    
    public static final short FLAG_SET_ID_MESSAGE = 100;

    // ===========================================================
    // Fields
    // ===========================================================

    public int mUserID;
    public TeamType team;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SetUserIDServerMessage()
    {

    }

    public SetUserIDServerMessage( final int pUserID, final TeamType team )
    {
        this.mUserID = pUserID;
        this.team = team;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void set( final int pPaddleID )
    {
        this.mUserID = pPaddleID;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public short getFlag()
    {
        return FLAG_SET_ID_MESSAGE;
    }

    @Override
    protected void onReadTransmissionData( DataInputStream pDataInputStream ) throws IOException
    {
        this.mUserID = pDataInputStream.readInt();
        this.team = TeamType.fromServer( pDataInputStream.readInt() );
    }

    @Override
    protected void onWriteTransmissionData( final DataOutputStream pDataOutputStream ) throws IOException
    {
        pDataOutputStream.writeInt( this.mUserID );
        pDataOutputStream.writeInt( team.ordinal() );
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}