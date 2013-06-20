package org.BioLayoutExpress3D.Graph.Selection.SelectionUI;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import static java.lang.Math.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.Expression.*;
import org.BioLayoutExpress3D.Graph.GraphElements.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Expression.Panels.ExpressionGraphPanel.*;
import static org.BioLayoutExpress3D.Environment.AnimationEnvironment.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.data.category.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

/**
*
* GraphPopupComponent class that provides popupMenu functionality on top of the OpenGL renderer.
*
* @author Thanos Theo, 2008-2009-2010
* @version 3.0.0.0
*
*/

public final class GraphPopupComponent implements Runnable
{

    private static final Color BIOLAYOUT_MENU_ITEM_BACKGROUND_COLOR = new Color(255, 255, 204);
    private static final int POPUP_COMPONENT_PIXEL_OFFSET = 5;
    private static final String ANIMATION_X_AXIS_LABEL = "X Axis: Time Block";
    private static final String ANIMATION_Y_AXIS_LABEL = "Y Axis: Intensity";
    private static final int TIME_BLOCKS_COLUMNS_GRANULARITY = 10;
    private static final int EXPRESSION_PLOT_X_AXIS_NAMES_LENGTH_THRESHOLD = 15;
    private static final String NAME_TAIL = "...";

    private Component component = null;
    private int popupX = 0;
    private int popupY = 0;
    private String popupNodeName = "";
    private GraphNode graphNode = null;
    private boolean isPetriNet = false;
    private LayoutFrame layoutFrame = null;

    private ExpressionData expressionData = null;
    private boolean drawGridLines = false;
    private boolean drawAxesLegend = false;
    private ExpressionEnvironment.TransformType transformType = ExpressionEnvironment.TransformType.RAW;

    private JPopupMenu popupMenu = null;
    private JMenuItem popupMenuItem = null;
    private SimulationResultsSimplePlotPanel simulationResultsSimplePlotPanel = null;

    public GraphPopupComponent()
    {
        initPopupMenu();
    }

    /**
    *  Initializes the JPopupMenu object to be used for the popup component functionality.
    */
    private void initPopupMenu()
    {
        popupMenu = new JPopupMenu();
        // set popupMenu to be heavyweight so as to be visible on top of the main OpenGL heavyweight canvas
        popupMenu.setLightWeightPopupEnabled(false);
        popupMenu.setLayout( new BorderLayout() );
    }

    /**
    *  Initializes the JPopupMenuItem object to be used for the popup component functionality.
    */
    private void initPopupMenuItem(String popupMenuItemName)
    {
        initPopupMenu();
        popupMenuItem = popupMenu.add(popupMenuItemName);
        popupMenuItem.setIcon(BIOLAYOUT_MENU_ITEM_ICON);
        popupMenuItem.setBackground(BIOLAYOUT_MENU_ITEM_BACKGROUND_COLOR);
        popupMenuItem.setForeground(Color.BLACK);
    }

    /**
    *  Sets the GraphPopupComponent object.
    */
    public void setPopupComponent(Component component, int popupX, int popupY, String popupNodeName, GraphNode graphNode, boolean isPetriNet, LayoutFrame layoutFrame)
    {
        this.component = component;
        this.popupX = popupX;
        this.popupY = popupY;
        this.popupNodeName = popupNodeName;
        this.graphNode = graphNode;
        this.isPetriNet = isPetriNet;
        this.layoutFrame = layoutFrame;

        expressionData = layoutFrame.getExpressionData();
        drawGridLines = PLOT_GRID_LINES.get();
        drawAxesLegend = PLOT_AXES_LEGEND.get();
        transformType = ExpressionEnvironment.TransformType.values()[PLOT_TRANSFORM.get()];
    }

    private JPanel createExpressionPlot()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int totalColumns = expressionData.getTotalColumns();
        Integer index = expressionData.getIdentityMap(graphNode.getNodeName());

        if ((totalColumns == 0) || (index == null))
        {
            return null;
        }

        expressionData.setTransformType(transformType);
        float[] transformedData = expressionData.getTransformedRow(index);

        for (int column = 0; column < totalColumns; column++)
        {
            String columnName = expressionData.getColumnName(column);
            dataset.addValue(transformedData[column], "Value", columnName);
        }

        JFreeChart expressionGraphJFreeChart = ChartFactory.createLineChart(
                null, null, null, dataset,
                PlotOrientation.VERTICAL, false, false, false);

        CategoryPlot plot = (CategoryPlot) expressionGraphJFreeChart.getPlot();
        plot.getRenderer().setSeriesPaint(0, graphNode.getColor());

        CategoryAxis axis = plot.getDomainAxis();
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);

        ChartPanel chartPanel = new ChartPanel(expressionGraphJFreeChart);

        return chartPanel;
    }

    @Override
    /**
    *  Overrides the run() method.
    */
    public void run()
    {
        try
        {
            if ( component.hasFocus() )
            {
                if ( SHOW_POPUP_OVERLAY_PLOT.get() && DATA_TYPE.equals(DataTypes.EXPRESSION) &&
                        !expressionData.isTransposed() )
                {
                    initPopupMenuItem("Node Name & Expression Profile: " + popupNodeName);
                    popupMenu.add(popupMenuItem, BorderLayout.NORTH);
                    JPanel plot = createExpressionPlot();

                    if (plot != null)
                    {
                        plot.setPreferredSize(new Dimension(APPLICATION_SCREEN_DIMENSION.width / 3,
                            APPLICATION_SCREEN_DIMENSION.height / 3));
                        popupMenu.add(plot, BorderLayout.CENTER);
                    }
                }
                else if ( SHOW_POPUP_OVERLAY_PLOT.get() && isPetriNet && !graphNode.ismEPNTransition() && (ANIMATION_SIMULATION_RESULTS != null) )
                {
                    initPopupMenuItem("Node Name & Simulation Profile: " + popupNodeName);
                    simulationResultsSimplePlotPanel = new SimulationResultsSimplePlotPanel(true);
                    simulationResultsSimplePlotPanel.setPreferredSize( new Dimension(APPLICATION_SCREEN_DIMENSION.width / 3, APPLICATION_SCREEN_DIMENSION.height / 3) );
                    popupMenu.add(popupMenuItem, BorderLayout.NORTH);
                    popupMenu.add(simulationResultsSimplePlotPanel, BorderLayout.CENTER);
                }
                else
                    initPopupMenuItem(popupNodeName);

                popupMenu.show(component, popupX + POPUP_COMPONENT_PIXEL_OFFSET, popupY + POPUP_COMPONENT_PIXEL_OFFSET);
            }

            // re-request focus in OpenGL renderer component so as to be able to process correctly key events
            component.requestFocus();
        }
        catch (Exception exc)
        {
            if (DEBUG_BUILD) println("Exception with GraphPopupComponent:\n" + exc.getMessage());
        }
    }

    /**
    *  Sets the PopupMenu visibility.
    */
    public void setPopupMenuVisible(boolean isVisible)
    {
        popupMenu.setVisible(isVisible);
    }

    /**
    *  Checks the PopupMenu visibility.
    */
    public boolean isPopupMenuVisible()
    {
        return popupMenu.isVisible();
    }

     /**
    *  The SimulationResultsSimplePlotPanel inner class.
    */
    private class SimulationResultsSimplePlotPanel extends JPanel
    {

        /**
        *  Serial version UID variable for the SimulationResultsSimplePlotPanel class.
        */
        public static final long serialVersionUID = 111222333444525799L;

        /**
        *  The constructor of the SimulationResultsSimplePlotPanel inner class.
        */
        public SimulationResultsSimplePlotPanel(boolean isDoubleBuffered)
        {
            super(isDoubleBuffered);
        }

        @Override
        /**
        *  Overrides the paintComponent() method.
        */
        public void paintComponent(Graphics g)
        {
            // render the parentComponent background
            super.paintComponent(g);

            int totalTimeBlocks = layoutFrame.getSignalingPetriNetSimulationDialog().getTimeBlocks();
            int nodeID = graphNode.getNodeID();

            // don't render anything if no simulation results, besides the parentComponent background (above)
            if (totalTimeBlocks == 0) return;

            Graphics2D g2d = (Graphics2D)g;
            AffineTransform origTransform = g2d.getTransform(); // save original affine transform
            RenderingHints currentRenderingHints = g2d.getRenderingHints();  // save original rendering hints
            org.BioLayoutExpress3D.StaticLibraries.ImageProducer.qualityRendering(g2d);
            // trick to force bigger distances between the Y axis legends
            Font prevFont = g2d.getFont();
            g2d.setFont( prevFont.deriveFont(AXIS_FONT_STYLE, VALUES_FONT_SIZE) );
            g2d.setStroke(AXES_BASIC_STROKE);
            g2d.setColor(DESCRIPTIONS_COLOR);

            double maxStringWidth = Double.MIN_VALUE;
            double maxStringHeight = Double.MIN_VALUE;
            Rectangle2D rectangle2D = null;
            for (int i = 0; i < totalTimeBlocks; i++)
            {
                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(Integer.toString(i + 1), g2d);

                if (rectangle2D.getWidth() > maxStringWidth)
                    maxStringWidth = rectangle2D.getWidth();

                if (rectangle2D.getHeight() > maxStringHeight)
                    maxStringHeight = rectangle2D.getHeight();
            }

            g2d.setStroke(THIN_BASIC_STROKE);
            g2d.setFont(prevFont);

            int padY = (int)maxStringWidth + popupMenuItem.getHeight();
            double width = this.getSize().getWidth() - 1.2 * PAD_X;
            double height = this.getSize().getHeight();
            double columnWidth = width / totalTimeBlocks;

            int plotRectangleWidth = (int)( (width / totalTimeBlocks) * (totalTimeBlocks - 1) ) + 2;
            g2d.setStroke(THIN_BASIC_STROKE);
            g2d.setColor( PLOT_BACKGROUND_COLOR.get() );
            g2d.fill3DRect(PAD_X, 0, plotRectangleWidth, (int)(height - padY), true);
            g2d.setColor(DESCRIPTIONS_COLOR);
            g2d.drawRect( PAD_X, 0, plotRectangleWidth, (int)(height - padY) );

            double value = ANIMATION_SIMULATION_RESULTS.getValue(nodeID, 0);
            double max = value;
            double min = value;
            for (int i = 1; i < totalTimeBlocks; i++)
            {
                value = ANIMATION_SIMULATION_RESULTS.getValue(nodeID, i);
                double halfError = ANIMATION_SIMULATION_RESULTS.getError(nodeID, i) * 0.5;

                if ((value + halfError) > max)
                {
                    max = value + halfError;
                }

                if ((value - halfError) < min)
                {
                    min = value - halfError;
                }
            }

            double yScale = (height - padY) / (max - min);

            Color nodeColor = graphNode.getColor();

            for (int i = 0; i < totalTimeBlocks - 1; i++)
            {
                double currentX = (i * columnWidth) + PAD_X;
                double nextX = ( (i + 1) * columnWidth ) + PAD_X;
                double thisY = ANIMATION_SIMULATION_RESULTS.getValue(nodeID, i) - min;
                double nextY = ANIMATION_SIMULATION_RESULTS.getValue(nodeID, i + 1) - min;
                double halfThisError = ANIMATION_SIMULATION_RESULTS.getError(nodeID, i) * 0.5;
                double halfNextError = ANIMATION_SIMULATION_RESULTS.getError(nodeID, i + 1) * 0.5;

                thisY *= yScale;
                nextY *= yScale;
                halfThisError *= yScale;
                halfNextError *= yScale;

                int[] xs =
                {
                    (int) currentX, (int) nextX, (int) nextX, (int) currentX
                };
                int[] ys =
                {
                    (int) (height - padY - thisY + halfThisError),
                    (int) (height - padY - nextY + halfNextError),
                    (int) (height - padY - nextY - halfNextError),
                    (int) (height - padY - thisY - halfThisError)
                };

                g2d.setColor(new Color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), 31));
                g2d.fillPolygon(xs, ys, 4);

                g2d.setStroke(THIN_BASIC_STROKE);
                g2d.setColor(nodeColor);
                g2d.drawLine(xs[0], ys[0], xs[1], ys[1]);
                g2d.drawLine(xs[3], ys[3], xs[2], ys[2]);

                g2d.setColor(nodeColor);
                g2d.setStroke(THICK_BASIC_STROKE);
                g2d.drawLine((int) currentX, (int) (height - padY - thisY), (int) nextX, (int) (height - padY - nextY));
            }

            if (drawAxesLegend)
            {
                prevFont = g2d.getFont();
                g2d.setFont( prevFont.deriveFont(AXIS_FONT_STYLE, AXIS_FONT_SIZE) );

                double maxAxesLegendStringWidth = 0.0;
                double maxAxesLegendStringHeight = 0.0;
                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(ANIMATION_X_AXIS_LABEL, g2d);
                if (rectangle2D.getWidth() > maxAxesLegendStringWidth)
                    maxAxesLegendStringWidth = rectangle2D.getWidth();
                if (rectangle2D.getHeight() > maxAxesLegendStringHeight)
                    maxAxesLegendStringHeight = rectangle2D.getHeight();

                rectangle2D = g2d.getFontMetrics( g2d.getFont() ).getStringBounds(ANIMATION_Y_AXIS_LABEL, g2d);
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
                g2d.drawString(ANIMATION_X_AXIS_LABEL, PAD_X + plotRectangleWidth - (int)(maxAxesLegendStringWidth + 1.5 * PAD_BORDER) - 1, (int)(maxAxesLegendStringHeight + 1.5 * PAD_BORDER) - 2);
                g2d.drawString(ANIMATION_Y_AXIS_LABEL, PAD_X + plotRectangleWidth - (int)(maxAxesLegendStringWidth + 1.5 * PAD_BORDER) - 1, (int)(2 * maxAxesLegendStringHeight + 2.0 * PAD_BORDER) - 2);

                g2d.setStroke(THIN_BASIC_STROKE);
                g2d.setFont(prevFont);
            }

            g2d.setStroke(THIN_BASIC_STROKE);
            g2d.setColor(DESCRIPTIONS_COLOR);
            int tickY = 0;
            double tickHeight = ( (max - min) / Y_TICKS ) * yScale;
            Double result = 0.0;
            String label = "";
            for (int ticks = 0; ticks < Y_TICKS; ticks++)
            {
                tickY = (int)(ticks * tickHeight);
                result = ( (double)ticks * tickHeight / yScale ) + min;
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
            if ( (totalTimeBlocks * maxStringHeight) > width )
            {
                Font currentFont = g2d.getFont();
                int newFontSize = (int)floor( currentFont.getSize() / ( (totalTimeBlocks * maxStringHeight) / width ) );
                g2d.setFont( currentFont.deriveFont(currentFont.getStyle(), newFontSize) );
            }
            */

            int[] timeBlocksColumnsToBeDisplayed = new int[TIME_BLOCKS_COLUMNS_GRANULARITY - 1];
            for (int i = 0; i < TIME_BLOCKS_COLUMNS_GRANULARITY - 1; i++)
                timeBlocksColumnsToBeDisplayed[i] = (int)( totalTimeBlocks * ( (i + 1) / 10.0f ) );
            double tickWidth = width / (double)totalTimeBlocks;
            for (int ticks = 0; ticks < totalTimeBlocks; ticks++)
            {
                if ( (ticks == 0) || checkValueInArray(ticks + 1, timeBlocksColumnsToBeDisplayed) || (ticks == totalTimeBlocks - 1) )
                {
                    g2d.setColor(DESCRIPTIONS_COLOR);
                    g2d.drawString( Integer.toString(ticks + 1), (int)(height - padY + PAD_BORDER), -(int)( PAD_X + (ticks * tickWidth) ) );

                    if (drawGridLines)
                    {
                        g2d.setColor(GRID_LINES_COLOR);
                        g2d.drawLine( (int)(height - padY), -(int)( PAD_X + (ticks * tickWidth) ), 0, -(int)( PAD_X + (ticks * tickWidth) ) );
                    }
                }
            }

            g2d.setTransform(origTransform); // restore original affine transform
            g2d.setRenderingHints(currentRenderingHints); // restore original rendering hints
        }

        /**
        *  Checks if the given value is in the given array.
        */
        private boolean checkValueInArray(int value, int[] timeBlocksColumnsToBeDisplayed)
        {
            for (int i = 0; i < timeBlocksColumnsToBeDisplayed.length; i++)
                if (timeBlocksColumnsToBeDisplayed[i] == value)
                    return true;

            return false;
        }


    }


}