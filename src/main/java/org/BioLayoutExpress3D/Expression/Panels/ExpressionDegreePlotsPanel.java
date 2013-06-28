package org.BioLayoutExpress3D.Expression.Panels;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import static java.lang.Math.*;
import java.text.DecimalFormat;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.ValueMarker;

/**
*
* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class ExpressionDegreePlotsPanel extends JPanel
{
    private int totalRows = 0;
    private int minThreshold = 0;
    private int threshold = 0;
    private String thresholdString = "";
    private int[][] histoGram  = null;
    private int[] maxDegree = null;
    private int[] maxCount = null;
    private int[] allNodes = null;
    private int[] allEdges = null;

    private JFreeChart nodesEdgesChart;
    private JFreeChart degreeChart;
    private JLabel countsLabel;
    private XYSeries degreeSeries;
    private XYDataset degreeDataset;
    private XYSeries edgesSeries;
    private XYSeries nodesSeries;
    private XYSeriesCollection edgesNodesDataset;
    private ValueMarker thresholdMarker;

    public ExpressionDegreePlotsPanel(int[][] counts, int totalRows,
            int minThreshold, int threshold, String thresholdString)
    {
        super(true);
        this.setLayout(new GridBagLayout());

        this.totalRows = totalRows;
        this.minThreshold = minThreshold;
        this.threshold = threshold;
        this.thresholdString = thresholdString;

        updateCounts(counts);

        initCharts();
    }

    private void calculateDistances(int threshold, int[][] counts)
    {
        int nodesCounter = 0;
        int totalEdges = 0;
        int edgesCounter = 0;
        int max = 0;

        for (int i = 0; i < totalRows; i++)
        {
            edgesCounter = 0;
            for (int j = (minThreshold + threshold); j <= 100; j++)
                if (counts[i][j] > 0)
                      edgesCounter += counts[i][j];

            if (edgesCounter > 0)
            {
                nodesCounter++;
                totalEdges += edgesCounter;
                histoGram[threshold][edgesCounter]++;
                if (edgesCounter >= max)
                    max = edgesCounter;
            }
        }

        maxDegree[threshold] = max;
        max = 0;

        for (int i = 0; i < totalRows; i++)
            if (histoGram[threshold][i] > max)
                max = histoGram[threshold][i];

        maxCount[threshold] = max;
        allNodes[threshold] = nodesCounter;
        allEdges[threshold] = totalEdges / 2;
    }

    public void updateCounts(int[][] counts)
    {
        allNodes = new int[101 - minThreshold];
        allEdges = new int[101 - minThreshold];
        maxDegree = new int[101 - minThreshold];
        maxCount = new int[101 - minThreshold];
        histoGram = new int[101 - minThreshold][totalRows];

        for (int i = 0; i < (101 - minThreshold); i++)
        {
            calculateDistances(i, counts);
        }
    }

    public void updatePlots(int threshold, String thresholdString)
    {
        this.threshold = threshold;
        if (threshold < minThreshold)
        {
            System.out.println("updatePlots: threshold " + threshold + " < minThreshold" + minThreshold);
        }

        this.thresholdString = thresholdString;

        this.updateEdgesNodesChart();
        this.updateDegreeChart();
        this.updateCountsLabel();

        this.repaint();
    }

    private JFreeChart createEdgesNodesChart()
    {
        edgesSeries = new XYSeries("Edges");
        nodesSeries = new XYSeries("Nodes");
        edgesNodesDataset = new XYSeriesCollection();
        edgesNodesDataset.addSeries(edgesSeries);
        edgesNodesDataset.addSeries(nodesSeries);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Graph Size vs. Correlation Threshold", null, null,
                edgesNodesDataset, PlotOrientation.VERTICAL, false, false, false);
        Font font = chart.getTitle().getFont();
        font = font.deriveFont(12.0f);
        chart.getTitle().setFont(font);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();

        r.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
        r.setSeriesFillPaint(0, Color.PINK);
        r.setUseFillPaint(true);
        r.setSeriesOutlinePaint(0, Color.BLACK);
        r.setUseOutlinePaint(true);

        r.setSeriesShape(1, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
        r.setSeriesFillPaint(1, Color.MAGENTA);
        r.setUseFillPaint(true);
        r.setSeriesOutlinePaint(1, Color.BLACK);
        r.setUseOutlinePaint(true);

        NumberAxis xAxis = new NumberAxis("Correlation Threshold");
        xAxis.setNumberFormatOverride(new DecimalFormat("##"));
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setAutoRange(true);
        plot.setDomainAxis(xAxis);

        LogAxis yAxis = new LogAxis("Nodes / Edges");
        yAxis.setNumberFormatOverride(new DecimalFormat("#.##E0"));
        plot.setRangeAxis(yAxis);

        thresholdMarker = new ValueMarker(threshold);
        thresholdMarker.setPaint(Color.red);
        thresholdMarker.setStroke(new BasicStroke(2.0F));
        plot.addDomainMarker(thresholdMarker);

        return chart;
    }

    private void updateEdgesNodesChart()
    {
        if (edgesSeries != null && nodesSeries != null)
        {
            edgesSeries.clear();
            nodesSeries.clear();

            for (int i = 0; i < (100 - minThreshold); i++)
            {
                edgesSeries.add((double) i + minThreshold, allEdges[i]);
                nodesSeries.add((double) i + minThreshold, allNodes[i]);
            }

            thresholdMarker.setValue(threshold);
        }
    }

    private JFreeChart createDegreeChart()
    {
        degreeSeries = new XYSeries("Degree");
        degreeDataset = new XYSeriesCollection(degreeSeries);
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Graph Degree Distribution", null, null,
                degreeDataset, PlotOrientation.VERTICAL, false, false, false);
        Font font = chart.getTitle().getFont();
        font = font.deriveFont(12.0f);
        chart.getTitle().setFont(font);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
        r.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0));
        r.setSeriesFillPaint(0, Color.PINK);
        r.setUseFillPaint(true);
        r.setSeriesOutlinePaint(0, Color.BLACK);
        r.setUseOutlinePaint(true);

        LogAxis xAxis = new LogAxis("Node Degree");
        xAxis.setNumberFormatOverride(new DecimalFormat("###.#"));
        LogAxis yAxis = new LogAxis("Nodes");
        yAxis.setNumberFormatOverride(new DecimalFormat("###.#"));
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        return chart;
    }

    private void updateDegreeChart()
    {
        if (degreeSeries != null)
        {
            degreeSeries.clear();

            int numPoints = maxDegree[threshold - minThreshold] + 1;
            for (int i = 1; i < numPoints; i++)
            {
                if (histoGram[threshold - minThreshold][i] > 0)
                {
                    degreeSeries.add(i, histoGram[threshold - minThreshold][i]);
                }
            }

            //TODO fitted line
        }
    }

    private void updateCountsLabel()
    {
        countsLabel.setText("Nodes: " + allNodes[threshold - minThreshold] +
                ", Edges: " + allEdges[threshold - minThreshold] +
                ", Correlation (R) = " + thresholdString);
    }

    private void initCharts()
    {
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.weighty = 1.0;

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        nodesEdgesChart = createEdgesNodesChart();
        this.add(new ChartPanel(nodesEdgesChart), c);
        updateEdgesNodesChart();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        degreeChart = createDegreeChart();
        this.add(new ChartPanel(degreeChart), c);
        updateDegreeChart();

        c.weighty = 0.0;
        c.fill = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        countsLabel = new JLabel("Some Text");
        this.add(countsLabel, c);
        updateCountsLabel();
    }
}