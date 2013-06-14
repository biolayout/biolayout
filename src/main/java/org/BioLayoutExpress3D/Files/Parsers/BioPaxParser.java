package org.BioLayoutExpress3D.Files.Parsers;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.BioLayoutExpress3D.CoreUI.*;
import org.BioLayoutExpress3D.CoreUI.Dialogs.*;
import org.BioLayoutExpress3D.DataStructures.Tuple6;
import org.BioLayoutExpress3D.DataStructures.Tuple7;
import org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes2D;
import org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes3D;
import org.BioLayoutExpress3D.Network.*;
import org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup1;
import org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup2;
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
import org.biopax.paxtools.model.level3.Process;
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
    private HashMap<Entity, Vertex> entityVertexMap = null; //created during parsing

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
            //nc.addNetworkConnection("testfrom", "testto", "testedge", false, false, false);

            int progressCounter = 0;
            layoutProgressBarDialog.prepareProgressBar(2, "Parsing " + file.getName());
            layoutProgressBarDialog.startProgressBar();
            layoutProgressBarDialog.incrementProgress(++progressCounter);
            
            //int edgeCounter = 1;
            Model model = handler.convertFromOWL(new FileInputStream(file)); //construct object model from OWL file
            
            //TODO query BioPAX level
            //convert to level 3 or deal with level 2 modelEntitySet separately?
            
            //level 3
            Set<Entity> modelEntitySet = model.getObjects(Entity.class);
           
            //create a graph node for each entity
            
            logger.info(modelEntitySet.size() + " Entities parsed from " + file.getName());
            /*
            Vertex testVertex = new Vertex("TESTVERTEX", nc);
            nc.getVerticesMap().put("TESTVERTEX", testVertex);
            
            Vertex testVertex2 = new Vertex("TESTVERTEX2", nc);
            nc.getVerticesMap().put("TESTVERTEX2", testVertex2);
            
            Edge testEdge = new Edge(testVertex, testVertex2, 0.0f);
            nc.getEdges().add(testEdge);
            */
//        Map<String, Tuple7> interactionNameMap = new HashMap<String, Tuple7>();

            entityVertexMap = new HashMap<Entity, Vertex>(modelEntitySet.size());
            for(Entity entity: modelEntitySet)
            {
                logger.info("Entity RDFId: " + entity.getRDFId());
                logger.info("Entity displayName: " + entity.getDisplayName());
                
                
                String xrefs = Arrays.toString(entity.getXref().toArray());
                logger.info("Entity Xrefs: " + xrefs);
                
                Vertex vertex;
                String vertexName = entity.getRDFId() + ":" + entity.getDisplayName();
                //vertexName = (entity.getDisplayName() != null ? entity.getDisplayName() : xrefs);
                //messageColor = (color != null ? color : messageColor);
                vertex = new Vertex(vertexName, nc);
                if(entity instanceof Interaction)
                {
                    Interaction interaction = (Interaction)entity;
                    Tuple7 interactionShape;
                    interactionShape = BioPAXParser.lookupInteractionShape(interaction);
                    
                    BioPAXParser.setVertexPropertiesInteraction(vertex, interactionShape);
               }
                else
                {
                    Tuple6 entityShape = BioPAXParser.lookupEntityShape(entity);
                    if(entityShape.second instanceof GraphmlShapesGroup2) //Pathway
                    {
                        logger.info("Pathway vertex");
                        BioPAXParser.setVertexPropertiesPathway(vertex, entityShape);
                    }
                    else //PhysicalEntity, Gene
                    {
                        BioPAXParser.setVertexPropertiesEntity(vertex, entityShape);
                    }
               }
                nc.getVerticesMap().put(vertex.getVertexName(), vertex);
                
                entityVertexMap.put(entity, vertex);
                
               
                
            }
            
            
            
            /*
             * Connect all entity and participant vertices
             * 
             * Algorithm:
             * For each Entity
             *  get Vertex
             *  get participant Interactions
             *  for each participant Interaction
             *      get participant's Vertex
             *      connect to entity Vertex
             */
            
            Set<Entity> graphEntitySet = entityVertexMap.keySet();
            logger.info("Entity Set has " + graphEntitySet.size() + " Entities");
            
            int hasInteractionCount = 0; //TEST
            
            for(Entity entity: graphEntitySet)
            {
                //if physical entity is component of complex, create edge
                if(entity instanceof Complex)
                {
                    Complex complex = (Complex) entity;
                    Set<PhysicalEntity> components = complex.getComponent();
                    logger.info("Complex contains " + components.size() + " components");
                    for(PhysicalEntity component: components)
                    {
                        this.connectEntityVertices(complex, component, 0.0f); //TODO stoichiometry as edge weight?
                    }
                }
                
                if(entity instanceof Pathway)
                {
                    Pathway pathway = (Pathway) entity;
                    Set<Process> processes = pathway.getPathwayComponent();
                    logger.info("Pathway contains " + processes.size() + " processes");
                    for(Process process: processes)
                    {
                        this.connectEntityVertices(pathway, process, 0.0f);
                    }
                    //TODO pathway steps: label edges?
                }
                
                Set<Interaction> interactionSet = entity.getParticipantOf();
                
                logger.info("Entity " + entity.getRDFId() + " participates in " + interactionSet.size() + " Interactions");
                if(interactionSet.size() > 0)
                {
                    hasInteractionCount++;
                }
                
                for(Interaction interaction: interactionSet)
                {
                    this.connectEntityVertices(entity, interaction, 0.0f);
                    
                }
            }
            
            logger.info(hasInteractionCount + " Entities have Interactions");
           
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

    /**
     * Create a graph Edge between the Nodes mapped to a pair of Entities.
     * @param entityFrom
     * @param entityTo
     * @param edgeWeight
     * @return the created Edge
     */
    private Edge connectEntityVertices(Entity entityFrom, Entity entityTo, float edgeWeight)
    {
        Vertex vertexFrom = entityVertexMap.get(entityFrom);
        Vertex vertexTo = entityVertexMap.get(entityTo);
        Edge edge = new Edge(vertexFrom, vertexTo, edgeWeight);

        vertexFrom.addConnection(vertexTo, edge);
        vertexTo.addConnection(vertexFrom, edge);
        nc.getEdges().add(edge);
        return edge;
    }
    
    /**
     * Looks up graph node shape according to Interaction type.
     * @param interaction
     * @return a graph node shape. If no mapping found, returns shape for Interaction.
     */
    private static Tuple7 lookupInteractionShape(Interaction interaction)
    {
        //String className = interaction.getClass().getSimpleName();
        Tuple7 shape;// = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get(className); //node shape assigned according to interaction type
        /*
        if(shape == null)
        {
            shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Interaction");  //generic Interaction                                      
        }
*/
        if(interaction instanceof TemplateReaction)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("TemplateReaction");                                        
         }
         else if(interaction instanceof Control)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Control");                                        

         }
         else if(interaction instanceof Conversion)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Conversion");                                        

         }
         else if(interaction instanceof MolecularInteraction)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("MolecularInteraction");                                        

         }
         else if(interaction instanceof GeneticInteraction)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("GeneticInteraction");                                        

         }
         else
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_INTERACTION_MAP.get("Interaction");                                        

         }
            
        return shape;
    }

    /**
     * Looks up graph node shape according to Entity type. 
     * To be used for PhysicalEntity, Pathway and Gene.
     * @param entity
     * @return a graph node shape. If no mapping found, returns shape for SimplePhysicalEntity.
     */
    private static Tuple6 lookupEntityShape(Entity entity)
    {
        //String className = entity.getClass().getSimpleName();
        //Tuple6 shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get(className); //node shape assigned according to entity type
        /*
        if(shape == null)
         {
             shape = GraphmlLookUpmEPNTables.BIOPAX_MEPN_MAP.get("SimplePhysicalEntity"); //default generic entity                      
         }
*/
        Tuple6 shape;
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
        else if(entity instanceof Pathway || entity instanceof Process) //superinterface of Pathway
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
    private static void setVertexPropertiesEntity(Vertex vertex, Tuple6<String, GraphmlShapesGroup3, Color, Float, Shapes2D, Shapes3D> shapeLookup)
    {
        vertex.setVertex2DShape(shapeLookup.fifth);
        vertex.setVertex3DShape(shapeLookup.sixth);
        vertex.setVertexSize(shapeLookup.fourth);
        vertex.setVertexColor(shapeLookup.third);
    }
    
    //special case for pathway - uses different enumeration
    private static void setVertexPropertiesPathway(Vertex vertex, Tuple6<String, GraphmlShapesGroup2, Color, Float, Shapes2D, Shapes3D> shapeLookup)
    {
        vertex.setVertex2DShape(shapeLookup.fifth);
        vertex.setVertex3DShape(shapeLookup.sixth);
        vertex.setVertexSize(shapeLookup.fourth);
        vertex.setVertexColor(shapeLookup.third);
    }

    //interaction
   private static void setVertexPropertiesInteraction(Vertex vertex, Tuple7<String, String, GraphmlShapesGroup1, Color, Float, Shapes2D, Shapes3D> shapeLookup)
   {
        vertex.setVertex2DShape(shapeLookup.sixth);
        vertex.setVertex3DShape(shapeLookup.seventh);
        vertex.setVertexSize(shapeLookup.fifth);
        vertex.setVertexColor(shapeLookup.fourth);
       
   }
    
}