package org.BioLayoutExpress3D.Expression.Dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.BioLayoutExpress3D.Expression.Panels.*;
import org.BioLayoutExpress3D.StaticLibraries.*;
import org.BioLayoutExpress3D.Utils.*;
import static java.lang.Math.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Expression.ExpressionEnvironment.*;

/**
*
* @author Full refactoring by Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class ExpressionLoaderSummaryDialog extends JDialog implements ChangeListener, CaretListener, DocumentListener
{
    /**
    *  Serial version UID variable for the ExpressionLoaderSummaryDialog class.
    */
    public static final long serialVersionUID = 111222333444555709L;

    private JFrame jframe = null;

    private int minThreshold = 0;
    private int currentThreshold = 0;
    private float currentThresholdFloat = 0.0f;
    private boolean proceed = false;

    private AbstractAction okAction = null;
    private AbstractAction cancelAction = null;
    private JSlider thresholdSlider = null;
    private FloatNumberField thresholdValueTextField = null;
    private ExpressionDegreePlotsPanel expressionDegreePlotsPanel = null;

    public ExpressionLoaderSummaryDialog(JFrame jframe, int[][] counts, int totalRows, int filteredRows)
    {
        super(jframe, "Expression Graph Settings", true);

        this.jframe = jframe;

        initActions();
        initComponents(counts, totalRows, filteredRows);

        this.setSize(750, 500);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
    }

    private void initComponents(int[][] counts, int totalRows, int filteredRows)
    {
        minThreshold = (int)rint(100.0f * STORED_CORRELATION_THRESHOLD);
        currentThreshold = (int)rint(100.0f * CURRENT_CORRELATION_THRESHOLD);
        currentThresholdFloat = CURRENT_CORRELATION_THRESHOLD;

        JPanel topPanel = new JPanel(true);
        expressionDegreePlotsPanel = new ExpressionDegreePlotsPanel( counts, totalRows, filteredRows,
                minThreshold, currentThreshold, createCorrelationTextValue(currentThreshold) );
        JPanel downPanel = new JPanel(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, expressionDegreePlotsPanel, null);
        splitPane.setEnabled(false); // disable the split pane as we use it here just for its look & feel with the DegreePlots JPanel

        thresholdSlider = new JSlider(minThreshold, 100);
        thresholdSlider.setValue(currentThreshold);
        thresholdSlider.addChangeListener(this);
        thresholdSlider.setToolTipText("Correlation Value");
        thresholdValueTextField = new FloatNumberField(0, 5);
        thresholdValueTextField.addCaretListener(this);
        thresholdValueTextField.setDocument( new TextFieldFilter(TextFieldFilter.FLOAT) );
        thresholdValueTextField.getDocument().addDocumentListener(this);
        thresholdValueTextField.setEditable(false);
        thresholdValueTextField.getCaret().setVisible(false);
        thresholdValueTextField.setText( createCorrelationTextValue( thresholdSlider.getValue() ) );

        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("OK");
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setToolTipText("Cancel");
        downPanel.add( new JLabel("Please Select A Correlation Value: ") );
        downPanel.add(thresholdSlider);
        downPanel.add(thresholdValueTextField);
        downPanel.add(okButton);
        downPanel.add(cancelButton);

        Container container = this.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add(topPanel, BorderLayout.NORTH);
        container.add(splitPane, BorderLayout.CENTER);
        container.add(downPanel, BorderLayout.SOUTH);
    }

    private void initActions()
    {
        okAction = new AbstractAction("OK")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555710L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                proceed = true;
                setVisible(false);
            }
        };

        cancelAction = new AbstractAction("Cancel")
        {
            /**
            *  Serial version UID variable for the AbstractAction class.
            */
            public static final long serialVersionUID = 111222333444555711L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                proceed = false;
                setVisible(false);
            }
        };
    }

    private String createCorrelationTextValue(int value)
    {
        String correlationText = Utils.numberFormatting(value / 100.0, 2);
        if (correlationText.length() == 3)
            return (correlationText + "0");
        else if (correlationText.length() == 1)
            return (correlationText + DECIMAL_SEPARATOR_STRING + "00");
        else
            return correlationText;
    }

    @Override
    public void stateChanged (ChangeEvent e)
    {
        if ( e.getSource().equals(thresholdSlider) && !thresholdValueTextField.isFocusOwner() )
        {
            currentThreshold = thresholdSlider.getValue();
            currentThresholdFloat = thresholdValueTextField.getValue();
            expressionDegreePlotsPanel.updatePlots( currentThreshold, createCorrelationTextValue(currentThreshold) );
            thresholdValueTextField.setText( createCorrelationTextValue(currentThreshold) );

            if ( thresholdValueTextField.isEditable() )
            {
                thresholdValueTextField.setEditable(false);
                thresholdValueTextField.getCaret().setVisible(false);
            }
        }
    }

    @Override
    public void caretUpdate(CaretEvent ce)
    {
        if( ce.getSource().equals(thresholdValueTextField) && thresholdValueTextField.isFocusOwner() )
        {
            if ( !thresholdValueTextField.isEditable() )
            {
                thresholdValueTextField.setEditable(true);
                thresholdValueTextField.getCaret().setVisible(true);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de)
    {
        if( de.getDocument().equals( thresholdValueTextField.getDocument() ) && thresholdValueTextField.isFocusOwner() )
            thresholdValueTextFieldAndUpdateValuesAccordingly();
    }

    @Override
    public void removeUpdate(DocumentEvent de)
    {
        if( de.getDocument().equals( thresholdValueTextField.getDocument() ) && thresholdValueTextField.isFocusOwner() )
            thresholdValueTextFieldAndUpdateValuesAccordingly();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {}

    private void thresholdValueTextFieldAndUpdateValuesAccordingly()
    {
        currentThresholdFloat = thresholdValueTextField.getValue();
        if (currentThresholdFloat > 1.0f)
        {
            JOptionPane.showMessageDialog(jframe, "The Correlation value cannot be bigger than 1" + DECIMAL_SEPARATOR_STRING + "00" + ".\nPlease try inserting a smaller Correlation value.", "Correlation value too large!", JOptionPane.INFORMATION_MESSAGE);
            currentThreshold = 100;
            invokeLaterInEventQueue(true, "1" + DECIMAL_SEPARATOR_STRING + "00", 100);
        }
        else if (currentThresholdFloat < STORED_CORRELATION_THRESHOLD)
        {
            JOptionPane.showMessageDialog(jframe, "The Correlation value cannot be smaller than " + STORED_CORRELATION_THRESHOLD + ".\nPlease try inserting a bigger Correlation value.", "Correlation value too small!", JOptionPane.INFORMATION_MESSAGE);
            currentThreshold = minThreshold;
            invokeLaterInEventQueue(true, createCorrelationTextValue(minThreshold), minThreshold);
        }
        else
        {
            currentThreshold = (int)rint(100.0f * currentThresholdFloat);
            invokeLaterInEventQueue(false, null, currentThreshold);
        }
    }

    private void invokeLaterInEventQueue(final boolean thresholdValueTextFieldOrThresholdSlider, final String text, final int value)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (thresholdValueTextFieldOrThresholdSlider)
                    thresholdValueTextField.setText(text);
                thresholdSlider.setValue(value);
                expressionDegreePlotsPanel.updatePlots( value, createCorrelationTextValue(value) );
            }
        });
    }

    public boolean proceed()
    {
        CURRENT_CORRELATION_THRESHOLD = currentThresholdFloat;

        return proceed;
    }


}