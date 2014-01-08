package se.footballaddicts.strategicballs.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

import se.footballaddicts.strategicballs.multiplayer.Move.MoveType;

public class EndRoundClientMessage extends ClientMessage
{
    private static final short FLAG_END_ROUND_MESSAGE = 1;
    
    private Object    mUserID;
    private Set<Move> mMoves;

    public EndRoundClientMessage( Object userID, Set<Move> moves )
    {
        this.setmUserID( userID );
        this.setMoves( moves );
    }

    @Override
    public short getFlag()
    {
        return FLAG_END_ROUND_MESSAGE;
    }

    @Override
    protected void onReadTransmissionData( DataInputStream pDataInputStream ) throws IOException
    {
        int size = pDataInputStream.readInt();

        this.mMoves = new HashSet<Move>( size );

        for( int i = 0; i < size; i++ )
        {
            MoveType moveType = MoveType.getMoveTypeFromId( pDataInputStream.readInt() );
            int[] from = new int[ 2 ];
            from[0] = pDataInputStream.readInt();
            from[1] = pDataInputStream.readInt();
            int[] to = new int[ 2 ];
            to[0] = pDataInputStream.readInt();
            to[1] = pDataInputStream.readInt();
            Move move = new Move( moveType, from, to );
            mMoves.add( move );
        }
    }

    @Override
    protected void onWriteTransmissionData( DataOutputStream pDataOutputStream ) throws IOException
    {
        pDataOutputStream.writeInt( mMoves.size() );
        for ( Move move : mMoves )
        {
            pDataOutputStream.writeInt( move.getMoveType().id );
            pDataOutputStream.writeInt( move.getFrom()[0] );
            pDataOutputStream.writeInt( move.getFrom()[1] );
            pDataOutputStream.writeInt( move.getTo()[0] );
            pDataOutputStream.writeInt( move.getTo()[1] );
        }
    }

    public Object getmUserID()
    {
        return mUserID;
    }

    public void setmUserID( Object mUserID )
    {
        this.mUserID = mUserID;
    }

    public Set<Move> getMoves()
    {
        return mMoves;
    }

    public void setMoves( Set<Move> moves )
    {
        this.mMoves = moves;
    }

}
