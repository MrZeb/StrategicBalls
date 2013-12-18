package se.footballaddicts.strategicballs.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import se.footballaddicts.strategicballs.BallsConstants;

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

    // ===========================================================
    // Fields
    // ===========================================================

    public int mUserID;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SetUserIDServerMessage()
    {

    }

    public SetUserIDServerMessage( final int pUserID )
    {
        this.mUserID = pUserID;
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
        return 0;
    }

    @Override
    protected void onReadTransmissionData( DataInputStream pDataInputStream ) throws IOException
    {
        this.mUserID = pDataInputStream.readInt();
    }

    @Override
    protected void onWriteTransmissionData( final DataOutputStream pDataOutputStream ) throws IOException
    {
        pDataOutputStream.writeInt( this.mUserID );
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}