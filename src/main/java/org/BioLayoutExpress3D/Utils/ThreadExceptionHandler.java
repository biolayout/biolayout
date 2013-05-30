package org.BioLayoutExpress3D.Utils;

/**
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 */

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.BioLayoutExpress3D.BuildConfig;
import org.BioLayoutExpress3D.Utils.Path;
import org.BioLayoutExpress3D.Environment.DataFolder;

public class ThreadExceptionHandler implements
        Thread.UncaughtExceptionHandler
{
    // If the exception occurs in the EDT, calling JOptionPane.showMessageDialog
    // can cause a repaint and another throw, so we have this guard to avoid that
    // case or others like it
    boolean handlingThreadException = false;

    @Override
    public void uncaughtException(Thread thread, Throwable e)
    {
        if (!handlingThreadException)
        {
            handlingThreadException = true;

            String build = "Build: " + BuildConfig.VERSION +
                    (!BuildConfig.BUILD_TAG.isEmpty() ? " " + BuildConfig.BUILD_TAG : "") +
                    "(" + BuildConfig.BUILD_TIME + ")";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

            String logText = build + "\n" + timeStamp +
                    ": Exception \"" + e.toString() + "\" occurred in thread ID " +
                    thread.getId() + "(" + thread.getName() + ")\n" + stackTraceForThrowable(e);

            System.out.println(logText);

            try
            {
                String dataFolder = DataFolder.get();
                String exceptionLogFileName = Path.combine(dataFolder, "UncaughtExceptions.txt");
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(exceptionLogFileName, true)));
                out.println(logText);
                out.close();
            }
            catch (IOException ioe)
            {
            }

            JOptionPane.showMessageDialog(null,
                    "Exception \"" + e.toString() + "\" occurred in thread ID " +
                    thread.getId() + "(" + thread.getName() + "):\n\n" +
                    stackTraceForThrowable(e, 5) + "...",
                    "Thread exception", JOptionPane.ERROR_MESSAGE);
        }

        handlingThreadException = false;
    }

    private String stackTraceForThrowable(Throwable e)
    {
        return stackTraceForThrowable(e, -1);
    }

    private String stackTraceForThrowable(Throwable e, int numFrames)
    {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement stackTraceElement : e.getStackTrace())
        {
            if (numFrames == 0)
            {
                break;
            }
            else if (numFrames > 0)
            {
                numFrames--;
            }

            sb.append(stackTraceElement.toString()).append("\n");
        }

        return sb.toString();
    }
}