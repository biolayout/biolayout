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
        down_left_corner = PointFactory.INSTANCE.newDPoint(d_l_c);
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
        DPoint f_rep_u_on_v = PointFactory.INSTANCE.newDPoint();
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
            F_rep.set(v, PointFactory.INSTANCE.newDPoint());
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
                    f_rep_u_on_v = vector_v_minus_u.scaled(scalar);
                }
                F_rep.set(v, F_rep.get(v).plus(f_rep_u_on_v));
                F_rep.set(u, F_rep.get(u).minus(f_rep_u_on_v));
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
        List<IPoint> neighbour_boxes;
        List<node> neighbour_box;
        IPoint act_neighbour_box;
        IPoint neighbour;
        DPoint f_rep_u_on_v = PointFactory.INSTANCE.newDPoint();
        DPoint vector_v_minus_u;
        DPoint pos_u, pos_v;
        double norm_v_minus_u;
        double scalar;

        int i, j, k, act_i, act_j, act_k, uIndex, vIndex, length;
        node u, v;
        double x, y, gridboxlength;//length of a box in the GRID

        //init F_rep
        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            F_rep.set(v, PointFactory.INSTANCE.newDPoint());
        }

        //init max_gridindex and set contained_nodes;

        max_gridindex = (int) (Math.sqrt((double) (G.numberOfNodes())) / grid_quotient());
        max_gridindex = ((max_gridindex > 0) ? max_gridindex : 1);
        int i_num_grid_cells = max_gridindex;
        int j_num_grid_cells = max_gridindex;
        int k_num_grid_cells = PointFactory.INSTANCE.dimensions() == PointFactory.Dimensions._2 ? 1 : max_gridindex;
        List<node>[][][] contained_nodes = new ArrayList[i_num_grid_cells][j_num_grid_cells][k_num_grid_cells];

        for (i = 0; i < i_num_grid_cells; i++)
        {
            for (j = 0; j < j_num_grid_cells; j++)
            {
                for (k = 0; k < k_num_grid_cells; k++)
                {
                    contained_nodes[i][j][k] = new ArrayList();
                }
            }
        }

        gridboxlength = boxlength / max_gridindex;
        for (Iterator<node> iter = G.nodesIterator(); iter.hasNext();)
        {
            v = iter.next();
            DPoint offset = A.get(v).get_position().minus(down_left_corner);
            int x_index = (int) (offset.getX() / gridboxlength);
            int y_index = (int) (offset.getY() / gridboxlength);
            int z_index = (int) (offset.getZ() / gridboxlength);
            contained_nodes[x_index][y_index][z_index].add(v);
        }

        //force calculation

        for (i = 0; i < i_num_grid_cells; i++)
        {
            for (j = 0; j < j_num_grid_cells; j++)
            {
                for (k = 0; j < k_num_grid_cells; j++)
                {
                    //step1: calculate forces inside contained_nodes(i,j,k)

                    length = contained_nodes[i][j][k].size();
                    List<node> nodearray_i_j_k = new ArrayList<node>();
                    for (node n : contained_nodes[i][j][k])
                    {
                        nodearray_i_j_k.add(n);
                    }

                    for (uIndex = 0; uIndex < length; uIndex++)
                    {
                        for (vIndex = uIndex + 1; vIndex < length; vIndex++)
                        {
                            u = nodearray_i_j_k.get(uIndex);
                            v = nodearray_i_j_k.get(vIndex);
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
                                f_rep_u_on_v = vector_v_minus_u.scaled(scalar);
                            }

                            F_rep.set(v, F_rep.get(v).plus(f_rep_u_on_v));
                            F_rep.set(u, F_rep.get(u).minus(f_rep_u_on_v));
                        }
                    }

                    //step 2: calculated forces to nodes in neighbour boxes

                    //find_neighbour_boxes

                    neighbour_boxes = new ArrayList<IPoint>();
                    for (int i_n = i - 1; i_n <= i + 1; i_n++)
                    {
                        for (int j_n = j - 1; j_n <= j + 1; j_n++)
                        {
                            for (int k_n = k - 1; k_n <= k + 1; k_n++)
                            {
                                if ((i_n >= 0) && (j_n >= 0) && (k_n >= 0) &&
                                        (i_n < i_num_grid_cells) && (j_n < j_num_grid_cells) && (k_n < k_num_grid_cells))
                                {
                                    neighbour = PointFactory.INSTANCE.newIPoint();
                                    neighbour.setX(i_n);
                                    neighbour.setY(j_n);
                                    neighbour.setZ(k_n);

                                    if ((i_n != i) || (j_n != j) || (k_n != k))
                                    {
                                        neighbour_boxes.add(neighbour);
                                    }
                                }
                            }
                        }
                    }


                    //forget neighbour_boxes that already had access to this box
                    for (IPoint act_neighbour_box_it : neighbour_boxes)
                    {//forall
                        act_i = act_neighbour_box_it.getX();
                        act_j = act_neighbour_box_it.getY();
                        act_k = act_neighbour_box_it.getZ();

                        boolean top = (act_k == k - 1 && !(act_i == i - 1 && act_j == j - 1));
                        boolean middle = (act_k == k && (act_j == j + 1 || (act_j == j && act_i == i + 1)));
                        boolean bottom = (act_k == k + 1 && (act_i == i + 1 && act_j == j + 1));

                        if (top || middle || bottom)
                        {//if1
                            for (node v_it : contained_nodes[i][j][k])
                            {
                                for (node u_it : contained_nodes[act_i][act_j][act_k])
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
                                        f_rep_u_on_v = vector_v_minus_u.scaled(scalar);
                                    }
                                    F_rep.set(v_it, F_rep.get(v_it).plus(f_rep_u_on_v));
                                    F_rep.set(u_it, F_rep.get(u_it).minus(f_rep_u_on_v));
                                }//for
                            }
                        }//if1
                    }//forall
                }
            }
        }
    }

    public void make_initialisations(double bl, DPoint d_l_c, int grid_quot)
    {
        grid_quotient(grid_quot);
        down_left_corner = PointFactory.INSTANCE.newDPoint(d_l_c); //export this two values from FMMM
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
