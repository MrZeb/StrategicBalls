package se.footballaddicts.strategicballs;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.DrawType;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.graphics.Point;

public class Cell extends Rectangle
{
    private Point coordinates;

    public Cell( float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager, DrawType pDrawType )
    {
        super( pX, pY, pWidth, pHeight, pVertexBufferObjectManager, pDrawType );
    }

    public Cell( int x, int y, int i, int j, VertexBufferObjectManager vertexBufferObjectManager )
    {
        super( x, y, i, j, vertexBufferObjectManager );
    }

    public Point getCoordinates()
    {
        return coordinates;
    }

    public void setCoordinates( Point coordinates )
    {
        this.coordinates = coordinates;
    }

}
