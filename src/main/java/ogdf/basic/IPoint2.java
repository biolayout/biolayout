package ogdf.basic;

public class IPoint2
{
    public int m_x; //!< The x-coordinate.
    public int m_y; //!< The y-coordinate.

    //! Creates a real point (0,0).
    public IPoint2()
    {
        this(0, 0);
    }

    //! Creates a real point (\a x,\a y).
    public IPoint2(int x, int y)
    {
        m_x = x;
        m_y = y;
    }

    //! Copy constructor.
    public IPoint2(IPoint2 p)
    {
        this(p.m_x, p.m_y);
    }

    //! Relaxed equality operator.
    public boolean equals(IPoint2 p)
    {
        return m_x == p.m_x && m_y == p.m_y;
    }

    //! Returns the norm of the point.
    public double norm()
    {
        return java.lang.Math.sqrt(m_x * m_x + m_y * m_y);
    }

    // gives the euclidean distance between p and *this
    public double distance(IPoint2 p)
    {
        double dx = p.m_x - m_x;
        double dy = p.m_y - m_y;
        return java.lang.Math.sqrt((dx * dx) + (dy * dy));
    }

    // adds p to *this
    public IPoint2 plus(IPoint2 p)
    {
        return new IPoint2(m_x + p.m_x, m_y + p.m_y);
    }

    // subtracts p from *this
    public IPoint2 minus(IPoint2 p)
    {
        return new IPoint2(m_x - p.m_x, m_y - p.m_y);
    }
}