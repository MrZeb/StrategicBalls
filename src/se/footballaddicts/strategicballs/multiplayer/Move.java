package se.footballaddicts.strategicballs.multiplayer;

public class Move
{
    public enum MoveType { PLAYER, BALL };
    
    private MoveType moveType;
    private int[][] from;
    private int[][] to;
    
    public Move( MoveType moveType, int[][] from, int[][] to )
    {
        this.moveType = moveType;
        this.from = from;
        this.to = to;
    }
    
    public MoveType getMoveType()
    {
        return moveType;
    }

    public int[][] getFrom()
    {
        return from;
    }

    public int[][] getTo()
    {
        return to;
    }
    
    
}
