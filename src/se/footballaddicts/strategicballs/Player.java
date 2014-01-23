package se.footballaddicts.strategicballs;

import org.andengine.entity.sprite.Sprite;

import android.graphics.Point;

public class Player extends BallsEntity
{
    enum Position
    {
        GOALKEEPER( 1 ), DEFENDER( 1 ), ATTACKER( 3 );

        int moves;

        Position( int moves )
        {
            this.moves = moves;
        }

        public static Position getPositionForIndex( int index )
        {
            if( index == 0 )
            {
                return GOALKEEPER;
            }
            else if( index == 1 || index == 2 )
            {
                return DEFENDER;
            }
            else
            {
                return ATTACKER;
            }
        }
    }

    enum Team
    {
        A, B;
    }

    private Team     team;
    private Position position;

    public Player( Point coordinates, Position position, Team team )
    {
        super( coordinates );

        this.team = team;
    }

    public Player( Point coordinates, Position position, Team team, Sprite sprite )
    {
        super( coordinates, sprite );

        this.team = team;
    }

    public Team getTeam()
    {
        return team;
    }

    public void setTeam( Team team )
    {
        this.team = team;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition( Position position )
    {
        this.position = position;
    }
}
