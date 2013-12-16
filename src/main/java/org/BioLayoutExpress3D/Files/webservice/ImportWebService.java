/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
 
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
    
    /*
    public static final String CPATH2_ENDPOINT = "http://purl.org/pc2/current/{command}"; //general command endpoint
    public static final String CPATH2_ENDPOINT_SEARCH = "http://purl.org/pc2/current/{command}.{format}"; //endpoint to specify search results format (xml/json)
    */
    
    //public static final String CPATH2_ENDPOINT = "http://webservice.baderlab.org:48080/{command}"; //general command endpoint
    //public static final String CPATH2_ENDPOINT_SEARCH = "http://webservice.baderlab.org:48080/{command}.{format}"; //endpoint to specify search results format (xml/json)
    
    //"http://purl.org/pc2/current/search.xml?q=Q06609" 
    //http://www.pathwaycommons.org/pc2/search.xml?q=Q06609
    //http://purl.org/pc2/current/
    //http://webservice.baderlab.org:48080/search.xml?q=Q06609
    //http://webservice.baderlab.org:48080
    
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
        importWebServiceAction = new AbstractAction("Network from Public Database...") //submenu item text
        {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                try
                {
                    logger.info("action performed: " + importWebServiceAction.toString());
                    if(importWebServiceDialog == null || !importWebServiceDialog.isVisible())
                    {
                        importWebServiceDialog = new ImportWebServiceDialog(layoutFrame, "Import Network", false);
                    }
                    else
                    {
                        importWebServiceDialog.requestFocus();
                        importWebServiceDialog.toFront();
                    }
                }
                catch(Exception e)
                {
                    logger.warning(e.getMessage());
                }
            }
        };  
    }
}
