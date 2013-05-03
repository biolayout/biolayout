/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

/**
 * Dialogue for querying remote databases via web service
 * @author Derek Wright
 */
public class ImportWebServiceDialog extends JDialog implements ActionListener{
    private static final Logger logger = Logger.getLogger(ImportWebServiceDialog.class.getName());
    
    private JPanel panel;
    private JButton searchButton;
    private JButton cancelButton;
    private JTextField searchField;

    public ImportWebServiceDialog(JFrame frame, String myMessage, boolean modal) {
        super(frame, modal);
        panel = new JPanel();
        getContentPane().add(panel);
        
        panel.add(new JLabel(myMessage));

        searchButton = new JButton("Search");
        searchButton.setToolTipText("Search");     
        searchButton.addActionListener(this);
        panel.add(searchButton); 
        
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel");     
        cancelButton.addActionListener(this);
        panel.add(cancelButton);  
        
        String fieldString = "Enter a search term...";
        searchField = new JTextField(fieldString);
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                if (searchField.getText() != null
                        && searchField.getText().startsWith("Enter")) {
                    searchField.setText("");
                }
            }
        });   	
            
        JLabel searchLabel = new JLabel("Search", JLabel.RIGHT);
        searchLabel.setLabelFor(searchField);  
        
        panel.add(searchField);
        panel.add(searchLabel);
               
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(searchButton == e.getSource()) {
            logger.info("User chose Search");
            
            //ClientRequest req = new ClientRequest(PATHWAY_COMMONS_ENDPOINT);
            ClientRequest req = new ClientRequest(ImportWebService.CPATH2_ENDPOINT);
            /*
            req
                .queryParameter("version", "2.0")
                .queryParameter("q", "TP53")
                .queryParameter("format", "xml")
                .queryParameter("cmd", "search");
            */
            String searchTerm = searchField.getText();
            req
                .pathParameter("command", "search")
                .pathParameter("format", "xml")
                .queryParameter("q", searchTerm);

            try
            {
                ClientResponse<String> res = req.get(String.class);
                logger.info(res.getEntity());
            }
            catch(Exception exception)
            {
                logger.warning(exception.getMessage());
            }
        }
        else if(cancelButton == e.getSource()) {
            logger.info("User cancelled.");
            setVisible(false);
        } 
    }
}
