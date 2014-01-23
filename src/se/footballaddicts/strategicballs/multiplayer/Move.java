package se.footballaddicts.strategicballs.multiplayer;

import se.footballaddicts.strategicballs.Player.PlayerType;
import se.footballaddicts.strategicballs.Player.TeamType;
import android.graphics.Point;

public class Move
{
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((moveType == null) ? 0 : moveType.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
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
        if( from == null )
        {
            if( other.from != null )
                return false;
        }
        else if( !from.equals( other.from ) )
            return false;
        if( moveType != other.moveType )
            return false;
        if( to == null )
        {
            if( other.to != null )
                return false;
        }
        else if( !to.equals( other.to ) )
            return false;
        return true;
    }

    public enum MoveType
    {
        PLAYER( 0 ), BALL( 1 );

        public int id;

        MoveType( int id )
        {
            this.id = id;
        }

        public static MoveType getMoveTypeFromId( int id )
        {
            if( id == 0 )
            {
                return PLAYER;
            }
            else if( id == 1 )
            {
                return BALL;
            }
            else
            {
                return PLAYER;
            }
        }

    };

    private MoveType   moveType;
    private TeamType       team;
    private PlayerType playerType;
    private Point      from;
    private Point      to;

    public Move( MoveType moveType, TeamType team, PlayerType playerType, Point from, Point to )
    {
        this.moveType = moveType;
        this.team = team;
        this.setPlayerType( playerType );
        this.from = from;
        this.to = to;
    }

    public MoveType getType()
    {
        return moveType;
    }

    public Point getFrom()
    {
        return from;
    }

    public void setFrom( Point from )
    {
        this.from = from;
    }

    public Point getTo()
    {
        return to;
    }

    public void setTo( Point to )
    {
        this.to = to;
    }

    public PlayerType getPlayerType()
    {
        return playerType;
    }

    public void setPlayerType( PlayerType playerType )
    {
        this.playerType = playerType;
    }

    public TeamType getTeam()
    {
        return team;
    }

    public void setTeam( TeamType team )
    {
        this.team = team;
    }
}