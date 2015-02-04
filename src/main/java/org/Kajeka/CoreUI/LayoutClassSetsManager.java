package org.Kajeka.CoreUI;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.*;
import org.Kajeka.Network.*;
import static org.Kajeka.Environment.GlobalEnvironment.*;
import static org.Kajeka.DebugConsole.ConsoleOutput.*;
import org.biopax.paxtools.model.level3.Entity;

/**
*
* @author Anton Enright,
* @author full refactoring by Thanos Theo, 2008-2009
* @author Derek Wright 2014
* @version 3.3
*
*/

public final class LayoutClassSetsManager
{
    private ArrayList<LayoutClasses> classSetNames = null;
    private HashMap<LayoutClasses, String> classSetNamesMap = null;
    private HashMap<String, LayoutClasses> classSetNameIDsMap = null;

    private String currentClassSetName = "";
    private int currentClassSetID = 0;
    private int totalclassSetNames = 0;

    /**
     * Bidirectional Map of BioPAX entities to graph vertices. Populated when parsing BioPAX OWL data.
     * Used to display BioPAX fields associated with a node in the Class Viewer.
     * Null until create method called.
     */
    private HashBiMap<Entity, Vertex> entityVertexMap = null;

    public LayoutClassSetsManager()
    {
        classSetNames = new ArrayList<LayoutClasses>();
        classSetNamesMap = new HashMap<LayoutClasses, String>();
        classSetNameIDsMap = new HashMap<String, LayoutClasses>();
    }

    public LayoutClasses createNewClassSet(String newName)
    {
        if ( classSetExists(newName) )
        {
            return getClassSet(newName);
        }
        else
        {
            LayoutClasses classes = new LayoutClasses(newName, totalclassSetNames);

            classSetNames.add(classes);
            classSetNamesMap.put(classes, newName);
            classSetNameIDsMap.put(newName, classes);

            if (DEBUG_BUILD) println("Creating New Class Set: " + newName);

            LayoutClasses rootclasses = classSetNames.get(0);
            for ( Vertex vertex : rootclasses.getClassesMembershipMap().keySet() )
                classes.setClass(vertex, 0);

            totalclassSetNames++;

            return classes;
        }
    }

    public boolean classSetExists(String name)
    {
        return classSetNameIDsMap.containsKey(name);
    }

    public LayoutClasses getClassSet(String name)
    {
        return ( classSetExists(name) ) ? getClassSetByName(name) : createNewClassSet(name);
    }

    public LayoutClasses getClassSet(int classSetID)
    {
        return classSetNames.get(classSetID);
    }

    public void switchClassSet(String classSetName)
    {
        if ( classSetExists(classSetName) )
        {
            currentClassSetName = classSetName;
            currentClassSetID = getClassSetByName(classSetName).getClassSetID();
        }
    }

    public LayoutClasses getCurrentClassSetAllClasses()
    {
        return classSetNames.get(currentClassSetID);
    }

    public String getCurrentClassSetName()
    {
        return currentClassSetName;
    }

    public int getCurrentClassSetID()
    {
        return currentClassSetID;
    }

    public ArrayList<LayoutClasses> getClassSetNames()
    {
        return classSetNames;
    }

    public int getTotalClassSets()
    {
        return totalclassSetNames;
    }

    public LayoutClasses getClassSetByName(String name)
    {
        return classSetNameIDsMap.get(name);
    }

    public void clearClassSets()
    {
        for (LayoutClasses layoutClasses : classSetNames)
            layoutClasses.clearClasses();

        LayoutClasses.resetVertexClassColorAssigner();

        currentClassSetName = "";
        currentClassSetID = 0;
        totalclassSetNames = 0;

        classSetNames.clear();
        classSetNamesMap.clear();
        classSetNameIDsMap.clear();
        entityVertexMap = null; //may never have been initialized

        createNewClassSet("Default Classes");
    }

    public BiMap<Entity, Vertex> getEntityVertexMap()
    {
        return entityVertexMap;
    }

    /**
     * Creates a new empty BioPAX Entity to Vertex Map with capacity initialized according to the number of BioPAX entities
     * @param entitySet - a set of BioPAX Entity
     * @return the new HashMap
     */
    public BiMap<Entity, Vertex> createEntityVertexMap(Set<Entity> entitySet)
    {
        entityVertexMap = HashBiMap.create(entitySet.size());
        return entityVertexMap;
    }
}