/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchHit;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchResponse;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

/**
 * Dialogue for querying remote databases via web service
 * @author Derek Wright
 */
public class ImportWebServiceDialog extends JDialog implements ActionListener{
    private static final Logger logger = Logger.getLogger(ImportWebServiceDialog.class.getName());
    
    private JPanel buttonPanel;
    private JButton searchButton;
    private JButton cancelButton;
    private JTextField searchField;
    private JTextField organismField;
    private DefaultTableModel model; 

    public ImportWebServiceDialog(JFrame frame, String myMessage, boolean modal) {
        
        //construct search dialog
        super(frame, modal);
        this.setTitle(myMessage);
        
        buttonPanel = new JPanel();
        
        //search button
        searchButton = new JButton("Search");
        searchButton.setToolTipText("Search");     
        searchButton.addActionListener(this);
        buttonPanel.add(searchButton); 
        
        //cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel");     
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);  

        getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
        
        JPanel fieldPanel = new JPanel();

        //search term text field
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
        
        //search term label
        JLabel searchLabel = new JLabel("Name", JLabel.RIGHT);
        searchLabel.setLabelFor(searchField);  
        
        fieldPanel.add(searchField);
        fieldPanel.add(searchLabel);
        
        //organism text field
        String organismString = "Enter NCBI ID...";
        organismField = new JTextField(organismString);
        organismField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                if (organismField.getText() != null
                        && organismField.getText().startsWith("Enter")) {
                    organismField.setText("");
                }
            }
        });   	
        
        //organism text field
        JLabel organismLabel = new JLabel("Organism", JLabel.RIGHT);
        organismLabel.setLabelFor(organismField);  
        
        fieldPanel.add(organismField);
        fieldPanel.add(organismLabel);
        
        getContentPane().add(fieldPanel, BorderLayout.PAGE_START);

        String[] colHeadings = {"Name", "Organism", "Database"};
        int numRows = 0;
        model = new DefaultTableModel(numRows, colHeadings.length) ;
        model.setColumnIdentifiers(colHeadings);
        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);
        getContentPane().add(pane, BorderLayout.CENTER);

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
            String organism = organismField.getText();
            
            req
                .pathParameter("command", "search")
                .pathParameter("format", "xml")
                .queryParameter("q", searchTerm)
                .queryParameter("organism", organism);
        
            try
            {
                ClientResponse<SearchResponse> res = req.get(SearchResponse.class);
                SearchResponse searchResponse = res.getEntity();
                
                logger.info(searchResponse.getNumHits() + " search hits");
                
                List<SearchHit> searchHits = searchResponse.getSearchHit();
                
                model.setRowCount(0); //clear previous search results
                for(SearchHit hit : searchHits)
                {
                    model.addRow(new Object[]{hit.getName(), hit.getOrganism(), hit.getDataSource()});  
                    //model.addElement(hit.getName());
                }
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
