package org.BioLayoutExpress3D.Expression.Panels;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import static java.lang.Math.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.DataStructures.*;
import org.BioLayoutExpress3D.Expression.*;
import org.BioLayoutExpress3D.Expression.Dialogs.*;
import org.BioLayoutExpress3D.Graph.GraphElements.*;
import org.BioLayoutExpress3D.Network.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import org.BioLayoutExpress3D.Textures.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.chart.renderer.category.DefaultCategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;

/**
*
* The Expression Graph Panel class.
*
* @author Full refactoring and all updates by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class ExpressionGraphPanel extends JPanel implements ActionListener
{
    /**
    *  Serial version UID variable for the ExpressionGraph class.
    */
    public static final long serialVersionUID = 111222333444555705L;

    public static final int PAD_X = 60;
    public static final int PAD_BORDER = 5;
    public static final int Y_TICKS = 20;

    public static final Color DESCRIPTIONS_COLOR = Color.BLACK;
    public static final Color GRID_LINES_COLOR = Color.GRAY;
    public static final BasicStroke THIN_BASIC_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static final BasicStroke THICK_BASIC_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public static final String EXPRESSION_X_AXIS_LABEL = "Sample";
    public static final String EXPRESSION_Y_AXIS_LABEL = "Intensity";
    public static final int VALUES_FONT_SIZE = 6;
    public static final int AXIS_FONT_SIZE = 14;
    public static final int AXIS_FONT_STYLE = Font.ITALIC | Font.BOLD;
    public static final BasicStroke AXES_BASIC_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final int WARNING_MESSAGE_FOR_RENDERING_NUMBER_OF_PLOTS = 10;

    private JFrame jframe = null;
    private LayoutFrame layoutFrame = null;
    private ExpressionData expressionData = null;
    private JPanel expressionGraphCheckBoxesPanel = null;

    private JCheckBox gridLinesCheckBox = null;
    private JComboBox<String> classStatComboBox = null;
    private JComboBox<String> selectionStatComboBox = null;
    private JCheckBox axesLegendCheckBox = null;
    private JCheckBox hideSampleLabelsCheckBox = null;
    private JComboBox<String> transformComboBox = null;
    private JButton exportPlotExpressionProfileAsButton = null;

    private AbstractAction renderPlotImageToFileAction = null;
    private AbstractAction renderAllCurrentClassSetPlotImagesToFilesAction = null;
    private AbstractAction exportPlotExpressionProfileAsAction = null;

    private JFileChooser exportPlotExpressionProfileToFileChooser = null;
    private FileNameExtensionFilter fileNameExtensionFilterText = null;

    private ChartPanel expressionGraphPlotPanel = null;
    private ExpressionChooseClassesToRenderPlotImagesFromDialog expressionChooseClassesToRenderPlotImagesFromDialog = null;

    public ExpressionGraphPanel(JFrame jframe, LayoutFrame layoutFrame, ExpressionData expressionData)
    {
        super(true);

        this.jframe = jframe;
        this.layoutFrame = layoutFrame;
        this.expressionData = expressionData;

        initActions();
        initComponents();
        initExportPlotExpressionProfileToFileChooser();
    }

    /**
    *  This method is called from within the constructor to initialize the expression graph panel actions.
    */
    private void initActions()
    {
        exportPlotExpressionProfileAsAction = new AbstractAction("Export Plot Expression Profile As...")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555736L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        };
        exportPlotExpressionProfileAsAction.setEnabled(false);

        renderPlotImageToFileAction = new AbstractAction("Render Plot To File...")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555736L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                initiateTakeSingleScreenShotProcess();
            }
        };
        renderPlotImageToFileAction.setEnabled(false);

        renderAllCurrentClassSetPlotImagesToFilesAction = new AbstractAction("Render Class Set Plots To Files...")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 112222333444555993L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                expressionChooseClassesToRenderPlotImagesFromDialog.setVisible(true);
            }
        };
        renderAllCurrentClassSetPlotImagesToFilesAction.setEnabled(false);
    }

    private enum StatisticType
    {
        Individual_Lines,
        Mean_Line,
        Mean_Histogram,
        Mean_With_Std_Dev,
        Mean_Line_With_Std_Dev,
        Mean_Histogram_With_Std_Dev,
        Mean_With_Std_Err,
        Mean_Line_With_Std_Err,
        Mean_Histogram_With_Std_Err,
        IQR_Box_Plot
    }

    /**
    *  This method is called from within the constructor to initialize the expression graph panel components.
    */
    private void initComponents()
    {
        gridLinesCheckBox = new JCheckBox("Grid Lines");
        gridLinesCheckBox.setToolTipText("Grid Lines");
        axesLegendCheckBox = new JCheckBox("Axes Legend");
        axesLegendCheckBox.setToolTipText("Axes Legend");
        hideSampleLabelsCheckBox = new JCheckBox("Hide Sample Names");
        hideSampleLabelsCheckBox.setToolTipText("Hide Sample Names");
        exportPlotExpressionProfileAsButton = new JButton(exportPlotExpressionProfileAsAction);
        exportPlotExpressionProfileAsButton.setToolTipText("Export Plot Expression Profile As...");
        gridLinesCheckBox.addActionListener(this);
        axesLegendCheckBox.addActionListener(this);
        hideSampleLabelsCheckBox.addActionListener(this);
        gridLinesCheckBox.setSelected(PLOT_GRID_LINES.get());
        axesLegendCheckBox.setSelected(PLOT_AXES_LEGEND.get());
        hideSampleLabelsCheckBox.setSelected(PLOT_HIDE_SAMPLES.get());

        classStatComboBox = new JComboBox<String>();
        for (StatisticType type : StatisticType.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            classStatComboBox.addItem(s);
        }
        classStatComboBox.setSelectedIndex(PLOT_CLASS_STATISTIC_TYPE.get());
        classStatComboBox.setToolTipText("Class Plot");
        classStatComboBox.addActionListener(this);

        selectionStatComboBox = new JComboBox<String>();
        for (StatisticType type : StatisticType.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            selectionStatComboBox.addItem(s);
        }
        selectionStatComboBox.setSelectedIndex(PLOT_SELECTION_STATISTIC_TYPE.get());
        selectionStatComboBox.setToolTipText("Selection Plot");
        selectionStatComboBox.addActionListener(this);

        transformComboBox = new JComboBox<String>();
        for (ExpressionEnvironment.TransformType type : ExpressionEnvironment.TransformType.values())
        {
            String s = Utils.titleCaseOf(type.toString());
            transformComboBox.addItem(s);
        }
        transformComboBox.setSelectedIndex(PLOT_TRANSFORM.get());
        transformComboBox.setToolTipText("Transform");
        transformComboBox.addActionListener(this);

        JPanel expressionGraphUpperPartPanel = new JPanel(true);

        expressionGraphCheckBoxesPanel = new JPanel(true);
        JPanel plotOptionsLine1 = new JPanel();
        JPanel plotOptionsLine2 = new JPanel();
        plotOptionsLine1.add(new JLabel("Scaling:"));
        plotOptionsLine1.add(transformComboBox);
        plotOptionsLine1.add(gridLinesCheckBox);
        plotOptionsLine1.add(axesLegendCheckBox);
        plotOptionsLine1.add(hideSampleLabelsCheckBox);
        plotOptionsLine2.add(new JLabel("Class Plot:"));
        plotOptionsLine2.add(classStatComboBox);
        plotOptionsLine2.add(new JLabel("Selection Plot:"));
        plotOptionsLine2.add(selectionStatComboBox);
        expressionGraphCheckBoxesPanel.setLayout(new BoxLayout(expressionGraphCheckBoxesPanel, BoxLayout.PAGE_AXIS));
        expressionGraphCheckBoxesPanel.add(plotOptionsLine1);
        expressionGraphCheckBoxesPanel.add(plotOptionsLine2);

        expressionGraphPlotPanel = createExpressionPlot();
        expressionChooseClassesToRenderPlotImagesFromDialog = new ExpressionChooseClassesToRenderPlotImagesFromDialog(jframe, layoutFrame, this);

        JPanel expressionGraphButtonPanel = new JPanel(true);
        expressionGraphButtonPanel.add(exportPlotExpressionProfileAsButton);

        expressionGraphUpperPartPanel.setLayout( new BorderLayout() );
        expressionGraphUpperPartPanel.add(expressionGraphCheckBoxesPanel, BorderLayout.NORTH);
        expressionGraphUpperPartPanel.add(expressionGraphPlotPanel, BorderLayout.CENTER);

        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
        this.add(expressionGraphUpperPartPanel);
        this.add(expressionGraphButtonPanel);
        this.add( Box.createRigidArea( new Dimension(10, 10) ) );
    }

    private void initExportPlotExpressionProfileToFileChooser()
    {
        String saveFilePath = FILE_CHOOSER_PATH.get().substring(0, FILE_CHOOSER_PATH.get().lastIndexOf( System.getProperty("file.separator") ) + 1);
        exportPlotExpressionProfileToFileChooser = new JFileChooser(saveFilePath);
        fileNameExtensionFilterText = new FileNameExtensionFilter("Save as a Text File", "txt");
        exportPlotExpressionProfileToFileChooser.setFileFilter(fileNameExtensionFilterText);
        exportPlotExpressionProfileToFileChooser.setDialogTitle("Export Plot Expression Profile As");
    }

    private CategoryPlot plot;

    class RowData
    {
        public RowData(int columns, Color color)
        {
            rows = new ArrayList<Integer>();
            classColor = color;
        }

        public ArrayList<Integer> rows;
        public Color classColor;
    }

    private void addStatisticalPlot(int datasetIndex, int seriesIndex, ArrayList<Integer> rows, Color color, String className,
            StatisticType type)
    {
        float[] mean = expressionData.getMeanForRows(rows);
        float[] stddev = expressionData.getStddevForRows(rows);
        float[] stderr = expressionData.getStderrForRows(rows);

        switch (type)
        {
            case Individual_Lines:
                break;

            case Mean_Line:
            {
                DefaultCategoryDataset dataset = (DefaultCategoryDataset)plot.getDataset(datasetIndex);
                AbstractCategoryItemRenderer r = (AbstractCategoryItemRenderer)plot.getRenderer(datasetIndex);

                if (dataset == null)
                {
                    dataset = new DefaultCategoryDataset();
                    r = new DefaultCategoryItemRenderer();
                }

                for (int column = 0; column < mean.length; column++)
                {
                    String columnName = expressionData.getColumnName(column);
                    dataset.addValue(mean[column], "Mean of " + className, columnName);
                }

                DefaultCategoryItemRenderer dcir = (DefaultCategoryItemRenderer)r;
                plot.setDataset(datasetIndex, dataset);
                dcir.setSeriesPaint(seriesIndex, color);
                dcir.setSeriesShapesVisible(seriesIndex, false);
                dcir.setSeriesStroke(seriesIndex, new BasicStroke(3.0f, 1, 1, 1.0f, new float[]
                        {
                            9.0f, 4.0f
                        }, 0.0f));

                // The shapes aren't shown, but this defines the tooltip hover zone
                dcir.setBaseShape(new Rectangle2D.Double(-10.0, -10.0, 20.0, 20.0));
                dcir.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

                plot.setRenderer(datasetIndex, dcir);
            }
            break;

            case Mean_Histogram:
            {
                DefaultCategoryDataset dataset = (DefaultCategoryDataset)plot.getDataset(datasetIndex);
                AbstractCategoryItemRenderer r = (AbstractCategoryItemRenderer)plot.getRenderer(datasetIndex);

                if (dataset == null)
                {
                    dataset = new DefaultCategoryDataset();
                    r = new BarRenderer();
                }

                for (int column = 0; column < mean.length; column++)
                {
                    String columnName = expressionData.getColumnName(column);
                    dataset.addValue(mean[column], "Mean of " + className, columnName);
                }

                BarRenderer br = (BarRenderer)r;
                plot.setDataset(datasetIndex, dataset);
                br.setSeriesPaint(seriesIndex, color);

                // The shapes aren't shown, but this defines the tooltip hover zone
                br.setBaseShape(new Rectangle2D.Double(-10.0, -10.0, 20.0, 20.0));
                br.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
                br.setShadowVisible(false);
                br.setBarPainter(new StandardBarPainter());

                plot.setRenderer(datasetIndex, br);
            }
            break;

            case Mean_With_Std_Dev:
            case Mean_Line_With_Std_Dev:
            case Mean_Histogram_With_Std_Dev:
            case Mean_With_Std_Err:
            case Mean_Line_With_Std_Err:
            case Mean_Histogram_With_Std_Err:
            {
                DefaultStatisticalCategoryDataset dataset = (DefaultStatisticalCategoryDataset)plot.getDataset(datasetIndex);
                AbstractCategoryItemRenderer r = (AbstractCategoryItemRenderer)plot.getRenderer(datasetIndex);

                if (dataset == null)
                {
                    dataset = new DefaultStatisticalCategoryDataset();

                    switch (type)
                    {
                        case Mean_Histogram_With_Std_Dev:
                        case Mean_Histogram_With_Std_Err:
                            StatisticalBarRenderer sbr = new StatisticalBarRenderer();
                            sbr.setErrorIndicatorPaint(Color.black);
                            r = sbr;
                            break;

                        case Mean_Line_With_Std_Dev:
                        case Mean_Line_With_Std_Err:
                        {
                            StatisticalLineAndShapeRenderer slsr = new StatisticalLineAndShapeRenderer(true, true);
                            slsr.setUseSeriesOffset(true);
                            r = slsr;
                            break;
                        }

                        default:
                        {
                            StatisticalLineAndShapeRenderer slsr = new StatisticalLineAndShapeRenderer(false, true);
                            slsr.setUseSeriesOffset(true);
                            r = slsr;
                            break;
                        }
                    }
                }

                for (int column = 0; column < mean.length; column++)
                {
                    String columnName = expressionData.getColumnName(column);

                    switch (type)
                    {
                        case Mean_With_Std_Dev:
                        case Mean_Line_With_Std_Dev:
                        case Mean_Histogram_With_Std_Dev:
                            dataset.add(mean[column], stddev[column], className, columnName);
                            break;

                        case Mean_With_Std_Err:
                        case Mean_Line_With_Std_Err:
                        case Mean_Histogram_With_Std_Err:
                            dataset.add(mean[column], stderr[column], className, columnName);
                            break;
                    }
                }

                plot.setDataset(datasetIndex, dataset);

                r.setSeriesShape(seriesIndex, ShapeUtilities.createDiamond(3.0f));
                r.setSeriesPaint(seriesIndex, color);
                r.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
                plot.setRenderer(datasetIndex, r);
            }
            break;

            case IQR_Box_Plot:
            {
                DefaultBoxAndWhiskerCategoryDataset dataset = (DefaultBoxAndWhiskerCategoryDataset)plot.getDataset(datasetIndex);
                AbstractCategoryItemRenderer r = (AbstractCategoryItemRenderer)plot.getRenderer(datasetIndex);

                if (dataset == null)
                {
                    dataset = new DefaultBoxAndWhiskerCategoryDataset();
                    r = new BoxAndWhiskerRenderer();
                }

                ArrayList<float[]> data = new ArrayList<float[]>();
                for (int rowIndex : rows)
                {
                    data.add(expressionData.getTransformedRow(rowIndex));
                }

                for (int column = 0; column < mean.length; column++)
                {
                    ArrayList<Double> values = new ArrayList<Double>();
                    for (float[] row : data)
                    {
                        values.add((double) row[column]);
                    }

                    String columnName = expressionData.getColumnName(column);
                    dataset.add(values, className, columnName);
                }

                BoxAndWhiskerRenderer bawr = (BoxAndWhiskerRenderer)r;
                plot.setDataset(datasetIndex, dataset);
                bawr.setSeriesPaint(seriesIndex, color);
                bawr.setMedianVisible(true);
                bawr.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
                plot.setRenderer(datasetIndex, bawr);
            }
            break;
        }
    }

    public void refreshPlot()
    {
        boolean drawGridLines = PLOT_GRID_LINES.get();
        boolean drawStatsOfClass = StatisticType.values()[PLOT_CLASS_STATISTIC_TYPE.get()] != StatisticType.Individual_Lines;
        boolean drawStatsOfSelection = StatisticType.values()[PLOT_SELECTION_STATISTIC_TYPE.get()] != StatisticType.Individual_Lines;
        boolean drawAxesLegend = PLOT_AXES_LEGEND.get();
        boolean hideSampleLabels = PLOT_HIDE_SAMPLES.get();

        HashSet<GraphNode> expandedSelectedNodes =
                layoutFrame.getGraph().getSelectionManager().getExpandedSelectedNodes();
        int numSelectedNodes = expandedSelectedNodes.size();

        int totalColumns = expressionData.getTotalColumns();
        int datasetIndex = 0;

        plot.getRangeAxis().setAutoRange(false);

        if (numSelectedNodes > 0 && totalColumns > 0)
        {
            ExpressionEnvironment.TransformType transformType =
                    ExpressionEnvironment.TransformType.values()[PLOT_TRANSFORM.get()];
            expressionData.setTransformType(transformType);

            // Mean of selection
            RowData meanOfSelection = new RowData(totalColumns, new Color(0));
            int meanR = 0;
            int meanG = 0;
            int meanB = 0;

            // Mean of class
            HashMap<VertexClass, RowData> meanOfClassMap = new HashMap<VertexClass, RowData>();

            for (GraphNode graphNode : expandedSelectedNodes)
            {
                Integer index = expressionData.getIdentityMap(graphNode.getNodeName());
                if (index == null)
                {
                    continue;
                }

                float[] transformedData = expressionData.getTransformedRow(index);

                Color nodeColor = graphNode.getColor();
                VertexClass nodeClass = graphNode.getVertexClass();

                if (drawStatsOfSelection)
                {
                    meanOfSelection.rows.add(index);
                    meanR += nodeColor.getRed();
                    meanG += nodeColor.getGreen();
                    meanB += nodeColor.getBlue();
                }

                if (drawStatsOfClass)
                {
                    if (!meanOfClassMap.containsKey(nodeClass))
                    {
                        meanOfClassMap.put(nodeClass, new RowData(totalColumns, nodeColor));
                    }

                    RowData data = meanOfClassMap.get(nodeClass);
                    data.rows.add(index);
                }

                if (!drawStatsOfSelection && !drawStatsOfClass)
                {
                    String nodeName = graphNode.getNodeName();

                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (int column = 0; column < totalColumns; column++)
                    {
                        String columnName = expressionData.getColumnName(column);
                        dataset.addValue(transformedData[column], nodeName, columnName);
                    }

                    plot.setDataset(datasetIndex, dataset);
                    DefaultCategoryItemRenderer r = new DefaultCategoryItemRenderer();
                    r.setSeriesPaint(0, nodeColor);
                    r.setSeriesShapesVisible(0, false);

                    // The shapes aren't shown, but this defines the tooltip hover zone
                    r.setBaseShape(new Rectangle2D.Double(-10.0, -10.0, 20.0, 20.0));
                    r.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

                    plot.setRenderer(datasetIndex, r);
                    datasetIndex++;
                }
            }

            plot.setDataset(datasetIndex, null);
            if (drawStatsOfSelection)
            {
                Color color = new Color(meanR / numSelectedNodes, meanG / numSelectedNodes, meanB / numSelectedNodes);

                addStatisticalPlot(datasetIndex, 0, meanOfSelection.rows, color, "Mean",
                        StatisticType.values()[PLOT_SELECTION_STATISTIC_TYPE.get()]);
                datasetIndex++;
            }

            plot.setDataset(datasetIndex, null);
            if (drawStatsOfClass)
            {
                int seriesIndex = 0;
                for (Map.Entry<VertexClass, RowData> entry : meanOfClassMap.entrySet())
                {
                    VertexClass vertexClass = entry.getKey();
                    RowData data = entry.getValue();
                    Color color = entry.getValue().classColor;

                    addStatisticalPlot(datasetIndex, seriesIndex++, data.rows, color, vertexClass.getName(),
                        StatisticType.values()[PLOT_CLASS_STATISTIC_TYPE.get()]);
                }
                datasetIndex++;
            }
        }

        // Remove any datasets that shouldn't be displayed any more
        while (datasetIndex < plot.getDatasetCount())
        {
            plot.setDataset(datasetIndex, null);
            plot.setRenderer(datasetIndex, null);
            datasetIndex++;
        }

        if (drawAxesLegend)
        {
            plot.getDomainAxis().setLabel(EXPRESSION_X_AXIS_LABEL);
            plot.getRangeAxis().setLabel(EXPRESSION_Y_AXIS_LABEL);
        }
        else
        {
            plot.getDomainAxis().setLabel(null);
            plot.getRangeAxis().setLabel(null);
        }

        plot.getRangeAxis().setAutoRange(true);

        plot.setRangeGridlinesVisible(drawGridLines);
        plot.setDomainGridlinesVisible(drawGridLines);
        plot.getDomainAxis().setTickLabelsVisible(!hideSampleLabels);
        plot.setBackgroundPaint(PLOT_BACKGROUND_COLOR.get());
        plot.setRangeGridlinePaint(PLOT_GRIDLINES_COLOR.get());
        plot.setDomainGridlinePaint(PLOT_GRIDLINES_COLOR.get());

        exportPlotExpressionProfileAsAction.setEnabled(!expandedSelectedNodes.isEmpty());
    }

    private ChartPanel createExpressionPlot()
    {
        JFreeChart expressionGraphJFreeChart = ChartFactory.createLineChart(
                null, null, null, null,
                PlotOrientation.VERTICAL, false, true, false);

        plot = (CategoryPlot) expressionGraphJFreeChart.getPlot();
        plot.setBackgroundPaint(PLOT_BACKGROUND_COLOR.get());
        plot.setRangeGridlinePaint(PLOT_GRIDLINES_COLOR.get());
        plot.setDomainGridlinePaint(PLOT_GRIDLINES_COLOR.get());

        CategoryAxis axis = plot.getDomainAxis();
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);

        ChartPanel chartPanel = new ChartPanel(expressionGraphJFreeChart);
        chartPanel.setMaximumDrawWidth(4096);
        chartPanel.setMaximumDrawHeight(4096);

        return chartPanel;
    }

    private void initiateTakeSingleScreenShotProcess()
    {
        File saveScreenshotFile = layoutFrame.getGraph().saveImageToFile(jframe, "Render Plot Image To File As", "plot");
        if (saveScreenshotFile != null)
            savePlotToImageFile(saveScreenshotFile, true, "");
    }

    public void initiateTakeMultipleClassesScreenShotsProcess()
    {
        int initialClassIndex = layoutFrame.getClassViewerFrame().getClassIndex();
        int startingClassIndex = expressionChooseClassesToRenderPlotImagesFromDialog.getStartingClassIndex();
        int currentClassIndex = startingClassIndex;
        int endingClassIndex = expressionChooseClassesToRenderPlotImagesFromDialog.getEndingClassIndex() + 1;

        int option = 0;
        if (endingClassIndex - startingClassIndex < WARNING_MESSAGE_FOR_RENDERING_NUMBER_OF_PLOTS)
            option = JOptionPane.YES_OPTION;
        else
            option = JOptionPane.showConfirmDialog(jframe, "Please note that it may take some time to render all " + Integer.toString(endingClassIndex - startingClassIndex) + " class plot images.\nAre you sure you want to proceed ?", "Render All Current Class Set Plot Images To Files Process", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION)
        {
            File initialSaveScreenshotFile = layoutFrame.getGraph().saveImageToFile(jframe, "Render All Current Class Set Plot Images To Files As", "plot");
            if (initialSaveScreenshotFile != null)
            {
                layoutFrame.getClassViewerFrame().setCurrentClassIndex(currentClassIndex); // sets the current Class index
                VertexClass currentVertexClass = layoutFrame.getClassViewerFrame().navigateToCurrentClass();

                boolean savedOk = false;
                String currentVertexClassName = "";
                Tuple2<File, String> tuple2 = null;
                int numberOfSelectedNodes = 0;

                do
                {
                    currentVertexClassName = currentVertexClass.getName();
                    numberOfSelectedNodes = layoutFrame.getGraph().getSelectionManager().getSelectedNodes().size();
                    tuple2 = addCurrentClassNameToSaveScreenshotFile(initialSaveScreenshotFile, currentVertexClassName, numberOfSelectedNodes);
                    savedOk = savePlotToImageFile(tuple2.first, false, (numberOfSelectedNodes > 0) ? currentVertexClassName + " (" + numberOfSelectedNodes + " nodes)" : currentVertexClassName);
                    layoutFrame.getClassViewerFrame().setTitle("Class Viewer (Now Rendering Plot Image To File " + ++currentClassIndex + " of " + endingClassIndex + " for Class: " + tuple2.second + ( (numberOfSelectedNodes > 0) ? " with " + numberOfSelectedNodes + " nodes" : "") + ")");
                }
                while ( ( currentVertexClass = layoutFrame.getClassViewerFrame().navigateToNextClass(false) ) != null && (currentClassIndex < endingClassIndex) && savedOk );

                String numberOfRenderedPlotImages = (startingClassIndex == 0) && (endingClassIndex == layoutFrame.getClassViewerFrame().numberOfAllClasses() - 1) ? "All" : Integer.toString(endingClassIndex - startingClassIndex);
                JOptionPane.showMessageDialog(jframe, "Render " + numberOfRenderedPlotImages + " Current Class Set Plot Images To Files Process successfully finished.", "Render " + numberOfRenderedPlotImages + " Current Class Set Plot Images To Files Process", JOptionPane.INFORMATION_MESSAGE);
                layoutFrame.getClassViewerFrame().setCurrentClassIndex(initialClassIndex); // sets the initial Class index
                currentVertexClass = layoutFrame.getClassViewerFrame().navigateToCurrentClass();
            }
        }
    }

    private Tuple2<File, String> addCurrentClassNameToSaveScreenshotFile(File saveScreenshotFile, String currentVertexClassName, int numberOfSelectedNodes)
    {
        String saveScreenshotFileName = saveScreenshotFile.getAbsolutePath().substring( 0, saveScreenshotFile.getAbsolutePath().lastIndexOf(".") );
        if (currentVertexClassName.length() > 50) currentVertexClassName = currentVertexClassName.substring(0, 50); // make sure class name string not too long
        String format = saveScreenshotFile.getAbsolutePath().substring( saveScreenshotFile.getAbsolutePath().lastIndexOf(".") + 1, saveScreenshotFile.getAbsolutePath().length() );

        currentVertexClassName = currentVertexClassName.replaceAll("\\;", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\:", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\/", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\*", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\<", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\>", "_");  // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\\"", "_"); // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\|", "");   // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\\\", "_"); // for filename compatibility
        currentVertexClassName = currentVertexClassName.replaceAll("\\?", "_");  // for filename compatibility

        return Tuples.tuple(new File(saveScreenshotFileName + " for Class " + ( (numberOfSelectedNodes > 0) ? currentVertexClassName + " (" + numberOfSelectedNodes + " nodes)" : currentVertexClassName ) + "." + format), currentVertexClassName);
    }

    private boolean savePlotToImageFile(File saveScreenshotFile, boolean initiateTakeSingleScreenShotProcess, String className)
    {
        try
        {
            BufferedImage upperBorderScreenshotImage = null;
            Graphics g = null;

            upperBorderScreenshotImage = new BufferedImage(expressionGraphPlotPanel.getWidth(), ( ( !className.isEmpty() ) ? 1 : 2 ) * PAD_BORDER, Transparency.OPAQUE);
            g = upperBorderScreenshotImage.createGraphics();
            g.setColor( expressionGraphPlotPanel.getBackground() );
            g.fillRect(0, 0, upperBorderScreenshotImage.getWidth(), upperBorderScreenshotImage.getHeight());

            if ( !className.isEmpty() )
            {
                BufferedImage upperBorderClassNameScreenshotImage = createCenteredTextImage(className, expressionGraphPlotPanel.getFont().deriveFont(AXIS_FONT_STYLE, AXIS_FONT_SIZE), DESCRIPTIONS_COLOR, true, false, expressionGraphPlotPanel.getBackground(), expressionGraphPlotPanel.getWidth());
                upperBorderScreenshotImage = ImageSFXs.createCollatedImage(upperBorderScreenshotImage, upperBorderClassNameScreenshotImage, ImageSFXsCollateStates.COLLATE_SOUTH, true);

                BufferedImage upperBorderBottomScreenshotImage = new BufferedImage(expressionGraphPlotPanel.getWidth(), PAD_BORDER, Transparency.OPAQUE);
                g = upperBorderBottomScreenshotImage.createGraphics();
                g.setColor( expressionGraphPlotPanel.getBackground() );
                g.fillRect(0, 0, upperBorderBottomScreenshotImage.getWidth(), upperBorderBottomScreenshotImage.getHeight());

                upperBorderScreenshotImage = ImageSFXs.createCollatedImage(upperBorderScreenshotImage, upperBorderBottomScreenshotImage, ImageSFXsCollateStates.COLLATE_SOUTH, true);
            }

            BufferedImage rightBorderScreenshotImage = new BufferedImage(PAD_BORDER / 2, expressionGraphPlotPanel.getHeight() + upperBorderScreenshotImage.getHeight(), Transparency.OPAQUE);
            g = rightBorderScreenshotImage.createGraphics();
            g.setColor( expressionGraphPlotPanel.getBackground() );
            g.fillRect(0, 0, rightBorderScreenshotImage.getWidth(), rightBorderScreenshotImage.getHeight());

            BufferedImage plotScreenshotImage = new BufferedImage(expressionGraphPlotPanel.getWidth(), expressionGraphPlotPanel.getHeight(), Transparency.OPAQUE);
            expressionGraphPlotPanel.paintComponent( plotScreenshotImage.createGraphics() );

            plotScreenshotImage = ImageSFXs.createCollatedImage(upperBorderScreenshotImage, plotScreenshotImage, ImageSFXsCollateStates.COLLATE_SOUTH, true);
            plotScreenshotImage = ImageSFXs.createCollatedImage(rightBorderScreenshotImage, plotScreenshotImage, ImageSFXsCollateStates.COLLATE_WEST, true);

            String format = saveScreenshotFile.getAbsolutePath().substring( saveScreenshotFile.getAbsolutePath().lastIndexOf(".") + 1, saveScreenshotFile.getAbsolutePath().length() );
            ImageIO.write(plotScreenshotImage, format, saveScreenshotFile);
            if (initiateTakeSingleScreenShotProcess)
                InitDesktop.open(saveScreenshotFile);

            return true;
        }
        catch (Exception exc)
        {
            if (!initiateTakeSingleScreenShotProcess) layoutFrame.getClassViewerFrame().setTitle("Class Viewer");

            if (DEBUG_BUILD) println("Exception in ExpressionGraphPlotPanel.savePlotToImageFile():\n" + exc.getMessage());

            JOptionPane.showMessageDialog(jframe, "Something went wrong while saving the plot image to file:\n" + exc.getMessage() + "\nPlease try again with a different file name/path/drive.", "Error with saving the image to file!", JOptionPane.ERROR_MESSAGE);

            if (initiateTakeSingleScreenShotProcess)
                initiateTakeSingleScreenShotProcess();
            else
                initiateTakeMultipleClassesScreenShotsProcess();

            return false;
        }
    }

    private BufferedImage createCenteredTextImage(String text, Font font, Color fontColor, boolean isAntiAliased, boolean usesFractionalMetrics, Color backGroundColor, int imageWidth)
    {
        FontRenderContext frc = new FontRenderContext(null, isAntiAliased, usesFractionalMetrics);
        Rectangle2D rectangle2D = font.getStringBounds(text, frc);

        if ( imageWidth < rectangle2D.getWidth() )
        {
            int newFontSize = (int)floor( font.getSize() / (rectangle2D.getWidth() / imageWidth) );
            return createCenteredTextImage(text, font.deriveFont(font.getStyle(), newFontSize), fontColor, isAntiAliased, usesFractionalMetrics, backGroundColor, imageWidth);
        }
        else
        {
            BufferedImage image = new BufferedImage(imageWidth, (int)ceil( rectangle2D.getHeight() ), BufferedImage.OPAQUE);
            Graphics2D g = image.createGraphics();
            g.setColor(backGroundColor);
            g.fillRect(0, 0, imageWidth, image.getHeight());
            g.setColor(fontColor);
            g.setFont(font);
            Object antiAliased = (isAntiAliased) ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliased);
            Object fractionalMetrics = (usesFractionalMetrics) ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics);
            g.drawString(text, (float)( imageWidth - rectangle2D.getWidth() ) / 2, -(float)rectangle2D.getY());
            g.dispose();

            return image;
        }
    }

    private void save()
    {
        int dialogReturnValue = 0;
        boolean doSaveFile = false;
        File saveFile = null;

        exportPlotExpressionProfileToFileChooser.setSelectedFile( new File( IOUtils.getPrefix( layoutFrame.getFileNameLoaded() ) + "_Expression_Profile" ) );

        if (exportPlotExpressionProfileToFileChooser.showSaveDialog(jframe) == JFileChooser.APPROVE_OPTION)
        {
            String extension = fileNameExtensionFilterText.getExtensions()[0];
            String fileName = exportPlotExpressionProfileToFileChooser.getSelectedFile().getAbsolutePath();
            if ( fileName.endsWith(extension) ) fileName = IOUtils.getPrefix(fileName);

            saveFile = new File(fileName + "." + extension);

            if ( saveFile.exists() )
            {
                dialogReturnValue = JOptionPane.showConfirmDialog(jframe, "This File Already Exists.\nDo you want to Overwrite it?", "This File Already Exists. Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                if (dialogReturnValue == JOptionPane.YES_OPTION)
                    doSaveFile = true;
            }
            else
            {
                doSaveFile = true;
            }
        }

        if (doSaveFile)
        {
            saveExportPlotExpressionProfileFile(saveFile);
            FILE_CHOOSER_PATH.set( saveFile.getAbsolutePath() );
        }
    }

    private void saveExportPlotExpressionProfileFile(File file)
    {
        try
        {
            int totalColumns = expressionData.getTotalColumns();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("Name\t");
            for (int j = 0; j < totalColumns; j++)
                fileWriter.write(expressionData.getColumnName(j) + "\t");
            fileWriter.write("\n");

            Integer index = null;
            HashSet<GraphNode> expandedSelectedNodes = layoutFrame.getGraph().getSelectionManager().getExpandedSelectedNodes();
            for (GraphNode graphNode : expandedSelectedNodes)
            {
                index = expressionData.getIdentityMap( graphNode.getNodeName() );
                if (index == null) continue;

                fileWriter.write(graphNode.getNodeName() + "\t");
                for (int j = 0; j < totalColumns; j++)
                    fileWriter.write(expressionData.getExpressionDataValue(index, j) + "\t");
                fileWriter.write("\n");
            }

            fileWriter.flush();
            fileWriter.close();

            InitDesktop.edit(file);
        }
        catch (IOException ioe)
        {
            if (DEBUG_BUILD) println("Exception in saveExportPlotExpressionProfileFile()\n" + ioe.getMessage());

            JOptionPane.showMessageDialog(jframe, "Something went wrong while saving the file:\n" + ioe.getMessage() + "\nPlease try again with a different file name/path/drive.", "Error with saving the file!", JOptionPane.ERROR_MESSAGE);
            save();
        }
    }

    public AbstractAction getRenderPlotImageToFileAction()
    {
        return renderPlotImageToFileAction;
    }

    public AbstractAction getRenderAllCurrentClassSetPlotImagesToFilesAction()
    {
        return renderAllCurrentClassSetPlotImagesToFilesAction;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource().equals(gridLinesCheckBox))
        {
            PLOT_GRID_LINES.set(gridLinesCheckBox.isSelected());
        }
        else if (e.getSource().equals(classStatComboBox))
        {
            PLOT_CLASS_STATISTIC_TYPE.set(classStatComboBox.getSelectedIndex());
        }
        else if (e.getSource().equals(selectionStatComboBox))
        {
            PLOT_SELECTION_STATISTIC_TYPE.set(selectionStatComboBox.getSelectedIndex());
        }
        else if (e.getSource().equals(axesLegendCheckBox))
        {
            PLOT_AXES_LEGEND.set(axesLegendCheckBox.isSelected());
        }
        else if (e.getSource().equals(hideSampleLabelsCheckBox))
        {
            PLOT_HIDE_SAMPLES.set(hideSampleLabelsCheckBox.isSelected());
        }
        else if (e.getSource().equals(transformComboBox))
        {
            PLOT_TRANSFORM.set(transformComboBox.getSelectedIndex());
        }

        layoutFrame.getLayoutGraphPropertiesDialog().setHasNewPreferencesBeenApplied(true);
        refreshPlot();
        this.repaint();
    }


}