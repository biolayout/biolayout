/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
 
/**
 *
 * @author Derek Wright
 */
public class ImportWebService {

    private static final Logger logger = Logger.getLogger(ImportWebService.class.getName());
    private LayoutFrame layoutFrame = null;
    private AbstractAction importWebServiceAction = null;
        

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
        importWebServiceAction = new AbstractAction("Network from Public Database...")
        {
            @Override
            public void actionPerformed(ActionEvent action)
            {
                logger.info("action performed");
            }
        };
                
    }
    
}
