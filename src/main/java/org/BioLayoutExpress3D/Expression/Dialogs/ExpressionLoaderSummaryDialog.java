package org.BioLayoutExpress3D.Expression.Dialogs;

import java.util.HashSet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.BioLayoutExpress3D.Expression.Panels.*;
import org.BioLayoutExpress3D.Expression.ExpressionData;
import org.BioLayoutExpress3D.StaticLibraries.*;
import org.BioLayoutExpress3D.Utils.*;
import static java.lang.Math.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.Expression.ExpressionEnvironment.*;
import org.BioLayoutExpress3D.Files.Parsers.ExpressionParser;

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

    private JCheckBox filterValueCheckBox = null;
    private FloatNumberField filterValueField = null;
    private JCheckBox filterStddevCheckBox = null;
    private FloatNumberField filterStddevField = null;
    private HashSet<Integer> filteredValueRows = null;
    private HashSet<Integer> filteredStddevRows = null;

    private ExpressionData expressionData;
    private ExpressionParser scanner;

    public ExpressionLoaderSummaryDialog(JFrame jframe, ExpressionData expressionData, ExpressionParser scanner)
    {
        super(jframe, "Expression Graph Settings", true);

        this.jframe = jframe;
        this.expressionData = expressionData;
        this.scanner = scanner;

        initActions();
        initComponents();

        this.setSize(750, 600);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocation( ( SCREEN_DIMENSION.width - this.getWidth() ) / 2, ( SCREEN_DIMENSION.height - this.getHeight() ) / 2 );
    }

    private void initComponents()
    {
        minThreshold = (int)rint(100.0f * STORED_CORRELATION_THRESHOLD);
        currentThreshold = (int)rint(100.0f * CURRENT_CORRELATION_THRESHOLD);
        currentThresholdFloat = CURRENT_CORRELATION_THRESHOLD;

        JPanel topPanel = new JPanel(true);
        expressionDegreePlotsPanel = new ExpressionDegreePlotsPanel(
                expressionData.getCounts(), expressionData.getTotalRows(),
                minThreshold, currentThreshold, createCorrelationTextValue(currentThreshold) );
        JPanel downPanel = new JPanel(true);
        downPanel.setLayout(new BoxLayout(downPanel, BoxLayout.PAGE_AXIS));

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

        JPanel topLine = new JPanel();
        filterValueCheckBox = new JCheckBox(new AbstractAction("FilterValueToggle")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                filterValueField.setEnabled(filterValueCheckBox.isSelected());
                refreshFilterSet();
            }
        });

        filterValueCheckBox.setText("Filter Rows With All Values Less Than");
        filterValueCheckBox.setSelected(false);
        filterValueField = new FloatNumberField(0, 5);
        filterValueField.addCaretListener(this);
        filterValueField.setDocument(new TextFieldFilter(TextFieldFilter.FLOAT));
        filterValueField.setEnabled(false);
        filterValueField.setValue(0.0f);
        topLine.add(filterValueCheckBox);
        topLine.add(filterValueField);

        JPanel middleLine = new JPanel();
        filterStddevCheckBox = new JCheckBox(new AbstractAction("FilterStddevToggle")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                filterStddevField.setEnabled(filterStddevCheckBox.isSelected());
                refreshFilterSet();
            }
        });

        filterStddevCheckBox.setText("Filter Rows With Standard Deviation Less Than");
        filterStddevCheckBox.setSelected(false);
        filterStddevField = new FloatNumberField(0, 5);
        filterStddevField.addCaretListener(this);
        filterStddevField.setDocument(new TextFieldFilter(TextFieldFilter.FLOAT));
        filterStddevField.setEnabled(false);
        filterStddevField.setValue(0.0f);
        middleLine.add(filterStddevCheckBox);
        middleLine.add(filterStddevField);

        JPanel bottomLine = new JPanel();
        JButton okButton = new JButton(okAction);
        okButton.setToolTipText("OK");
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setToolTipText("Cancel");
        bottomLine.add( new JLabel("Please Select A Correlation Value: ") );
        bottomLine.add(thresholdSlider);
        bottomLine.add(thresholdValueTextField);
        bottomLine.add(okButton);
        bottomLine.add(cancelButton);

        downPanel.add(topLine);
        downPanel.add(middleLine);
        downPanel.add(bottomLine);

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
        if (e.getSource().equals(thresholdSlider) && !thresholdValueTextField.isFocusOwner())
        {
            currentThreshold = thresholdSlider.getValue();
            currentThresholdFloat = thresholdValueTextField.getValue();
            expressionDegreePlotsPanel.updatePlots(currentThreshold,
                    createCorrelationTextValue(currentThreshold));
            thresholdValueTextField.setText(createCorrelationTextValue(currentThreshold));

            if (thresholdValueTextField.isEditable())
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
        else if (ce.getSource().equals(filterValueField))
        {
            float valueThreshold = filterValueField.getValue();
            filteredValueRows = expressionData.filterMinValue(valueThreshold);
            refreshFilterSet();
        }
        else if (ce.getSource().equals(filterStddevField))
        {
            float stddevThreshold = filterStddevField.getValue();
            filteredStddevRows = expressionData.filterMinStddev(stddevThreshold);
            refreshFilterSet();
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
                {
                    thresholdValueTextField.setText(text);
                }
                thresholdSlider.setValue(value);
                expressionDegreePlotsPanel.updatePlots(value, createCorrelationTextValue(value));
            }
        });
    }

    public void refreshFilterSet()
    {
        CURRENT_FILTER_SET = new HashSet<Integer>();

        if (filterValueCheckBox.isSelected() && filteredValueRows != null)
        {
            CURRENT_FILTER_SET.addAll(filteredValueRows);
        }

        if (filterStddevCheckBox.isSelected() && filteredStddevRows != null)
        {
            CURRENT_FILTER_SET.addAll(filteredStddevRows);
        }

        scanner.rescan();
        expressionDegreePlotsPanel.updateCounts(expressionData.getCounts());
        expressionDegreePlotsPanel.updatePlots(currentThreshold,
                createCorrelationTextValue(currentThreshold));
    }

    public boolean proceed()
    {
        CURRENT_CORRELATION_THRESHOLD = currentThresholdFloat;

        return proceed;
    }
}