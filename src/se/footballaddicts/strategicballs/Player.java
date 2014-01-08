package se.footballaddicts.strategicballs;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;

import android.graphics.Point;

public class Player extends Entity
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

    private Point    coordinates;
    private Position position;
    private Team     team;
    private Sprite   sprite;

    public Player( Point coordinates, Position position, Team team )
    {
        this.coordinates = coordinates;
        this.position = position;
        this.team = team;
        this.sprite = null;
    }

    public Player( Point coordinates, Position position, Team team, Sprite sprite )
    {
        this.coordinates = coordinates;
        this.position = position;
        this.team = team;
        this.sprite = sprite;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition( Position position )
    {
        this.position = position;
    }

    public Point getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates( Point coordinates )
    {
        this.coordinates = coordinates;
    }

    public Team getTeam()
    {
        return team;
    }

    public void setTeam( Team team )
    {
        this.team = team;
    }

    public Sprite getSprite()
    {
        return sprite;
    }

    public void setSprite( Sprite sprite )
    {
        this.sprite = sprite;
    }
}
