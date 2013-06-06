package org.BioLayoutExpress3D.ClassViewerUI.Tables;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.BioLayoutExpress3D.ClassViewerUI.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.Network.*;

/**
*
*  The ClassViewerTable class supports initial sorting at a particular column of the table and full tooltip support for both columns and cells.
*  It is used it for all the tables of the Class Viewer.
*
* @author Thanos Theo, 2008-2009-2010-2011
* @version 3.0.0.0
*
*/

public final class ClassViewerTable extends JTable
{
    /**
    *  Serial version UID variable for the ClassViewerTable class.
    */
    public static final long serialVersionUID = 111222333444555600L;

    /**
    *  Variable to store the column names.
    */
    private String[] columnNames = null;

    /**
    *  The constructor of the ClassViewerTable class.
    */
    public ClassViewerTable(TableModel tableModel, String[] columnNames)
    {
        super(tableModel);

        this.columnNames = columnNames;
    }

    /**
    *  Implements table cell tool tips.
    */
    @Override
    public String getToolTipText(MouseEvent e)
    {
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        Object cellData = null;

        if (rowIndex >= 0 && colIndex >= 0)
        {
            cellData = getValueAt(rowIndex, colIndex);
        }
        else
        {
            System.out.println("Calling ClassViewerTable.getValueAt(" + rowIndex + ", " + colIndex + ")");
        }

        if (cellData == null)
        {
            return "";
        }

        if (cellData instanceof Color)
        {
            Color color = (Color)cellData;
            return "RGB Color value: " + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
        }
        else if (cellData instanceof Double)
        {
            double val = ( (Double)cellData ).doubleValue();
            String text = "";

            if ( (val > 1000) || (val < 0.0001) )
                text = ClassViewerFrame.EntropyTableCellRenderer.DECIMAL_FORMAT_1.format(cellData);
            else
                text = ClassViewerFrame.EntropyTableCellRenderer.DECIMAL_FORMAT_2.format(cellData);

            return text;
        }
        else if (cellData instanceof VertexClass)
        {
            VertexClass vertexClass = (VertexClass)cellData;
            return ( ( !vertexClass.getName().equals(LayoutClasses.NO_CLASS) ) ? "Class Name: " + vertexClass.getName() : LayoutClasses.NO_CLASS );
        }
        else
            return cellData.toString();
    }

    /**
    *  Implements table header tool tips.
    */
    @Override
    protected JTableHeader createDefaultTableHeader()
    {
        return new JTableHeader(columnModel)
        {
            /**
            *  Serial version UID variable for the inner class.
            */
            public static final long serialVersionUID = 111222333444555601L;

            @Override
            public String getToolTipText(MouseEvent e)
            {
                int index = columnModel.getColumnIndexAtX( e.getPoint().x );
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return columnNames[realIndex];
            }
        };
    }

    /**
    *  Updates the table's column names.
    */
    public void updateTableColumnNames(String[] columnNames)
    {
        this.columnNames = columnNames;
    }

    /**
    *  Sorts the table by a given column.
    */
    public void sortTableByColumn(int columnIndexToSort, TableRowSorter<?> sorter)
    {
        sortTableByColumn(columnIndexToSort, sorter, true);
    }

    /**
    *  Sorts the table by a given column.
    *  Overriden version that can select ascending or descending order.
    */
    public void sortTableByColumn(int columnIndexToSort, TableRowSorter<?> sorter, boolean isAscending)
    {
        RowSorter.SortKey sortKey = new RowSorter.SortKey(columnIndexToSort, (isAscending) ? SortOrder.ASCENDING : SortOrder.DESCENDING);
        ArrayList<RowSorter.SortKey> sorterList = new ArrayList<RowSorter.SortKey>(1);
        sorterList.add(sortKey);
        sorter.setSortable(columnIndexToSort, true);
        sorter.setSortKeys(sorterList);
        sorter.sort();
    }

    /**
    *  Static inner class that implements a comparator for vertex class sorting. To be used with the table.
    */
    public static class VertexClassSorting implements Comparator<VertexClass>, Serializable
    {

        /**
        *  Serial version UID variable for the VertexClassSorting class.
        */
        public static final long serialVersionUID = 111222333444555620L;

        @Override
        public int compare(VertexClass vertexClass1, VertexClass vertexClass2)
        {
            // push the NO_CLASS string at the end of a possible sorting
            return VertexClass.compare( vertexClass1.getName(), vertexClass2.getName() );
        }


    }


}