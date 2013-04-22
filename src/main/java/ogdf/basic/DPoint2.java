package ogdf.basic;

public class DPoint2
{
    public double m_x; //!< The x-coordinate.
    public double m_y; //!< The y-coordinate.

    //! Creates a real point (0,0).
    public DPoint2()
    {
        this(0.0, 0.0);
    }

    //! Creates a real point (\a x,\a y).
    public DPoint2(double x, double y)
    {
        m_x = x;
        m_y = y;
    }

    //! Copy constructor.
    public DPoint2(DPoint2 dp)
    {
        this(dp.m_x, dp.m_y);
    }

    //! Relaxed equality operator.
    public boolean equals(DPoint2 dp)
    {
        return DIsEqual(m_x, dp.m_x) && DIsEqual(m_y, dp.m_y);
    }

    //! Returns the norm of the point.
    public double norm()
    {
        return java.lang.Math.sqrt(m_x * m_x + m_y * m_y);
    }

    // gives the euclidean distance between p and *this
    public double distance(DPoint2 p)
    {
        double dx = p.m_x - m_x;
        double dy = p.m_y - m_y;
        return java.lang.Math.sqrt((dx * dx) + (dy * dy));
    }

    // adds p to *this
    public DPoint2 plus(DPoint2 p)
    {
        return new DPoint2(m_x + p.m_x, m_y + p.m_y);
    }

    // subtracts p from *this
    public DPoint2 minus(DPoint2 p)
    {
        return new DPoint2(m_x - p.m_x, m_y - p.m_y);
    }

    final static double OGDF_GEOM_EPS = 1e-06;

    public static boolean DIsEqual(double a, double b)
    {
        return (a < (b + OGDF_GEOM_EPS) && a > (b - OGDF_GEOM_EPS));
    }

    public static boolean DIsGreater(double a, double b)
    {
        return (a > (b + OGDF_GEOM_EPS));
    }

    public static boolean DIsGreaterEqual(double a, double b)
    {
        return (a > (b - OGDF_GEOM_EPS));
    }

    public static boolean DIsLess(double a, double b)
    {
        return (a < (b - OGDF_GEOM_EPS));
    }

    public static boolean DIsLessEqual(double a, double b)
    {
        return (a < (b + OGDF_GEOM_EPS));
    }
}