package org.BioLayoutExpress3D.Files;

import java.awt.event.*;
import java.io.*;
import java.lang.Math;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.LayoutProgressBarDialog;
import org.BioLayoutExpress3D.StaticLibraries.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;
import org.BioLayoutExpress3D.Network.Edge;
import org.BioLayoutExpress3D.Network.NetworkComponentContainer;
import org.BioLayoutExpress3D.Network.NetworkRootContainer;
import org.BioLayoutExpress3D.Network.Vertex;

/**
 * The ExportD3 class is used to export to HTML files using D3.js
 *
 * @author Tim Angus <tim.angus@roslin.ed.ac.uk>
 *
*/
public final class ExportD3
{
    private LayoutFrame layoutFrame = null;
    private JFileChooser fileChooser = null;
    private AbstractAction exportD3Action = null;
    private FileNameExtensionFilter fileNameExtensionFilterD3 = null;

    private NetworkRootContainer nc;

    public ExportD3(LayoutFrame layoutFrame)
    {
        this.layoutFrame = layoutFrame;

        initComponents();
    }

    private void initComponents()
    {
        fileNameExtensionFilterD3 = new FileNameExtensionFilter("Save as a D3.js HTML File", "html");

        String saveFilePath = FILE_CHOOSER_PATH.get().substring(
                0, FILE_CHOOSER_PATH.get().lastIndexOf(System.getProperty("file.separator")) + 1);
        fileChooser = new JFileChooser(saveFilePath);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(fileNameExtensionFilterD3);

        exportD3Action = new AbstractAction("D3.js HTML File...")
        {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                setFileChooser("Save D3.js HTML File");
                save();
            }
        };
        exportD3Action.setEnabled(false);
    }

    private void setFileChooser(String fileChooserTitle)
    {
        fileChooser.setDialogTitle(fileChooserTitle);
        fileChooser.setSelectedFile(new File(IOUtils.getPrefix(layoutFrame.getFileNameLoaded())));
    }

    public AbstractAction getExportD3Action()
    {
        return exportD3Action;
    }

    private void save()
    {
        int dialogReturnValue;
        boolean doSaveFile = true;
        File saveFile = null;

        if (fileChooser.showSaveDialog(layoutFrame) == JFileChooser.APPROVE_OPTION)
        {
            String fileExtension = fileNameExtensionFilterD3.getExtensions()[0];

            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            fileName = IOUtils.removeMultipleExtensions(fileName, fileExtension);
            saveFile = new File(fileName + "." + fileExtension);

            if (saveFile.exists())
            {
                // Do you want to overwrite
                dialogReturnValue = JOptionPane.showConfirmDialog(layoutFrame,
                        "This File Already Exists.\nDo you want to Overwrite it?",
                        "This File Already Exists. Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);

                doSaveFile = (dialogReturnValue == JOptionPane.YES_OPTION);
            }
        }

        if (doSaveFile)
        {
            // saving process on its own thread, to effectively decouple it from the main GUI thread
            Thread runLightWeightThread = new Thread(new ExportD3Process(saveFile));
            runLightWeightThread.setPriority(Thread.NORM_PRIORITY);
            runLightWeightThread.start();
        }
    }

    private void saveD3File(File saveFile)
    {
        try
        {
            String fileName = saveFile.getCanonicalPath();
            String baseSaveFileName = fileName.substring(0, fileName.lastIndexOf("."));
            nc = layoutFrame.getNetworkRootContainer();
            nc.createNetworkComponentsContainer();
            nc.sortNetworkComponentsContainerByLayoutSize();
            int maxComponentIdDigits = (int)(Math.log10(nc.getComponentCollection().size()) + 1);
            int componentId = 0;

            LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();
            layoutProgressBarDialog.prepareProgressBar(0, "Writing " + fileName);
            layoutProgressBarDialog.startProgressBar();
            for (NetworkComponentContainer ncc : nc.getComponentCollection())
            {
                int vertexId = 0;
                HashMap<Vertex, Integer> vertexNameMap = new HashMap<Vertex, Integer>();
                StringBuilder json = new StringBuilder();
                boolean first;
                json.append("{\n\t\"nodes\":[\n");

                first = true;
                for (Vertex v : ncc.getVertices())
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        json.append(",\n");
                    }

                    String name = v.getVertexName();
                    int classId = v.getVertexClass().getClassID();
                    if (name == null)
                    {
                        name = "";
                    }

                    json.append("\t\t{\"name\":\"");
                    json.append(name);
                    json.append("\",\"group\":");
                    json.append(classId);
                    json.append("}");
                    vertexNameMap.put(v, vertexId++);
                }

                json.append("\n\t],\n\t\"links\":[\n");

                first = true;
                for (Edge e : ncc.getEdges())
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        json.append(",\n");
                    }

                    int firstVertexId = vertexNameMap.get(e.getFirstVertex());
                    int secondVertexId = vertexNameMap.get(e.getSecondVertex());
                    json.append("\t\t{\"source\":");
                    json.append(firstVertexId);
                    json.append(",\"target\":");
                    json.append(secondVertexId);
                    json.append(",\"value\":");
                    json.append(e.getWeight());
                    json.append("}");
                }

                json.append("\n\t]\n}");

                String componentIdString = String.format("%0" + maxComponentIdDigits + "d", componentId);
                componentId++;

                String componentJsonFilename = baseSaveFileName + ".component." + componentIdString + ".json";
                File componentJsonFile = new File(componentJsonFilename);
                BufferedWriter bf = new BufferedWriter(new FileWriter(componentJsonFile));
                bf.write(json.toString());
                bf.close();
            }
            layoutProgressBarDialog.endProgressBar();
            layoutProgressBarDialog.stopProgressBar();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(layoutFrame, "Failed export to D3. Reason given:\n" + e.getMessage(),
                    "Export failure", JOptionPane.ERROR_MESSAGE);

            if (DEBUG_BUILD)
            {
                println("Failed to write D3 file" + e.toString());
            }
        }
    }

    private class ExportD3Process implements Runnable
    {
        private File saveFile = null;

        private ExportD3Process(File saveFile)
        {
            this.saveFile = saveFile;
        }

        @Override
        public void run()
        {
            saveD3File(saveFile);

            FILE_CHOOSER_PATH.set(saveFile.getAbsolutePath());
        }
    }
}