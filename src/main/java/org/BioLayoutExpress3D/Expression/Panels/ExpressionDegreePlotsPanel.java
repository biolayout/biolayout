package org.BioLayoutExpress3D.Expression.Panels;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import static java.lang.Math.*;

/**
*
* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class ExpressionDegreePlotsPanel extends JPanel
{
    /**
    *  Serial version UID variable for the ExpressionDegreePlotsPanel class.
    */
    public static final long serialVersionUID = 111222333444555703L;

    private static final int POINT_SIZE = 4;

    private int totalRows = 0;
    private int minThreshold = 0;
    private int threshold = 0;
    private String thresholdString = "";
    private int[][] histoGram  = null;
    private int[] maxDegree = null;
    private int[] maxCount = null;
    private int[] allNodes = null;
    private int[] allEdges = null;
    private Font tickFont = null;
    private Font axisFont = null;
    private Font legendFont = null;

    public ExpressionDegreePlotsPanel(int[][] counts, int totalRows, int minThreshold, int threshold, String thresholdString)
    {
        super(true);

        this.totalRows = totalRows;
        this.minThreshold = minThreshold;
        this.threshold = threshold;
        this.thresholdString = thresholdString;

        allNodes = new int[101 - minThreshold];
        allEdges = new int[101 - minThreshold];
        maxDegree = new int[101 - minThreshold];
        maxCount = new int[101 - minThreshold];
        histoGram = new int[101 - minThreshold][totalRows];

        Font currentFont = this.getFont();
        tickFont = currentFont.deriveFont(Font.PLAIN, 8);
        axisFont = currentFont.deriveFont(Font.BOLD, 12);
        legendFont = currentFont.deriveFont(Font.BOLD, 15);

        for (int i = 0; i < (101 - minThreshold); i++)
            calculateDistances(i, counts);
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

    private void drawDegreePlot(Graphics2D g2, int x, int y, int width, int height)
    {
        double sumXX = 0.0;
        double sumXY = 0.0;
        double sumX = 0.0;
        double sumY = 0.0 ;
        double a = 0.0;
        double b = 0.0;
        int n = 0;

        g2.setPaint(Color.WHITE);
        g2.fillRect(x,y,width,height);
        g2.setPaint(Color.BLACK);
        g2.drawRect(x,y,width,height);

        int adjustX = 20;
        int adjustY = 20;
        int padX = (int)rint(adjustX / 2);
        int padY = (int)rint(adjustY / 2);

        width = width - adjustX;
        height = height - adjustY;
        y += padY;
        x += padX;

        double maxX = log(maxDegree[threshold - minThreshold]);
        double maxY = log(maxCount[threshold - minThreshold]);

        ArrayList<Integer> logScale = logScale(maxCount[threshold - minThreshold]);
        Color color = new Color(240, 240, 240);
        double scaleX = 0.0;
        int pointX = 0;
        for (int i = 0; i < logScale.size(); i++)
        {
            scaleX = log( logScale.get(i) ) / maxX;
            if (scaleX < 0.0)
            {
                continue;
            }

            pointX = (int)( x + rint(width * scaleX) );

            g2.setPaint(color);
            g2.drawLine(pointX, y, pointX, y + height);
            g2.setPaint(Color.BLACK);
            g2.drawLine(pointX, y + height + padY, pointX, y + height + padY - 5);
            drawXCenteredText( g2, pointX, y + height + padY + 8, tickFont, logScale.get(i).toString() );
        }

        logScale = logScale(maxDegree[threshold - minThreshold]);
        double scaleY = 0.0;
        int pointY = 0;
        for (int i = 0; i < logScale.size(); i++)
        {
            scaleY = 1.0 - log( logScale.get(i) ) / maxY;
            if (scaleY < 0.0)
            {
                continue;
            }

            pointY = (int)( y + rint(height * scaleY) );

            g2.setPaint(color);
            g2.drawLine(x, pointY, x + width, pointY);
            g2.setPaint(Color.BLACK);
            g2.drawLine(x - padX, pointY, x - padX + 5, pointY);
            drawYCenteredText( g2, x - padX, pointY, tickFont, logScale.get(i).toString() );
        }

        for (int i = 1; i <= maxDegree[threshold - minThreshold]; i++)
        {
            scaleX = log(i) / maxX;
            scaleY = 1.0 - log(histoGram[threshold - minThreshold][i]) / maxY;

            if (histoGram[threshold - minThreshold][i] > 0)
            {
                pointX = (int)( x + rint( width * scaleX) );
                pointY = (int)( y + rint(height * scaleY) );

                g2.setPaint(Color.PINK);
                g2.fillOval(pointX, pointY, POINT_SIZE, POINT_SIZE);
                g2.setPaint(Color.BLACK);
                g2.drawOval(pointX, pointY, POINT_SIZE, POINT_SIZE);

                sumX += pointX;
                sumY += pointY;
                sumXX += (pointX * pointX);
                sumXY += (pointX * pointY);
                n++;
            }
        }

        b = ( (n * sumXY) - (sumX * sumY) ) / ( (n * sumXX) - (sumX * sumX) );
        a = (sumY - b * sumX) / n;
        double startY = rint(a) + rint(b * x);
        double endY = rint(a) + rint(b * (x + width) );
        g2.setPaint(Color.BLACK);
        drawXCenteredText(g2, x + (width / 2), 20, axisFont, "Graph Degree Distribution at: " + thresholdString);

        g2.setPaint(Color.BLACK);
        drawXCenteredText(g2, x + (width / 2), y + height + padY + 20, axisFont, "Node Degree");

        g2.rotate(-PI / 2, x - padX - 1, y + (height / 2));
        drawXCenteredText(g2, x - padX, y + (height / 2), axisFont, "No. of Nodes");
        g2.rotate( PI / 2, x - padX, y + (height / 2));

        g2.setPaint( (threshold >= 85) ? Color.RED : Color.GREEN );
        g2.drawLine( x, (int)( y + startY ), x + width, (int)( y + endY ) );
        g2.setPaint(Color.BLACK);
        g2.setFont(legendFont);
        drawPanelXCenteredText(g2, this.getHeight() - 6, legendFont, "Nodes: " + allNodes[threshold - minThreshold] + ", \t Edges: " + allEdges[threshold - minThreshold] + ", \t Correlation (R) = " + thresholdString);
    }

    private void drawEdgesPlot(Graphics2D g2,int x, int y, int width, int height)
    {
        g2.setPaint(Color.WHITE);
        g2.fillRect(x, y, width, height);
        g2.setPaint(Color.BLACK);
        g2.drawRect(x, y, width, height);

        double maxX = 100;
        double maxY = log(allEdges[0]);

        int adjustX = 20;
        int adjustY = 20;
        int padX = (int)rint(adjustX / 2);
        int padY = (int)rint(adjustY / 2);

        width = width - adjustX;
        height = height - adjustY;
        y = y + padY;
        x = x + padX;

        Color color = new Color(240, 240, 240);
        ArrayList<Integer> logScale = logScale(allEdges[0]);
        double scaleY = 0.0;
        int pointY = 0;
        for (int i = 0; i < logScale.size(); i++)
        {
            scaleY = 1.0 - log( logScale.get(i) ) / maxY;
            pointY = y +(int)rint(height * scaleY);

            g2.setPaint(color);
            g2.drawLine(x, pointY, x + width, pointY);
            g2.setPaint(Color.BLACK);
            g2.drawLine(x - padX, pointY, x - padX + 5, pointY);
            drawYCenteredText( g2, x - padX, pointY, tickFont, logScale.get(i).toString() );
        }

        double scaleX = 0.0;
        double scaleZ = 0.0;
        int pointX = 0;
        int pointZ = 0;
        for (int i = 0; i < (101 - minThreshold); i++)
        {
            scaleX = (double)i / (maxX - minThreshold);
            scaleY = 1.0 - log(allEdges[i]) / maxY;
            scaleZ = 1.0 - log(allNodes[i]) / maxY;

            pointX = x + (int)rint(width * scaleX);
            pointY = y + (int)rint(height * scaleY);
            pointZ = y + (int)rint(height * scaleZ);

            g2.setPaint(Color.PINK);
            g2.fillOval(pointX, pointY, POINT_SIZE, POINT_SIZE);
            g2.setPaint(Color.BLACK);
            g2.drawOval(pointX, pointY, POINT_SIZE, POINT_SIZE);
            g2.setPaint(Color.MAGENTA);
            g2.fillOval(pointX, pointZ, POINT_SIZE, POINT_SIZE);
            g2.setPaint(Color.BLACK);
            g2.drawOval(pointX, pointZ, POINT_SIZE, POINT_SIZE);

            g2.setPaint(Color.BLACK);
            g2.drawLine(pointX, y + height + padY, pointX, y + height + padY - 5);
            drawXCenteredText( g2, pointX, y + height + 8 + padY, tickFont, Integer.toString(i) );
        }

        g2.setPaint(Color.BLACK);
        drawXCenteredText(g2, x + (width / 2), 20, axisFont, "Graph Size vs Correlation Threshold");

        g2.setPaint(Color.BLACK);
        drawXCenteredText(g2, x + (width / 2), y + height + padY + 20, axisFont, "Correlation Threshold");

        g2.rotate(-PI / 2, x - padX - 1, y + (height / 2));
        drawXCenteredText(g2, x - padX, y + (height / 2), axisFont, "Number of Nodes / Edges");
        g2.rotate( PI / 2, x - padX, y + (height / 2));

        double scalelineX = (double)(threshold - minThreshold) / (maxX - minThreshold);
        int pointlinex = (int)( x + rint(width * scalelineX) );

        g2.setPaint(Color.RED);
        g2.drawLine(pointlinex, y, pointlinex, height + y);

    }

    private void drawXCenteredText(Graphics2D g2, int x, int y, Font font, String string)
    {
       int width = g2.getFontMetrics(font).stringWidth(string) + 2;

       g2.setFont(font);
       g2.drawString(string, (int)( x - rint(width / 2.0) ), y);
    }

    private void drawYCenteredText(Graphics2D g2, int x, int y, Font font, String string)
    {
       int height = g2.getFontMetrics(font).getHeight() + 2;

       g2.setFont(font);
       g2.drawString(string, x, (int)( y + rint(height / 2.0) + 2) );
    }

    private void drawPanelXCenteredText(Graphics2D g2, int y, Font font, String string)
    {
       int width = g2.getFontMetrics(font).stringWidth(string) + 2;

       g2.setFont(font);
       g2.drawString(string, (this.getWidth() - width) / 2, y);
    }

    // Simple Fuction that generates a useful and readable Log Scale from 1 to some Max Number
    private ArrayList<Integer> logScale(int maxValue)
    {
        int[] scale = { 1, 2, 3, 5, 7 };
        int currentValue = 0;
        int realValue = 1;
        int cycles = 1;
        ArrayList<Integer> logScale = new ArrayList<Integer>();

        while (realValue < maxValue)
        {
            realValue = scale[currentValue] * ( (int)pow(10, (cycles - 1) ) );
            logScale.add(realValue);

            currentValue++;
            if (currentValue > 4)
            {
                currentValue = 0;
                cycles++;
            }
        }

        return logScale;
    }

    public void updatePlots(int threshold, String thresholdString)
    {
        this.threshold = threshold;
        this.thresholdString = thresholdString;

        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        int padX = 10;
        int padY = 5;
        int bottomY = 40;
        int topY = 20;

        int plotWidth = this.getWidth() / 2 - (padX * 2);
        int plotHeight = this.getHeight() - (padY * 4 + bottomY + topY);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        drawEdgesPlot(g2, padX, padY + topY, plotWidth, plotHeight);
        padX += plotWidth + 20;
        drawDegreePlot(g2, padX, padY + topY, plotWidth, plotHeight);
    }


}