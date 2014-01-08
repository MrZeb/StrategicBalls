package se.footballaddicts.strategicballs.multiplayer;

import java.util.Arrays;

public class Move
{
    public enum MoveType { 
        PLAYER(0), BALL(1);
        
        public int id;
        
        MoveType( int id )
        {
            this.id = id;
        }
        
        public static MoveType getMoveTypeFromId( int id )
        {
            if ( id == 0 )
            {
                return PLAYER;
            }
            else if ( id == 1 )
            {
                return BALL;
            }
            else
            {
                return PLAYER;
            }
        }
        
    };
    
    private MoveType moveType;
    private int[] from;
    private int[] to;
    
    public Move( MoveType moveType, int[] from, int[] to )
    {
        this.moveType = moveType;
        this.from = from;
        this.to = to;
    }
    
    public MoveType getMoveType()
    {
        return moveType;
    }

    public int[] getFrom()
    {
        return from;
    }

    public int[] getTo()
    {
        return to;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( from );
        result = prime * result + ((moveType == null) ? 0 : moveType.hashCode());
        result = prime * result + Arrays.hashCode( to );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        Move other = (Move) obj;
        if( !Arrays.equals( from, other.from ) )
            return false;
        if( moveType != other.moveType )
            return false;
        if( !Arrays.equals( to, other.to ) )
            return false;
        return true;
    }
    
    
}
