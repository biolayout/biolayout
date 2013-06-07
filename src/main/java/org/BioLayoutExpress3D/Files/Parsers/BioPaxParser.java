package org.BioLayoutExpress3D.Files.Parsers;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.DataStructures.Tuple6;
import org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes2D;
import org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes3D;
import org.BioLayoutExpress3D.Network.*;
import org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup3;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.TemplateReaction;

/**
* Parser for BioPAX Level 3 OWL encoded as RDF/XML. 
* Uses PaxTools library.
* @author Derek Wright
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
            
            //TODO query BioPAX level
            //convert to level 3 or deal with level 2 entities separately?
            
            //level 3
            Set<Entity> entities = model.getObjects(Entity.class);
            
            //create a graph node for each entity
            int entityCount = 0;
            for(Entity entity: entities)
            {
                ++entityCount;
                logger.info("Entity RDFId: " + entity.getRDFId());
                logger.info("Entity displayName: " + entity.getDisplayName());
                logger.info("Entity Xrefs: " + Arrays.toString(entity.getXref().toArray()));
                
                
            }
            /*
                //get interactions participant of
                Set<Interaction> participantInteractions = entity.getParticipantOf();

                //connect entity and participants
                Interaction[] interactionArray = participantInteractions.toArray(new Interaction[0]);
                for(int i = 0; i < interactionArray.length; i++)
                {
                    Interaction interaction = interactionArray[i];
                    
                }
*/
            
            logger.info(entityCount + " entities found");
            
            
            Set<Interaction> interactions = model.getObjects(Interaction.class); //get all interactions
            for (Interaction interaction : interactions) 
            {    
                logger.info("Interaction RDFId: " + interaction.getRDFId());
                logger.info("Interaction displayName: " + interaction.getDisplayName());
                logger.info("Interaction Xrefs: " + Arrays.toString(interaction.getXref().toArray()));
                                
                Set<Entity> participants = interaction.getParticipant();
                //construct array of node names
                
                //connect all the entities in the interaction
                Entity[] entityArray = participants.toArray(new Entity[0]);     
                
                //shapes corresponding to each member of entityArray, to be looked up from GraphmlLookUpmEPNTables
                Tuple6[] nodeShapeArray = new Tuple6[entityArray.length]; 
                               
                //look up shape for each entity in entityArray and assign to corresponding index in nodeShapeArray
                for(int i = 0; i < entityArray.length ; i++ )
                {
                    Entity entity = entityArray[i];
                    if(entity instanceof Complex){
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Complex");
                    }
                    else if(entity instanceof Dna)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Dna");
                    }
                    else if(entity instanceof DnaRegion)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("DnaRegion");                        
                    }
                    else if(entity instanceof NucleicAcid)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("NucleicAcid");                                                
                    }
                    else if(entity instanceof Protein)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Protein");                        
                    }
                    else if(entity instanceof Rna)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Rna");                        
                    }
                    else if(entity instanceof RnaRegion)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("RnaRegion");                        
                    }
                    else if(entity instanceof SimplePhysicalEntity)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SimplePhysicalEntity");                        
                    }
                    else if(entity instanceof SmallMolecule)
                    {
                        nodeShapeArray[i] = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SmallMolecule");                        
                    }
                }
                                
                Entity from, to; //BioPAX entities to be connected in graph
                String nameFrom, nameTo; //node names for graph
                boolean foundFrom, foundTo; //flags for nodes already present in graph
                
                String edgeName = interaction.getRDFId();
                
                for(int outer = 0; outer < entityArray.length - 1; outer++)
                {
                    from = entityArray[outer];
                    nameFrom = from.getRDFId();
                    
                    foundFrom = false; 
                    if(nc.getVerticesMap().containsKey(nameFrom))
                    {
                        foundFrom = true;
                    }
                                        
                    for(int inner = outer + 1; inner < entityArray.length; inner++)
                    {
                        foundTo = false;
                        
                        to = entityArray[inner];
                        nameTo = to.getRDFId();
                        
                        //set flag if node already present in vertices map - so don't need to set shapes again
                        
                        if(nc.getVerticesMap().containsKey(nameTo))
                        {
                            foundTo = true;
                        }
                        
                        nc.addNetworkConnection(nameFrom, nameTo, edgeName, false, false, false);
                        
                        //second node is new, set shape
                        if(!foundTo)
                        {
                            Vertex vertexTo = nc.getVerticesMap().get(nameTo);  
                            setVertexProperties(vertexTo, nodeShapeArray[inner]);
                        }
                        
                    } //end for
                    
                    //first node is new, set shape
                    if(!foundFrom)
                    {
                        Vertex vertexFrom = nc.getVerticesMap().get(nameFrom);
                        setVertexProperties(vertexFrom, nodeShapeArray[outer]);
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

    //TODO ENUM MAP OF ENTITIES?
    //TODO use Class.getSimpleName to lookup
    private static Tuple6 lookupShape(Entity entity)
    {
        Tuple6 shape; //node shape to be assigned according to entity type
        if(entity instanceof PhysicalEntity)
        {
            if(entity instanceof Complex){
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Complex");
            }
            else if(entity instanceof Dna)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Dna");
            }
            else if(entity instanceof DnaRegion)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("DnaRegion");                        
            }
            else if(entity instanceof NucleicAcid)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("NucleicAcid");                                                
            }
            else if(entity instanceof Protein)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Protein");                        
            }
            else if(entity instanceof Rna)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Rna");                        
            }
            else if(entity instanceof RnaRegion)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("RnaRegion");                        
            }
            else if(entity instanceof SimplePhysicalEntity)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SimplePhysicalEntity");                        
            }
            else if(entity instanceof SmallMolecule)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SmallMolecule");                        
            }
            else
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SimplePhysicalEntity"); //default generic entity                      
            }
        }
        else if(entity instanceof Interaction)
        {
            if(entity instanceof TemplateReaction)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("TemplateReaction");                                        
            }
            else if(entity instanceof Control)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Control");                                        
                
            }
            else if(entity instanceof Conversion)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Conversion");                                        
                
            }
            else if(entity instanceof MolecularInteraction)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("MolecularInteraction");                                        
                
            }
            else if(entity instanceof GeneticInteraction)
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("GeneticInteraction");                                        
                
            }
            else
            {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Interaction");                                        
                
            }
            
        }
        else if(entity instanceof Pathway)
        {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Pathway");                     
            
        }
        else if (entity instanceof Gene)
        {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("Gene");
        }
        else
        {
                shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("PhysicalEntity");                                                    
        }
        return shape;
   }
    
    /**
     * Set display properties of a Vertex (graph node) from mEPN-style properties.
     * Expects a tuple in the format found in GraphmlLookUpmEPNTables.GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3
     * @param shapeLookup - a tuple of size, shape and color
     */
    private static void setVertexProperties(Vertex vertex, Tuple6<String, GraphmlShapesGroup3, Color, Float, Shapes2D, Shapes3D> shapeLookup)
    {
        vertex.setVertex2DShape(shapeLookup.fifth);
        vertex.setVertex3DShape(shapeLookup.sixth);
        vertex.setVertexSize(shapeLookup.fourth);
        vertex.setVertexColor(shapeLookup.third);
    }   
}