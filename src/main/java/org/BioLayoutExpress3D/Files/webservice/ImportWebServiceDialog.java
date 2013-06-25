/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import com.google.common.base.Joiner;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
    private JRadioButton sifRadio, bioPAXRadio;
    private List<SearchHit> searchHits;
    
    /**
     * Name of directory where files are downloaded from the web service.
     */
    public static final String DIRECTORY = "import";

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
        JLabel searchLabel = new JLabel("Keywords", JLabel.LEFT);
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
        JLabel organismLabel = new JLabel("Organism", JLabel.LEFT);
        organismLabel.setLabelFor(organismField);  
        
        fieldPanel.add(organismField);
        fieldPanel.add(organismLabel);
        
        //web service format parameters - set as action command
        String sifFormat = "BINARY_SIF";
        String bioPAXFormat = "BIOPAX";
        
        sifRadio = new JRadioButton("SIF");
        sifRadio.setActionCommand(sifFormat);
        bioPAXRadio = new JRadioButton("BioPAX");
        bioPAXRadio.setActionCommand(bioPAXFormat);
        bioPAXRadio.setSelected(true); //default selection BioPAX
        
        final ButtonGroup downloadOptionGroup = new ButtonGroup();
        downloadOptionGroup.add(bioPAXRadio);
        downloadOptionGroup.add(sifRadio);

        fieldPanel.add(bioPAXRadio);
        fieldPanel.add(sifRadio);
        
        getContentPane().add(fieldPanel, BorderLayout.PAGE_START);

        String[] colHeadings = {"Name", "Organism", "Database"};
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

                    SearchHit hit = searchHits.get(row); //get SearchHit that relates to values in table row
                    String uriString = hit.getUri();
                    logger.info("URI is " + uriString);
                    
                    //download option - get format parameter from radio button actionPerformed
                    JRadioButton formatRadio;
                    String fileExtension;
                    if(sifRadio.isSelected())
                    {
                        formatRadio = sifRadio;
                        fileExtension = ".sif";
                    }
                    else
                    {
                        formatRadio = bioPAXRadio;
                        fileExtension = ".owl";
                    }
                    String formatParameter = formatRadio.getActionCommand();
                    
                    String hitName = target.getModel().getValueAt(row, 0).toString(); //search hit name
                    String fileName = hitName + fileExtension; //name of .owl or .sif file to be created

                    ClientRequest req = new ClientRequest(ImportWebService.CPATH2_ENDPOINT);
                    req
                        .pathParameter("command", "get")
                        .queryParameter("uri", uriString)
                        .queryParameter("format", formatParameter);
                    try
                    {
                        ClientResponse<String> res = req.get(String.class);
                        String responseString = res.getEntity();
                        logger.info(res.getEntity());
                        logger.info("DataFolder: " + DataFolder.get());
                        
                        //create directory to store downloaded file
                        File importDir = new File(DataFolder.get(), DIRECTORY);
                        if(!importDir.exists())
                        {
                            importDir.mkdir();
                        }
                        
                        //create file and save web service data
                        File importFile = new File(importDir, fileName);
                        logger.info("Writing to file " + importFile);
                        FileUtils.writeStringToFile(importFile, responseString);

                        //display file
                        logger.info("Opening file " + importFile);
                        ImportWebServiceDialog.this.setVisible(false);
                        localFrame.loadDataSet(importFile);
                    }
                    catch(Exception exception)
                    {
                        logger.warning("Could not retrieve " + fileName + " from Pathway Commons CPath2");
                        logger.warning(exception.getMessage());
                    }
               }
            }
         });

        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) 
    {
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
                .queryParameter("datasource", "reactome")  //TODO OPTION
                .queryParameter("type", "Pathway"); //TODO OPTION
        
            try
            {
                ClientResponse<SearchResponse> res = req.get(SearchResponse.class);
                SearchResponse searchResponse = res.getEntity();
                
                logger.info(searchResponse.getNumHits() + " search hits");
                
                searchHits = searchResponse.getSearchHit();
                
                model.setRowCount(0); //clear previous search results
                
                for(SearchHit hit : searchHits)
                {
                    List<String> organismList = hit.getOrganism(); //URIs of organisms at identifiers.org
                    
                    //extract organism ID for each organism URI
                    String[] organismArray = organismList.toArray(new String[0]);
                    for (int i = 0; i < organismArray.length; i++)
                    {
                        String organismString = organismArray[i];
                        organismArray[i] = organismString.substring(organismString.lastIndexOf("/")+1, organismString.length());
                    }
                    
                    //display comma-separated list of organisms
                    Joiner joiner = Joiner.on(',').skipNulls();
                    String joinedOrganisms = joiner.join(organismArray);
                    
                    model.addRow(new Object[]{hit.getName(), joinedOrganisms, hit.getDataSource()});  
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
