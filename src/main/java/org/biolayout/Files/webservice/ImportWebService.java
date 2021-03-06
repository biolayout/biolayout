/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biolayout.Files.webservice;

import com.google.common.base.Joiner;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.biolayout.CoreUI.LayoutFrame;
import org.biolayout.Graph.GraphElements.GraphNode;
 
/**
 *
 * @author Derek Wright
 */
public class ImportWebService {

    private static final Logger logger = Logger.getLogger(ImportWebService.class.getName());
    private LayoutFrame layoutFrame = null;
    private AbstractAction importWebServiceAction = null;
        
    public static final String PATHWAY_COMMONS_ENDPOINT = "http://www.pathwaycommons.org/pc/webservice.do";
    
    public static final String CPATH2_ENDPOINT = "http://www.pathwaycommons.org/pc2/{command}"; //general command endpoint
    public static final String CPATH2_ENDPOINT_SEARCH = "http://www.pathwaycommons.org/pc2/{command}.{format}"; //endpoint to specify search results format (xml/json)
    
    /**
     * Singleton instance of dialog.
     */
    private static ImportWebServiceDialog importWebServiceDialog;
    
    public ImportWebService(LayoutFrame layoutFrame)
    {
        this.layoutFrame = layoutFrame;
        initComponents();
    }
    
    public AbstractAction getImportWebServiceAction() {
        return importWebServiceAction;
    }
 
    private void initComponents()
    {
        importWebServiceAction = new AbstractAction("Search Pathway Commons...") //submenu item text
        {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                try
                {
                    if(importWebServiceDialog == null || !importWebServiceDialog.isVisible())
                    {
                        importWebServiceDialog = new ImportWebServiceDialog(layoutFrame, "Import Network", false);
                    }
                    else
                    {
                        importWebServiceDialog.requestFocus();
                        importWebServiceDialog.toFront();
                    }
                    String searchString = "";       
                    
                    //populate search field with selected node names
                    Set<GraphNode> selectedNodes = layoutFrame.getGraph().getSelectionManager().getSelectedNodes();
                    int size = selectedNodes.size();
                    if(size > 0)
                    {
                        String[] selectedNodeNames = new String[size];

                        int i = 0;
                        for(GraphNode graphNode : selectedNodes)
                        {
                            //put quotes round node name
                            String name = graphNode.getNodeName();
                            if(name != null && !name.isEmpty())
                            {
                                name = '"' + name + '"';
                            }
                            selectedNodeNames[i] = name;
                            i++;
                        }

                        //create AND-separated search string
                        Joiner andJoiner = Joiner.on(" AND ").skipNulls(); 
                        searchString = andJoiner.join(selectedNodeNames);
                    }
                    importWebServiceDialog.getSearchField().setText(searchString);
                }
                catch(Exception e)
                {
                    logger.warning(e.getMessage());
                }
            }
        };  
    }
}
