package org.BioLayoutExpress3D.ClassViewerUI;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
public abstract class ClassViewerPlotPanel extends JPanel
{
    public ClassViewerPlotPanel()
    {
        super(true);
    }

    public abstract void onFirstShown();
    public abstract AbstractAction getRenderPlotImageToFileAction();
    public abstract AbstractAction getRenderAllCurrentClassSetPlotImagesToFilesAction();
    public abstract void refreshPlot();
}
