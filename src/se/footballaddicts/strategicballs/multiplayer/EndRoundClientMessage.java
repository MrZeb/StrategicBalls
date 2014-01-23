package se.footballaddicts.strategicballs.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;

import se.footballaddicts.strategicballs.Player.PlayerType;
import se.footballaddicts.strategicballs.Player.TeamType;
import se.footballaddicts.strategicballs.multiplayer.Move.MoveType;
import android.graphics.Point;
import android.util.Log;

public class EndRoundClientMessage extends ClientMessage
{
    public static final short FLAG_END_ROUND_MESSAGE = 1;

    private Integer           mUserID;
    private Set<Move>         mMoves;

    public EndRoundClientMessage()
    {
    }

    public EndRoundClientMessage( Integer userID, Set<Move> moves )
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
        mUserID = pDataInputStream.readInt();

        int size = pDataInputStream.readInt();

        this.mMoves = new HashSet<Move>( size );

        for( int i = 0; i < size; i++ )
        {
            MoveType moveType = MoveType.getMoveTypeFromId( pDataInputStream.readInt() );
            TeamType team = TeamType.values()[pDataInputStream.readInt()];
            PlayerType type = PlayerType.getTypeForIndex( pDataInputStream.readInt() );
            Point from = new Point( pDataInputStream.readInt(), pDataInputStream.readInt() );
            Point to = new Point( pDataInputStream.readInt(), pDataInputStream.readInt() );

            Move move = new Move( moveType, team, type, from, to );

            mMoves.add( move );
        }
    }

    @Override
    protected void onWriteTransmissionData( DataOutputStream pDataOutputStream ) throws IOException
    {
        pDataOutputStream.writeInt( mUserID );

        pDataOutputStream.writeInt( mMoves.size() );

        for( Move move : mMoves )
        {
            Log.d( "write", move.getType() + "" );

            pDataOutputStream.writeInt( move.getType().id );

            if( move.getTeam() != null )
            {
                pDataOutputStream.writeInt( move.getTeam().ordinal() );
            }
            else
            {
                pDataOutputStream.writeInt( 0 );
            }
            if( move.getPlayerType() != null )
            {
                pDataOutputStream.writeInt( move.getPlayerType().getIndex() );
            }
            else
            {
                pDataOutputStream.writeInt( 0 );
            }

            pDataOutputStream.writeInt( move.getFrom().x );
            pDataOutputStream.writeInt( move.getFrom().y );
            pDataOutputStream.writeInt( move.getTo().x );
            pDataOutputStream.writeInt( move.getTo().y );
        }
    }

    public Integer getmUserID()
    {
        return mUserID;
    }

    public void setmUserID( Integer pUserID )
    {
        this.mUserID = pUserID;
    }

    public Set<Move> getMoves()
    {
        return mMoves;
    }

    public void setMoves( Set<Move> moves )
    {
        this.mMoves = moves;
    }

    @Override
    public String toString()
    {
        return "EndRoundClientMessage [mUserID=" + mUserID + ", mMoves=" + mMoves + "]";
    }

}
