package org.BioLayoutExpress3D.Simulation.Panels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import org.BioLayoutExpress3D.ClassViewerUI.ClassViewerPlotPanel;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
import org.BioLayoutExpress3D.Graph.GraphElements.GraphNode;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Environment.AnimationEnvironment.*;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
public class SimulationResultsPanel extends ClassViewerPlotPanel
{
    private LayoutFrame layoutFrame;
    private ChartPanel subPlot;

    public SimulationResultsPanel(LayoutFrame layoutFrame)
    {
        super();

        this.layoutFrame = layoutFrame;
        this.setLayout(new BorderLayout());
    }

    @Override
    public void onFirstShown()
    {
        refreshPlot();
    }

    @Override
    public AbstractAction getRenderPlotImageToFileAction()
    {
        return null; //FIXME
    }

    @Override
    public AbstractAction getRenderAllCurrentClassSetPlotImagesToFilesAction()
    {
        return null; //FIXME
    }

    @Override
    public void refreshPlot()
    {
        ArrayList<GraphNode> graphNodes = new ArrayList<GraphNode>(
                layoutFrame.getGraph().getSelectionManager().getExpandedSelectedNodes());

        if (subPlot == null)
        {
            subPlot = createSimulationResultsPlot(layoutFrame, graphNodes);
            this.add(subPlot, BorderLayout.CENTER);
        }
        else
        {
            refreshDataSeries(subPlot, layoutFrame, graphNodes);
        }
    }

    private static void refreshDataSeries(ChartPanel chartPanel, LayoutFrame layoutFrame, ArrayList<GraphNode> graphNodes)
    {
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();

        int totalTimeBlocks = layoutFrame.getSignalingPetriNetSimulationDialog().getTimeBlocks();

        YIntervalSeriesCollection datasetCollection = new YIntervalSeriesCollection();
        DeviationRenderer dr = (DeviationRenderer) plot.getRenderer();

        for (GraphNode graphNode : graphNodes)
        {
            if (graphNode.ismEPNTransition())
            {
                continue;
            }

            int nodeID = graphNode.getNodeID();
            YIntervalSeries dataset = new YIntervalSeries("Simulation results " + graphNode.getNodeName());
            for (int timeBlock = 1; timeBlock < totalTimeBlocks; timeBlock++)
            {
                double value = ANIMATION_SIMULATION_RESULTS.getValue(nodeID, timeBlock);
                double halfError = ANIMATION_SIMULATION_RESULTS.getError(nodeID, timeBlock) * 0.5;
                dataset.add(timeBlock, value, value - halfError, value + halfError);
            }

            datasetCollection.addSeries(dataset);
            dr.setSeriesPaint(datasetCollection.getSeriesCount() - 1, graphNode.getColor());
            dr.setSeriesFillPaint(datasetCollection.getSeriesCount() - 1, graphNode.getColor());
        }

        plot.setDataset(datasetCollection);
    }

    public static ChartPanel createSimulationResultsPlot(LayoutFrame layoutFrame, ArrayList<GraphNode> graphNodes)
    {
        YIntervalSeriesCollection datasetCollection = new YIntervalSeriesCollection();
        DeviationRenderer dr = new DeviationRenderer(true, false);

        JFreeChart simulationResultsJFreeChart = ChartFactory.createXYLineChart(
                null, null, null, datasetCollection,
                PlotOrientation.VERTICAL, false, false, false);

        XYPlot plot = (XYPlot) simulationResultsJFreeChart.getPlot();
        plot.setBackgroundPaint(PLOT_BACKGROUND_COLOR.get());
        plot.setRangeGridlinePaint(PLOT_GRIDLINES_COLOR.get());
        plot.setDomainGridlinePaint(PLOT_GRIDLINES_COLOR.get());

        ValueAxis axis = plot.getDomainAxis();
        axis.setLowerMargin(0.0);
        axis.setUpperMargin(0.0);

        dr.setSeriesStroke(0, new BasicStroke(2.0f, 1, 1));
        dr.setAlpha(0.333f);
        plot.setRenderer(dr);

        ChartPanel chartPanel = new ChartPanel(simulationResultsJFreeChart);
        chartPanel.setMaximumDrawWidth(4096);
        chartPanel.setMaximumDrawHeight(4096);

        refreshDataSeries(chartPanel, layoutFrame, graphNodes);

        return chartPanel;
    }
}
