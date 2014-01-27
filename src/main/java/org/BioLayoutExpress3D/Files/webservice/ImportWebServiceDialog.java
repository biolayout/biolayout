/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.BioLayoutExpress3D.Files.webservice;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.query.CPathGetQuery;
import cpath.query.CPathQuery;
import cpath.query.CPathSearchQuery;
import cpath.query.CPathTraverseQuery;
import cpath.service.GraphType;
import cpath.service.OutputFormat;
import cpath.service.jaxb.SearchHit;
import cpath.service.jaxb.SearchResponse;
import cpath.service.jaxb.TraverseEntry;
import cpath.service.jaxb.TraverseResponse;

import gov.nih.nlm.ncbi.soap.eutils.EFetchTaxonService;
import gov.nih.nlm.ncbi.soap.eutils.EUtilsServiceSoap;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.EFetchRequest;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.EFetchResult;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.ObjectFactory;
import gov.nih.nlm.ncbi.soap.eutils.efetch_taxonomy.TaxonType;

//import gov.nih.nlm.ncbi.www.soap.eutils.EFetchTaxonServiceStub; //Apache Axis2 stub
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
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
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ClientResponse;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Dialogue for searchQuerying remote databases via web service
 * @author Derek Wright
 */
public class ImportWebServiceDialog extends JDialog implements ActionListener{
    private static final Logger logger = Logger.getLogger(ImportWebServiceDialog.class.getName());

    //web service command parameters
    public static final String FORMAT_SIF = "BINARY_SIF";
    public static final String FORMAT_BIOPAX = "BIOPAX";

    public static final String DATASOURCE_REACTOME = "reactome";
    public static final String DATASOURCE_PID = "pid";
    public static final String DATASOURCE_PHOSPHOSITEPLUS = "phosphosite"; 
    public static final String DATASOURCE_HUMANCYC = "humancyc";
    public static final String DATASOURCE_HPRD = "HPRD";
    public static final String DATASOURCE_PANTHER = "panther";
    
    public static final String COMMAND_TOP_PATHWAYS = "top_pathways";
    public static final String COMMAND_SEARCH = "search";
    public static final String COMMAND_GET = "get";
    
    //timeouts for web service operations in seconds
    public static final int TIMEOUT_SEARCH = 30;
    public static final int TIMEOUT_GET = 60;
    
    private JButton searchButton, cancelButton,nextButton, previousButton, stopButton, openButton;
    private JTextField searchField, organismField;
    private JComboBox<String> networkTypeCombo;
    private String networkType = ""; //stores selected value of networkTypeCombo when search is run
    private DefaultTableModel model; 
    private LayoutFrame frame;
    private JLabel numHitsLabel, retrievedLabel, pagesLabel, statusLabel;
    private JEditorPane editorPane;
    private JCheckBox anyOrganismCheckBox, allDatasourceCheckBox, nameCheckBox;
    private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private JTable table; //search results table
  
    private List<SearchHit> searchHits; //retrieved search hits
    private int currentPage;
    private int maxHitsPerPage;
    private int totalHits; //total number of search searchQuery matches
    private ClientRequestFactory clientRequestFactory;
    private LinkedHashMap<JCheckBox, String> datasourceDisplayCommands, organismDisplayCommands;  
    //private Map<String, String> organismIdNameMap; //map of NCBI name keys and scientific name values
    private Map <SearchHit, Integer> hitInteractionCountMap; //map of search hitd to the number of interactions
    /*
    private static final Map<String, String> DATABASE_URI_DISPLAY = new HashMap<String, String>(); //map of database URI to display name for search results
    static
    {
        DATABASE_URI_DISPLAY.put("reactome", "Reactome");
        DATABASE_URI_DISPLAY.put("pid", "NCI Nature");
        DATABASE_URI_DISPLAY.put("psp", "PhosphoSitePlus");
        DATABASE_URI_DISPLAY.put("humancyc", "HumanCyc");
        DATABASE_URI_DISPLAY.put("hprd", "HPRD");
        DATABASE_URI_DISPLAY.put("panther", "PANTHER");
    }
    */
    
    /**
     * Maps search hit URI of database to display name. Map contents are immutable.
     */
    public static final Map<String, String> DATABASE_URI_DISPLAY = ImmutableMap.<String, String>builder()
        .put("reactome", "Reactome")
        .put("pid", "NCI Nature")
        .put("psp", "PhosphoSitePlus")
        .put("humancyc", "HumanCyc")
        .put("hprd", "HPRD")
        .put("panther", "PANTHER")
        .build();

    /**
     * Map of NCBI organism ID to species name. Not immutable so we can add new species from NCBI web service. 
     * Common species hard coded to avoid unnecessary web service calls. Map is final but contents are not!
     */
    private static final Map<String, String> organismIdNameMap = new HashMap<String, String>();
    static
    {
        organismIdNameMap.put("9606", "Homo sapiens");
        organismIdNameMap.put("11676", "Human immunodeficiency virus 1");
        organismIdNameMap.put("10090", "Mus musculus");
        organismIdNameMap.put("10116", "Rattus norvegicus");
    }

    private SearchWorker searchWorker = null; //search operation concurrent task runner
    private GetWorker getWorker = null; //GET operation concurrent task runner
    private CPathQuery<SearchResponse> searchQuery; //query for top pathways and search
    private CPathQuery getQuery;
    private ClientResponse<String> getClientResponse; //web service response containing GET results (BioPAX document)
    
    private static final Joiner commaJoiner = Joiner.on(',').skipNulls(); //for creating comma-separated strings

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
        
        //organismIdNameMap = new HashMap<String, String>();
        hitInteractionCountMap = new HashMap<SearchHit, Integer>();
        
        this.frame = frame;
        this.setTitle(myMessage);
        
        //search button
        searchButton = this.createJButton("Search", "Search", true);
        getRootPane().setDefaultButton(searchButton); //searches with enter key
        
        previousButton = this.createJButton("< Previous", "Return to previous page", false); //previous button
        nextButton = this.createJButton("Next >", "Next page", false); //next button
        cancelButton = this.createJButton("Cancel", "Close dialog", true); //cancel button
        stopButton = this.createJButton("Stop", "Stop Search", false); //stop button
        openButton = this.createJButton("Open", "Open network", false); //open button

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
        anyOrganismCheckBox.setSelected(true);
        anyOrganismCheckBox.addActionListener(this);
        enableDisableOrganism(true);
        
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
        allDatasourceCheckBox.setSelected(true);
        allDatasourceCheckBox.addActionListener(this);
        enableDisableDatasource(true);
        
        //Network Type Drop Down
        networkTypeCombo = new JComboBox<String>();
        networkTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "Pathway", "Interaction", "PhysicalEntity", "EntityReference", "Top Pathways" }));
        JLabel networkTypeLabel = new JLabel("Type", JLabel.TRAILING);    
        networkTypeLabel.setLabelFor(networkTypeCombo);

        //labels for search info
        totalHits = 0;
        numHitsLabel = new JLabel("Hits: " + totalHits);

        // same font but bold
        Font font = numHitsLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        numHitsLabel.setFont(boldFont);        
        numHitsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        retrievedLabel = new JLabel("Retrieved: 0");
        retrievedLabel.setFont(boldFont);
        retrievedLabel.setHorizontalAlignment(SwingConstants.CENTER);

        
        currentPage = 0;
        pagesLabel = new JLabel("Page: " + currentPage);
        pagesLabel.setFont(boldFont);
        pagesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        /**********add form fields******************/
        
        fieldPanel.add(searchLabel, "align label");
        fieldPanel.add(searchField, "");
        
        nameCheckBox = new JCheckBox("Name", true);
        nameCheckBox.setToolTipText("Restrict search to name field only");
        fieldPanel.add(nameCheckBox, "wrap");

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

        //network type
        fieldPanel.add(networkTypeLabel, "align label");
        fieldPanel.add(networkTypeCombo, "wrap");

        fieldPanel.add(searchButton, "tag ok, span, split 4, sizegroup bttn");
        fieldPanel.add(cancelButton, "tag cancel, sizegroup bttn");
        fieldPanel.add(stopButton, "tag yes, sizegroup bttn");
        fieldPanel.add(openButton, "tag no, sizegroup bttn");
        
        fieldPanel.setPreferredSize(new Dimension(888, 205));
        
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(fieldPanel, BorderLayout.PAGE_START);                
        //getContentPane().add(fieldPanel, BorderLayout.PAGE_START);
        
        JPanel hitsPanel = new JPanel();
        hitsPanel.setLayout(new MigLayout());
        
        hitsPanel.add(previousButton, "span, split 2, center, sizegroup hbttn");
        hitsPanel.add(nextButton, "sizegroup hbttn, wrap");
        
        hitsPanel.add(statusLabel, "span, align center, wrap");
        hitsPanel.add(numHitsLabel, "w 33%, sizegroup hits");
        hitsPanel.add(retrievedLabel, "w 33%, sizegroup hits");
        hitsPanel.add(pagesLabel, "w 33%, sizegroup hits");        
        
        hitsPanel.setPreferredSize(new Dimension(888, 88));
        //getContentPane().add(hitsPanel, BorderLayout.PAGE_END);
        searchPanel.add(hitsPanel, BorderLayout.PAGE_END);
        
        /**********************************************/
 
        Font defaultFont = this.getFont();
        String fontFamily = defaultFont.getFamily();
        HTMLEditorKit hed = new HTMLEditorKit();
        StyleSheet ss = hed.getStyleSheet();
        ss.addRule("body {font-family : " + fontFamily + "}");
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
        editorPane.addHyperlinkListener(new HyperlinkListener() 
        {
            public void hyperlinkUpdate(HyperlinkEvent e) 
            {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
                {
                    if(Desktop.isDesktopSupported()) 
                    {
                        try
                        {
                            Desktop.getDesktop().browse(e.getURL().toURI());                            
                        }
                        catch(Exception exception)
                        {
                            logger.warning("Cannot open web browser: " + exception.getMessage()); //TODO error alert                            
                            statusLabel.setText("Unable to open web browser");
                        }
                    }
                }
            }
        });
        
        String text = "<b>Excerpt:</b>";
        editorPane.setText(text);
        
        String[] colHeadings = {"Name", "Database", "BioPAX Class", "Pathways"};
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
        
        table = new ZebraJTable(model);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //set column widths
        table.getColumn("Name").setPreferredWidth(400);
        table.getColumn("Database").setPreferredWidth(75);
        table.getColumn("BioPAX Class").setPreferredWidth(125);
        
        hitInteractionCountMap.clear();
        
        //display search hit info when row selected
        ListSelectionModel rowSelectionModel = table.getSelectionModel();
        rowSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return; //Ignore extra messages.

                openButton.setEnabled(true);

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) 
                {
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
                    
                    //count number of interactions for a pathway
                    String interactionsHTML = "";
                   
                    if(networkType.equals("Pathway"))
                    {
                        //calculate interaction count using TRAVERSE query
                        int pathwayCount = hit.getPathway().size() + 1; //getPathway returns sub-pathways - does not include the top level pathway
                        ArrayList<String> uriList = new ArrayList<String>(pathwayCount);
                        uriList.add(hit.getUri());
                        uriList.addAll(hit.getPathway());

                        //traverse all interactions for all pathways //TODO threading? //TODO what happens with GO term URI? //TODO what to count for interactions?
                        CPathClient client = CPathClient.newInstance();
                        CPathTraverseQuery traverseQuery = client.createTraverseQuery().sources(uriList).propertyPath("Pathway/pathwayComponent*:Interaction");
                        HashSet<String> uniqueUriSet = new HashSet<String>(); //set of unique interaction URIs
                        interactionsHTML = "<b>Interactions: </b> ";

                        //check if interaction count has been previously cached
                        Integer interactionCount = hitInteractionCountMap.get(hit);
                        if(interactionCount != null)
                        {
                            logger.info("Interaction count found: " + interactionCount);
                            interactionsHTML += interactionCount;
                            interactionsHTML += "<br />";
                        }
                        else //interactions have not been previously counted - do traverse searchQuery
                        {
                            try
                            {
                                TraverseResponse traverseResponse = traverseQuery.result(); //run traverse query
                                List<TraverseEntry> traverseEntryList = traverseResponse.getTraverseEntry();

                                for (TraverseEntry traverseEntry : traverseEntryList) 
                                {
                                    logger.info(traverseEntry.getUri());
                                    List<String> traverseEntryValues = traverseEntry.getValue();
                                    uniqueUriSet.addAll(traverseEntryValues);
                                }
                                interactionCount = uniqueUriSet.size();
                                hitInteractionCountMap.put(hit, interactionCount);
                                interactionsHTML += interactionCount;
                            }
                            catch(CPathException exception)
                            {
                                logger.warning(exception.getMessage());
                                interactionsHTML += "unknown";
                            }
                            finally
                            {
                                interactionsHTML += "<br />";
                            }
                        }
                    }
                    
                    //display excerpt
                    String uri = hit.getUri();
                    String abbreviatedUri = uri.substring(0, Math.min(uri.length(), 22)) + "..."; 

                    editorPane.setText("<b>Excerpt:</b><br />" 
                            + hit.getExcerpt() 
                            + "<br />" 
                            + "<b>URI: </b>"
                            + "<a href='" + hit.getUri() + "'>" + abbreviatedUri + "</a>"
                            + "<br />" 
                            + interactionsHTML
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
        
        
        //final LayoutFrame localFrame = frame; //local reference can be referenced from inner class
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) 
            {
                if (e.getClickCount() == 2) //open network
                {
                    openNetwork();
                }
            }
         });
        
        //split search results on left and highlighted search hit excerpt on right
        JScrollPane tableScrollPane = new JScrollPane(table);
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, editorScrollPane);
        //getContentPane().add(splitPane ,BorderLayout.CENTER);
        searchPanel.add(splitPane ,BorderLayout.CENTER);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        //tabbedPane.setLayout(new BorderLayout());
        tabbedPane.addTab("Search", searchPanel);
        tabbedPane.addTab("Advanced", new JLabel("This is the Advanced tab!"));
        getContentPane().add(tabbedPane, BorderLayout.CENTER);  
        
                
        pack();
        splitPane.setDividerLocation(0.75);
        
        setLocationRelativeTo(frame);
        setVisible(true);
    }
    
    /**
     * Get a BioPAX OWL file from Pathway Commons and display network.
     * Gets URI of selected search hit in table
     * Sends GET request to Pathway Commons
     * Downloads the file to a sub-directory of the application directory defined by DIRECTORY
     * Loads the network in BioLayout.
     * @throws IndexOutOfBoundsException - if table row not selected or not valid
     */
    private void openNetwork() throws IndexOutOfBoundsException
    {
        int viewRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(viewRow);

        SearchHit hit = searchHits.get(modelRow); //get SearchHit that relates to values in table model row (converted from sorted view row index)
        String uriString = hit.getUri(); //URI for GET request

        String fileExtension = ".owl";

        //if search hit name is empty use BioPAX class as filename instead
        String hitName = hit.getName();
        if(hitName == null || hitName.isEmpty())
        {
            hitName = hit.getBiopaxClass();
        }
        
        String fileName = hitName + fileExtension; //name of .owl file to be created
        
        CPathClient client = CPathClient.newInstance();
        String[] uriArray = {uriString};
        
        if(networkType.equals("Pathway") || networkType.equals("Top Pathways")) //just get the pathway itself
        {
            getQuery = client.createGetQuery().sources(uriArray);
        }
        else //Interaction etc - network neighbourhood graph query
        {
            getQuery = client.createGraphQuery().sources(uriArray).kind(GraphType.NEIGHBORHOOD);
        }

        //retrieve file from Pathway Commons and load file for display
        getWorker = new GetWorker(fileName);
        getWorker.execute();
    }

    /*
     * Performs Pathway Commons CPath2 web service GET operation concurrently and displays graph
     */
    private class GetWorker extends SwingWorker<File, Void>
    {
        /**
         * Constructor
         * @param fileName - name of file to save BioPAX data in
         */
        public GetWorker(String fileName) 
        {
            super();
            fName = fileName;
        }
        
        /**
         * The name of the BioPAX OWL file to be saved in sub-directory DIRECTORY of the application directorY
         */
        private String fName;
        
        @Override
        /**
         * Perform GET operation on Pathway Commons web service to retrieve BioPAX file
         * @return a BioPAX OWL File
         */
        protected File doInBackground() throws Exception
        {
            SwingWorker actualWorker = new SwingWorker<String, Void>() //anonymous inner worker to handle timeout for GET operation
            {
                 @Override
                protected String doInBackground() throws Exception 
                {
                    statusLabel.setText("Downloading...");              
                    ImportWebServiceDialog.this.getRootPane().setCursor(waitCursor);

                    previousButton.setEnabled(false);
                    nextButton.setEnabled(false);
                    searchButton.setEnabled(false);

                    stopButton.setEnabled(true);
                    openButton.setEnabled(false);
                    
                    String responseString = getQuery.stringResult(OutputFormat.BIOPAX);
                    return responseString;
                }
                
            };
            actualWorker.execute();
            String responseString = "";
            try
            {
                responseString = (String)actualWorker.get(TIMEOUT_GET, TimeUnit.SECONDS); //TODO constant 
            }
            catch(Exception exception)
            {
                logger.warning(exception.getMessage());
                actualWorker.cancel(true); //stop inner search thread
                Exception cause = (Exception)exception.getCause(); //PathwayCommonsException is wrapped in ExecutionException
                if(cause != null)
                {
                    exception = cause;
                }
                throw exception;                
            }
            
            //create directory to store downloaded file
            File importDir = new File(DataFolder.get(), DIRECTORY);
            if(!importDir.exists())
            {
                importDir.mkdir();
            }

            //create file and save web service data
            File importFile = new File(importDir, fName);
            logger.info("Writing to file " + importFile);
            FileUtils.writeStringToFile(importFile, responseString); //throws IOException
            statusLabel.setText("Success! Downloaded file: " + fName);      
            return importFile;
        }
       
        @Override
        /**
         * Save OWL file, parse and display graph
         */
        protected void done() 
        {
            /*
            * @throws PathwayCommonsException - when HTTP status code is not 200
            * @throws IOException - if OWL file cannot be written
            * @throws Exception - web service does not respond
            */
            try
            {
                File importFile = get(); //perform Pathway Commons GET in the background

                //parse and display file
                logger.info("Opening file: " + importFile);
                frame.requestFocus();
                frame.toFront();                        
                frame.loadDataSet(importFile);
                statusLabel.setText("Opened file: " + fName);      
            }
            catch(IllegalStateException exception) //runtime exception
            {
               logger.warning(exception.getMessage());
               statusLabel.setText("Search failed: connection not released");
            }
            catch(InterruptedException exception)
            {
                logger.warning(exception.getMessage());
                statusLabel.setText("Search failed: interrupted");
            }
            catch(Exception exception)
            {
                logger.warning(exception.getMessage());                     
                Throwable cause = exception.getCause(); //get wrapped exception
                if(cause == null)
                {
                    cause = exception;
                }
                     
                if(cause instanceof PathwayCommonsException || cause instanceof CPathException) //HTTP error code returned from GET request
                {
                    logger.warning(exception.getMessage());
                    statusLabel.setText("Fetch error: " + exception.getMessage());
                }
                else if(cause instanceof UnknownHostException) //Pathway Commons down
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Fetch error: unable to reach Pathway Commons");
                }
                else if(cause instanceof SocketException) //computer offline
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Fetch error: offline");
                }
                else if(cause instanceof TimeoutException) //no response after set time
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Fetch error: timeout");
                }
                else
                {
                    logger.warning(exception.getMessage());
                    statusLabel.setText("Fetch error: unable to get " + fName + " from Pathway Commons"); 
                }
            }
            finally
            {
                //release connection and close socket to stop IllegalStateException being generated following 460 error
                if(getClientResponse != null)
                {
                    getClientResponse.releaseConnection(); 
                }
                
                ImportWebServiceDialog.this.getRootPane().setCursor(defaultCursor);                
                restoreButtons();
            }
        }
    }
    
    /**
     * Enable or disable dialog buttons when dialog is in resting state according to search results.
     */
    private void restoreButtons()
    {
        stopButton.setEnabled(false);
        
        if(table.getSelectedRow() != -1) //a row is selected
        {
            openButton.setEnabled(true);
        }
        
        searchButton.setEnabled(true);
        enableDisablePreviousButton(); //enable/disable previous button 
        enableDisableNextButton(); //enable/disable next button
    }
    
    /**
     * Enable or disable the Next button according to whether more search hits exist following the current page
     */
    private void enableDisableNextButton()
    {
        if(maxHitsPerPage > 0 && totalHits > 0) //no hits - avoid division by zero
        {    
            int numPages = (totalHits + maxHitsPerPage - 1) / maxHitsPerPage; //calculate number of pages, round up integer division
            if((currentPage + 1) < numPages) //pages indexed from zero
            {
                nextButton.setEnabled(true);
            }
        }
        else
        {
            nextButton.setEnabled(false);
        }
    }
    
    /**
     * Enable or disable the Previous button according to whether more search hits exist before the current page
     */
    private void enableDisablePreviousButton()
    {
        if(currentPage > 0) //on first page, disable previous
        {
            previousButton.setEnabled(true);
        }
        else
        {
            previousButton.setEnabled(false);
        }
    }
    
    /**
     * Creates a JButton with an ActionListener for this dialog. Convenience method.
     * @param text
     * @param toolTipText
     * @param enabled
     * @return - a new JButton
     */
    private JButton createJButton(String text, String toolTipText, boolean enabled)
    {
        JButton button = new JButton(text);
        button.setToolTipText(toolTipText);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }
    
    /**
     * Performs Pathway Commons search concurrently
     */
    private class SearchWorker extends SwingWorker<SearchResponse, Void>
    {
        /**
         * Perform Pathway Commons search
         */
        @Override
        protected SearchResponse doInBackground() throws Exception
        {
            SwingWorker actualWorker = new SwingWorker<SearchResponse, Void>() //anonymous inner worker to handle timeout
            {
                @Override
                protected SearchResponse doInBackground() throws Exception 
                {
                    //display message/cursor and disable buttons
                    statusLabel.setText("Searching...");
                    ImportWebServiceDialog.this.getRootPane().setCursor(waitCursor);
                    previousButton.setEnabled(false);
                    nextButton.setEnabled(false);
                    searchButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    openButton.setEnabled(false);
                    
                    return searchQuery.result(); //perform search
                }
            };

            actualWorker.execute();
            try 
            {
                SearchResponse sr = (SearchResponse)actualWorker.get(TIMEOUT_SEARCH, TimeUnit.SECONDS); //15 second timeout
                return sr;
            } 
            catch (Exception exception) 
            {
                logger.warning(exception.getMessage());
                actualWorker.cancel(true); //stop inner search thread
                Exception cause = (Exception)exception.getCause(); //PathwayCommonsException is wrapped in ExecutionException
                if(cause != null)
                {
                    exception = cause;
                }
                throw exception;
            }
        }

        /**
         * Display search results
         */
        @Override
        protected void done()
        {
            try
            {
                SearchResponse searchResponse = get(); //calls doInBackground() to perform search
                if(searchResponse != null)
                {
                    stopButton.setEnabled(false);
                    statusLabel.setText("Search complete: success!");
                    totalHits = searchResponse.getNumHits();
                    numHitsLabel.setText("Hits: " + totalHits);

                    searchHits = searchResponse.getSearchHit();            
                    int numRetrieved = searchHits.size();
                    retrievedLabel.setText("Retrieved: " + numRetrieved);

                    maxHitsPerPage = searchResponse.getMaxHitsPerPage(); //maximum number of search hits per page

                    //display current page number
                    currentPage = searchResponse.getPageNo();
                    pagesLabel.setText("Page: " + currentPage);

                    displaySearchResults();

                    if(organismIdNameMap.size() > 0)
                    {
                        fetchScientificNames(); //populate organismIdNameMap from NCBI SOAP web service
                    }
                }
                else
                {
                    statusLabel.setText("Search complete: no hits");
                }
            }
            catch(IllegalStateException exception) //runtime exception
            {
               logger.warning(exception.getMessage());
               statusLabel.setText("Search failed: connection not released");
            }
            catch(InterruptedException exception)
            {
                logger.warning(exception.getMessage());
                statusLabel.setText("Search failed: interrupted");
            }
            catch(ExecutionException exception)
            {
                 logger.warning(exception.getMessage());                     
                 Throwable cause = exception.getCause(); //get wrapped exception - the culprit!
                 if(cause == null)
                 {
                     cause = exception;
                 }
                     
                if(cause instanceof PathwayCommonsException) //no search hits
                {
                   logger.warning(cause.getMessage());
                   clearSearchResults(); //clear previous search results
                   statusLabel.setText("Search error: " + cause.getMessage());
                }
                else if(cause instanceof UnknownHostException) //Pathway Commons down
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Search failed: unable to reach Pathway Commons");
                }
                else if(cause instanceof SocketException) //computer offline
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Search failed: offline");
                }
                else if(cause instanceof TimeoutException) //no response after set time
                {
                   logger.warning(cause.getMessage());
                   statusLabel.setText("Search failed: timeout");
                }
                else
                {
                    logger.warning(exception.getMessage());
                    statusLabel.setText("Search failed: generic error");
                }
            }
            finally
            {
                //release connection and close socket to stop IllegalStateException being generated following 460 error
                /*
                if(searchClientResponse != null)
                {
                    searchClientResponse.releaseConnection(); 
                }
                */
                ImportWebServiceDialog.this.getRootPane().setCursor(defaultCursor);                
                restoreButtons();
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        if(searchButton == e.getSource() || nextButton == e.getSource() || previousButton == e.getSource()) 
        {
              //TODO previous/next should remember last search params in case user changes
            networkType = this.networkTypeCombo.getSelectedItem().toString();
            
            try
            {
                CPathClient client = CPathClient.newInstance(); //TODO catch org.springframework.web.client.HttpClientErrorException
                if(networkType.equals("Top Pathways"))
                {
                    searchQuery = client.createTopPathwaysQuery();
                }
                else
                {
                    String searchTerm = searchField.getText();
                    String organism = organismField.getText();
                    
                    //restrict search to name
                    if(nameCheckBox.isSelected())
                    {
                        searchTerm = "name:'" + searchTerm + "'";
                    }
                    
                    logger.info("Search term: " + searchTerm);

                    CPathSearchQuery searchQuery = client.createSearchQuery().queryString(searchTerm).typeFilter(networkType);

                    //TODO muliple organisms with separator?
                    HashSet<String> organismSet = new HashSet<String>();
                    if(!organism.equals(""))
                    {
                        organismSet.add(organism); //TODO multiple organisms comma separated
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
                    //searchClientRequest.queryParameter("page", pageParameter);
                    searchQuery.page(pageParameter);                

                    //add parameters for datasource checkboxes
                    if(!allDatasourceCheckBox.isSelected())
                    {
                        HashSet<String> datasourceSet = new HashSet<String>();                    
                        for (Map.Entry<JCheckBox, String> entry : datasourceDisplayCommands.entrySet()) {
                             JCheckBox checkBox = entry.getKey();
                             if(checkBox.isSelected())
                             {
                                 String datasourceParameter = entry.getValue();
                                 datasourceSet.add(datasourceParameter);
                             }
                        }            
                        searchQuery.datasourceFilter(datasourceSet);
                    }

                    //add parameters for organism checkboxes
                    if(!anyOrganismCheckBox.isSelected()) //don't add organism parameters if Any is selected
                    {
                        for (Map.Entry<JCheckBox, String> entry : organismDisplayCommands.entrySet()) {
                             JCheckBox checkBox = entry.getKey();
                             if(checkBox.isSelected())
                             {
                                 String organismParameter = entry.getValue();
                                 organismSet.add(organismParameter);
                             }
                        }
                    }

                    if(!organismSet.isEmpty())
                    {
                        searchQuery.datasourceFilter(organismSet);
                    }

                    this.searchQuery = (CPathQuery<SearchResponse>)searchQuery;
                }
                search(); //perform search
            }
            catch(HttpClientErrorException exception)
            {
                statusLabel.setText("Unable to connect to Pathway Commons");
                logger.warning(exception.getMessage());
            }
        }
        else if(stopButton == e.getSource()) //stop running process
        { 
            if(searchWorker != null && !searchWorker.isDone()) //stop search process
            {
                try //stop search
                {
                    statusLabel.setText("Stopping search...");
                    boolean cancelled = searchWorker.cancel(true);
                    if(cancelled)
                    {
                        statusLabel.setText("Search stopped");
                    }
                    else
                    {
                        statusLabel.setText("Search has already completed");
                    }
                    stopButton.setEnabled(false);
                    logger.info("search SwingWorker cancel returned " + cancelled);
                }
                catch(CancellationException exception)
                {
                    statusLabel.setText("Unable to stop search");
                    logger.warning("Unable to stop search SwingWorker: " + exception.getMessage());
                }
            }
            
            if(getWorker != null && !getWorker.isDone()) //stop download process
            {
                try //stop download
                {
                    statusLabel.setText("Stopping open...");
                    boolean cancelled = getWorker.cancel(true);
                    if(cancelled)
                    {
                        statusLabel.setText("Open stopped");
                    }
                    else
                    {
                        statusLabel.setText("Download has already completed");
                    }
                    stopButton.setEnabled(false);
                    logger.info("Download SwingWorker cancel returned " + cancelled);
                }
                catch(CancellationException exception)
                {
                    statusLabel.setText("Unable to stop download");
                    logger.warning("Unable to stop download SwingWorker: " + exception.getMessage());
                }
            }
        }
        else if(cancelButton == e.getSource())
        {
            //stop search threads
            if(searchWorker != null && !searchWorker.isDone()) //stop search process before closing
            {
                boolean cancelled = searchWorker.cancel(true);
                logger.info("search SwingWorker cancel returned " + cancelled);
            }
            
            //stop GET threads
            if(getWorker != null && !getWorker.isDone())
            {
                boolean cancelled = getWorker.cancel(true);
                logger.info("GET SwingWorker cancel returned " + cancelled);
            }
            
            this.dispose(); //destroy the dialog to free up resources
        }
        else if(openButton == e.getSource()) //search hit selected in table then open button pressed
        {
            openNetwork();
        }
        else if(anyOrganismCheckBox == e.getSource()) //"Any" organism checkbox has been checked or unchecked
        {
            enableDisableOrganism(anyOrganismCheckBox.isSelected()); //enable/disable organism checkboxes and text field
        }
        else if(allDatasourceCheckBox == e.getSource())
        {
            enableDisableDatasource(allDatasourceCheckBox.isSelected()); //enable/disable datasource checkboxes and text field
        }
    }

     /**
     * Runs Pathway Commons REST web service SEARCH and displays results
     * @param searchClientRequest - contains search parameters
     */
    private void search()
    {
        searchWorker = new SearchWorker(); //concurrent threading for search process
        searchWorker.execute();
    }
    
    private void clearSearchResults()
    {
        model.setRowCount(0); //clear previous search results
        editorPane.setText(""); //clear excerpt pane
    }

    /**
     * Adds organism NCBI IDs as key to organismIdNameMap so that values may be populated later from NCBI Taxonomy SOAP service
     */
    private void mapOrganisms(SearchHit hit)
    {
        List<String> organismList = hit.getOrganism(); //URIs of organisms at identifiers.org

        //extract organism ID for each organism URI
        String[] organismArray = organismList.toArray(new String[0]);
        for (int i = 0; i < organismArray.length; i++)
        {
            String organismString = organismArray[i];
            organismArray[i] = organismString.substring(organismString.lastIndexOf("/")+1, organismString.length()); //extract ID from URI
            //add to NCBI ID/name map for later web service lookup if not already added
            if(!organismIdNameMap.containsKey(organismArray[i]))
            {
                organismIdNameMap.put(organismArray[i], organismArray[i]); //value also has NCBI ID as placeholder - to be replaced with name from web service
            }
        }
    }
    
    private void displaySearchResults()
    {
        clearSearchResults(); 
        
        for(SearchHit hit : searchHits)
        {
            this.mapOrganisms(hit);
            
            //comma-separated string of datasources for display
            List<String> databases = hit.getDataSource();
            String[] databaseArray = databases.toArray(new String[0]);
            for(int i = 0; i < databaseArray.length; i++)
            {
                String databaseUri = databaseArray[i];

                //replace with database real name if found in map
                for (Map.Entry<String, String> entry : DATABASE_URI_DISPLAY.entrySet()) 
                {
                    String databaseString = entry.getKey();
                    if(databaseUri.contains(databaseString))
                    {
                        databaseArray[i] = entry.getValue();
                    }
                }
            }
            String joinedDatabases = commaJoiner.join(databaseArray);

            //model.addRow(new Object[]{hit.getName(), joinedOrganisms, joinedDatabases, hit.getBiopaxClass(), hit.getPathway().size()});  
            model.addRow(new Object[]{hit.getName(), joinedDatabases, hit.getBiopaxClass(), hit.getPathway().size()});  

        }//end for
    }
    
    /**
    * Populate organism scientific names from NCBI web service
    */
    private boolean fetchScientificNames()
    {
        EFetchTaxonService service = new EFetchTaxonService();
        EUtilsServiceSoap serviceSoap = service.getEUtilsServiceSoap();
        ObjectFactory objectFactory = new ObjectFactory();
        EFetchRequest requ = objectFactory.createEFetchRequest();
         
        //set comma-separated String of organism IDs as search parameter
        String eFetchQuery = commaJoiner.join(this.organismIdNameMap.keySet());
        logger.info("eFetchQuery: " + eFetchQuery);
        requ.setId(eFetchQuery);
        
        try
        {
            EFetchResult resp = serviceSoap.runEFetch(requ);
            logger.info("EFetchResult: " + resp.getTaxaSet().getTaxon().size() + " Taxa");
            List<TaxonType> taxon = resp.getTaxaSet().getTaxon();
            for(TaxonType taxonType : taxon)
            {
                organismIdNameMap.put(taxonType.getTaxId(), taxonType.getScientificName());
            }
            return true;
        }
        catch(Exception exception) //com.sun.xml.internal.ws.client.ClientTransportException thrown if web service down - will display organism ID as name
        {
            logger.warning("runEFetch failed: " + exception);
            return false;
        }
    }
    
    private void enableDisableOrganism(boolean anySelected)
    {
        //enable/disable organism checkboxes
        for(JCheckBox checkBox: organismDisplayCommands.keySet())
        {
            if(anySelected)
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
        if(anySelected)
        {
            organismField.setText("");
            organismField.setEnabled(false);
        }
        else
        {
            organismField.setEnabled(true);
        }
    }
    
    private void enableDisableDatasource(boolean allSelected)
    {
        for(JCheckBox checkBox: datasourceDisplayCommands.keySet())
        {
            if(allSelected)
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

    /**
     * Accessor for search field so search terms can be set externally e.g. from Class Viewer
     * @return search field
     */
    public JTextField getSearchField() 
    {
        return searchField;
    }
}