package org.Kajeka.Utils;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.text.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;

/**
*
* The FloatNumberField class is a JTextField customized for float input/output.
*
* @author Full refactoring by Thanos Theo, 2008-2009
* @version 3.0.0.0
*
*/

public final class FloatNumberField extends JTextField
{
    /**
    *  Serial version UID variable for the FloatNumberField class.
    */
    public static final long serialVersionUID = 111222333444555778L;

    private NumberFormat numberFormatter = null;

    public FloatNumberField(float value, int columns)
    {
        super(columns);

        numberFormatter = NumberFormat.getInstance();
        numberFormatter.setParseIntegerOnly(false);
        numberFormatter.setGroupingUsed(false);
        numberFormatter.setMaximumFractionDigits(2);

        setValue(value);
    }

    public void setParseIntegerOnly(boolean flag)
    {
        numberFormatter.setParseIntegerOnly(flag);
    }

    public void setMaximumFractionDigits(int n)
    {
        numberFormatter.setMaximumFractionDigits(n);
    }

    public boolean isEmpty()
    {
        return getText().isEmpty();
    }

    public float getValue()
    {
        try
        {
            String text = getText();
            Number number = numberFormatter.parse(text);
            return number.floatValue();
        }
        catch (ParseException parseExc)
        {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.

            if (DEBUG_BUILD) println("FloatNumberField.getValue() ParseException:\n" + parseExc.getMessage());

            Toolkit.getDefaultToolkit().beep();

            return 0.0f;
        }
    }

    public void setValue(float value)
    {
        String s = numberFormatter.format(value);
        setText(s);
    }

    @Override
    protected Document createDefaultModel()
    {
        return new FloatNumberDocument();
    }

    private static class FloatNumberDocument extends PlainDocument
    {
        /**
        *  Serial version UID variable for the WholeNumberDocument class.
        */
        public static final long serialVersionUID = 111222333444555779L;

        @Override
        public void insertString(int offset, String str, AttributeSet attrib) throws BadLocationException
        {
            if ( (str == null) || ( str.isEmpty() ) )
                return;

            char[] source = str.toCharArray();
            char[] result = new char[source.length];

            int j = 0;
            for (int i = 0; i < result.length; i++)
                if ( Character.isDigit(source[i]) || (source[i] == DECIMAL_SEPARATOR_CHARACTER) )
                    result[j++] = source[i];

            super.insertString(offset, new String(result, 0, j), attrib);
        }


    }


}