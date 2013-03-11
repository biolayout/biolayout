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

    public static final String EXPRESSION_X_AXIS_LABEL = "X Axis: Sample";
    public static final String EXPRESSION_Y_AXIS_LABEL = "Y Axis: Intensity";
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
    private JCheckBox classMeanCheckBox = null;
    private JCheckBox selectionMeanCheckBox = null;
    private JCheckBox rescaleCheckBox = null;
    private JCheckBox axesLegendCheckBox = null;
    private JComboBox<String> transformComboBox = null;
    private JButton exportPlotExpressionProfileAsButton = null;

    private AbstractAction renderPlotImageToFileAction = null;
    private AbstractAction renderAllCurrentClassSetPlotImagesToFilesAction = null;
    private AbstractAction exportPlotExpressionProfileAsAction = null;

    private JFileChooser exportPlotExpressionProfileToFileChooser = null;
    private FileNameExtensionFilter fileNameExtensionFilterText = null;

    private ExpressionGraphPlotPanel expressionGraphPlotPanel = null;
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

        renderPlotImageToFileAction = new AbstractAction("Render Plot Image To File As...")
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

        renderAllCurrentClassSetPlotImagesToFilesAction = new AbstractAction("Render All Current Class Set Plot Images To Files As...")
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

    /**
    *  This method is called from within the constructor to initialize the expression graph panel components.
    */
    private void initComponents()
    {
        gridLinesCheckBox = new JCheckBox("Grid Lines");
        gridLinesCheckBox.setToolTipText("Grid Lines");
        classMeanCheckBox = new JCheckBox("Class Mean");
        classMeanCheckBox.setToolTipText("Class Mean");
        selectionMeanCheckBox = new JCheckBox("Selection Mean");
        selectionMeanCheckBox.setToolTipText("Selection Mean");
        rescaleCheckBox = new JCheckBox("Rescale");
        rescaleCheckBox.setToolTipText("Rescale");
        axesLegendCheckBox = new JCheckBox("Axes Legend");
        axesLegendCheckBox.setToolTipText("Axes Legend");
        exportPlotExpressionProfileAsButton = new JButton(exportPlotExpressionProfileAsAction);
        exportPlotExpressionProfileAsButton.setToolTipText("Export Plot Expression Profile As...");
        gridLinesCheckBox.addActionListener(this);
        classMeanCheckBox.addActionListener(this);
        selectionMeanCheckBox.addActionListener(this);
        rescaleCheckBox.addActionListener(this);
        axesLegendCheckBox.addActionListener(this);
        gridLinesCheckBox.setSelected( PLOT_GRID_LINES.get() );
        classMeanCheckBox.setSelected( PLOT_CLASS_MEAN.get() );
        selectionMeanCheckBox.setSelected( PLOT_SELECTION_MEAN.get() );
        rescaleCheckBox.setSelected( PLOT_RESCALE.get() );
        axesLegendCheckBox.setSelected( PLOT_AXES_LEGEND.get() );

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
        expressionGraphCheckBoxesPanel.add(transformComboBox);
        expressionGraphCheckBoxesPanel.add(gridLinesCheckBox);
        expressionGraphCheckBoxesPanel.add(classMeanCheckBox);
        expressionGraphCheckBoxesPanel.add(selectionMeanCheckBox);
        expressionGraphCheckBoxesPanel.add(rescaleCheckBox);
        expressionGraphCheckBoxesPanel.add(axesLegendCheckBox);

        expressionGraphPlotPanel = new ExpressionGraphPlotPanel(true);
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

    private class ExpressionGraphPlotPanel extends JPanel
    {

        /**
        *  Serial version UID variable for the ExpressionGraphPlotPanel class.
        */
        public static final long serialVersionUID = 111222333444555799L;

        /**
        *  The constructor of the ExpressionGraphPlotPanel inner class.
        */
        public ExpressionGraphPlotPanel(boolean isDoubleBuffered)
        {
            super(isDoubleBuffered);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            // render the panel background
            super.paintComponent(g);

            int totalColumns = expressionData.getTotalColumns();
            // don't render anything if no expression, besides the panel background (above)
            if (totalColumns == 0) return;

            Graphics2D g2d = (Graphics2D)g;
            AffineTransform origTransform = g2d.getTransform(); // save original affine transform
            RenderingHints currentRenderingHints = g2d.getRenderingHints();  // save original rendering hints
            org.BioLayoutExpress3D.StaticLibraries.ImageProducer.qualityRendering(g2d);

            double maxStringWidth = Double.MIN_VALUE;
            double maxStringHeight = Double.MIN_VALUE;
            Rectangle2D rectangle2D = null;
            for (int j = 0; j < totalColumns; j++)
            {
                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(expressionData.getColumnName(j), g2d);

                if (rectangle2D.getWidth() > maxStringWidth)
                    maxStringWidth = rectangle2D.getWidth();

                if (rectangle2D.getHeight() > maxStringHeight)
                    maxStringHeight = rectangle2D.getHeight();
            }

            int padY = (int)maxStringWidth + expressionGraphCheckBoxesPanel.getHeight();
            double width = this.getSize().getWidth() - PAD_X;
            double height = this.getSize().getHeight();
            double columnWidth = width / totalColumns;

            boolean drawGridLines = PLOT_GRID_LINES.get();
            boolean drawMeanOfClass = PLOT_CLASS_MEAN.get();
            boolean drawMeanOfSelection = PLOT_SELECTION_MEAN.get();
            boolean drawRescale = PLOT_RESCALE.get();
            boolean drawAxesLegend = PLOT_AXES_LEGEND.get();
            ExpressionEnvironment.TransformType transformType =
                    ExpressionEnvironment.TransformType.values()[PLOT_TRANSFORM.get()];

            expressionData.setTransformType(transformType);

            int plotRectangleWidth = (int)( ( width / totalColumns ) * (totalColumns - 1) ) + 2;
            g2d.setStroke(THIN_BASIC_STROKE);
            g2d.setColor( PLOT_BACKGROUND_COLOR.get() );
            g2d.fill3DRect(PAD_X, 0, plotRectangleWidth, (int)(height - padY), true);
            g2d.setColor(DESCRIPTIONS_COLOR);
            g2d.drawRect( PAD_X, 0, plotRectangleWidth, (int)(height - padY) );

            double value = 0.0;
            double max = Double.MIN_VALUE;
            double min = 0.0; // has to be 0.0 so as to not confuse the plot rendering
            Integer index = null;
            HashSet<GraphNode> expandedSelectedNodes = layoutFrame.getGraph().getSelectionManager().getExpandedSelectedNodes();
            renderPlotImageToFileAction.setEnabled( !expandedSelectedNodes.isEmpty() );
            exportPlotExpressionProfileAsAction.setEnabled( !expandedSelectedNodes.isEmpty() );
            for (GraphNode graphNode : expandedSelectedNodes)
            {
                index = expressionData.getIdentityMap( graphNode.getNodeName() );
                if (index == null) continue;

                float[] transformedData = expressionData.getTransformedRow(index);
                for (int j = 0; j < totalColumns; j++)
                {
                    value = transformedData[j];

                    if (value > max)
                        max = value;

                    if (value < min)
                        min = value;
                }
            }

            double yScale = (height - padY) / (max - min);

            expandedSelectedNodes = layoutFrame.getGraph().getSelectionManager().getExpandedSelectedNodes();
            double[][] meanLineStepsData = (drawMeanOfClass || drawMeanOfSelection) ? new double[totalColumns][2] : null;
            HashMap<Color, double[][]> meanOfClassLineStepsDataMap = (drawMeanOfClass) ? new HashMap<Color, double[][]>() : null;
            HashMap<Color, Integer> meanOfClassLineStepsTimesUsedMap = (drawMeanOfClass) ? new HashMap<Color, Integer>() : null;
            HashSet<Color> lineColors = (drawMeanOfSelection) ? new HashSet<Color>(): null;
            Color nodeColor = null;
            for (GraphNode graphNode : expandedSelectedNodes)
            {
               index = expressionData.getIdentityMap( graphNode.getNodeName() );
               if (index == null) continue;

               nodeColor = graphNode.getColor();
               if (drawMeanOfClass)
               {
                   if ( !meanOfClassLineStepsDataMap.containsKey(nodeColor) )
                   {
                       // put meanOfClassLineStepsData in HashMap
                       meanLineStepsData = new double[totalColumns][2];
                       meanOfClassLineStepsTimesUsedMap.put(nodeColor, 1);
                       meanOfClassLineStepsDataMap.put(nodeColor, meanLineStepsData);
                   }
                   else
                   {
                       meanLineStepsData = meanOfClassLineStepsDataMap.get(nodeColor);
                       meanOfClassLineStepsTimesUsedMap.put(nodeColor, meanOfClassLineStepsTimesUsedMap.get(nodeColor) + 1); // increment integer counter in HashMap
                   }
               }
               else if (drawMeanOfSelection)
               {
                   lineColors.add(nodeColor);
               }
               else
               {
                   g2d.setColor(nodeColor);
               }

               float[] transformedData = expressionData.getTransformedRow(index);

               double currentX = 0.0;
               double nextX = 0.0;
               double thisY = 0.0;
               double nextY = 0.0;
               for (int j = 0; j < totalColumns - 1; j++)
               {
                   currentX = (j * columnWidth) + PAD_X;
                   nextX = ( (j + 1) * columnWidth ) + PAD_X;
                   thisY = transformedData[j] - min;
                   nextY = transformedData[j + 1] - min;

                   if (drawMeanOfClass || drawMeanOfSelection)
                   {
                       meanLineStepsData[j][0] += thisY;
                       meanLineStepsData[j][1] += nextY;
                   }
                   else
                   {
                       thisY *= yScale;
                       nextY *= yScale;

                       g2d.drawLine( (int)currentX, (int)(height - padY - thisY), (int)nextX, (int)(height - padY - nextY) );
                   }
               }
            }

            if (drawMeanOfClass)
            {
                if ( !meanOfClassLineStepsDataMap.isEmpty() )
                {
                    g2d.setStroke(THICK_BASIC_STROKE);

                    // put meanOfClassLineStepsData in HashMap as last step from previous loop
                    meanOfClassLineStepsDataMap.put(nodeColor, meanLineStepsData);

                    if (drawRescale)
                    {
                        ArrayList<Double> allMaxValues = new ArrayList<Double>();
                        ArrayList<Double> allMinValues = new ArrayList<Double>();
                        for ( Color lineColor : meanOfClassLineStepsDataMap.keySet() )
                        {
                            double tempMax = Double.MIN_VALUE;
                            double tempMin = Double.MAX_VALUE;
                            double checkNewMinMaxY = 0.0;
                            // has to check both this & next min/max for correct results
                            for (int j = 0; j < totalColumns; j++)
                            {
                                checkNewMinMaxY = meanOfClassLineStepsDataMap.get(lineColor)[j][0] / meanOfClassLineStepsTimesUsedMap.get(lineColor);

                                if (checkNewMinMaxY > tempMax)
                                    tempMax = checkNewMinMaxY;

                                if (checkNewMinMaxY < tempMin)
                                    tempMin = checkNewMinMaxY;

                                checkNewMinMaxY = meanOfClassLineStepsDataMap.get(lineColor)[j][1] / meanOfClassLineStepsTimesUsedMap.get(lineColor);

                                if (checkNewMinMaxY > tempMax)
                                    tempMax = checkNewMinMaxY;

                                if (checkNewMinMaxY < tempMin)
                                    tempMin = checkNewMinMaxY;
                            }

                            allMaxValues.add(tempMax);
                            allMinValues.add(tempMin);
                        }

                        max = allMaxValues.get(0);
                        min = 0.0; // has to be 0.0 so as to not confuse the plot rendering
                        for (int j = 1; j < meanOfClassLineStepsDataMap.keySet().size(); j++)
                        {
                            if (allMaxValues.get(j) > max)
                                max = allMaxValues.get(j);

                            if (allMaxValues.get(j) < min)
                                min = allMaxValues.get(j);
                        }

                        yScale = (height - padY) / (max - min);
                    }

                    for ( Color lineColor : meanOfClassLineStepsDataMap.keySet() )
                    {
                        g2d.setColor(lineColor);

                        double currentX = 0.0;
                        double nextX = 0.0;
                        double thisY = 0.0;
                        double nextY = 0.0;
                        for (int j = 0; j < totalColumns - 1; j++)
                        {
                            currentX = (j * columnWidth) + PAD_X;
                            nextX = ( (j + 1) * columnWidth ) + PAD_X;
                            thisY = ( meanOfClassLineStepsDataMap.get(lineColor)[j][0] / meanOfClassLineStepsTimesUsedMap.get(lineColor) ) * yScale;
                            nextY = ( meanOfClassLineStepsDataMap.get(lineColor)[j][1] / meanOfClassLineStepsTimesUsedMap.get(lineColor) ) * yScale;

                            g2d.drawLine( (int)currentX, (int)(height - padY - thisY), (int)nextX, (int)(height - padY - nextY) );
                        }
                    }

                    g2d.setStroke(THIN_BASIC_STROKE);
                }
            }

            if (drawMeanOfSelection)
            {
                if ( !lineColors.isEmpty() )
                {
                    g2d.setStroke(THICK_BASIC_STROKE);

                    int colorR = 0;
                    int colorG = 0;
                    int colorB = 0;
                    for (Color lineColor : lineColors)
                    {
                        colorR += lineColor.getRed();
                        colorG += lineColor.getGreen();
                        colorB += lineColor.getBlue();
                    }

                    colorR /= lineColors.size();
                    colorG /= lineColors.size();
                    colorB /= lineColors.size();

                    g2d.setColor( new Color(colorR, colorG, colorB) );

                    if (drawRescale)
                    {
                        max = Double.MIN_VALUE;
                        min = 0.0; // has to be 0.0 so as to not confuse the plot rendering
                        double checkNewMinMaxY = 0.0;
                        // has to check both this & next min/max for correct results
                        for (int j = 0; j < totalColumns; j++)
                        {
                            checkNewMinMaxY = meanLineStepsData[j][0] / expandedSelectedNodes.size();

                            if (checkNewMinMaxY > max)
                                max = checkNewMinMaxY;

                            if (checkNewMinMaxY < min)
                                min = checkNewMinMaxY;

                            checkNewMinMaxY = meanLineStepsData[j][1] / expandedSelectedNodes.size();

                            if (checkNewMinMaxY > max)
                                max = checkNewMinMaxY;

                            if (checkNewMinMaxY < min)
                                min = checkNewMinMaxY;
                        }

                        yScale = (height - padY) / (max - min);
                    }

                    double currentX = 0.0;
                    double nextX = 0.0;
                    double thisY = 0.0;
                    double nextY = 0.0;
                    for (int j = 0; j < totalColumns - 1; j++)
                    {
                        currentX = (j * columnWidth) + PAD_X;
                        nextX = ( (j + 1) * columnWidth ) + PAD_X;
                        thisY = ( meanLineStepsData[j][0] / expandedSelectedNodes.size() ) * yScale;
                        nextY = ( meanLineStepsData[j][1] / expandedSelectedNodes.size() ) * yScale;

                        g2d.drawLine( (int)currentX,(int)(height - padY - thisY), (int)nextX, (int)(height - padY - nextY) );
                    }

                    g2d.setStroke(THIN_BASIC_STROKE);
                }
            }

            if (drawAxesLegend)
            {
                Font prevFont = g2d.getFont();
                g2d.setFont( prevFont.deriveFont(AXIS_FONT_STYLE, AXIS_FONT_SIZE) );

                double maxAxesLegendStringWidth = 0.0;
                double maxAxesLegendStringHeight = 0.0;
                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(EXPRESSION_X_AXIS_LABEL, g2d);
                if (rectangle2D.getWidth() > maxAxesLegendStringWidth)
                    maxAxesLegendStringWidth = rectangle2D.getWidth();
                if (rectangle2D.getHeight() > maxAxesLegendStringHeight)
                    maxAxesLegendStringHeight = rectangle2D.getHeight();

                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(EXPRESSION_Y_AXIS_LABEL, g2d);
                if (rectangle2D.getWidth() > maxAxesLegendStringWidth)
                    maxAxesLegendStringWidth = rectangle2D.getWidth();
                if (rectangle2D.getHeight() > maxAxesLegendStringHeight)
                    maxAxesLegendStringHeight = rectangle2D.getHeight();

                g2d.setStroke(AXES_BASIC_STROKE);
                g2d.setColor(DESCRIPTIONS_COLOR);

                // draw rectangle
                int[] lineCoords = { PAD_X + plotRectangleWidth - (int)maxAxesLegendStringWidth - 2 * PAD_BORDER, PAD_BORDER, PAD_X + plotRectangleWidth - PAD_BORDER, (int)(2 * maxAxesLegendStringHeight + 2.5 * PAD_BORDER) };
                g2d.drawLine(lineCoords[0], lineCoords[1], lineCoords[2], lineCoords[1]);
                g2d.drawLine(lineCoords[0], lineCoords[1], lineCoords[0], lineCoords[3]);
                g2d.drawLine(lineCoords[0], lineCoords[3], lineCoords[2], lineCoords[3]);
                g2d.drawLine(lineCoords[2], lineCoords[3], lineCoords[2], lineCoords[1]);

                // draw axes legend
                g2d.drawString(EXPRESSION_X_AXIS_LABEL, PAD_X + plotRectangleWidth - (int)(maxAxesLegendStringWidth + 1.5 * PAD_BORDER) - 1, (int)(maxAxesLegendStringHeight + 1.5 * PAD_BORDER) - 2);
                g2d.drawString(EXPRESSION_Y_AXIS_LABEL, PAD_X + plotRectangleWidth - (int)(maxAxesLegendStringWidth + 1.5 * PAD_BORDER) - 1, (int)(2 * maxAxesLegendStringHeight + 2.0 * PAD_BORDER) - 2);

                g2d.setStroke(THIN_BASIC_STROKE);
                g2d.setFont(prevFont);
            }

            g2d.setColor(DESCRIPTIONS_COLOR);
            int tickY = 0;
            double tickHeight = ( (max - min) / Y_TICKS ) * yScale;
            Double result = 0.0;
            String label = "";
            for (int ticks = 0; ticks < Y_TICKS; ticks++)
            {
                tickY = (int)(ticks * tickHeight);
                if (transformType == ExpressionEnvironment.TransformType.LOG_SCALE)
                {
                    result = pow(E, (double) ticks * tickHeight / yScale);
                }
                else
                {
                    result = ((double) ticks * tickHeight / yScale) + min;
                }
                label = " " + ( ( !result.equals(Double.NaN) ) ? Utils.numberFormatting(result, 1) : "" );

                g2d.drawLine( PAD_X, (int)(height - padY - tickY), PAD_X + PAD_BORDER, (int)(height - padY - tickY) );
                g2d.drawString( label, 0, (int)(height - padY - tickY) );
            }

            if ( drawGridLines && (min < 0.0) )
            {
                g2d.setColor(GRID_LINES_COLOR);
                g2d.drawLine( PAD_X, (int)(max * yScale), (int)width, (int)(max * yScale) );
            }

            // add rotation to original transform to preserve any previous transformations
            AffineTransform addedRotation = new AffineTransform(origTransform);
            addedRotation.rotate(PI / 2.0);
            g2d.setTransform(addedRotation);

            // for using a smaller font when samples too many
            /*
            if ( (totalColumns * maxStringHeight) > width )
            {
                Font currentFont = g2d.getFont();
                int newFontSize = (int)floor( currentFont.getSize() / ( (totalColumns * maxStringHeight) / width ) );
                g2d.setFont( currentFont.deriveFont(currentFont.getStyle(), newFontSize) );
            }
            */

            int xTicks = totalColumns;
            double tickWidth = width / (double)xTicks;
            for (int ticks = 0; ticks < xTicks; ticks++)
            {
                g2d.setColor(DESCRIPTIONS_COLOR);
                g2d.drawString( expressionData.getColumnName(ticks), (int)(height - padY + PAD_BORDER), -(int)( PAD_X + (ticks * tickWidth) ) );

                if (drawGridLines)
                {
                    g2d.setColor(GRID_LINES_COLOR);
                    g2d.drawLine( (int)(height - padY), -(int)( PAD_X + (ticks * tickWidth) ), 0, -(int)( PAD_X + (ticks * tickWidth) ) );
                }
            }

            g2d.setTransform(origTransform); // restore original affine transform
            g2d.setRenderingHints(currentRenderingHints); // restore original rendering hints
        }


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
        else if (e.getSource().equals(classMeanCheckBox))
        {
            PLOT_CLASS_MEAN.set(classMeanCheckBox.isSelected());
        }
        else if (e.getSource().equals(selectionMeanCheckBox))
        {
            PLOT_SELECTION_MEAN.set(selectionMeanCheckBox.isSelected());
        }
        else if (e.getSource().equals(rescaleCheckBox))
        {
            PLOT_RESCALE.set(rescaleCheckBox.isSelected());
        }
        else if (e.getSource().equals(axesLegendCheckBox))
        {
            PLOT_AXES_LEGEND.set(axesLegendCheckBox.isSelected());
        }
        else if (e.getSource().equals(transformComboBox))
        {
            PLOT_TRANSFORM.set(transformComboBox.getSelectedIndex());
        }

        layoutFrame.getLayoutGraphPropertiesDialog().setHasNewPreferencesBeenApplied(true);
        this.repaint();
    }


}