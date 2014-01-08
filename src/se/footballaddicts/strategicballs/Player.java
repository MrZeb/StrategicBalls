package se.footballaddicts.strategicballs;

import org.andengine.entity.sprite.Sprite;

public class Player
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
        RED, BLUE;
    }

    private int      number;
    private Position position;
    private Team     team;
    private Sprite   sprite;

    public Player( int number, Position position, Team team )
    {
        this.number = number;
        this.position = position;
        this.team = team;
        this.sprite = null;
    }

    public Player( int number, Position position, Team team, Sprite sprite )
    {
        this.number = number;
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

    public int getNumber()
    {
        return number;
    }

    public void setNumber( int number )
    {
        this.number = number;
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
