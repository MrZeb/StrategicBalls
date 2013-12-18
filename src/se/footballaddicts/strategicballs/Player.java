package se.footballaddicts.strategicballs;

import org.andengine.entity.sprite.Sprite;

public class Player
{
    enum Position
    {
        GOALKEEPER, DEFENDER, ATTACKER;
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
