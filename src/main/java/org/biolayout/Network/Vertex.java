package org.biolayout.Network;

import java.awt.*;
import java.util.*;
import org.biolayout.Utils.*;
import static org.biolayout.Network.NetworkContainer.*;
import static org.biolayout.StaticLibraries.Random.*;
import static org.biolayout.Environment.GlobalEnvironment.Shapes2D.*;
import static org.biolayout.Environment.GlobalEnvironment.Shapes3D.*;
import static org.biolayout.Environment.GlobalEnvironment.*;

/**
*  org.biolayout.Network.Vertex
*
*  Created by CGG EBI on Wed Aug 07 2002.
*
* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011-2012
* @version 3.0.0.0
*
*/

public final class Vertex implements Comparable<Vertex>
{

    private HashMap<Vertex, Edge> edgeConnections = null;
    private Point3D point = null;
    private int vertexID = 0;

    private NetworkContainer nc = null;
    private String vertexName = "";
    private String description = "";
    private String originalDescription = "";

    private float vertexSize = 0.0f;
    private Color vertexColor = null;
    private Shapes2D vertex2DShape = CIRCLE;
    private Shapes3D vertex3DShape = SPHERE;
    private float vertexTransparencyAlpha = 1.0f;
    private String vertexURLString = "";
    private Edge selfEdge = null;
    private byte packedBooleanFlags = 0; // packed boolean flags so as to not use 1 byte per boolean for the vertex

    public Vertex(String vertexName, NetworkContainer nc)
    {
        this.vertexName = vertexName;
        this.nc = nc;

        point = RANDOM_INITIAL_LAYOUT_COORDS ? new Point3D( CANVAS_X_SIZE * nextFloat(), CANVAS_Y_SIZE * nextFloat(), CANVAS_Z_SIZE * nextFloat() )
                                             : new Point3D(vertexID % CANVAS_X_SIZE, (vertexID + 100) % CANVAS_Y_SIZE, (vertexID + 200) % CANVAS_Z_SIZE);
        description = "";
        originalDescription = "";
        vertexSize = DEFAULT_NODE_SIZE.get();
        vertexColor = DEFAULT_NODE_COLOR;
        vertexID = nc.getFRLayout().getAndIncrementCurrentVertexCount();

        nc.getLayoutClassSetsManager().getCurrentClassSetAllClasses().setClass(this, 0);

        edgeConnections = new HashMap<Vertex, Edge>(0); // so as to minimize memory usage as default load capacity is 16
    }

    public void addConnection(Vertex vertex, Edge edge)
    {
        if (!this.equals(vertex))
        {
            edgeConnections.put(vertex, edge);
        }
        else
        {
            selfEdge = edge;
        }
    }
    
    public void removeConnection(Edge edge){
        if (edge.getFirstVertex() == this){
            edgeConnections.remove(edge.getSecondVertex());
        } else {
            edgeConnections.remove(edge.getFirstVertex());
        }
    }

    public String getVertexName()
    {
        return vertexName;
    }

    public Point3D getVertexPoint()
    {
        return point;
    }

    public void setVertexPoint(Point3D point)
    {
        this.point = point;
    }

    public int getVertexID()
    {
        return vertexID;
    }

    public Shapes2D getVertex2DShape()
    {
        return vertex2DShape;
    }

    public Shapes3D getVertex3DShape()
    {
        return vertex3DShape;
    }

    public float getVertexTransparencyAlpha()
    {
        return vertexTransparencyAlpha;
    }

    public String getVertexURLString()
    {
        return vertexURLString;
    }

    public void setVertexID(int vertexID)
    {
        this.vertexID = vertexID;
    }

    public void setVertex2DShape(Shapes2D vertex2DShape)
    {
        this.vertex2DShape = vertex2DShape;
    }

    public void setVertex3DShape(Shapes3D vertex3DShape)
    {
        this.vertex3DShape = vertex3DShape;
    }

    public void setVertexTransparencyAlpha(float vertexTransparencyAlpha)
    {
        this.vertexTransparencyAlpha = vertexTransparencyAlpha;
    }

    public void setVertexURLString(String vertexURLString)
    {
        this.vertexURLString = vertexURLString;
    }

    public void setVertexLocation(float locationX, float locationY)
    {
        point.setLocation(locationX, locationY);
    }

    public void setVertexLocation(float locationX, float locationY, float locationZ)
    {
        point.setLocation(locationX, locationY, locationZ);
    }

    public float getX()
    {
        return point.getX();
    }

    public float getY()
    {
        return point.getY();
    }

    public float getZ()
    {
        return point.getZ();
    }

    public void scaleLocation(float scaleFactor)
    {
        point.setLocation( scaleFactor * point.getX(), scaleFactor * point.getY() );
    }

    public void setVertexName(String vertexName)
    {
        this.vertexName = vertexName;
    }

    public Edge getSelfEdge()
    {
        return selfEdge;
    }

    public HashMap<Vertex, Edge> getEdgeConnectionsMap()
    {
        return edgeConnections;
    }

    public void setComponentDisplacement(float ratio, float width, float height)
    {
        point.setLocation( (point.getX() + width) / ratio, (point.getY() + height) / ratio );
    }

    public void setComponentDisplacement(float ratio, float width, float height, float zValue)
    {
        point.setLocation( (point.getX() + width) / ratio, (point.getY() + height) / ratio, (zValue / ratio) + 500 );
    }

    public void setZvalue(float zValue)
    {
        point.z = zValue;
    }

    public float getVertexSize()
    {
        return vertexSize;
    }

    public void setVertexSize(float vertexSize)
    {
        this.vertexSize = vertexSize;
    }

    public void setVertexColor(Color vertexColor)
    {
        this.vertexColor = vertexColor;
        packedBooleanFlags |= 1; //(1 << 0); // set to true
    }

    public Color getVertexColor()
    {
        return vertexColor;
    }

    public VertexClass getVertexClass()
    {
        return nc.getLayoutClassSetsManager().getCurrentClassSetAllClasses().getVertexClass(this);
    }

    public void setVertexClass(VertexClass vertexClass)
    {
        nc.getLayoutClassSetsManager().getCurrentClassSetAllClasses().setClass(this, vertexClass);
    }

    public String getRawDescription()
    {
        return originalDescription;
    }

    public String getDescription()
    {
        if ( description.isEmpty() )
            return description;

        String formattedDescription = "";
        String[] data = description.split(" ");
        for (int i = 0; i < data.length; i++ )
        {
            if ( (i != 0) && (i % 4 == 0) )
                formattedDescription += "<br>";

            formattedDescription += " " + data[i];
        }

        return formattedDescription;
    }

    public void setDescription(String description)
    {
        description += "--" + description + "<br>";
        originalDescription += "--" + description;
    }

    public void removeColorOverride()
    {
        if ( getOverrideClassColor() ) // skip if true already to avoid XOR set errors
            packedBooleanFlags ^= 1; //(1 << 0); // set to false
    }

    public boolean getOverrideClassColor()
    {
        return (packedBooleanFlags & 1) == 1; //( (packedBooleanFlags >> 0) & 1 ) == 1;
    }

    public void setPseudoVertex()
    {
        packedBooleanFlags |= (1 << 1); // set to true
        vertex2DShape = RECTANGLE;
        vertex3DShape = CUBE;
    }

    public boolean isPseudoVertex()
    {
        return ( (packedBooleanFlags >> 1) & 1 ) == 1;
    }

    public void setShowVertexName(boolean showVertexName)
    {
        if (showVertexName)
            packedBooleanFlags |= (1 << 2); // set to true
        else
        {
            if ( isShowVertexName() ) // skip if true already to avoid XOR set errors
                packedBooleanFlags ^= (1 << 2);
        }
    }

    public boolean isShowVertexName()
    {
        return ( (packedBooleanFlags >> 2) & 1 ) == 1;
    }

    public void setmEPNComponent()
    {
        packedBooleanFlags |= (1 << 3); // set to true
    }

    public boolean ismEPNComponent()
    {
        return ( (packedBooleanFlags >> 3) & 1 ) == 1;
    }

    public void setmEPNTransition()
    {
        packedBooleanFlags |= (1 << 4); // set to true
    }

    public boolean ismEPNTransition()
    {
        return ( (packedBooleanFlags >> 4) & 1 ) == 1;
    }

    @Override
    public int compareTo(Vertex obj)
    {
        return (obj.vertexID < this.vertexID) ? -1 : ( (obj.vertexID == this.vertexID) ? 0 : 1);
    }


}