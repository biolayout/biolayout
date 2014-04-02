package org.BioLayoutExpress3D.Utils;

import static java.lang.Math.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class Point3D
{
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;

    public Point3D()
    {
        this(0.0f, 0.0f, 0.0f);
    }

    public Point3D(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(double x, double y, double z)
    {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }

    public Point3D(Point3D point3D)
    {
        this.x = point3D.x;
        this.y = point3D.y;
        this.z = point3D.z;
    }

    public float getX()
    {
        return x;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public float getY()
    {
        return y;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public float getZ()
    {
        return z;
    }

    public void setZ(float z)
    {
        this.z = z;
    }

    public void setLocation(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public void setLocation(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setLocation(Point3D point)
    {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        x1 -= x2;
        y1 -= y2;
        z1 -= z2;

        return (float)sqrt(x1 * x1 + y1 * y1 + z1 * z1);
    }

    @Override
    public String toString()
    {
        return "( " + x + ", " + y + ", " + z + " )";
    }


}