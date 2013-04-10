package ogdf.energybased;

/*
 * $Revision: 2552 $
 *
 * last checkin:
 *   $Author: gutwenger $
 *   $Date: 2012-07-05 16:45:20 +0200 (Do, 05. Jul 2012) $
 ***************************************************************/
/**
 * \file \brief Implementation of class FruchtermanReingold (computation of forces).
 *
 * \author Stefan Hachul
 *
 * \par License: This file is part of the Open Graph Drawing Framework (OGDF).
 *
 * \par Copyright (C)<br> See README.txt in the root directory of the OGDF installation for details.
 *
 * \par This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License Version 2 or 3 as published by the Free Software Foundation; see the file LICENSE.txt included in the
 * packaging of this file for details.
 *
 * \par This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * \par You should have received a copy of the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * \see http://www.gnu.org/copyleft/gpl.html
 **************************************************************
 */

import java.util.*;
import ogdf.basic.*;
import static org.BioLayoutExpress3D.Environment.GlobalEnvironment.*;
import static org.BioLayoutExpress3D.DebugConsole.ConsoleOutput.*;

class FruchtermanReingold
{
    //Import updated information of the drawing area.

    public void update_boxlength_and_cornercoordinate(double b_l, DPoint d_l_c)
    {
        boxlength = b_l;
        down_left_corner = new DPoint(d_l_c);
    }
    private int _grid_quotient;//for coarsening the FrRe-grid
    private int max_gridindex; //maximum index of a grid row/column
    private double boxlength;  //length of drawing box
    private DPoint down_left_corner;//down left corner of drawing box

    //The number k of rows and colums of the grid is sqrt(|V|) / frGridQuotient()
    //(Note that in [FrRe] frGridQuotient() is 2.)
    private void grid_quotient(int p)
    {
        _grid_quotient = ((0 <= p) ? p : 2);
    }

    private int grid_quotient()
    {
        return _grid_quotient;
    }

    public FruchtermanReingold()
    {
        grid_quotient(2);
    }

    public void calculate_exact_repulsive_forces(
            Graph G,
            NodeArray<NodeAttributes> A,
            NodeArray<DPoint> F_rep)
    {
        //naive algorithm by Fruchterman & Reingold
        numexcept N;
        node v, u;
        DPoint f_rep_u_on_v = new DPoint();
        DPoint vector_v_minus_u;
        DPoint pos_u, pos_v;
        double norm_v_minus_u;
        int node_number = G.numberOfNodes();
        List<node> array_of_the_nodes = new ArrayList<node>();
        int i, j;
        double scalar;

        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            F_rep.set(v, new DPoint());
        }

        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            array_of_the_nodes.add(v);
        }

        for (i = 0; i < node_number; i++)
        {
            for (j = i + 1; j < node_number; j++)
            {
                u = array_of_the_nodes.get(i);
                v = array_of_the_nodes.get(j);
                pos_u = A.get(u).get_position();
                pos_v = A.get(v).get_position();
                if (pos_u == pos_v)
                {//if2  (Exception handling if two nodes have the same position)
                    pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
                }//if2
                vector_v_minus_u = pos_v.minus(pos_u);
                norm_v_minus_u = vector_v_minus_u.norm();
                if (!numexcept.f_rep_near_machine_precision(norm_v_minus_u, f_rep_u_on_v))
                {
                    scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
                    f_rep_u_on_v.m_x = scalar * vector_v_minus_u.m_x;
                    f_rep_u_on_v.m_y = scalar * vector_v_minus_u.m_y;
                }
                F_rep.set(v, new DPoint(F_rep.get(v).plus(f_rep_u_on_v)));
                F_rep.set(u, new DPoint(F_rep.get(u).minus(f_rep_u_on_v)));
            }
        }
    }

    public void calculate_approx_repulsive_forces(
            Graph G,
            NodeArray<NodeAttributes> A,
            NodeArray<DPoint> F_rep)
    {
        //GRID algorithm by Fruchterman & Reingold
        numexcept N;
        List<DPoint> neighbour_boxes; // should be IPoint
        List<node> neighbour_box;
        DPoint act_neighbour_box; // should be IPoint
        DPoint neighbour; // should be IPoint
        DPoint f_rep_u_on_v = new DPoint();
        DPoint vector_v_minus_u;
        DPoint pos_u, pos_v;
        double norm_v_minus_u;
        double scalar;

        int i, j, act_i, act_j, k, l, length;
        node u, v;
        double x, y, gridboxlength;//length of a box in the GRID
        int x_index, y_index;

        //init F_rep
        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            F_rep.set(v, new DPoint());
        }

        //init max_gridindex and set contained_nodes;

        max_gridindex = (int) (Math.sqrt((double) (G.numberOfNodes())) / grid_quotient());
        max_gridindex = ((max_gridindex > 0) ? max_gridindex : 1);
        List<node>[][] contained_nodes = new ArrayList[max_gridindex][max_gridindex];

        for (i = 0; i < max_gridindex; i++)
        {
            for (j = 0; j < max_gridindex; j++)
            {
                contained_nodes[i][j] = new ArrayList();
            }
        }

        gridboxlength = boxlength / max_gridindex;
        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            x = A.get(v).get_x() - down_left_corner.m_x;//shift comput. box to nullpoint
            y = A.get(v).get_y() - down_left_corner.m_y;
            x_index = (int) (x / gridboxlength);
            y_index = (int) (y / gridboxlength);
            contained_nodes[x_index][y_index].add(v);
        }

        //force calculation

        for (i = 0; i < max_gridindex; i++)
        {
            for (j = 0; j < max_gridindex; j++)
            {
                //step1: calculate forces inside contained_nodes(i,j)

                length = contained_nodes[i][j].size();
                List<node> nodearray_i_j = new ArrayList<node>();
                for (node n : contained_nodes[i][j])
                {
                    nodearray_i_j.add(n);
                }

                for (k = 0; k < length; k++)
                {
                    for (l = k + 1; l < length; l++)
                    {
                        u = nodearray_i_j.get(k);
                        v = nodearray_i_j.get(l);
                        pos_u = A.get(u).get_position();
                        pos_v = A.get(v).get_position();
                        if (pos_u == pos_v)
                        {//if2  (Exception handling if two nodes have the same position)
                            pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
                        }//if2
                        vector_v_minus_u = pos_v.minus(pos_u);
                        norm_v_minus_u = vector_v_minus_u.norm();

                        if (!numexcept.f_rep_near_machine_precision(norm_v_minus_u, f_rep_u_on_v))
                        {
                            scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
                            f_rep_u_on_v.m_x = scalar * vector_v_minus_u.m_x;
                            f_rep_u_on_v.m_y = scalar * vector_v_minus_u.m_y;
                        }

                        F_rep.set(v, new DPoint(F_rep.get(v).plus(f_rep_u_on_v)));
                        F_rep.set(u, new DPoint(F_rep.get(u).minus(f_rep_u_on_v)));
                    }
                }

                //step 2: calculated forces to nodes in neighbour boxes

                //find_neighbour_boxes

                neighbour_boxes = new ArrayList<DPoint>();
                for (k = i - 1; k <= i + 1; k++)
                {
                    for (l = j - 1; l <= j + 1; l++)
                    {
                        if ((k >= 0) && (l >= 0) && (k < max_gridindex) && (l < max_gridindex))
                        {
                            neighbour = new DPoint(k, l);
                            if ((k != i) || (l != j))
                            {
                                neighbour_boxes.add(neighbour);
                            }
                        }
                    }
                }


                //forget neighbour_boxes that already had access to this box
                for (DPoint act_neighbour_box_it : neighbour_boxes)
                {//forall
                    act_i = (int) act_neighbour_box_it.m_x;
                    act_j = (int) act_neighbour_box_it.m_y;
                    if ((act_j == j + 1) || ((act_j == j) && (act_i == i + 1)))
                    {//if1
                        for (node v_it : contained_nodes[i][j])
                        {
                            for (node u_it : contained_nodes[act_i][act_j])
                            {//for
                                pos_u = A.get(u_it).get_position();
                                pos_v = A.get(v_it).get_position();
                                if (pos_u == pos_v)
                                {//if2  (Exception handling if two nodes have the same position)
                                    pos_u = numexcept.choose_distinct_random_point_in_radius_epsilon(pos_u);
                                }//if2
                                vector_v_minus_u = pos_v.minus(pos_u);
                                norm_v_minus_u = vector_v_minus_u.norm();

                                if (!numexcept.f_rep_near_machine_precision(norm_v_minus_u, f_rep_u_on_v))
                                {
                                    scalar = f_rep_scalar(norm_v_minus_u) / norm_v_minus_u;
                                    f_rep_u_on_v.m_x = scalar * vector_v_minus_u.m_x;
                                    f_rep_u_on_v.m_y = scalar * vector_v_minus_u.m_y;
                                }
                                F_rep.set(v_it, new DPoint(F_rep.get(v_it).plus(f_rep_u_on_v)));
                                F_rep.set(u_it, new DPoint(F_rep.get(u_it).minus(f_rep_u_on_v)));
                            }//for
                        }
                    }//if1
                }//forall
            }
        }
    }

    public void make_initialisations(double bl, DPoint d_l_c, int grid_quot)
    {
        grid_quotient(grid_quot);
        down_left_corner = new DPoint(d_l_c); //export this two values from FMMM
        boxlength = bl;
    }

    public double f_rep_scalar(double d)
    {
        if (d > 0.0)
        {
            return 1.0 / d;
        }
        else
        {
            if (DEBUG_BUILD)
            {
                println("Error  f_rep_scalar nodes at same position");
            }
            return 0.0;
        }
    }
}
