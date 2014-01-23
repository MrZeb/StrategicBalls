package se.footballaddicts.strategicballs;

import org.andengine.entity.sprite.Sprite;

import android.graphics.Point;

public class Player extends BallsEntity
{
    enum PlayerType
    {
        GOALKEEPER( 1 ), DEFENDER( 1 ), ATTACKER( 3 );

        int moves;

        PlayerType( int moves )
        {
            this.moves = moves;
        }

        public static PlayerType getPositionForIndex( int index )
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

        public Point getLogicalCoordinates( Team team, int pitchLength, int index )
        {
            /* index is internal index for each position */

            int xBase = team == Team.A ? 0 : pitchLength;
            int factor = team == Team.A ? 1 : -1;

            switch( this )
            {
                case ATTACKER:
                    if( index == 3 )
                    {
                        return new Point( 5 * factor + xBase, 2 );
                    }
                    else if( index == 4 )
                    {
                        return new Point( 5 * factor + xBase, 5 );
                    }
                    else
                    {
                        return new Point( 5 * factor + xBase, 8 );
                    }

                case DEFENDER:
                    if( index == 1 )
                    {
                        return new Point( 3 * factor + xBase, 3 );
                    }
                    else if( index == 2 )
                    {
                        return new Point( 3 * factor + xBase, 7 );
                    }

                case GOALKEEPER:

                    if( index == 0 )
                    {
                        return new Point( xBase, 5 );
                    }
                default:
                    return null;
            }
        }
    }

    enum Team
    {
        A, B;
    }

    private Team     team;
    private PlayerType position;

    public Player( Point coordinates, PlayerType position, Team team )
    {
        super( coordinates );

        this.team = team;
    }

    public Player( Point coordinates, PlayerType position, Team team, Sprite sprite )
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

    public PlayerType getPosition()
    {
        return position;
    }

    public void setPosition( PlayerType position )
    {
        this.position = position;
    }
}
