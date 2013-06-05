package org.BioLayoutExpress3D.Network;

import org.BioLayoutExpress3D.DataStructures.*;
import org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static java.awt.Color.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup1.*;
import static org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup2.*;
import static org.BioLayoutExpress3D.Network.GraphmlLookUpmEPNTables.GraphmlShapesGroup3.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes2D.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.Shapes3D.*;

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
                                             OLIGERMISATION,
                                             CLEAVAGE,
                                             AUTO_CLEAVAGE,
                                             DISSOCIATION,
                                             RATE_LIMITING_CATALYSIS,
                                             CATALYSIS,
                                             AUTO_CATALYSIS,
                                             TRANSLOCATION,
                                             TRANSCRIPTION_TRANSLATION,
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
                                             UNKNOWN_TRANSITION,

                                             // Edge Annotations
                                             EDGE_ACTIVATES,
                                             EDGE_INHIBITS,
                                             EDGE_CATALYSIS,

                                             // Dummy Component
                                             DUMMY_COMPONENT,

                                             // No mEPN Notation
                                             NONE
                                           }

    /**
    *  GraphmlShapesGroup1 graphml shapes which will be functioning as transitions in the mSPN simulation.
    */
    public static final GraphmlShapesGroup1[] GRAPHML_SHAPES_TO_TRANSITIONS = {
                                                                                // Boolean Logic Operators
                                                                                AND,

                                                                                // Process Nodes
                                                                                BINDING,
                                                                                OLIGERMISATION,
                                                                                CLEAVAGE,
                                                                                AUTO_CLEAVAGE,
                                                                                DISSOCIATION,
                                                                                RATE_LIMITING_CATALYSIS,
                                                                                CATALYSIS,
                                                                                AUTO_CATALYSIS,
                                                                                TRANSLOCATION,
                                                                                TRANSCRIPTION_TRANSLATION,
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
                                                                          Tuples.tuple("B",   "ellipse", BINDING,                      decode("#FFFF99"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("O",   "ellipse", OLIGERMISATION,               decode("#FFFF99"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("X",   "ellipse", CLEAVAGE,                     decode("#BFBFBF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("AX",  "ellipse", AUTO_CLEAVAGE,                decode("#BFBFBF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("D",   "ellipse", DISSOCIATION,                 decode("#FFCC99"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("RLC", "ellipse", RATE_LIMITING_CATALYSIS,      decode("#9999FF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("C",   "ellipse", CATALYSIS,                    decode("#9999FF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("AC",  "ellipse", AUTO_CATALYSIS,               decode("#9999FF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("T",   "ellipse", TRANSLOCATION,                decode("#00CCFF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("TL",  "ellipse", TRANSCRIPTION_TRANSLATION,    decode("#99CCFF"), 5.0f, CIRCLE, SPHERE),
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
                                                                          Tuples.tuple("/",   "ellipse", SINK_PROTEASOMAL_DEGRADATION, decode("#FFFFFF"), 5.0f, CIRCLE, TORUS),
                                                                          Tuples.tuple("Ox",  "ellipse", OXIDATION,                    decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("M",   "ellipse", MUTATION,                     decode("#FF99CC"), 5.0f, CIRCLE, SPHERE),
                                                                          Tuples.tuple("?",   "ellipse", UNKNOWN_TRANSITION,           decode("#F0FFFF"), 5.0f, CIRCLE, SPHERE),

                                                                          // Edge Annotations
                                                                          Tuples.tuple("A",   "diamond", EDGE_ACTIVATES,               decode("#00CC33"), 5.0f, DIAMOND, OCTAHEDRON),
                                                                          Tuples.tuple("I",   "diamond", EDGE_INHIBITS,                decode("#FF0000"), 5.0f, DIAMOND, OCTAHEDRON),
                                                                          Tuples.tuple("C",   "diamond", EDGE_CATALYSIS,               decode("#CC99FF"), 5.0f, DIAMOND, OCTAHEDRON),

                                                                          // Dummy Component
                                                                          Tuples.tuple("",    "ellipse", DUMMY_COMPONENT,              decode("#FFFFFF"), 3.0f, CIRCLE, SPHERE)
                                                                        };

    /**
    *  Shapes group 2 enumeration, used to do a look-up for shape & color.
    */
    public static enum GraphmlShapesGroup2 {
                                             // Other
                                             PATHWAY_MODULE,
                                             PATHWAY_OUTPUT,

                                             // Petri Net Transition Nodes
                                             TRANSITION_VERTICAL,
                                             TRANSITION_HORIZONTAL,
                                             TRANSITION_DIAMOND,

                                             // No mEPN Notation
                                             NONE
                                           }

    /**
    *  Look-up table 2, used to do a look-up for shape & color.
    *  Type Tuple6<String, GraphmlShapesGroup2, Color, Float, Shapes2D, Shapes3D>.
    */
    public static final Tuple6[] GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_2 = {
                                                                          // Other
                                                                          Tuples.tuple("octagon",   PATHWAY_MODULE,        decode("#00FF00"), 5.0f, HEXAGON,                       DODECAHEDRON),
                                                                          Tuples.tuple("octagon",   PATHWAY_OUTPUT,        decode("#F0FFFF"), 5.0f, HEXAGON,                       DODECAHEDRON),

                                                                          // Petri Net Transition Nodes
                                                                          Tuples.tuple("rectangle", TRANSITION_VERTICAL,   decode("#000000"), 5.0f, Shapes2D.RECTANGLE_VERTICAL,   Shapes3D.RECTANGLE_VERTICAL),
                                                                          Tuples.tuple("rectangle", TRANSITION_HORIZONTAL, decode("#000000"), 5.0f, Shapes2D.RECTANGLE_HORIZONTAL, Shapes3D.RECTANGLE_HORIZONTAL), // distinguish by width/height sizes
                                                                          Tuples.tuple("diamond",   TRANSITION_DIAMOND,    decode("#000000"), 4.0f, DIAMOND,                       OCTAHEDRON),
                                                                       };

    /**
    *  Shapes group 3 enumeration, used to do a look-up for shape only.
    */
    public static enum GraphmlShapesGroup3 {
                                             // Components
                                             PROTEIN_COMPLEX,
                                             PROTEIN_PEPTIDE,
                                             GENE,
                                             DNA_SEQUENCE,
                                             SIMPLE_BIOCHEMICAL,
                                             GENERIC_ENTITY,
                                             DRUG,
                                             ION_SIMPLE_MOLECULE,

                                             // Other
                                             ENERGY_MOLECULAR_TRANSFER,
                                             CONDITIONAL_SWITCH,

                                             // No mEPN Notation
                                             NONE
                                           }

    /**
    *  Look-up table 3, used to do a look-up for shape only, color used for BL-side rendering colorization so as to avoid random node color assignment.
    *  Type Tuple6<String, GraphmlShapesGroup3, Color, Float, Shapes2D, Shapes3D>.
    */
    public static final Tuple6[] GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3 = {
                                                                          // Components
                                                                          Tuples.tuple("roundrectangle", PROTEIN_COMPLEX,           decode("#FFFF99"), 10.0f, ROUND_RECTANGLE, ROUND_CUBE_LARGE), //index 0
                                                                          Tuples.tuple("roundrectangle", PROTEIN_PEPTIDE,           decode("#CCFFFF"),  7.0f, ROUND_RECTANGLE, ROUND_CUBE_THIN), // distinguish by using ':' in name //index 1
                                                                          Tuples.tuple("rectangle",      GENE,                      decode("#CCCC00"), 25.0f, RECTANGLE,       GENE_MODEL), //index 2
                                                                          Tuples.tuple("parallelogram",  DNA_SEQUENCE,              decode("#CCCC00"),  6.0f, PARALLELOGRAM,   CONE_RIGHT), //index 3
                                                                          Tuples.tuple("hexagon",        SIMPLE_BIOCHEMICAL,        decode("#FFA200"), 10.0f, HEXAGON,         PINEAPPLE_SLICE_TOROID), //index 4
                                                                          Tuples.tuple("ellipse",        GENERIC_ENTITY,            decode("#CC99FF"), 15.5f, CIRCLE,          PINEAPPLE_SLICE_ELLIPSOID), //index 5
                                                                          Tuples.tuple("trapezoid",      DRUG,                      decode("#FFFF00"),  5.0f, TRAPEZOID1,      DOUBLE_PYRAMID_THIN), //index 6
                                                                          Tuples.tuple("diamond",        ION_SIMPLE_MOLECULE,       decode("#C0C0C0"),  5.0f, DIAMOND,         DOUBLE_PYRAMID_LARGE), //index 7

                                                                          // Other
                                                                          Tuples.tuple("trapezoid2",     ENERGY_MOLECULAR_TRANSFER, decode("#FFFFFF"), 10.0f, TRAPEZOID2,      TRAPEZOID_DOWN), //index 8
                                                                          Tuples.tuple("octagon",        CONDITIONAL_SWITCH,        decode("#FF0000"), 20.0f, OCTAGON,         ICOSAHEDRON) //index 9
                                                                       };
    
    /**
     * Map of BioPAX physical entity names to mEPN shapes.
     * Keys: Complex, Dna, DnaRegion, NucleicAcid, Protein, Rna, RnaRegion, SimplePhysicalEntity, SmallMolecule
     * @return a Map with key BioPAX type and value an entry from GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3
     */
    public static final Map<String, Tuple6> BIOPAX_MEPN_PHYSICAL_ENTITY_MAP;

    //Create BIOPAX_MEPN_PHYSICAL_ENTITY_MAP
    static {
        Map<String, Tuple6> result = new HashMap<String, Tuple6>();
        result.put("Complex", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[0]); //PROTEIN_COMPLEX
        result.put("Dna", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[2]); //GENE
        result.put("DnaRegion", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[2]); //GENE
        result.put("NucleicAcid", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[2]); //GENE
        result.put("Protein", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[1]); //PROTEIN_PEPTIDE
        result.put("Rna", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[2]); //GENE
        result.put("RnaRegion", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[2]); //GENE
        result.put("SimplePhysicalEntity", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[5]); //GENERIC_ENTITY
        result.put("SmallMolecule", GRAPHML_MEPN_SHAPES_LOOKUP_TABLE_3[4]); //SIMPLE_BIOCHEMICAL
        
        BIOPAX_MEPN_PHYSICAL_ENTITY_MAP = Collections.unmodifiableMap(result);
    }
    
    /**
    *  Graphml Petri Net inhibitor arrowhead look-up table.
    */
    public static final String[] GRAPHML_PETRI_NET_INHIBITOR_ARROWHEAD_LOOK_UP_TABLE = { "diamond", "t_shape", "white_diamond", "none" };


}