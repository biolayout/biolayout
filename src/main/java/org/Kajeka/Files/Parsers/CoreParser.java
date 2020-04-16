package org.Kajeka.Files.Parsers;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.Kajeka.Analysis.*;
import org.Kajeka.CoreUI.*;
import org.Kajeka.CoreUI.Dialogs.*;
import org.Kajeka.DataStructures.*;
import org.Kajeka.Network.*;
import org.Kajeka.Utils.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.Correlation.CorrelationEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;

/**
 *
 * org.Kajeka.File.CoreParser
 *
 * @author Full refactoring by Thanos Theo, 2008-2009-2010-2011-2012
 * @version 3.0.0.0
 *
 */
public class CoreParser
{

    protected NetworkContainer nc = null;
    protected LayoutFrame layoutFrame = null;
    protected File file = null;
    protected BufferedReader fileReaderBuffered = null;
    protected BufferedReader fileReaderCounter = null;
    protected String line = "";
    protected ArrayList<String> tokens = null;
    protected int numberOfTokens = 0;
    protected int currentTokenIndex = 0;
    protected boolean iscorrelationData = false;
    protected boolean isSif = false;
    protected boolean isSuccessful = false;

    /**
     * String variable to store the simple file name.
     */
    protected String simpleFileName = "";

    /**
     * GraphmlNetworkContainer reference (reference for the grapml network
     * container).
     */
    private GraphmlNetworkContainer gnc = null;

    /**
     * Variable only used for graphml files.
     */
    private HashMap<String, Tuple6<float[], String[], String[], String[], String[], String>> allGraphmlNodesMap = null;

    /**
     * Variable only used for graphml files.
     */
    private HashMap<String, Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]>> allGraphmlEdgesMap = null;

    /**
     * Variable only used for graphml files.
     */
    private ArrayList<GraphmlComponentContainer> alGraphmllPathwayComponentContainersFor3D = null;

    private ArrayList<Integer> nodeIdColumns;
    private int edgeWeightColumn;
    private int edgeTypeColumn;
    private float filterWeight;

    /**
     * The constructor of the CoreParser class.
     */
    public CoreParser(NetworkContainer nc, LayoutFrame layoutFrame,
            java.util.List<Integer> nodeIdColumns, int edgeWeightColumn,
            int edgeTypeColumn, float filterWeight)
    {
        this.nc = nc;
        this.layoutFrame = layoutFrame;
        this.nodeIdColumns = new ArrayList<Integer>(nodeIdColumns);
        this.edgeWeightColumn = edgeWeightColumn;
        this.edgeTypeColumn = edgeTypeColumn;
        this.filterWeight = filterWeight;
    }

    public CoreParser(NetworkContainer nc, LayoutFrame layoutFrame)
    {
        this(nc, layoutFrame, Arrays.asList(0, 1), 2, 3, 0.0f);
    }

    public boolean init(File file, String fileExtension)
    {
        this.file = file;

        try
        {
            fileReaderCounter = new BufferedReader(new FileReader(file));
            fileReaderBuffered = new BufferedReader(new FileReader(file));

            isSif = fileExtension.equals(SupportedInputFileTypes.SIF.toString());
            simpleFileName = file.getName();

            return true;
        }
        catch (Exception exc)
        {
            try
            {
                fileReaderCounter.close();
                fileReaderBuffered.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD)
                {
                    println("IOException while closing streamers in CoreParser.init():\n" + ioe.getMessage());
                }
            }
            finally
            {

            }

            return false;
        }
    }

    private Pattern quotedStringRegex = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    protected void tokenize(String line)
    {
        tokens = new ArrayList<String>();
        Matcher m = quotedStringRegex.matcher(line);
        while (m.find())
        {
            if (m.group(1) != null)
            {
                tokens.add(m.group(1));
            }
            else
            {
                tokens.add(m.group(2));
            }
        }

        currentTokenIndex = 0;
        numberOfTokens = tokens.size();
    }

    public boolean parse()
    {
        int totalLines = 0;
        int counter = 0;

        isSuccessful = false;
        nc.setOptimized(false);
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        try
        {
            while ((line = fileReaderCounter.readLine()) != null)
            {
                totalLines++;
            }

            layoutProgressBarDialog.prepareProgressBar(totalLines, "Parsing " + simpleFileName + " Graph...");
            layoutProgressBarDialog.startProgressBar();

            while ((line = fileReaderBuffered.readLine()) != null)
            {
                layoutProgressBarDialog.incrementProgress(++counter);

                tokenize(line);
                if (line.length() > 0)
                {
                    if (line.startsWith("//"))
                    {
                        updateVertexProperties();
                    }
                    else
                    {
                        createVertices(counter);
                    }
                }
            }

            if (nc.getIsGraphml())
            {
                gnc.initAllGraphmlNodesMap(allGraphmlNodesMap, allGraphmlEdgesMap, alGraphmllPathwayComponentContainersFor3D);
            }

            if (!iscorrelationData)
            {
                AnnotationTypeManagerBG.getInstanceSingleton().setChipGeneCount(nc.getVerticesMap().size());

                if (DEBUG_BUILD)
                {
                    println("Got a total of:" + AnnotationTypeManagerBG.getInstanceSingleton().getChipGeneCount());
                }
            }

            isSuccessful = true;
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD)
            {
                println("IOException in CoreParser.parse():\n" + ioe.getMessage());
            }
        }
        finally
        {
            try
            {
                fileReaderCounter.close();
                fileReaderBuffered.close();
            }
            catch (IOException ioe)
            {
                if (DEBUG_BUILD)
                {
                    println("IOException while closing streams in CoreParser.parse():\n" + ioe.getMessage());
                }
            }
            finally
            {
                layoutProgressBarDialog.endProgressBar();
            }
        }

        return isSuccessful;
    }

    private void updateVertexProperties()
    {
        String property = getNext();
        String vertex = "";
        String field1 = "", field2 = "", field3 = "", field4 = "", field5 = "", field6 = "", field7 = "";

        if (property.startsWith("//CORRELATION_DATA") || property.startsWith("//EXPRESSION_DATA"))
        {
            field1 = getNext();
            field2 = getNext();
            field3 = getNext();

            if (DEBUG_BUILD)
            {
                println("Correlation data file used was:" + field1);
            }

            String correlationFileName = field1.substring(field1.lastIndexOf(System.getProperty("file.separator")) + 1, field1.length());
            String correlationFilePath = field1.substring(0, field1.lastIndexOf(System.getProperty("file.separator")) + 1);

            CORRELATION_FILE = correlationFileName;
            CORRELATION_FILE_PATH = correlationFilePath;
            CORRELATION_DATA_FIRST_COLUMN = Integer.parseInt(field2);

            iscorrelationData = true;
            if (property.equals("//EXPRESSION_DATA"))
            {
                // Older files don't have this data, so fill with likely values
                CORRELATION_DATA_FIRST_ROW = 1;
                CORRELATION_DATA_TRANSPOSE = false;
                CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field3);
                CURRENT_SCALE_TRANSFORM = ScaleTransformType.NONE;
            }
            else if (property.equals("//EXPRESSION_DATA_V2")) // 3.0 through 3.2
            {
                field4 = getNext();
                field5 = getNext();

                CORRELATION_DATA_FIRST_ROW = Integer.parseInt(field3);
                CORRELATION_DATA_TRANSPOSE = Boolean.parseBoolean(field4);
                CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field5);
                CURRENT_SCALE_TRANSFORM = ScaleTransformType.NONE;
            }
            else if (property.equals("//CORRELATION_DATA_V1") || property.equals("//EXPRESSION_DATA_V3")) // 1.0
            {
                field4 = getNext();
                field5 = getNext();
                field6 = getNext();

                CORRELATION_DATA_FIRST_ROW = Integer.parseInt(field3);
                CORRELATION_DATA_TRANSPOSE = Boolean.parseBoolean(field4);
                CURRENT_CORRELATION_THRESHOLD = Float.parseFloat(field5);
                CURRENT_SCALE_TRANSFORM = Enum.valueOf(ScaleTransformType.class, field6);
            }
            else
            {
                iscorrelationData = false;
            }
        }
        else if (property.equals("//NODECOORD"))
        {
            field1 = getNext();
            field2 = getNext();
            field3 = getNext();
            field4 = getNext();

            if (field4.isEmpty())
            {
                field4 = "0.0";
            }

            if (DEBUG_BUILD)
            {
                println("name: " + field1 + " coord1: " + field2 + " coord2: " + field2);
            }

            nc.updateVertexLocation(field1, Float.parseFloat(field2), Float.parseFloat(field3), Float.parseFloat(field4));

        }
        else if (property.equals("//NODEDESC"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setDescription(field1);
            }
        }
        else if (property.equals("//NODECLASS"))
        {
            vertex = getNext();
            field1 = getNext();
            field2 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                // IF CLASS SET PROVIDED ADD THE VERTEX TO THE CLASS SET
                // ELSE IF NO CLASS SET IS PROVIDED ADD TO THE DEFAULT CLASSES ID 0
                LayoutClasses lc = ((field2.length() > 0) ? nc.getLayoutClassSetsManager().getClassSet(field2) : nc.getLayoutClassSetsManager().getClassSet(0));
                VertexClass vc = lc.createClass(field1);
                lc.setClass(nc.getVerticesMap().get(vertex), vc);

                if (!iscorrelationData)
                {
                    AnnotationTypeManagerBG.getInstanceSingleton().add(vertex, lc.getClassSetName(), vc.getName());
                }
            }
        }
        else if (property.equals("//NODECLASSCOLOR"))
        {
            field1 = getNext();
            field2 = getNext();
            field3 = getNext();

            LayoutClasses lc = null;

            if (field3.length() > 0)
            {
                // IF CLASS SET PROVIDED SET THE COLOR TO THE CLASS SET
                lc = nc.getLayoutClassSetsManager().getClassSet(field2);
                lc.createClass(field1).setColor(Color.decode(field3));
            }
            else
            {
                // IF NO CLASS SET IS PROVIDED SET THE COLOR TO THE DEFAULT CLASSES ID 0
                lc = nc.getLayoutClassSetsManager().getClassSet(0);
                lc.createClass(field1).setColor(Color.decode(field2));
            }
        }
        else if (property.equals("//NODESIZE"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setVertexSize(Float.parseFloat(field1));
            }
        }
        else if (property.equals("//NODECOLOR"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setVertexColor(Color.decode(field1));
            }
        }
        else if (property.equals("//NODESHAPE"))
        {
            vertex = getNext();
            field1 = getNext();
            field2 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setVertex2DShape(get2DShapeForString(field1));
                nc.getVerticesMap().get(vertex).setVertex3DShape(get3DShapeForString(field2));
            }
        }
        else if (property.equals("//NODEALPHA"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setVertexTransparencyAlpha(Float.parseFloat(field1));
            }
        }
        else if (property.equals("//NODEURL"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                nc.getVerticesMap().get(vertex).setVertexURLString(field1);
            }
        }
        else if (property.equals("//NODETYPE"))
        {
            vertex = getNext();
            field1 = getNext();

            if (nc.getVerticesMap().containsKey(vertex))
            {
                if (field1.equals("IS_MEPN_COMPONENT"))
                {
                    nc.getVerticesMap().get(vertex).setmEPNComponent();
                }
                else if (field1.equals("IS_MEPN_TRANSITION"))
                {
                    nc.getVerticesMap().get(vertex).setmEPNTransition();
                }
            }
        }
        else if (property.equals("//CURRENTCLASSSET"))
        {
            nc.getLayoutClassSetsManager().switchClassSet(getNext());
        }
        else if (property.equals("//EDGESIZE"))
        {
            DEFAULT_EDGE_SIZE.set(Float.parseFloat(getNext()));
        }
        else if (property.equals("//EDGECOLOR"))
        {
            DEFAULT_EDGE_COLOR.set(Color.decode(getNext()));
        }
        else if (property.equals("//EDGEARROWHEADSIZE"))
        {
            ARROW_HEAD_SIZE.set(Integer.parseInt(getNext()));
        }
        else if (property.equals("//DEFAULTSEARCH"))
        {
            if (DEBUG_BUILD)
            {
                println("Default Search found.");
            }

            field1 = getNext();
            boolean preset = false;
            for (int i = 0; i < PRESET_SEARCH_URL.length; i++)
            {
                if (field1.equals(PRESET_SEARCH_URL[i].getName()))
                {
                    if (DEBUG_BUILD)
                    {
                        println("Is a Preset Search.");
                    }

                    SEARCH_URL = PRESET_SEARCH_URL[i];

                    if (DEBUG_BUILD)
                    {
                        println(SEARCH_URL.getUrl());
                    }

                    preset = true;

                    break;
                }
            }

            if (!preset)
            {
                if (DEBUG_BUILD)
                {
                    println("Is a Custom Search.");
                }

                SearchURL customSearchURL = new SearchURL(field1);
                SEARCH_URL = customSearchURL;

                if (DEBUG_BUILD)
                {
                    println(SEARCH_URL.getUrl());
                }

                CUSTOM_SEARCH = true;
            }
        }
        else if (property.equals("//HAS_GRAPHML_NODE_DATA"))
        {
            // enable graphml-style parsing

            field1 = getNext();
            field2 = getNext();
            field3 = getNext();

            float rangeX = Float.parseFloat(field1);
            float rangeY = Float.parseFloat(field2);

            nc.initGraphmlNetworkContainer();
            gnc = nc.getGraphmlNetworkContainer();
            gnc.setIsGraphml(true);
            gnc.setRangeX(rangeX);
            gnc.setRangeY(rangeY);
            gnc.setIsPetriNet(field3.equals("IS_SPN_MEPN_GRAPHML_GRAPH_TYPE"));

            allGraphmlNodesMap = new HashMap<String, Tuple6<float[], String[], String[], String[], String[], String>>();
            allGraphmlEdgesMap = new HashMap<String, Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]>>();
            alGraphmllPathwayComponentContainersFor3D = new ArrayList<GraphmlComponentContainer>();
        }
        else if (property.equals("//GRAPHML_NODE_DATA"))
        {
            // parse node key-to-name hashmap

            field1 = getNext();
            field2 = getNext();

            float graphmlCoordX = Float.parseFloat(getNext());
            float graphmlCoordY = Float.parseFloat(getNext());

            String nextString = getNext();
            // older graphml layout files may not have the Z coord axis, so this check is required before parsing
            float graphmlCoordZ = (!nextString.isEmpty()) ? Float.parseFloat(nextString) : 0.0f;

            Tuple6<float[], String[], String[], String[], String[], String> nodeTuple6 = Tuples.tuple(new float[]
            {
                0.0f, 0.0f, graphmlCoordX, graphmlCoordY, graphmlCoordZ
            },
                    new String[]
                    {
                        "", "", "", ""
                    },
                    new String[]
                    {
                        "", "", "", ""
                    },
                    new String[]
                    {
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
                    },
                    new String[]
                    {
                        field2
                    },
                    "");
            allGraphmlNodesMap.put(field1, nodeTuple6);
        }
        else if (property.equals("//GRAPHML_EDGE_DATA"))
        {
            // parse edge key-to-name hashmap

            field1 = getNext();
            field2 = getNext();

            float[] pathValues = new float[]
            {
                0.0f, 0.0f, 0.0f, 0.0f
            };
            ArrayList<Point2D.Float> allPointValues = new ArrayList<Point2D.Float>();
            while (!(field3 = getNext()).isEmpty())
            {
                field4 = getNext();
                allPointValues.add(new Point2D.Float(Float.parseFloat(field3), Float.parseFloat(field4)));
            }
            Tuple2<float[], ArrayList<Point2D.Float>> allPathValues = Tuples.tuple(pathValues, allPointValues);
            Tuple6<String, Tuple2<float[], ArrayList<Point2D.Float>>, String[], String[], String[], String[]> edgeTuple6 = Tuples.tuple(field2,
                    allPathValues,
                    new String[]
                    {
                        "", "", ""
                    },
                    new String[]
                    {
                        "", ""
                    },
                    new String[]
                    {
                        "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""
                    },
                    new String[]
                    {
                        "", "", "", ""
                    });
            allGraphmlEdgesMap.put(field1, edgeTuple6);
        }
        else if (property.equals("//GRAPHML_COMPONENT_CONTAINER_DATA"))
        {
            vertex = getNext();
            field1 = getNext();
            field2 = getNext();
            field3 = getNext();
            field4 = getNext();
            field5 = getNext();
            field6 = getNext();
            field7 = getNext();

            alGraphmllPathwayComponentContainersFor3D.add(new GraphmlComponentContainer("", vertex,
                    Integer.parseInt(field1),
                    Float.parseFloat(field2),
                    new Rectangle2D.Float(Float.parseFloat(field3), Float.parseFloat(field4), Float.parseFloat(field5), Float.parseFloat(field6)),
                    Color.decode(field7)
            )
            );
        }
    }

    private static <T extends Enum<T>> T getEnumValueForString(Class<T> clazz, HashMap<String, T> map, String field)
    {
        T value = map.get(field);
        if (value == null)
        {
            try
            {
                // Textual shape description
                value = Enum.valueOf(clazz, field);
            }
            catch (Exception e)
            {
                // Index shape description
                int index;
                try
                {
                    index = Integer.parseInt(field);
                }
                catch (NumberFormatException nfe)
                {
                    index = 0;
                }

                value = clazz.getEnumConstants()[index];
            }

            map.put(field, value);
        }

        return value;
    }

    HashMap<String, Shapes2D> shapes2DMap = new HashMap<String, Shapes2D>();

    private Shapes2D get2DShapeForString(String field)
    {
        return getEnumValueForString(Shapes2D.class, shapes2DMap, field);
    }

    HashMap<String, Shapes3D> shapes3DMap = new HashMap<String, Shapes3D>();

    private Shapes3D get3DShapeForString(String field)
    {
        return getEnumValueForString(Shapes3D.class, shapes3DMap, field);
    }

    private void createVertices(int lines)
    {
        String vertex1 = "";
        String vertex2 = "";
        String weightString = "";
        String edgeType = "";

        if (!isSif)
        {
            vertex1 = getToken(nodeIdColumns.get(0));
            vertex2 = getToken(nodeIdColumns.get(1));
            weightString = getToken(edgeWeightColumn);
            edgeType = getToken(edgeTypeColumn);
        }
        else
        {
            vertex1 = getNext();
            edgeType = getNext();
            vertex2 = getNext();
            weightString = getNext();
        }

        float weight = Float.NaN;

        if (weightString.length() > 0)
        {
            try
            {
                weight = Float.parseFloat(weightString.replace(',', '.'));
            }
            catch (NumberFormatException nfe)
            {
                if (DEBUG_BUILD)
                {
                    println("NumberFormatException in CoreParser.createVertices():\n" + nfe.getMessage());
                }
            }
        }

        // SPN type edge
        if (!edgeType.isEmpty() && edgeType.startsWith("SPN_"))
        {
            String edgeName = "";
            if (edgeType.contains("SPN_EDGE_VALUE:"))
            {
                String[] splitEdgeType = edgeType.split("\\s+");
                edgeName = splitEdgeType[0].substring(splitEdgeType[0].indexOf(":") + 1, splitEdgeType[0].length());
            }

            nc.addNetworkConnection(vertex1, vertex2, edgeName, edgeType.contains("SPN_IS_TOTAL_INHIBITOR_EDGE"), edgeType.contains("SPN_IS_PARTIAL_INHIBITOR_EDGE"), edgeType.contains("SPN_HAS_DUAL_ARROWHEAD"));
        }
        else if (!Float.isNaN(weight))
        {
            if (weight > filterWeight)
            {
                if (!edgeType.isEmpty())
                {
                    nc.addNetworkConnection(vertex1, edgeType + lines, weight / 2.0f);
                    nc.addNetworkConnection(edgeType + lines, vertex2, weight / 2.0f);

                    Vertex vertex = nc.getVerticesMap().get(edgeType + lines);
                    vertex.setVertexSize(vertex.getVertexSize() / 2);
                    vertex.setPseudoVertex();

                    LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);
                    VertexClass vc = lc.createClass(edgeType);
                    lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);
                }
                else
                {
                    nc.addNetworkConnection(vertex1, vertex2, weight);
                }
            }

            WEIGHTED_EDGES = true;
        }
        else if (!edgeType.isEmpty())
        {
            nc.addNetworkConnection(vertex1, edgeType + lines, 0.0f);
            nc.addNetworkConnection(edgeType + lines, vertex2, 0.0f);

            Vertex vertex = nc.getVerticesMap().get(edgeType + lines);
            vertex.setVertexSize(vertex.getVertexSize() / 2);
            vertex.setPseudoVertex();

            LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);
            VertexClass vc = lc.createClass(edgeType);
            lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);
        }
        else
        {
            nc.addNetworkConnection(vertex1, vertex2, 0.0f);
        }
    }

    protected String getNext()
    {
        if (currentTokenIndex >= numberOfTokens)
        {
            return "";
        }

        return tokens.get(currentTokenIndex++);
    }

    protected String getToken(int index)
    {
        if (index >= numberOfTokens || index < 0)
        {
            return "";
        }

        return tokens.get(index);
    }
}
