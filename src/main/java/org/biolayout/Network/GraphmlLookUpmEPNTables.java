package org.biolayout.Network;

import org.biolayout.DataStructures.*;
import org.biolayout.Environment.GlobalEnvironment.*;
import static java.awt.Color.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.biolayout.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup1.*;
import static org.biolayout.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup2.*;
import static org.biolayout.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup3.*;
import static org.biolayout.Environment.GlobalEnvironment.Shapes2D.*;
import static org.biolayout.Environment.GlobalEnvironment.Shapes3D.*;

/**
* Graphml LookUp mEPN tables.
*
*
* @author Thanos Theo, 2009-2010-2011
* @author Derek Wright
* @version 3.0.0.0
*
*/

public final class GraphmlLookUpmEPNTables
{

    /**
    *  mEPN notation Class Set name.
    */
    public static final String MEPN_NOTATION_CLASS_SET_NAME = "mEPN Scheme Version 2.0";

    /**
    *  Shapes group 1 enumeration, used to do a look-up for name & shape but also returning color.
    */
    public static enum GraphmlShapesGroup1 {
      // Boolean Logic Operators
      AND,
      OR,

      // Process Nodes
      BINDING,
      OLIGOMERIZATION,
      CLEAVAGE,
      AUTO_CLEAVAGE,
      DISSOCIATION,
      RATE_LIMITING_CATALYSIS,
      CATALYSIS,
      AUTO_CATALYSIS,
      TRANSLOCATION,
      TRANSLATION,
      TRANSCRIPTION,
      ACTIVATION,
      INHIBITION,
      PHOSPHORYLATION,
      DEPHOSPHORYLATION,
      AUTO_PHOSPHORYLATION,
      PHOSPHO_TRANSFER,
      UBIQUITISATION,
      SUMOYLATION,
      SELENYLATION,
      GLYCOSYLATION,
      PRENYLATION,
      METHYLATION,
      ACETYLATION,
      PALMITOYLATION,
      PROTONATION,
      SULPHATION,
      PEGYLATION,
      MYRISTOYLATION,
      HYDROXYLATION,
      SECRETION,
      SINK_PROTEASOMAL_DEGRADATION,
      OXIDATION,
      MUTATION,
      ISOMERISATION,
      DIFFUSION,
      REDUCTION,
      DIFFERENTIATION,
      MIGRATION,
      APOPTOSIS,
      MATURATION,
      PROLIFERATION,
      PRIME,
      PROMOTE,
      UNKNOWN_TRANSITION,

      // Edge Annotations
      EDGE_ACTIVATES,
      EDGE_INHIBITS,
      EDGE_CATALYSIS,

      // Dummy Component
      DUMMY_COMPONENT,

      // No mEPN Notation
      NONE
    };

    /**
    *  GraphmlShapesGroup1 graphml shapes which will be functioning as transitions in the mSPN simulation.
    */
    public static final GraphmlShapesGroup1[] GRAPHML_SHAPES_TO_TRANSITIONS = {
      // Boolean Logic Operators
      AND,

      // Process Nodes
      BINDING,
      OLIGOMERIZATION,
      CLEAVAGE,
      AUTO_CLEAVAGE,
      DISSOCIATION,
      RATE_LIMITING_CATALYSIS,
      CATALYSIS,
      AUTO_CATALYSIS,
      TRANSLOCATION,
      TRANSLATION,
      TRANSCRIPTION,
      ACTIVATION,
      INHIBITION,
      PHOSPHORYLATION,
      DEPHOSPHORYLATION,
      AUTO_PHOSPHORYLATION,
      PHOSPHO_TRANSFER,
      UBIQUITISATION,
      SUMOYLATION,
      SELENYLATION,
      GLYCOSYLATION,
      PRENYLATION,
      METHYLATION,
      ACETYLATION,
      PALMITOYLATION,
      PROTONATION,
      SULPHATION,
      PEGYLATION,
      MYRISTOYLATION,
      HYDROXYLATION,
      SECRETION,
      SINK_PROTEASOMAL_DEGRADATION,
      OXIDATION,
      MUTATION,
      ISOMERISATION,
      DIFFUSION,
      REDUCTION,
      DIFFERENTIATION,
      MIGRATION,
      APOPTOSIS,
      MATURATION,
      PROLIFERATION,
      PRIME,
      PROMOTE,
      UNKNOWN_TRANSITION
    };

    /**
    *  Look-up table 1, used to do a look-up for name & shape but also returning color.
    *  Type Tuple7<String, String, GraphmlShapesGroup1, Color, Float, Shapes2D, Shapes3D>.
    */
    public static final Tuple7[] GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_1 = {
      // Boolean Logic Operators
      Tuples.tuple("&",   "ellipse", AND,                          decode("#FF9900"), 5.0f, CIRCLE, TORUS_8_PETALS),
      Tuples.tuple("OR",  "ellipse", OR,                           decode("#CC99FF"), 5.0f, CIRCLE, SAUCER_4_PETALS),

      // Process Nodes
      Tuples.tuple("B",   "ellipse", BINDING,                      decode("#FFFF99"), 5.0f, CIRCLE, SPHERE), // BioPAX ComplexAssembly
      Tuples.tuple("O",   "ellipse", OLIGOMERIZATION,              decode("#FFFF99"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("X",   "ellipse", CLEAVAGE,                     decode("#BFBFBF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("AX",  "ellipse", AUTO_CLEAVAGE,                decode("#BFBFBF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("D",   "ellipse", DISSOCIATION,                 decode("#FFCC99"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("RLC", "ellipse", RATE_LIMITING_CATALYSIS,      decode("#9999FF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("C",   "ellipse", CATALYSIS,                    decode("#9999FF"), 5.0f, CIRCLE, SPHERE), // BioPAX BiochemicalReaction/Catalysis
      Tuples.tuple("AC",  "ellipse", AUTO_CATALYSIS,               decode("#9999FF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("T",   "ellipse", TRANSLOCATION,                decode("#00CCFF"), 5.0f, CIRCLE, SPHERE), // BioPAX Transport
      Tuples.tuple("TL",  "ellipse", TRANSLATION,                  decode("#99CCFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("TR",  "ellipse", TRANSCRIPTION,                decode("#99CCFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("A",   "ellipse", ACTIVATION,                   decode("#00CC33"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("I",   "ellipse", INHIBITION,                   decode("#FF0000"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("P",   "ellipse", PHOSPHORYLATION,              decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("-P",  "ellipse", DEPHOSPHORYLATION,            decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("AP",  "ellipse", AUTO_PHOSPHORYLATION,         decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("PT",  "ellipse", PHOSPHO_TRANSFER,             decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Ub",  "ellipse", UBIQUITISATION,               decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Su",  "ellipse", SUMOYLATION,                  decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Se",  "ellipse", SELENYLATION,                 decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Gy",  "ellipse", GLYCOSYLATION,                decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Pr",  "ellipse", PRENYLATION,                  decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Me",  "ellipse", METHYLATION,                  decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Ac",  "ellipse", ACETYLATION,                  decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Pa",  "ellipse", PALMITOYLATION,               decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("H+",  "ellipse", PROTONATION,                  decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Sp",  "ellipse", SULPHATION,                   decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Pe",  "ellipse", PEGYLATION,                   decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("My",  "ellipse", MYRISTOYLATION,               decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("OH",  "ellipse", HYDROXYLATION,                decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("S",   "ellipse", SECRETION,                    decode("#CCFFCC"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("/",   "ellipse", SINK_PROTEASOMAL_DEGRADATION, decode("#FFFFFF"), 5.0f, CIRCLE, TORUS),  // BioPAX Degradation
      Tuples.tuple("Ox",  "ellipse", OXIDATION,                    decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("M",   "ellipse", MUTATION,                     decode("#FF99CC"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Is",  "ellipse", ISOMERISATION,                decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Df",  "ellipse", DIFFUSION,                    decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Re",  "ellipse", REDUCTION,                    decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("?",   "ellipse", UNKNOWN_TRANSITION,           decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE), // BioPAX Interaction

      Tuples.tuple("Diff","ellipse", DIFFERENTIATION,              decode("#99FF99"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Migr","ellipse", MIGRATION,                    decode("#00CCFF"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Apop","ellipse", APOPTOSIS,                    decode("#FF6666"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Matu","ellipse", MATURATION,                   decode("#FF99CC"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Prol","ellipse", PROLIFERATION,                decode("#CCFFCC"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Prim","ellipse", PRIME,                        decode("#FF9900"), 5.0f, CIRCLE, SPHERE),
      Tuples.tuple("Prom","ellipse", PROMOTE,                      decode("#9999FF"), 5.0f, CIRCLE, SPHERE),

      // Edge Annotations
      Tuples.tuple("A",   "diamond", EDGE_ACTIVATES,               decode("#00CC33"), 5.0f, DIAMOND, OCTAHEDRON),
      Tuples.tuple("I",   "diamond", EDGE_INHIBITS,                decode("#FF0000"), 5.0f, DIAMOND, OCTAHEDRON),
      Tuples.tuple("C",   "diamond", EDGE_CATALYSIS,               decode("#CC99FF"), 5.0f, DIAMOND, OCTAHEDRON)
    };

    public static Tuple7 mepnShapeFor(GraphmlShapesGroup1 type)
    {
      for(int i = 0; i < GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_1.length; i++)
      {
        Tuple7 mepnShape = GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_1[i];

        if(mepnShape.third == type)
          return mepnShape;
      }

      return null;
    }

    /**
    *  Shapes group 2 enumeration, used to do a look-up for shape & color.
    */
    public static enum GraphmlShapesGroup2 {
      // Other
      PATHWAY_MODULE,

      // Petri Net Transition Nodes
      TRANSITION_VERTICAL,
      TRANSITION_HORIZONTAL,
      TRANSITION_DIAMOND,

      // No mEPN Notation
      NONE
    };

    /**
    *  Look-up table 2, used to do a look-up for shape & color.
    *  Type Tuple6<String, GraphmlShapesGroup2, Color, Float, Shapes2D, Shapes3D>.
    */
    public static final Tuple6[] GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_2 = {
      // Other
      Tuples.tuple("octagon",   PATHWAY_MODULE,        null,             12.0f, HEXAGON,                       DODECAHEDRON),

      // Petri Net Transition Nodes
      Tuples.tuple("rectangle", TRANSITION_VERTICAL,   decode("#000000"), 5.0f, Shapes2D.RECTANGLE_VERTICAL,   Shapes3D.RECTANGLE_VERTICAL),
      Tuples.tuple("rectangle", TRANSITION_HORIZONTAL, decode("#000000"), 5.0f, Shapes2D.RECTANGLE_HORIZONTAL, Shapes3D.RECTANGLE_HORIZONTAL), // distinguish by width/height sizes
      Tuples.tuple("diamond",   TRANSITION_DIAMOND,    decode("#000000"), 4.0f, DIAMOND,                       OCTAHEDRON),
    };

    public static Tuple6 mepnShapeFor(GraphmlShapesGroup2 type)
    {
      for(int i = 0; i < GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_2.length; i++)
      {
        Tuple6 mepnShape = GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_2[i];

        if(mepnShape.second == type)
          return mepnShape;
      }

      return null;
    }

    /**
    *  Shapes group 3 enumeration, used to do a look-up for shape only.
    */
    public static enum GraphmlShapesGroup3 {
      // Components
      PROTEIN_COMPLEX,
      PROTEIN_PEPTIDE,
      GENE,
      DNA_SEQUENCE, //used for BioPAX DnaRegion
      SIMPLE_BIOCHEMICAL,
      GENERIC_ENTITY,
      DRUG,
      ION_SIMPLE_MOLECULE,
      CELL,
      BACTERIA,
      VIRUS,
      PRIMARY_INPUT,
      TERMINAL_OUTPUT,

      // Other
      ENERGY_MOLECULAR_TRANSFER,
      CONDITIONAL_SWITCH,

      //BioPAX
      DNA,
      RNA,
      RNA_REGION,

      // No mEPN Notation
      NONE
    };

    /**
    *  Look-up table 3, used to do a look-up for shape only, color used for BL-side rendering colorization so as to avoid random node color assignment.
    *  Type Tuple6<String, GraphmlShapesGroup3, Color, Float, Shapes2D, Shapes3D>.
    */
    public static final Tuple6[] GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3 = {
      // Components
      Tuples.tuple("roundrectangle", PROTEIN_COMPLEX,           decode("#FFFF99"), 10.0f, ROUND_RECTANGLE, Shapes3D.ROUND_CUBE_LARGE),
      Tuples.tuple("roundrectangle", PROTEIN_PEPTIDE,           decode("#CCFFFF"),  7.0f, ROUND_RECTANGLE, Shapes3D.ROUND_CUBE_THIN), // distinguish by using ':' in name
      Tuples.tuple("rectangle",      GENE,                      decode("#CCCC00"), 25.0f, RECTANGLE,       Shapes3D.RECTANGLE_HORIZONTAL),
      Tuples.tuple("parallelogram",  DNA_SEQUENCE,              decode("#CCCC00"),  6.0f, PARALLELOGRAM,   Shapes3D.CONE_RIGHT),
      Tuples.tuple("hexagon",        SIMPLE_BIOCHEMICAL,        decode("#FFA200"), 10.0f, HEXAGON,         Shapes3D.PINEAPPLE_SLICE_TOROID),
      Tuples.tuple("ellipse",        GENERIC_ENTITY,            decode("#CC99FF"), 15.5f, CIRCLE,          Shapes3D.PINEAPPLE_SLICE_ELLIPSOID),
      Tuples.tuple("trapezoid",      DRUG,                      decode("#FFFF00"),  5.0f, TRAPEZOID1,      Shapes3D.DOUBLE_PYRAMID_THIN),
      Tuples.tuple("diamond",        ION_SIMPLE_MOLECULE,       decode("#C0C0C0"),  5.0f, DIAMOND,         Shapes3D.DOUBLE_PYRAMID_LARGE),

      Tuples.tuple("ellipse",        CELL,                      decode("#99CCFF"), 25.0f, ELLIPSE,         Shapes3D.PINEAPPLE_SLICE_ELLIPSOID),
      Tuples.tuple("star5",          BACTERIA,                  decode("#00CCCC"), 15.0f, STAR5,           Shapes3D.DOUBLE_PYRAMID_LARGE),
      Tuples.tuple("star8",          VIRUS,                     decode("#00FFFF"), 15.0f, STAR8,           Shapes3D.SAUCER_12_PETALS),
      Tuples.tuple("fatarrow",       PRIMARY_INPUT,             decode("#99FF99"), 15.0f, FATARROW,        Shapes3D.CONE_RIGHT),
      Tuples.tuple("star6",          TERMINAL_OUTPUT,           decode("#FF6666"), 15.0f, STAR6,           Shapes3D.SAUCER_9_PETALS),

      // Other
      Tuples.tuple("trapezoid2",     ENERGY_MOLECULAR_TRANSFER, decode("#FFFFFF"), 10.0f, TRAPEZOID2,      Shapes3D.TRAPEZOID_DOWN),
      Tuples.tuple("octagon",        CONDITIONAL_SWITCH,        decode("#FF0000"), 20.0f, OCTAGON,         Shapes3D.ICOSAHEDRON),

      //BioPAX
      Tuples.tuple("rectangle",      DNA,                      decode("#CCCC00"), 25.0f, RECTANGLE,       Shapes3D.GENE_MODEL),
      Tuples.tuple("parallelogram",  RNA,                      decode("#CCCC00"), 25.0f, PARALLELOGRAM,   Shapes3D.GENE_MODEL), //TODO SINGLE HELIX
      Tuples.tuple("parallelogram",  RNA_REGION,               decode("#CCCC00"), 6.0f,  PARALLELOGRAM,   Shapes3D.DUMB_BELL) //DUMBELL
    };

    public static Tuple6 mepnShapeFor(GraphmlShapesGroup3 type)
    {
      for(int i = 0; i < GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3.length; i++)
      {
        Tuple6 mepnShape = GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[i];

        if(mepnShape.second == type)
          return mepnShape;
      }

      return null;
    }

    /**
     * Map of BioPAX PhysicalEntity, Gene and Pathway class names to mEPN shapes.
     * Includes new shapes for BioPAX entities that do not map readily to mEPN.
     * @return a Map with key BioPAX type and value a tuple describing node shape, color and size
     */
    public static final Map<String, Tuple6> BIOPAX_MEPN_MAP;


    /**
     * Map of BioPAX Interaction class names to mEPN shapes.
     * Includes new shapes for BioPAX entities that do not map readily to mEPN.
     * @return a Map with key BioPAX type and value a tuple describing node shape, color and size
     */
    public static final Map<String, Tuple7> BIOPAX_MEPN_INTERACTION_MAP;

    //Create BIOPAX_MEPN_MAP
    static
    {
        Map<String, Tuple6> entityNameMap = new HashMap<String, Tuple6>();

        //physical entities
        entityNameMap.put("Complex", mepnShapeFor(PROTEIN_COMPLEX));
        entityNameMap.put("Dna", mepnShapeFor(DNA));
        entityNameMap.put("DnaRegion", mepnShapeFor(DNA_SEQUENCE));
        entityNameMap.put("Rna", mepnShapeFor(RNA));
        entityNameMap.put("RnaRegion", mepnShapeFor(RNA_REGION));
        entityNameMap.put("NucleicAcid", mepnShapeFor(GENE));
        entityNameMap.put("Protein", mepnShapeFor(PROTEIN_PEPTIDE));
        entityNameMap.put("SimplePhysicalEntity", mepnShapeFor(GENERIC_ENTITY));
        entityNameMap.put("SmallMolecule", mepnShapeFor(ION_SIMPLE_MOLECULE));

        entityNameMap.put("PhysicalEntity", mepnShapeFor(GENERIC_ENTITY)); // (default)

        entityNameMap.put("Pathway", mepnShapeFor(PATHWAY_MODULE));

        entityNameMap.put("Gene", mepnShapeFor(GENE));

        BIOPAX_MEPN_MAP = Collections.unmodifiableMap(entityNameMap);


        //interactions
        //BioPAX: BiochemicalReaction, Catalysis, ComplexAssembly, Control,     Conversion, Degradation, GeneticInteraction, Modulation MOD, MolecularInteraction, TemplateReaction TRE, TemplateReactionRegulation TRR, Transport T, TransportWithBiochemicalReaction  TWB
        //mEPN: catalysis,          catalysis,   binding        ,( new CTL)   new CON     sink        new(GI)
        Map<String, Tuple7> interactionNameMap = new HashMap<String, Tuple7>();

        interactionNameMap.put("BiochemicalReaction", mepnShapeFor(CATALYSIS));
        interactionNameMap.put("Catalysis", mepnShapeFor(CATALYSIS));


        interactionNameMap.put("ComplexAssembly", mepnShapeFor(BINDING));

        interactionNameMap.put("Control",                   Tuples.tuple("CTL",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //CTL (new)
        interactionNameMap.put("Conversion",                Tuples.tuple("CON",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE));  //CON (new)

        interactionNameMap.put("Degradation", mepnShapeFor(SINK_PROTEASOMAL_DEGRADATION));

        interactionNameMap.put("GeneticInteraction",         Tuples.tuple("GI",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //GI (new)
        interactionNameMap.put("Modulation",                 Tuples.tuple("MOD",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //MOD (new)
        interactionNameMap.put("MolecularInteraction",       Tuples.tuple("MI",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //MI (new)
        interactionNameMap.put("TemplateReaction",           Tuples.tuple("TRE",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //TRE (new)
        interactionNameMap.put("TemplateReactionRegulation", Tuples.tuple("TRE",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE));  //combined with Template Reaction

        interactionNameMap.put("Transport", mepnShapeFor(TRANSLOCATION));

        interactionNameMap.put("TransportWithBiochemicalReaction", Tuples.tuple("TWB",   "ellipse", UNKNOWN_TRANSITION, decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE)); //TWB (new)

        interactionNameMap.put("Interaction",  mepnShapeFor(UNKNOWN_TRANSITION));  // (default)

        BIOPAX_MEPN_INTERACTION_MAP = Collections.unmodifiableMap(interactionNameMap);
    }

    /**
    *  Graphml Petri Net inhibitor arrowhead look-up table.
    */
    public static final String[] GRAPHML_PETRI_NET_INHIBITOR_ARROWHEAD_LOOK_UP_TABLE = { "diamond", "t_shape", "white_diamond", "none" };
}
