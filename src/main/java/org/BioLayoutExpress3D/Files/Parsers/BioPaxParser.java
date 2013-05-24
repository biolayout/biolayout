package org.BioLayoutExpress3D.Files.Parsers;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.Network.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import org.BioLayoutExpress3D.Files.webservice.ImportWebService;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;

/**
* Parser for BioPAX Level 3 OWL encoded as RDF/XML. 
* Uses PaxTools library.
* @author Derek Wright
* @author Leon Goldovsky, Thanos Theo
*
*/

public final class BioPAXParser extends CoreParser
{
    private static final Logger logger = Logger.getLogger(BioPAXParser.class.getName());
    
    private BioPAXIOHandler handler;

    public BioPAXParser(NetworkContainer nc, LayoutFrame layoutFrame)
    {
        super(nc, layoutFrame);
    }

    /**
     * Initialize the parser.
     * @param file - the file to be parsed
     * @param fileExtension - the extension of the file ("owl")
     * @return true if parser initialized successfully, false if parser not initialized successfully
     */
    @Override
    public boolean init(File file, String fileExtension)
    {
        this.file = file;
        
        //use JAXP reader rather than Jena reader as only XML files expected + handles large files with high performance        
        handler = new SimpleIOHandler(); //auto-detects BioPAX level, default level 3
        return true;
    }
    
    /**
     * Parse the OWL file and create a network.
     * @return true if parsing successful, otherwise false
     */
    @Override
    public boolean parse()
    {
/*
        String currReaction = "";
        String currComplex = "";
        String currCat = "";
        String currParticipant = "";
        String controller = "";
        String controlled = "";
        
        HashMap<String, ArrayList<String>> links = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> cats = new HashMap<String, ArrayList<String>>();
        HashMap<String, String> participants = new HashMap<String, String>();    
*/
        isSuccessful = false;
        nc.setOptimized(false);

        LayoutProgressBarDialog layoutProgressBarDialog = layoutFrame.getLayoutProgressBar();

        try
        {
            int progressCounter = 0;
            layoutProgressBarDialog.prepareProgressBar(3, "Parsing " + file.getName());
            layoutProgressBarDialog.startProgressBar();
            layoutProgressBarDialog.incrementProgress(++progressCounter);
            
            int edgeCounter = 1;
            Model model = handler.convertFromOWL(new FileInputStream(file)); //construct object model from OWL file
            Set<Interaction> interactions = model.getObjects(Interaction.class);
            
            for (Interaction interaction : interactions) 
            {    
                logger.info("Interaction RDFId: " + interaction.getRDFId());
                logger.info("Interaction displayName: " + interaction.getDisplayName());
                logger.info("Interaction Xrefs: " + Arrays.toString(interaction.getXref().toArray()));
                                
                Set<Entity> participants = interaction.getParticipant();
                //contstruct array of node names
                
                //connect all the entities in the interaction
                Entity[] entityArray = participants.toArray(new Entity[0]);               
                Entity from, to;
                String nameFrom, nameTo;
                
                String edgeName = interaction.getRDFId();
                
                for(int outer = 0; outer < entityArray.length - 1; outer++)
                {
                    from = entityArray[outer];
                    nameFrom = from.getRDFId();
                    for(int inner = outer + 1; inner < entityArray.length; inner++)
                    {
                        to = entityArray[inner];
                        nameTo = to.getRDFId();
                        nc.addNetworkConnection(nameFrom, nameTo, edgeName, false, false, false);
                    }
                    
                }
                
                /*
                
                
                    nc.addNetworkConnection(vertex1, edgeType + lines, 0.0f);
                    nc.addNetworkConnection(edgeType + lines, vertex2, 0.0f);

                    Vertex vertex = nc.getVerticesMap().get(edgeType + lines);
                    vertex.setVertexSize(vertex.getVertexSize() / 2);
                    vertex.setPseudoVertex();

                    LayoutClasses lc = nc.getLayoutClassSetsManager().getClassSet(0);
                    VertexClass vc = lc.createClass(edgeType);
                    lc.setClass(nc.getVerticesMap().get(edgeType + lines), vc);

                for(Entity participant: participants)
                {
                    logger.info("Participant RDFId: " + participant.getRDFId());
                    logger.info("Participant displayName: " + participant.getDisplayName());
                    logger.info("Participant Xrefs: " + Arrays.toString(participant.getXref().toArray()));
                    
                    String nodeName = "";                    
                    if(str != null && !str.isEmpty())
                    {
                        
                    }

                    nc.addNetworkConnection("test", participant.getDisplayName(), interaction.getDisplayName(), false, false, false);
               }                
               */
            }
               
            layoutProgressBarDialog.incrementProgress(++progressCounter);
           
            isSuccessful = true;
        }
        catch(FileNotFoundException e)
        {
            //TODO display error dialogue
            logger.warning(e.getMessage());
            return false;
        }
        finally
        {
            layoutProgressBarDialog.endProgressBar();
        }

        return isSuccessful;
    }
}