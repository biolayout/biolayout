/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import com.google.common.base.Joiner;

import gov.nih.nlm.ncbi.soap.eutils.EFetchTaxonService;
import gov.nih.nlm.ncbi.soap.eutils.EUtilsServiceSoap;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.EFetchRequest;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.EFetchResult;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.ObjectFactory;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.TaxonType;

//import gov.nih.nlm.ncbi.www.soap.eutils.EFetchTaxonServiceStub; //Apache Axis2 stub

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import net.miginfocom.swing.MigLayout;
import org.BioLayoutExpress3D.CoreUI.LayoutFrame;
import org.BioLayoutExpress3D.Environment.DataFolder;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchHit;
import org.BioLayoutExpress3D.Files.webservice.schema.SearchResponse;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;

/**
 * Dialogue for querying remote databases via web service
 * @author Derek Wright
 */
public class ImportWebServiceDialog extends JDialog implements ActionListener{
    private static final Logger logger = Logger.getLogger(ImportWebServiceDialog.class.getName());

    //web service command parameters
    public static final String FORMAT_SIF = "BINARY_SIF";
    public static final String FORMAT_BIOPAX = "BIOPAX";

    public static final String DATASOURCE_REACTOME = "reactome";
    public static final String DATASOURCE_PID = "pid";
    public static final String DATASOURCE_PHOSPHOSITEPLUS = "phosphosite"; //working???
    public static final String DATASOURCE_HUMANCYC = "humancyc";
    public static final String DATASOURCE_HPRD = "HPRD";//working???
    public static final String DATASOURCE_PANTHER = "panther";
    
    public static final String COMMAND_TOP_PATHWAYS = "top_pathways";
    public static final String COMMAND_SEARCH = "search";
    public static final String COMMAND_GET = "get";
    
    private JButton searchButton;
    private JButton cancelButton;
    private JButton nextButton, previousButton;
    private JTextField searchField;
    private JTextField organismField;
    private JComboBox networkTypeCombo;
    private DefaultTableModel model; 
    private LayoutFrame frame;
    private JRadioButton sifRadio, bioPAXRadio;
    private JLabel numHitsLabel, retrievedLabel, pagesLabel;
    private JEditorPane editorPane;
    private JCheckBox anyOrganismCheckBox, allDatasourceCheckBox;
    
    private List<SearchHit> searchHits; //retrieved search hits
    private int currentPage;
    private int totalHits; //total number of search query matches
    private ClientRequestFactory clientRequestFactory;
    private LinkedHashMap<JCheckBox, String> datasourceDisplayCommands, organismDisplayCommands;  
    private Map<String, String> organismIdNameMap; //map of NCBI name keys and scientific name values
    
    /**
     * Name of directory where files are downloaded from the web service.
     */
    public static final String DIRECTORY = "import";   
    
    /**
     * Constructor.
     * @param frame
     * @param myMessage
     * @param modal 
     */
    public ImportWebServiceDialog(LayoutFrame frame, String myMessage, boolean modal) 
    {        
        //construct search dialog
        super(); //do not attach the dialog to a parent frame so it does not stay on top
        setModal(modal);
        
        setAlwaysOnTop(false);
        
        clientRequestFactory = new ClientRequestFactory();
        clientRequestFactory.setFollowRedirects(true);
        
        organismIdNameMap = new HashMap();
        
        this.frame = frame;
        
        this.setTitle(myMessage);
        
        //search button
        searchButton = new JButton("Search");
        searchButton.setToolTipText("Search");     
        searchButton.addActionListener(this);
        getRootPane().setDefaultButton(searchButton); //searches with enter key
        
        //previous button
        previousButton = new JButton("< Previous");
        previousButton.setToolTipText("Return to previous page");     
        previousButton.setEnabled(false); //disable until get search results
        previousButton.addActionListener(this);
        
        //next button
        nextButton = new JButton("Next >");
        nextButton.setToolTipText("Next page");     
        nextButton.setEnabled(false); //disable until get search results
        nextButton.addActionListener(this);

        //cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel");     
        cancelButton.addActionListener(this);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new MigLayout());

        //search term text field
        String fieldString = "Enter a search term...";
        searchField = new JTextField(fieldString, 70);
        
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                if (searchField.getText() != null
                        && searchField.getText().startsWith("Enter")) {
                    searchField.setText("");
                }
            }
        });   	
        
        //search term label
        JLabel searchLabel = new JLabel("Keywords", JLabel.TRAILING);    
        searchLabel.setLabelFor(searchField);  
                
        //organism text field
        String organismString = ""; //default message
        organismField = new JTextField(organismString, 35);
        organismField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent focusEvent) {
                if (organismField.getText() != null
                        && organismField.getText().startsWith("Enter")) {
                    organismField.setText("");
                }
            }
        });   	
        
        //organism text field
        JLabel organismLabel = new JLabel("Organism", JLabel.TRAILING);
        organismLabel.setLabelFor(organismField);  
        
        organismDisplayCommands = new LinkedHashMap<JCheckBox, String>();
        organismDisplayCommands.put(new JCheckBox("Human"), "9606");
        organismDisplayCommands.put(new JCheckBox("Mouse"), "10090");
        organismDisplayCommands.put(new JCheckBox("Fruit Fly"), "7227");
        organismDisplayCommands.put(new JCheckBox("Rat"), "10116");
        organismDisplayCommands.put(new JCheckBox("C. elegans"), "6239");
        organismDisplayCommands.put(new JCheckBox("S. cervisiae"), "4932");
        
        anyOrganismCheckBox = new JCheckBox("Any");
        anyOrganismCheckBox.addActionListener(this);
        
        //Network Type Drop Down
        networkTypeCombo = new JComboBox();
        networkTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pathway", "Interaction", "Top Pathways" }));
        JLabel networkTypeLabel = new JLabel("Type", JLabel.TRAILING);    
        networkTypeLabel.setLabelFor(networkTypeCombo);
        
        //Map checkboxes to web service commands
        datasourceDisplayCommands = new LinkedHashMap<JCheckBox, String>();
        datasourceDisplayCommands.put(new JCheckBox("Reactome"), "reactome");
        datasourceDisplayCommands.put(new JCheckBox("NCI Nature"), "pid");
        datasourceDisplayCommands.put(new JCheckBox("PhosphoSitePlus"), "phosphosite");
        datasourceDisplayCommands.put(new JCheckBox("HumanCyc"), "humancyc");
        datasourceDisplayCommands.put(new JCheckBox("HPRD"), "hprd");
        datasourceDisplayCommands.put(new JCheckBox("PANTHER"), "panther");        
        JLabel datasourceLabel = new JLabel("Data Source", JLabel.TRAILING);
        
        allDatasourceCheckBox = new JCheckBox("All");
        allDatasourceCheckBox.addActionListener(this);
        
        //SIF/BioPax Radio Buttons
        sifRadio = new JRadioButton("SIF");
        sifRadio.setActionCommand(FORMAT_SIF);
        bioPAXRadio = new JRadioButton("BioPAX");
        bioPAXRadio.setActionCommand(FORMAT_BIOPAX);
        bioPAXRadio.setSelected(true); //default selection BioPAX
        JLabel formatLabel = new JLabel("Format", JLabel.TRAILING);    
        
        final ButtonGroup downloadOptionGroup = new ButtonGroup();
        downloadOptionGroup.add(bioPAXRadio);
        downloadOptionGroup.add(sifRadio);

        totalHits = 0;
        numHitsLabel = new JLabel("Hits: " + totalHits);

        retrievedLabel = new JLabel("Retrieved: 0");
        
        currentPage = 0;
        pagesLabel = new JLabel("Page: " + currentPage);

        /**********add form fields******************/
        
        fieldPanel.add(searchLabel, "align label");
        fieldPanel.add(searchField, "wrap, span");

        fieldPanel.add(organismLabel, "align label");
        JPanel organismPanel = new JPanel();
        organismPanel.setLayout(new BoxLayout(organismPanel, BoxLayout.LINE_AXIS));
        for(JCheckBox checkBox: organismDisplayCommands.keySet())
        {
            organismPanel.add(checkBox);
        }        
        organismPanel.add(anyOrganismCheckBox);
        fieldPanel.add(organismPanel, "wrap");
        
        //organism checkboxes
        fieldPanel.add(new JLabel(), "align label"); //dummy label for empty cell
        fieldPanel.add(organismField, "wrap, span");                
        
        fieldPanel.add(networkTypeLabel, "newline, ,align label");
        fieldPanel.add(networkTypeCombo, "wrap");

        //datasource checkboxes
        fieldPanel.add(datasourceLabel);
        JPanel datasourcePanel = new JPanel();
        datasourcePanel.setLayout(new BoxLayout(datasourcePanel, BoxLayout.LINE_AXIS));
        for(JCheckBox checkBox: datasourceDisplayCommands.keySet())
        {
           datasourcePanel.add(checkBox);
        }
        datasourcePanel.add(allDatasourceCheckBox);
        fieldPanel.add(datasourcePanel, "wrap");
       
        fieldPanel.add(formatLabel, "align label");
        
        JPanel formatPanel = new JPanel();
        formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.LINE_AXIS));
        formatPanel.add(bioPAXRadio);
        formatPanel.add(sifRadio);
        fieldPanel.add(formatPanel, "wrap");
        
        fieldPanel.add(this.searchButton, "tag ok, span, split 4, sizegroup bttn");
        fieldPanel.add(this.previousButton, "tag back, sizegroup bttn");
        fieldPanel.add(this.nextButton, "tag next, sizegroup bttn");
        fieldPanel.add(this.cancelButton, "tag cancel, sizegroup bttn");
        
        fieldPanel.setPreferredSize(new Dimension(888, 250));
        getContentPane().add(fieldPanel, BorderLayout.PAGE_START);

        JPanel hitsPanel = new JPanel();
        hitsPanel.setLayout(new MigLayout());
        hitsPanel.add(numHitsLabel, "w 33%, sizegroup hits");
        hitsPanel.add(retrievedLabel, "w 33%, sizegroup hits");
        hitsPanel.add(pagesLabel, "w 33%, sizegroup hits");
        hitsPanel.setPreferredSize(new Dimension(888, 35));
        getContentPane().add(hitsPanel, BorderLayout.PAGE_END);
        
        /**********************************************/
 
        HTMLEditorKit hed = new HTMLEditorKit();
        StyleSheet ss = hed.getStyleSheet();
        ss.addRule("b {color : blue;}");
        ss.addRule(".hitHL {color : green; font-weight : bold}");
        Document doc = hed.createDefaultDocument();
        
        //Search hit excerpt
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setEditorKit(hed);
        editorPane.setDocument(doc);
        
        //open system web browser on hyperlink click
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if(Desktop.isDesktopSupported()) {
                        try
                        {
                            Desktop.getDesktop().browse(e.getURL().toURI());                            
                        }
                        catch(Exception exception)
                        {
                            logger.warning("Cannot open web browser: " + exception.getMessage()); //TODO error alert
                        }
                    }
                }
            }
        });
        
        String text = "<b>Excerpt:</b>";
        editorPane.setText(text);
        
        String[] colHeadings = {"Name", "Organism", "Database", "BioPAX Class"};
        int numRows = 0;
        
        model = new DefaultTableModel(numRows, colHeadings.length) 
        {
            @Override
            public boolean isCellEditable(int row, int column) 
            {
               //all cells false
               return false;
            }
        };
        
        model.setColumnIdentifiers(colHeadings);
        
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(Color.BLUE);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        ListSelectionModel rowSelectionModel = table.getSelectionModel();
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    SearchHit hit = searchHits.get(selectedRow);
                    
                    //construct HTML snippet of organism scientific names
                    List<String> organismIdList = hit.getOrganism();
                    String organismHTML = "<b>Organism:</b>";
                    for(String organismString : organismIdList)
                    {
                        String ncbiId = organismString.substring(organismString.lastIndexOf("/")+1, organismString.length());
                        String scientificName = organismIdNameMap.get(ncbiId);
                        organismHTML = organismHTML 
                                + "<br />" 
                                + "<a href='" + organismString + "'>" + scientificName + "</a>";
                    }
                    
                    editorPane.setText("<b>Excerpt:</b><br />" 
                            + hit.getExcerpt() 
                            + "<br>" 
                            + organismHTML);
                }
            }
        });        

        //center align header and cell contents        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment( JLabel.CENTER );
        headerRenderer.setBackground(Color.LIGHT_GRAY);
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(headerRenderer);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        for (int column = 1; column < table.getColumnCount(); ++column) //align columns 1-3
        {
            TableColumn tc = table.getColumnModel().getColumn(column);
            tc.setCellRenderer(centerRenderer);        
        }
        
        //size column width to fit biggest cell
        /* 
        int column = 3;
        int width = 0;
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max (comp.getPreferredSize().width, width);
        }
        TableColumn tc = table.getColumnModel().getColumn(column);
        tc.setPreferredWidth(width);
*/
        
        final LayoutFrame localFrame = frame; //local reference can be referenced from inner class
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) 
            {
               if (e.getClickCount() == 2) 
               {
                    JTable target = (JTable)e.getSource();
                    int viewRow = target.getSelectedRow();
                    int modelRow = target.convertRowIndexToModel(viewRow);
                    //int column = target.getSelectedColumn();
                    logger.info("Mouse double clicked on table view row " + viewRow);
                    logger.info("Mouse double clicked on table model row " + modelRow);

                    SearchHit hit = searchHits.get(modelRow); //get SearchHit that relates to values in table model row (converted from sorted view row index)
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
                    
                    String hitName = target.getModel().getValueAt(modelRow, 0).toString(); //search hit name
                    String fileName = hitName + fileExtension; //name of .owl or .sif file to be created

                    ClientRequest req = clientRequestFactory.createRequest(ImportWebService.CPATH2_ENDPOINT);
                    req
                        .pathParameter("command", COMMAND_GET)
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
                        
                        localFrame.requestFocus();
                        localFrame.toFront();                        
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
        
        //split search results on left and highlighted search hit excerpt on right
        JScrollPane scrollPane = new JScrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, editorPane);
        getContentPane().add(splitPane ,BorderLayout.CENTER);
                
        pack();
        splitPane.setDividerLocation(0.75);
        
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) 
    {
        if(searchButton == e.getSource() || nextButton == e.getSource() || previousButton == e.getSource()) 
        {
            
            //TODO previous/next should remember last search params in case user changes
            String networkType = this.networkTypeCombo.getSelectedItem().toString();

            ClientRequest req = clientRequestFactory.createRequest(ImportWebService.CPATH2_ENDPOINT_SEARCH);
            if(networkType.equals("Top Pathways"))
            {
                req
                    .pathParameter("command", COMMAND_TOP_PATHWAYS)
                    .pathParameter("format", "xml");
            }
            else
            {
                String searchTerm = searchField.getText();
                String organism = organismField.getText();

                req
                    .pathParameter("command", COMMAND_SEARCH)
                    .pathParameter("format", "xml")
                    .queryParameter("q", searchTerm)
                    .queryParameter("type", networkType);

                //TODO muliple organisms with separator?
                if(!organism.equals(""))
                {
                    req.queryParameter("organism", organism);
                }
                
                //page to retrieve
                int pageParameter = currentPage;
                if(nextButton == e.getSource())
                {
                   pageParameter++;
                }
                else if(previousButton == e.getSource())
                {
                    pageParameter--;
                }
                req.queryParameter("page", pageParameter);

                //add parameters for datasource checkboxes
                if(!allDatasourceCheckBox.isSelected())
                {
                    for (Map.Entry<JCheckBox, String> entry : datasourceDisplayCommands.entrySet()) {
                         JCheckBox checkBox = entry.getKey();
                         if(checkBox.isSelected())
                         {
                             String datasourceParameter = entry.getValue();
                             req.queryParameter("datasource", datasourceParameter);                        
                         }
                    }            
                }

                //add parameters for organism checkboxes
                if(!anyOrganismCheckBox.isSelected()) //don't add organism parameters if Any is selected
                {
                    for (Map.Entry<JCheckBox, String> entry : organismDisplayCommands.entrySet()) {
                         JCheckBox checkBox = entry.getKey();
                         if(checkBox.isSelected())
                         {
                             String organismParameter = entry.getValue();
                             req.queryParameter("organism", organismParameter);                        
                         }
                    }
                }
            }

            //perform search and display search response
            try
            {
                ClientResponse<SearchResponse> res = req.get(SearchResponse.class); //TODO progress bar //TODO ErrorResponse
                SearchResponse searchResponse = res.getEntity();
                
                totalHits = searchResponse.getNumHits();
                numHitsLabel.setText("Hits: " + totalHits);

                searchHits = searchResponse.getSearchHit();
                int numRetrieved = searchHits.size();
                retrievedLabel.setText("Retrieved: " + numRetrieved);
                
                int maxHitsPerPage = searchResponse.getMaxHitsPerPage();
                
                //display current page number
                currentPage = searchResponse.getPageNo();
                pagesLabel.setText("Page: " + currentPage);
                
                //INT[(numHits-1)/numHitsPerPage+1].
                //totalPages = 
                
                //enable/disable previous button
                if(currentPage > 0) //on first page, disable previous
                {
                    previousButton.setEnabled(true);
                }
                else
                {
                    previousButton.setEnabled(false);
                }

                //enable/disable next button
                int numPages = (totalHits + maxHitsPerPage - 1) / maxHitsPerPage; //calculate number of pages, round up integer division
                if((currentPage + 1) < numPages) //pages indexed from zero
                {
                    nextButton.setEnabled(true);
                }
                else
                {
                    nextButton.setEnabled(false);
                }
                
                model.setRowCount(0); //clear previous search results
                
                //map of database uri substrings to display names 
                Map<String, String> databaseUriDisplay = new HashMap<String, String>();
                databaseUriDisplay.put("reactome", "Reactome");
                databaseUriDisplay.put("pid", "NCI Nature");
                databaseUriDisplay.put("psp", "PhosphoSitePlus");
                databaseUriDisplay.put("humancyc", "HumanCyc");
                databaseUriDisplay.put("hprd", "HPRD");
                databaseUriDisplay.put("panther", "PANTHER");

                Joiner joiner = Joiner.on(',').skipNulls();
                
                for(SearchHit hit : searchHits)
                {
                    List<String> organismList = hit.getOrganism(); //URIs of organisms at identifiers.org
                    
                    //extract organism ID for each organism URI
                    String[] organismArray = organismList.toArray(new String[0]);
                    for (int i = 0; i < organismArray.length; i++)
                    {
                        String organismString = organismArray[i];
                        organismArray[i] = organismString.substring(organismString.lastIndexOf("/")+1, organismString.length());
                        //add to NCBI ID/name map for later web service lookup if not already added
                        if(!organismIdNameMap.containsKey(organismArray[i]))
                        {
                            organismIdNameMap.put(organismArray[i], organismArray[i]); //value also NCBI ID as placeholder - to be replaced with name from web service
                        }
                    }
                    
                    //display comma-separated list of organisms
                    String joinedOrganisms = joiner.join(organismArray);
                    
                    //display datasource
                    List<String> databases = hit.getDataSource();
                    String[] databaseArray = databases.toArray(new String[0]);
                    for(int i = 0; i < databaseArray.length; i++)
                    {
                        String databaseUri = databaseArray[i];
                        
                        //replace with database real name if found in map
                        for (Map.Entry<String, String> entry : databaseUriDisplay.entrySet()) 
                        {
                            String databaseString = entry.getKey();
                            if(databaseUri.contains(databaseString))
                            {
                                databaseArray[i] = entry.getValue();
                            }
                        }
                    }
                    String joinedDatabases = joiner.join(databaseArray);
                    
                    //display BioPAX Class
                    
                    model.addRow(new Object[]{hit.getName(), joinedOrganisms, joinedDatabases, hit.getBiopaxClass()});  
                }
                
                if(this.organismIdNameMap.size() > 0)
                {
                    //populate organism scientific names from NCBI web service
                    EFetchTaxonService service = new EFetchTaxonService();
                    EUtilsServiceSoap serviceSoap = service.getEUtilsServiceSoap();
                    ObjectFactory objectFactory = new ObjectFactory();
                    EFetchRequest requ = objectFactory.createEFetchRequest();
                    String eFetchQuery = joiner.join(this.organismIdNameMap.keySet()); //comma-separated organism IDs
                    requ.setId(eFetchQuery);
                    EFetchResult resp = serviceSoap.runEFetch(requ);
                    logger.info("EFetchResult: " + resp.getTaxaSet().getTaxon().size() + " Taxa");
                    List<TaxonType> taxon = resp.getTaxaSet().getTaxon();
                    for(TaxonType taxonType : taxon)
                    {
                        organismIdNameMap.put(taxonType.getTaxId(), taxonType.getScientificName());
                    }
                }
            }
            catch(Exception exception) //TODO ClientResponseFailure - display error dialog
            {
                logger.warning(exception.getMessage());
            }
        }
        else if(cancelButton == e.getSource()) {
            logger.info("User cancelled.");
            setVisible(false);
        }
        else if(anyOrganismCheckBox == e.getSource()) //"Any" organism checkbox has been checked or unchecked
        {
            //enable/disable organism checkboxes
            for(JCheckBox checkBox: organismDisplayCommands.keySet())
            {
                if(anyOrganismCheckBox.isSelected())
                {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                }
                else
                {
                    checkBox.setSelected(false);
                    checkBox.setEnabled(true);
                }
            }
            
            //enable/disable organism text field
            if(anyOrganismCheckBox.isSelected())
            {
                organismField.setText("");
                organismField.setEnabled(false);
            }
            else
            {
                organismField.setEnabled(true);
            }
        }
        else if(allDatasourceCheckBox == e.getSource())
        {
            for(JCheckBox checkBox: datasourceDisplayCommands.keySet())
            {
                if(allDatasourceCheckBox.isSelected())
                {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                }
                else
                {
                    checkBox.setSelected(false);
                    checkBox.setEnabled(true);
                }
            }
        }
    }
    
    /**
     * Sets the preferred width of the visible column specified by vColIndex. The column
     * will be just wide enough to show the column head and the widest cell in the column.
     * margin pixels are added to the left and right
     * (resulting in an additional width of 2*margin pixels).
     */ 
    public static void packColumn(JTable table, int vColIndex, int margin) 
    {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
        TableColumn col = colModel.getColumn(vColIndex);
        int width = 0;

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        java.awt.Component comp = renderer.getTableCellRendererComponent(
            table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int r=0; r<table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, vColIndex);
            comp = renderer.getTableCellRendererComponent(
                table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += 2*margin;

        // Set the width
        col.setPreferredWidth(width);
    }    
}
