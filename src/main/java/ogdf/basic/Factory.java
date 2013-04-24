package ogdf.basic;

import ogdf.energybased.*;

public interface Factory<T>
{
    public T newInstance();

    public class NodeAttributesFactory implements Factory
    {
        @Override
        public NodeAttributes newInstance()
        {
            return new NodeAttributes();
        }
    }

    public class EdgeAttributesFactory implements Factory
    {
        @Override
        public EdgeAttributes newInstance()
        {
            return new EdgeAttributes();
        }
    }

    public class IntegerFactory implements Factory
    {
        @Override
        public Integer newInstance()
        {
            return new Integer(0);
        }
    }

    public class DoubleFactory implements Factory
    {
        @Override
        public Double newInstance()
        {
            return new Double(0.0);
        }
    }

    public class DPointFactory implements Factory
    {
        @Override
        public DPoint newInstance()
        {
            // Nested factory singletons. Hmmmmm.
            return ogdf.basic.PointFactory.INSTANCE.newDPoint();
        }
    }

    public class nodeFactory implements Factory
    {
        @Override
        public node newInstance()
        {
            return new node(null, -1);
        }
    }

    public class edgeFactory implements Factory
    {
        @Override
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
