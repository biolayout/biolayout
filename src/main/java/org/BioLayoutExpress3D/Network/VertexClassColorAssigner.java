package org.BioLayoutExpress3D.Network;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

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

    private int nextColorIndex;
    private int numShifts;
    private HashMap<String,Color> assignedColors;

    public VertexClassColorAssigner()
    {
        reset();
    }

    public final void reset()
    {
        nextColorIndex = 0;
        numShifts = 0;
        assignedColors = new HashMap<String,Color>();
    }

    public Color assign(String name)
    {
        if(assignedColors.containsKey(name))
        {
            return assignedColors.get(name);
        }

        Color color = DISTINCT_COLORS.get(nextColorIndex);
        int r = (color.getRed() + (numShifts * SHIFT_VALUE)) % 256;
        int g = (color.getGreen() + (numShifts * SHIFT_VALUE)) % 256;
        int b = (color.getBlue() + (numShifts * SHIFT_VALUE)) % 256;

        color = new Color(r, g, b);
        assignedColors.put(name, color);

        nextColorIndex++;
        if(nextColorIndex == DISTINCT_COLORS.size())
        {
            nextColorIndex = 0;
            numShifts++;
        }

        return color;
    }
}
