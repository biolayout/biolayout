package ogdf.basic;

import ogdf.energybased.*;

public interface Factory<T>
{
    public T newInstance();

    public class NodeAttributesFactory implements Factory
    {
        public NodeAttributes newInstance()
        {
            return new NodeAttributes();
        }
    }

    public class EdgeAttributesFactory implements Factory
    {
        public EdgeAttributes newInstance()
        {
            return new EdgeAttributes();
        }
    }

    public class IntegerFactory implements Factory
    {
        public Integer newInstance()
        {
            return new Integer(0);
        }
    }

    public class DoubleFactory implements Factory
    {
        public Double newInstance()
        {
            return new Double(0.0);
        }
    }

    public class DPointFactory implements Factory
    {
        public DPoint2 newInstance()
        {
            return new DPoint2();
        }
    }

    public class nodeFactory implements Factory
    {
        public node newInstance()
        {
            return new node(null, -1);
        }
    }

    public class edgeFactory implements Factory
    {
        public edge newInstance()
        {
            return new edge(null, null, -1);
        }
    }

    static public NodeAttributesFactory NODE_ATTRIBUTES = new NodeAttributesFactory();
    static public EdgeAttributesFactory EDGE_ATTRIBUTES = new EdgeAttributesFactory();
    static public IntegerFactory INTEGER = new IntegerFactory();
    static public DoubleFactory DOUBLE = new DoubleFactory();
    static public DPointFactory DPOINT = new DPointFactory();
    static public nodeFactory NODE = new nodeFactory();
    static public edgeFactory EDGE = new edgeFactory();
}
