package org.BioLayoutExpress3D.Expression;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.Expression.Panels.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;

/**
*
* @author Anton Enright, full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class ExpressionViewerFrame extends JFrame
{
    /**
    *  Serial version UID variable for the ExpressionViewer class.
    */
    public static final long serialVersionUID = 111222333444555712L;

    private ExpressionGraphPanel expressionGraphPanel = null;
    private AbstractAction expressionViewerDialogAction = null;
    private AbstractAction okAction = null;

    // variables used for proper window event usage
    private boolean isWindowIconified = false;
    private boolean isWindowMaximized = false;
    private boolean windowWasMaximizedBeforeIconification = false;

    public ExpressionViewerFrame(LayoutFrame layoutFrame, ExpressionData data)
    {
        super("Expression Viewer");

        initFrame();
        initActions();
        initComponents(layoutFrame, data);
    }

    private void initFrame()
    {
        this.setIconImage(BIOLAYOUT_ICON_IMAGE);
        this.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                setVisible(false);
            }

            @Override
            public void windowIconified(WindowEvent e)
            {
                isWindowIconified = true;
                windowWasMaximizedBeforeIconification = isWindowMaximized; // maximized state is not 'remembered' once frame is iconified, so has to be done manually!
            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {
                isWindowIconified = false;
            }
        } );
        this.addWindowStateListener( new WindowAdapter()
        {
            @Override
            public void windowStateChanged(WindowEvent e)
            {
                isWindowMaximized = (getExtendedState() == JFrame.MAXIMIZED_VERT || getExtendedState() == JFrame.MAXIMIZED_HORIZ || getExtendedState() == JFrame.MAXIMIZED_BOTH);
            }
        } );
        this.addComponentListener( new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                super.componentShown(e);
            }
        } );
    }

    private void initComponents(LayoutFrame layoutFrame, ExpressionData data)
    {
        // Add the scroll pane to this window.
        expressionGraphPanel = new ExpressionGraphPanel(this, layoutFrame, data);
        JPanel bottomPanel = new JPanel(true);
        JButton renderPlotImageToFileButton = new JButton( expressionGraphPanel.getRenderPlotImageToFileAction() );
        renderPlotImageToFileButton.setToolTipText("Render Plot Image To File As...");
        bottomPanel.add(renderPlotImageToFileButton);
        bottomPanel.add( Box.createRigidArea( new Dimension(10, 10) ) );
        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("OK");
        bottomPanel.add(okButton);
        expressionGraphPanel.add(bottomPanel, BorderLayout.SOUTH);

        this.getContentPane().add(expressionGraphPanel);

        // this.pack();
        this.setSize(650, 600);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2);
    }

    private void initActions()
    {
        expressionViewerDialogAction = new AbstractAction("Expression Viewer")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555713L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if ( !isVisible() )
                {
                    if (getExtendedState() != JFrame.NORMAL)
                        setExtendedState(JFrame.NORMAL);

                    setSize(650, 600);
                    setLocation( ( SCREEN_DIMENSION.width - getWidth() ) / 2, ( SCREEN_DIMENSION.height - getHeight() ) / 2);
                    setVisible(true);
                    refreshExpressionViewer();
                }
                else
                {
                    processAndSetWindowState();
                }
            }
        };
        expressionViewerDialogAction.setEnabled(false);

        okAction = new  AbstractAction("OK")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555714L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        };
    }

    private void processAndSetWindowState()
    {
        // this process deiconifies a frame, the maximized bits are not affected
        if (isWindowIconified)
        {
            int iconifyState = this.getExtendedState();

            // set the iconified bit, inverse process
            // deIconifyState |= Frame.ICONIFIED;

            // clear the iconified bit
            iconifyState &= ~JFrame.ICONIFIED;

            // deiconify the frame
            this.setExtendedState(iconifyState);

            if (windowWasMaximizedBeforeIconification)
            {
                // this process maximizes a frame; the iconified bit is not affected
                int maximizeState = this.getExtendedState();

                // clear the maximized bits, inverse process
                // minimizeState &= ~Frame.MAXIMIZED_BOTH;

                // set the maximized bits
                maximizeState |= JFrame.MAXIMIZED_BOTH;

                // maximize the frame
                this.setExtendedState(maximizeState);
            }
        }

        this.toFront();
    }

    public AbstractAction getExpressionViewerAction()
    {
        return expressionViewerDialogAction;
    }

    public void refreshExpressionViewer()
    {
        expressionGraphPanel.refreshPlot();
        expressionGraphPanel.repaint();
    }


}