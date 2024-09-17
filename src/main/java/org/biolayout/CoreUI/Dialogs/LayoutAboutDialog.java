package org.biolayout.CoreUI.Dialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import org.biolayout.StaticLibraries.*;
import static org.biolayout.Environment.GlobalEnvironment.*;
import static org.biolayout.DebugConsole.ConsoleOutput.*;

/**
*
* @author Leon Goldovsky, full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class LayoutAboutDialog extends JDialog implements ActionListener, HyperlinkListener
{
    /**
    *  Serial version UID variable for the LayoutAboutDialog class.
    */
    public static final long serialVersionUID = 111222333444555670L;

    private JLabel image = null;
    private JButton okButton = null;
    private JLabel actionLabel = null;
    private AbstractAction aboutAction = null;

    public LayoutAboutDialog(JFrame frame, boolean isSplash)
    {
        super(frame, "About " + VERSION, !isSplash);

        this.setIconImages(ICON_IMAGES);
        image = new JLabel( new ImageIcon(LOGO_IMAGE) );

        if (isSplash)
        {
            actionLabel = new JLabel(" Loading...");
            actionLabel.setBackground(Color.WHITE);

            initComponentsForSplashDialog();
        }
        else
        {
            okButton = new JButton("OK");
            okButton.setToolTipText("OK");
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            initComponentsForAboutDialog();
        }
    }

    private void initComponentsForSplashDialog()
    {
        this.setUndecorated(true);
        this.setResizable(false);
        this.getContentPane().setLayout( new BorderLayout() );
        this.getContentPane().add(image, BorderLayout.CENTER);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel layoutPanel = new JPanel(true);
        layoutPanel.setLayout( new BorderLayout() );
        layoutPanel.add(actionLabel, BorderLayout.CENTER);

        this.getContentPane().add(layoutPanel, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponentsForAboutDialog()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10) );

        BufferedImage idealImage = null;
        for(BufferedImage iconImage : ICON_IMAGES)
        {
            if (iconImage.getWidth() <= 256)
            {
                if (idealImage != null)
                {
                    if (iconImage.getWidth() > idealImage.getWidth())
                    {
                        idealImage = iconImage;
                    }
                }
                else
                {
                    idealImage = iconImage;
                }
            }
        }

        JLabel icon = new JLabel(new ImageIcon(idealImage));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(icon);
        panel.add(Box.createRigidArea(new Dimension(20,20)));

        JLabel textLine1 = new JLabel(PRODUCT_NAME + " is a tool for the visualisation and analysis of networks.");
        textLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel textLine2 = new JLabel("© University of Edinburgh 2020-2024");
        textLine2.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(textLine1);
        panel.add(Box.createRigidArea(new Dimension(20,10)));
        panel.add(textLine2);
        panel.add(Box.createRigidArea(new Dimension(20,10)));

        okButton.addActionListener(this);
        panel.add(okButton);

        getContentPane().add(panel);

        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);

        aboutAction = new AbstractAction("About")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setLocationRelativeTo(null);
                setVisible(true);
            }
        };
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if ( e.getActionCommand().equals("OK") )
            setVisible(false);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent ev)
    {
        if (DEBUG_BUILD) println( ev.getEventType() );

        if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            URL url = ev.getURL();
            if (DEBUG_BUILD) println(url);
            InitDesktop.browse(url);
        }
    }

    public void finishedLoading()
    {
        this.setVisible(false);
        this.dispose();
    }

    public void setText(String string)
    {
        actionLabel.setText(" " + string);
    }

    public AbstractAction getAboutAction()
    {
        return aboutAction;
    }


}
