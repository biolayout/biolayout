package org.BioLayoutExpress3D.Files.Dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.BioLayoutExpress3D.CoreUI.Dialogs.LayoutProgressBarDialog;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
import org.BioLayoutExpress3D.Utils.FloatNumberField;
import org.BioLayoutExpress3D.Utils.TextFieldFilter;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 *
 * Some ideas from http://www.java2s.com/Code/Java/Swing-Components/EditableHeaderTableExample2.htm
 */
public class ColumnDataConfigurationDialog extends JDialog
{

    private LayoutFrame layoutFrame;
    private File file;
    private JTable previewTable;

    private FloatNumberField filterValueField;

    private JButton okButton;
    private JButton cancelButton;
    private AbstractAction okAction;
    private AbstractAction cancelAction;
    private boolean proceed;

    public boolean proceed()
    {
        return proceed;
    }
    private boolean failed;

    public boolean failed()
    {
        return failed;
    }

    public ColumnDataConfigurationDialog(LayoutFrame layoutFrame, File file)
    {
        super(layoutFrame, "Column Data Configuration", true);

        this.layoutFrame = layoutFrame;
        this.file = file;
        this.proceed = false;
        this.failed = true;

        if (parse())
        {
            initActions();
            initComponents();

            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setLocationRelativeTo(null);

            this.failed = false;
        }
    }

    private void columnSelectorChanged()
    {
        if (filterValueField != null)
        {
            filterValueField.setEnabled(false);
            for (int i = 0; i < previewTable.getColumnCount(); i++)
            {
                EditableHeaderTableColumn tableColumn =
                        (EditableHeaderTableColumn) previewTable.getColumnModel().getColumn(i);

                String value = (String) tableColumn.getHeaderValue();

                if (value.equals("Edge Weight"))
                {
                    filterValueField.setEnabled(true);
                }
            }
        }
    }

    private void initComponents()
    {
        this.setSize(640, 480);
        this.setMinimumSize(new Dimension(640, 240));

        previewTable = new JTable(rows.size(), numericColumns.length);
        TableColumnModel columnModel = previewTable.getColumnModel();
        previewTable.setTableHeader(new EditableHeader(columnModel));

        int rowNumber = 0;
        for (String[] row : rows)
        {
            for (int i = 0; i < numericColumns.length; i++)
            {
                previewTable.getModel().setValueAt(row[i], rowNumber, i);
            }
            rowNumber++;
        }

        String[] alphaColumnValues =
        {
            "Node ID", "Edge Type", "Ignore"
        };
        JComboBox<String> alphaComboBox = new JComboBox<String>();
        alphaComboBox.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                columnSelectorChanged();
            }
        });
        for (String alphaColumnValue : alphaColumnValues)
        {
            alphaComboBox.addItem(alphaColumnValue);
        }
        ComboRenderer alphaComboRenderer = new ComboRenderer(alphaColumnValues);

        String[] numericColumnValues =
        {
            "Edge Weight", "Node ID", "Edge Type", "Ignore"
        };
        JComboBox<String> numericComboBox = new JComboBox<String>();
        numericComboBox.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                columnSelectorChanged();
            }
        });
        for (String numericColumnValue : numericColumnValues)
        {
            numericComboBox.addItem(numericColumnValue);
        }
        ComboRenderer numericComboRenderer = new ComboRenderer(numericColumnValues);

        int numericColumnCount = 0;
        int alphaColumnCount = 0;

        for (int i = 0; i < previewTable.getColumnCount(); i++)
        {
            EditableHeaderTableColumn tableColumn =
                    (EditableHeaderTableColumn) previewTable.getColumnModel().getColumn(i);

            if (numericColumns[i])
            {
                if (totalAlphaColumns == 0)
                {
                    if (numericColumnCount < 2)
                    {
                        tableColumn.setHeaderValue("Node ID");
                    }
                    else if (numericColumnCount == 2)
                    {
                        tableColumn.setHeaderValue("Edge Weight");
                    }
                    else if (numericColumnCount == 3)
                    {
                        tableColumn.setHeaderValue("Edge Type");
                    }
                    else
                    {
                        tableColumn.setHeaderValue("Ignore");
                    }
                }
                else
                {
                    if (numericColumnCount == 0)
                    {
                        tableColumn.setHeaderValue("Edge Weight");
                    }
                    else
                    {
                        tableColumn.setHeaderValue("Ignore");
                    }
                }

                numericColumnCount++;

                tableColumn.setHeaderRenderer(numericComboRenderer);
                tableColumn.setHeaderEditor(new DefaultCellEditor(numericComboBox));
            }
            else
            {
                if (alphaColumnCount < 2)
                {
                    tableColumn.setHeaderValue("Node ID");
                }
                else
                {
                    tableColumn.setHeaderValue("Edge Type");
                }

                alphaColumnCount++;

                tableColumn.setHeaderRenderer(alphaComboRenderer);
                tableColumn.setHeaderEditor(new DefaultCellEditor(alphaComboBox));
            }
        }

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel buttonPanel = new JPanel();

        JLabel filterValueLabel = new JLabel("Filter Rows With Edge Weights Less Than");

        filterValueField = new FloatNumberField(0, 5);
        filterValueField.setDocument(new TextFieldFilter(TextFieldFilter.FLOAT));
        filterValueField.setValue(0.0f);
        columnSelectorChanged();

        okButton = new JButton(okAction);
        cancelButton = new JButton(cancelAction);

        buttonPanel.add(filterValueLabel);
        buttonPanel.add(filterValueField);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getRootPane().setDefaultButton(okButton);

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    public ArrayList<Integer> nodeIdColumns;
    public int edgeWeightColumn;
    public int edgeTypeColumn;
    public float filterValue()
    {
        return filterValueField.getValue();
    }

    private boolean configure()
    {
        int nodeIdColumnCount = 0;
        int edgeWeightColumnCount = 0;
        int edgeTypeColumnCount = 0;

        nodeIdColumns = new ArrayList<Integer>();
        edgeWeightColumn = -1;
        edgeTypeColumn = -1;

        for (int i = 0; i < previewTable.getColumnCount(); i++)
        {
            EditableHeaderTableColumn tableColumn =
                    (EditableHeaderTableColumn) previewTable.getColumnModel().getColumn(i);

            if (numericColumns[i])
            {
                String value = (String) tableColumn.getHeaderValue();

                if (!value.equals("Ignore"))
                {
                    edgeWeightColumn = i;
                    edgeWeightColumnCount++;
                }
            }
            else
            {
                String value = (String) tableColumn.getHeaderValue();

                if (value.equals("Node ID"))
                {
                    nodeIdColumns.add(i);
                    nodeIdColumnCount++;
                }
                else if (value.equals("Edge Type"))
                {
                    edgeTypeColumn = i;
                    edgeTypeColumnCount++;
                }
            }
        }

        if (edgeWeightColumnCount > 1)
        {
            JOptionPane.showMessageDialog(this,
                    "You must only select a single column to describe the edge weight.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (edgeTypeColumnCount > 1)
        {
            JOptionPane.showMessageDialog(this,
                    "You must only select a single column to describe the edge type.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (nodeIdColumnCount != 2)
        {
            JOptionPane.showMessageDialog(this,
                    "You must select exactly two columns to describe the node identifier.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void initActions()
    {
        okAction = new AbstractAction("OK")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (configure())
                {
                    proceed = true;
                    setVisible(false);
                }
            }
        };

        cancelAction = new AbstractAction("Cancel")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
            }
        };
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

    ArrayList<String[]> rows;
    boolean[] numericColumns;
    int totalAlphaColumns;
    int totalNumericColumns;

    private boolean parse()
    {
        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        try
        {
            BufferedReader fileReaderBuffered = new BufferedReader(new FileReader(file));
            String line;
            int numRows = 0;
            int numColumns = 0;
            rows = new ArrayList<String[]>();

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
                    if (numColumns > 0 && tokens.size() != numColumns)
                    {
                        // Malformed input; inconsistent number of columns
                        return false;
                    }

                    numColumns = tokens.size();
                    String[] row = new String[numColumns];
                    for (int i = 0; i < numColumns; i++)
                    {
                        row[i] = tokens.get(i);
                    }
                    rows.add(row);

                    layoutProgressBarDialog.incrementProgress(rowNumber++);
                }
            }

            rowNumber = 0;
            layoutProgressBarDialog.prepareProgressBar(numRows, "Analysing Columns...");
            layoutProgressBarDialog.startProgressBar();

            numericColumns = new boolean[numColumns];

            for (int i = 0; i < numColumns; i++)
            {
                numericColumns[i] = true;
            }

            for (String[] row : rows)
            {
                for (int i = 0; i < numColumns; i++)
                {
                    if (!isNumeric(row[i]))
                    {
                        numericColumns[i] = false;
                    }
                }

                layoutProgressBarDialog.incrementProgress(rowNumber++);
            }

            totalAlphaColumns = 0;
            totalNumericColumns = 0;

            for (int i = 0; i < numColumns; i++)
            {
                if (numericColumns[i])
                {
                    totalNumericColumns++;
                }
                else
                {
                    totalAlphaColumns++;
                }
            }
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            layoutProgressBarDialog.endProgressBar();
        }

        return true;
    }

    class ComboRenderer extends JComboBox<String> implements TableCellRenderer
    {

        ComboRenderer(String[] items)
        {
            for (int i = 0; i < items.length; i++)
            {
                addItem(items[i]);
            }
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column)
        {
            setSelectedItem(value);
            return this;
        }
    }
}

class EditableHeader extends JTableHeader implements CellEditorListener
{

    public final int HEADER_ROW = -10;
    transient protected int editingColumn;
    transient protected TableCellEditor cellEditor;
    transient protected Component editorComp;

    public EditableHeader(TableColumnModel columnModel)
    {
        super(columnModel);
        setReorderingAllowed(false);
        cellEditor = null;
        recreateTableColumn(columnModel);
    }

    @Override
    public void updateUI()
    {
        setUI(new EditableHeaderUI());
        resizeAndRepaint();
        invalidate();
    }

    protected void recreateTableColumn(TableColumnModel columnModel)
    {
        int n = columnModel.getColumnCount();
        EditableHeaderTableColumn[] newCols = new EditableHeaderTableColumn[n];
        TableColumn[] oldCols = new TableColumn[n];
        for (int i = 0; i < n; i++)
        {
            oldCols[i] = columnModel.getColumn(i);
            newCols[i] = new EditableHeaderTableColumn();
            newCols[i].copyValues(oldCols[i]);
        }
        for (int i = 0; i < n; i++)
        {
            columnModel.removeColumn(oldCols[i]);
        }
        for (int i = 0; i < n; i++)
        {
            columnModel.addColumn(newCols[i]);
        }
    }

    public boolean editCellAt(int index)
    {
        return editCellAt(index);
    }

    public boolean editCellAt(int index, EventObject e)
    {
        if (cellEditor != null && !cellEditor.stopCellEditing())
        {
            return false;
        }
        if (!isCellEditable(index))
        {
            return false;
        }
        TableCellEditor editor = getCellEditor(index);

        if (editor != null && editor.isCellEditable(e))
        {
            editorComp = prepareEditor(editor, index);
            editorComp.setBounds(getHeaderRect(index));
            add(editorComp);
            editorComp.validate();
            setCellEditor(editor);
            setEditingColumn(index);
            editor.addCellEditorListener(this);

            return true;
        }
        return false;
    }

    public boolean isCellEditable(int index)
    {
        if (getReorderingAllowed())
        {
            return false;
        }
        int columnIndex = columnModel.getColumn(index).getModelIndex();
        EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel
                .getColumn(columnIndex);
        return col.isHeaderEditable();
    }

    public TableCellEditor getCellEditor(int index)
    {
        int columnIndex = columnModel.getColumn(index).getModelIndex();
        EditableHeaderTableColumn col = (EditableHeaderTableColumn) columnModel
                .getColumn(columnIndex);
        return col.getHeaderEditor();
    }

    public void setCellEditor(TableCellEditor newEditor)
    {
        TableCellEditor oldEditor = cellEditor;
        cellEditor = newEditor;

        // firePropertyChange

        if (oldEditor != null && oldEditor instanceof TableCellEditor)
        {
            ((TableCellEditor) oldEditor)
                    .removeCellEditorListener((CellEditorListener) this);
        }
        if (newEditor != null && newEditor instanceof TableCellEditor)
        {
            ((TableCellEditor) newEditor)
                    .addCellEditorListener((CellEditorListener) this);
        }
    }

    public Component prepareEditor(TableCellEditor editor, int index)
    {
        Object value = columnModel.getColumn(index).getHeaderValue();
        boolean isSelected = true;
        int row = HEADER_ROW;
        Component comp = editor.getTableCellEditorComponent(getTable(), value,
                isSelected, row, index);
        if (comp instanceof JComponent)
        {
            ((JComponent) comp).setNextFocusableComponent(this);
        }
        return comp;
    }

    public TableCellEditor getCellEditor()
    {
        return cellEditor;
    }

    public Component getEditorComponent()
    {
        return editorComp;
    }

    public void setEditingColumn(int aColumn)
    {
        editingColumn = aColumn;
    }

    public int getEditingColumn()
    {
        return editingColumn;
    }

    public void removeEditor()
    {
        TableCellEditor editor = getCellEditor();
        if (editor != null)
        {
            editor.removeCellEditorListener(this);

            requestFocus();
            remove(editorComp);

            int index = getEditingColumn();
            Rectangle cellRect = getHeaderRect(index);

            setCellEditor(null);
            setEditingColumn(-1);
            editorComp = null;

            repaint(cellRect);
        }
    }

    public boolean isEditing()
    {
        return (cellEditor == null) ? false : true;
    }

    @Override
    public void editingStopped(ChangeEvent e)
    {
        TableCellEditor editor = getCellEditor();
        if (editor != null)
        {
            Object value = editor.getCellEditorValue();
            int index = getEditingColumn();
            columnModel.getColumn(index).setHeaderValue(value);
            removeEditor();
        }
    }

    @Override
    public void editingCanceled(ChangeEvent e)
    {
        removeEditor();
    }
}

class EditableHeaderUI extends BasicTableHeaderUI
{

    @Override
    protected MouseInputListener createMouseInputListener()
    {
        return new MouseInputHandler((EditableHeader) header);
    }

    public class MouseInputHandler extends BasicTableHeaderUI.MouseInputHandler
    {

        private Component dispatchComponent;
        protected EditableHeader header;

        public MouseInputHandler(EditableHeader header)
        {
            this.header = header;
        }

        private void setDispatchComponent(MouseEvent e)
        {
            Component editorComponent = header.getEditorComponent();
            Point p = e.getPoint();
            Point p2 = SwingUtilities.convertPoint(header, p, editorComponent);
            dispatchComponent = SwingUtilities.getDeepestComponentAt(
                    editorComponent, p2.x, p2.y);
        }

        private boolean repostEvent(MouseEvent e)
        {
            if (dispatchComponent == null)
            {
                return false;
            }
            MouseEvent e2 = SwingUtilities.convertMouseEvent(header, e,
                    dispatchComponent);
            dispatchComponent.dispatchEvent(e2);
            return true;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            if (!SwingUtilities.isLeftMouseButton(e))
            {
                return;
            }
            super.mousePressed(e);

            if (header.getResizingColumn() == null)
            {
                Point p = e.getPoint();
                TableColumnModel columnModel = header.getColumnModel();
                int index = columnModel.getColumnIndexAtX(p.x);
                if (index != -1)
                {
                    if (header.editCellAt(index, e))
                    {
                        setDispatchComponent(e);
                        repostEvent(e);
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            super.mouseReleased(e);
            if (!SwingUtilities.isLeftMouseButton(e))
            {
                return;
            }
            repostEvent(e);
            dispatchComponent = null;
        }
    }
}

class EditableHeaderTableColumn extends TableColumn
{

    protected TableCellEditor headerEditor;
    protected boolean isHeaderEditable;

    public EditableHeaderTableColumn()
    {
        setHeaderEditor(createDefaultHeaderEditor());
        isHeaderEditable = true;
    }

    public void setHeaderEditor(TableCellEditor headerEditor)
    {
        this.headerEditor = headerEditor;
    }

    public TableCellEditor getHeaderEditor()
    {
        return headerEditor;
    }

    public void setHeaderEditable(boolean isEditable)
    {
        isHeaderEditable = isEditable;
    }

    public boolean isHeaderEditable()
    {
        return isHeaderEditable;
    }

    public void copyValues(TableColumn base)
    {
        modelIndex = base.getModelIndex();
        identifier = base.getIdentifier();
        width = base.getWidth();
        minWidth = base.getMinWidth();
        setPreferredWidth(base.getPreferredWidth());
        maxWidth = base.getMaxWidth();
        headerRenderer = base.getHeaderRenderer();
        headerValue = base.getHeaderValue();
        cellRenderer = base.getCellRenderer();
        cellEditor = base.getCellEditor();
        isResizable = base.getResizable();
    }

    protected TableCellEditor createDefaultHeaderEditor()
    {
        return new DefaultCellEditor(new JTextField());
    }
}
