package se.footballaddicts.strategicballs;

import org.andengine.entity.sprite.Sprite;

import android.graphics.Point;

public class Player extends BallsEntity
{
    enum GeneralPlayerType
    {
        GOALKEEPER, DEFENDER, ATTACKER;
    }

    public enum PlayerType
    {
        GOALKEEPER( 0, 1 ), TOP_DEFENDER( 1, 1 ), BOT_DEFENDER( 2, 1 ), TOP_ATTACKER( 3, 3 ), MID_ATTACKER( 4, 3 ), BOT_ATTACKER( 5, 3 );

        int moves;
        int index;

        PlayerType( int index, int moves )
        {
            this.index = index;
            this.moves = moves;
        }

        public static PlayerType getTypeForIndex( int index )
        {
            switch( index )
            {
                case 0:
                    return GOALKEEPER;
                case 1:
                    return TOP_DEFENDER;
                case 2:
                    return BOT_DEFENDER;
                case 3:
                    return TOP_ATTACKER;
                case 4:
                    return MID_ATTACKER;
                case 5:
                    return BOT_ATTACKER;
                default:
                    return null;
            }
        }

        public Point getLogicalCoordinates( TeamType team, int pitchLength )
        {
            int xBase = team == TeamType.LEFT ? 0 : pitchLength;
            int factor = team == TeamType.LEFT ? 1 : -1;

            switch( this )
            {
                case TOP_ATTACKER:
                    return new Point( 5 * factor + xBase, 2 );

                case MID_ATTACKER:
                    return new Point( 5 * factor + xBase, 5 );

                case BOT_ATTACKER:
                    return new Point( 5 * factor + xBase, 8 );

                case TOP_DEFENDER:
                    return new Point( 3 * factor + xBase, 3 );

                case BOT_DEFENDER:
                    return new Point( 3 * factor + xBase, 7 );

                case GOALKEEPER:
                    return new Point( xBase, 5 );

                default:
                    return null;
            }
        }

        public GeneralPlayerType getGeneralType()
        {
            if( ordinal() == 0 )
            {
                return GeneralPlayerType.GOALKEEPER;
            }
            else if( ordinal() <= 2 )
            {
                return GeneralPlayerType.DEFENDER;
            }
            else
            {
                return GeneralPlayerType.ATTACKER;
            }
        }

        public int getIndex()
        {
            return index;
        }
    }

    public enum TeamType
    {
        LEFT, RIGHT;
        
        public static TeamType fromServer( int ordinal )
        {
            for ( TeamType type : TeamType.values() )
            {
                if ( type.ordinal() == ordinal )
                {
                    return type;
                }
            }
            return null;
        }
    }

    private TeamType       team;
    private PlayerType type;

    public Player( Point coordinates, PlayerType type, TeamType team )
    {
        super( coordinates );

        this.type = type;
        this.team = team;
    }

    public Player( Point coordinates, PlayerType type, TeamType team, Sprite sprite )
    {
        super( coordinates, sprite );

        this.type = type;
        this.team = team;
    }

    public TeamType getTeam()
    {
        return team;
    }

    public void setTeam( TeamType team )
    {
        this.team = team;
    }

    public PlayerType getType()
    {
        return type;
    }

    public void setType( PlayerType type )
    {
        this.type = type;
    }
}
