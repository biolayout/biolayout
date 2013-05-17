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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
import org.BioLayoutExpress3D.Environment.DataFolder;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchHit;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchResponse;
import org.apache.commons.io.FileUtils;
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
    private LayoutFrame frame;

    public ImportWebServiceDialog(LayoutFrame frame, String myMessage, boolean modal) {
        
        //construct search dialog
        super(frame, modal);
        
        this.frame = frame;
        
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

        String[] colHeadings = {"Name", "Organism", "Database", "URI"};
        int numRows = 0;
        
        model = new DefaultTableModel(numRows, colHeadings.length) {

            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
        
        model.setColumnIdentifiers(colHeadings);
        
        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);
        getContentPane().add(pane, BorderLayout.CENTER);
        
        final LayoutFrame localFrame = frame;
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) 
            {
               if (e.getClickCount() == 2) 
               {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    logger.info("Mouse double clicked on table row " + row + " column " + column);

                    String uriString = target.getModel().getValueAt(row, column).toString();
                    logger.info("URI is " + uriString);

                    ClientRequest req = new ClientRequest(ImportWebService.CPATH2_ENDPOINT);
                    req
                        .pathParameter("command", "get")
                        .queryParameter("uri", uriString)
                        .queryParameter("format", "BINARY_SIF"); //TEST - user to select
                    try
                    {
                        ClientResponse<String> res = req.get(String.class);
                        String responseString = res.getEntity();
                        logger.info(res.getEntity());
                        logger.info("DataFolder: " + DataFolder.get());
                        
                        //File testFile = new File("/Users/dwright8/Desktop/get.sif"); //TEST
                        File testFile = new File(DataFolder.get(), "get.sif");
                        logger.info("Writing to file " + testFile);
                        FileUtils.writeStringToFile(testFile, responseString);

                        logger.info("Opening file " + testFile);
                        ImportWebServiceDialog.this.setVisible(false);
                        localFrame.loadDataSet(testFile);
                    }
                    catch(Exception exception)
                    {
                        logger.warning("Could not retrieve SIF from Pathway Commons CPath2");
                        logger.warning(exception.getMessage());
                    }
               }
            }
         });

        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(searchButton == e.getSource()) {
            logger.info("User chose Search");
            
            ClientRequest req = new ClientRequest(ImportWebService.CPATH2_ENDPOINT_SEARCH);
            String searchTerm = searchField.getText();
            String organism = organismField.getText();
            
            req
                .pathParameter("command", "search")
                .pathParameter("format", "xml")
                .queryParameter("q", searchTerm)
                .queryParameter("organism", organism)
                .queryParameter("datasource", "reactome")
                .queryParameter("type", "BiochemicalReaction");
        
            try
            {
                ClientResponse<SearchResponse> res = req.get(SearchResponse.class);
                SearchResponse searchResponse = res.getEntity();
                
                logger.info(searchResponse.getNumHits() + " search hits");
                
                List<SearchHit> searchHits = searchResponse.getSearchHit();
                
                model.setRowCount(0); //clear previous search results
                for(SearchHit hit : searchHits)
                {
                    model.addRow(new Object[]{hit.getName(), hit.getOrganism(), hit.getDataSource(), hit.getUri()});  
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
