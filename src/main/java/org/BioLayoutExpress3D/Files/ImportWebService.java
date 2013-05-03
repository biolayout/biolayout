/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files;

import java.awt.event.ActionEvent;
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
    public static final String CPATH2_ENDPOINT = "http://www.pathwaycommons.org/pc2/{command}.{format}";
    //"http://purl.org/pc2/current/search.xml?q=Q06609" 
    //http://www.pathwaycommons.org/pc2/search.xml?q=Q06609
    
    
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
                    logger.info("action performed");
                    ImportWebServiceDialog importWebServiceDialog = new ImportWebServiceDialog(layoutFrame, "Import Network", true);


                    
                }
                catch(Exception e)
                {
                    logger.warning(e.getMessage());
                }
            }
        
        };     
    }
    
}
