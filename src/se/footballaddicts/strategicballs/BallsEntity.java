package se.footballaddicts.strategicballs;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;

import android.graphics.Point;

public class BallsEntity extends Entity
{
    private Sprite sprite;
    private Point  currentCoordinates;
    private Point  roundStartCoordinates;

    public BallsEntity( Point coordinates )
    {
        this.setRoundStartCoordinates( coordinates );
        this.sprite = null;
    }

    public BallsEntity( Point coordinates, Sprite sprite )
    {
        this.setRoundStartCoordinates( coordinates );
        this.sprite = sprite;
    }

    public Point getRoundStartCoordinates()
    {
        return roundStartCoordinates;
    }

    public void setRoundStartCoordinates( Point roundStartCoordinates )
    {
        this.roundStartCoordinates = roundStartCoordinates;
    }

    public Point getCurrentCoordinates()
    {
        return currentCoordinates;
    }

    public void setCurrentCoordinates( Point currentCoordinates )
    {
        this.currentCoordinates = currentCoordinates;
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
