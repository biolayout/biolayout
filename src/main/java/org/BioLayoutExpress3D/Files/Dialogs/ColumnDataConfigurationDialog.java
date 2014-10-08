package org.BioLayoutExpress3D.Files.Dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.BioLayoutExpress3D.CoreUI.Dialogs.LayoutProgressBarDialog;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
public class ColumnDataConfigurationDialog extends JDialog implements ActionListener
{
    private LayoutFrame layoutFrame;
    private File file;

    public ColumnDataConfigurationDialog(LayoutFrame layoutFrame, File file)
    {
        super(layoutFrame, "Column Data Configuration", true);

        this.layoutFrame = layoutFrame;
        this.file = file;

        if(parse())
        {
            initComponents();
        }
    }

    private void initComponents()
    {
        this.setSize(640, 480);

        JTable previewTable = new JTable(data.size(), numericColumns.length);

        int rowNumber = 0;
        for(String[] row : data)
        {
            for(int i = 0; i < numericColumns.length; i++)
            {
                previewTable.getModel().setValueAt(row[i], rowNumber, i);
            }
            rowNumber++;
        }

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private Pattern quotedStringRegex = Pattern.compile("\"([^\"]*)\"|(\\S+)");
    protected ArrayList<String> tokenize(String line)
    {
        ArrayList<String> tokens = new ArrayList<String>();
        Matcher m = quotedStringRegex.matcher(line);
        while (m.find())
        {
            if (m.group(1) != null)
            {
                tokens.add(m.group(1));
            }
            else
            {
                tokens.add(m.group(2));
            }
        }

        return tokens;
    }

    private boolean isNumeric(String value)
    {
        try
        {
            Float.parseFloat(value.replace(',', '.'));
            return true;
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }

    ArrayList<String[]> data;
    boolean[] numericColumns;

    private boolean parse()
    {
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        try
        {
            BufferedReader fileReaderBuffered = new BufferedReader(new FileReader(file));
            String line;
            int numRows = 0;
            int numColumns = 0;
            data = new ArrayList<String[]>();

            while (fileReaderBuffered.readLine() != null)
            {
                numRows++;
            }

            fileReaderBuffered = new BufferedReader(new FileReader(file));
            int rowNumber = 0;
            layoutProgressBarDialog.prepareProgressBar(numRows, "Counting Columns...");
            layoutProgressBarDialog.startProgressBar();

            while ((line = fileReaderBuffered.readLine()) != null)
            {
                if (line.length() > 0)
                {
                    ArrayList<String> tokens = tokenize(line);
                    if(numColumns > 0 && tokens.size() != numColumns)
                    {
                        // Malformed input; inconsistent number of columns
                        return false;
                    }

                    numColumns = tokens.size();
                    String[] row = new String[numColumns];
                    for(int i = 0; i < numColumns; i++)
                    {
                        row[i] = tokens.get(i);
                    }
                    data.add(row);

                    layoutProgressBarDialog.incrementProgress(rowNumber++);
                }
            }

            rowNumber = 0;
            layoutProgressBarDialog.prepareProgressBar(numRows, "Analysing Columns...");
            layoutProgressBarDialog.startProgressBar();

            numericColumns = new boolean[numColumns];
            for(int i = 0; i < numColumns; i++)
            {
                numericColumns[i] = true;
            }

            for(String[] row : data)
            {
                for(int i = 0; i < numColumns; i++)
                {
                    if(!isNumeric(row[i]))
                    {
                        numericColumns[i] = false;
                    }
                }

                layoutProgressBarDialog.incrementProgress(rowNumber++);
            }
        }
        catch(IOException e)
        {
            return false;
        }
        finally
        {
            layoutProgressBarDialog.endProgressBar();
        }

        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
    }
}
