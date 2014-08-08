package org.BioLayoutExpress3D.Network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */
public class VertexClassColorAssigner
{
    private static final ArrayList<Color> DISTINCT_COLORS = new ArrayList<Color>();
    private static final int SHIFT_VALUE = 127; // Must be prime

    static
    {
        DISTINCT_COLORS.add(new Color(0,   0,   0  )); // Black
        DISTINCT_COLORS.add(new Color(29,  105, 20 )); // Green
        DISTINCT_COLORS.add(new Color(233, 222, 187)); // Tan
        DISTINCT_COLORS.add(new Color(255, 205, 243)); // Pink
        DISTINCT_COLORS.add(new Color(41,  208, 208)); // Cyan
        DISTINCT_COLORS.add(new Color(129, 197, 122)); // Light green
        DISTINCT_COLORS.add(new Color(129, 74,  25 )); // Brown
        DISTINCT_COLORS.add(new Color(255, 255, 255)); // White
        DISTINCT_COLORS.add(new Color(42,  75,  215)); // Blue
        DISTINCT_COLORS.add(new Color(173, 35,  35 )); // Red
        DISTINCT_COLORS.add(new Color(255, 238, 51 )); // Yellow
        DISTINCT_COLORS.add(new Color(160, 160, 160)); // Light grey
        DISTINCT_COLORS.add(new Color(157, 175, 255)); // Light blue
        DISTINCT_COLORS.add(new Color(129, 38,  192)); // Purple
        DISTINCT_COLORS.add(new Color(255, 146, 51 )); // Orange
        DISTINCT_COLORS.add(new Color(87,  87,  87 )); // Dark grey
    }

    private int mod(int x, int y)
    {
        x %= y;
        return x < 0 ? x + y : x;
    }

    private Color chooseColorByInt(int i)
    {
        int numShifts = i / DISTINCT_COLORS.size();
        i %= DISTINCT_COLORS.size();
        Color color = DISTINCT_COLORS.get(i);
        int r = mod(color.getRed() + (numShifts * SHIFT_VALUE), 256);
        int g = mod(color.getGreen() + (numShifts * SHIFT_VALUE), 256);
        int b = mod(color.getBlue() + (numShifts * SHIFT_VALUE), 256);

        return new Color(r, g, b);
    }

    private HashMap<String,Color> assignedColors;

    public VertexClassColorAssigner()
    {
        reset();
    }

    public final void reset()
    {
        assignedColors = new HashMap<String,Color>();
    }

    public Color assign(String name)
    {
        if(assignedColors.containsKey(name))
        {
            return assignedColors.get(name);
        }

        String originalName = name;
        String nameWithoutDigits = "";
        int sumOfValues = 0;

        // Strip out digits and sum them
        Pattern digitsRegex = Pattern.compile("([^\\d]*)(\\d*)([^\\d]*)");
        Matcher digitsMatcher = digitsRegex.matcher(name);
        while (digitsMatcher.find())
        {
            String paddingString = "";
            String digits = digitsMatcher.group(2);

            if (!digits.isEmpty())
            {
                int value = Integer.parseInt(digits);
                sumOfValues += value;
            }

            nameWithoutDigits += digitsMatcher.group(1) + digitsMatcher.group(3);
        }

        name = nameWithoutDigits;

        // Sum the ASCII values of the non-digit characters in the name
        int hash = 0;
        for(int i = 0; i < name.length(); i++)
        {
            hash += name.charAt(i);
        }

        // Add on the sum of the numerical values
        hash += sumOfValues;
        hash -= 442; // This mysterious value is used in order to avoid having to remake existing diagrams
        hash = mod(hash, Integer.MAX_VALUE);

        Color color = chooseColorByInt(hash);
        assignedColors.put(originalName, color);

        return color;
    }
}
