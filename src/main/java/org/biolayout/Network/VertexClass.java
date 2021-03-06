package org.biolayout.Network;

import java.awt.*;
import java.util.ArrayList;
import org.biolayout.CoreUI.*;
import org.biolayout.StaticLibraries.*;

/**
*
* User: cggebi
* Date: Aug 30, 2002
*
* @author Full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class VertexClass implements Comparable<VertexClass>
{
    public static final float COLOR_UPPER_INTENSITY_PERCENTAGE = 0.90f;
    public static final int LOWER_THRESHOLD = 0;
    public static final int UPPER_THRESHOLD = (int)(COLOR_UPPER_INTENSITY_PERCENTAGE * 255);

    private int classID = 0;
    private String className = "";
    private Color classColor = null;

    public VertexClass(int classID, String className, LayoutClasses layoutClasses)
    {
        this.classID = classID;
        this.className = className;

        assignClassColor(layoutClasses);
    }

    private void assignClassColor(LayoutClasses layoutClasses)
    {
        classColor = layoutClasses.getVertexClassColorAssigner().assign(className);
    }

    public static Color createRandomColor()
    {
        return new Color( Random.getRandomRange(LOWER_THRESHOLD, UPPER_THRESHOLD), Random.getRandomRange(LOWER_THRESHOLD, UPPER_THRESHOLD), Random.getRandomRange(LOWER_THRESHOLD, UPPER_THRESHOLD) );
    }

    public void setName(String className)
    {
        this.className = className;
    }

    public String getName()
    {
        return className;
    }

    public void setColor(Color classColor)
    {
        this.classColor = classColor;
    }

    public Color getColor()
    {
        return classColor;
    }

    public int getClassID()
    {
        return classID;
    }

    public static int compare(String vertexClassName1, String vertexClassName2)
    {
        return vertexClassName1.compareTo(vertexClassName2);
    }

    @Override
    public String toString()
    {
        return className;
    }

    @Override
    public int compareTo(VertexClass vertexClass)
    {
        return compare( className, vertexClass.getName() );
    }


}