package CliqueSearchPack;

import java.util.*;
import java.io.*;
import java.lang.StrictMath;
import java.lang.Double;

import GraphPack.WeightedVertexPack.WeightedVertex;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;
import InterfacePack.IntRelate;
import TwoParametersInterfacePack.TwoParametersBehavior;
import ZeroParameterInterfacePack.ZeroParameterBehavior;
import DegreeBucketPack.DegreeBasedPartition;
import WeightBucketPack.WeightBasedPartition;
import ConfigPack.*;

import UsrPausePack.UsrPause;


public class CliqueSearch{
	private Random rand;
	// private BitSet vertex_used_bits;
	private ArrayList<Integer> top_level_weights_wrt_colors;	
	private ArrayList<Integer> last_top_level_weight_updated_colors;
	private final static int MAX_GUESS_NUM = 50;

	public CliqueSearch(int vertex_num, int seed)
	{
		rand = new Random(seed);
		// vertex_used_bits = new BitSet(vertex_num + 1);
		top_level_weights_wrt_colors = new ArrayList<Integer>();
		last_top_level_weight_updated_colors = new ArrayList<Integer>();
	}

	public ArrayList<Integer> get_top_level_weights_wrt_colors()
	{
		return top_level_weights_wrt_colors;
	}


	public ArrayList<Integer> top_weight_vertices(WeightedVertex[] vertices, ArrayList<Integer> remaining_vertex_list)
	{
		ArrayList<Integer> top_weight_v_list = new ArrayList<Integer>();
		int best_weight = 0;
		for(int v : remaining_vertex_list)
		{
			if(vertices[v].get_weight() > best_weight)
			{
				best_weight = vertices[v].get_weight();
				top_weight_v_list.clear();
				top_weight_v_list.add(v);
			}
			else if(vertices[v].get_weight() == best_weight)
			{
				top_weight_v_list.add(v);
			}
		}
		ArrayList<Integer> ret_list = new ArrayList<Integer>();
// System.out.println("top_weight_vertices: " + top_weight_v_list);
		// return new ArrayList<Integer>(top_weight_v_list.subList(0, 150));
		return top_weight_v_list;
	} // public ArrayList<Integer> top_weight_vertices(Vertex[] vertices, ArrayList<Integer> remaining_vertex_list)

	// public ArrayList<Integer> top_degree_vertices(Vertex[] vertices, LinkedList<Integer> remaining_vertex_list)
	private ArrayList<Integer> top_degree_vertices(Vertex[] vertices, ArrayList<Integer> remaining_vertex_list)
	{
		ArrayList<Integer> top_degree_v_list = new ArrayList<Integer>();
		int best_degree = 0;
		for(int v : remaining_vertex_list)
		{
			if(vertices[v].get_degree() > best_degree)
			{
				best_degree = vertices[v].get_degree();
				top_degree_v_list.clear();
				top_degree_v_list.add(v);
			}
			else if(vertices[v].get_degree() == best_degree)
			{
				top_degree_v_list.add(v);
			}
		}
		return top_degree_v_list;
	} // public ArrayList<Integer> top_degree_vertices(Vertex[] vertices, ArrayList<Integer> remaining_vertex_list)

	private ArrayList<Integer> random_degree_greedy_clique(int u, Vertex[] vertices, IntRelate int_relate_ob)
	{
		int[] nbs = vertices[u].get_neighbors();
		Integer[] nbs_int_obj_array = Arrays.stream(nbs).boxed().toArray(Integer[]::new);
		ArrayList<Integer> cand_vertices = new ArrayList<>(Arrays.asList(nbs_int_obj_array));
		ArrayList<Integer> cand_clique = new ArrayList<Integer>();
		cand_clique.add(u);
//System.out.println("having added " + u + " into the clique");
		while(!cand_vertices.isEmpty())
		{
			int best_v = 0;
			int best_degree = 0;
			ArrayList<Integer> top_degree_v_list = new ArrayList<Integer>();
			// Integer[] ia;

			for(int v : cand_vertices)
			{
					if(vertices[v].get_degree() > best_degree)
					{
						best_degree = vertices[v].get_degree();
						top_degree_v_list.clear();
						top_degree_v_list.add(v);
					}
					else if(vertices[v].get_degree() == best_degree)
					{
						top_degree_v_list.add(v);
					}
			} // for(int v : cand_vertices.toArray())

			int rand_top_degree_v = top_degree_v_list.get(rand.nextInt(top_degree_v_list.size()));
			top_degree_v_list.clear();
			cand_clique.add(rand_top_degree_v);
// System.out.println("having added " + rand_top_degree_v + " into the clique");
			for(int i = cand_vertices.size() - 1; i >= 0; i--)
			{
				// if(rand_top_degree_v == cand_vertices.get(i) || is_connected(rand_top_degree_v, cand_vertices.get(i)))
				if(rand_top_degree_v == cand_vertices.get(i) || !int_relate_ob.related((long)rand_top_degree_v, (long)cand_vertices.get(i)))
				{
//System.out.println("remove " + cand_vertices.get(i));
					cand_vertices.remove(i);
				}
			}
//System.out.println("cand_vertices: " + cand_vertices);
		} // while(!cand_vertices.isEmpty())
//System.out.println("One degree-greedy clique completed.");
		return cand_clique;
	} // public ArrayList<Integer> degree_greedy_clique(int u, Vertex[] vertices)


	public void update_top_level_weights_wrt_new_critical_cliques(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, WeightBasedPartition weight_based_partition) // 
	{
		BitSet top_level_weight_wrt_colors_updated_bits = new BitSet(top_level_weights_wrt_colors.size()); // 1 means updated, 0  means the opposite
		for(int i = 0; i <= top_level_weights_wrt_colors.size(); i++)
		{
// System.out.println("as to the " + (i + 1) + "-th color, the top weight is " + (i == top_level_weights_wrt_colors.size() ? 0 : top_level_weights_wrt_colors.get(i)) + ", trying to find a tighter bound+++++++++++++++++++++++");
// UsrPause.press_enter_to_continue();

			if(i < top_level_weights_wrt_colors.size())
			{
				if(i > 0 && !top_level_weight_wrt_colors_updated_bits.get(i-1) && top_level_weights_wrt_colors.get(i) == top_level_weights_wrt_colors.get(i-1))
				// if the top-level weight of the former color failed to be updated, 
				// and that of the current color is equal to it, then this weight will not be updated
				{
System.out.println("the top-level weight of the former color, i.e., the " + (i) + "-th color, failed to be updated,");
System.out.println("which implies that the top-level weight of this color, i.e., the " + (i+1) + "-th color will not (continue to) be updated either, since the weights are equal to each other");
						continue;
				}
			}

			ArrayList<Integer> cand_vertices;
			// prepare cand_vertices and lower_bound for i
			if(i == top_level_weights_wrt_colors.size())
				cand_vertices = weight_based_partition.get_vertex_list_of_weight_at_least(1);
			else
				cand_vertices = weight_based_partition.get_vertex_list_of_weight_at_least(top_level_weights_wrt_colors.get(i) + 1);

			int lower_bound_for_requested_clique = i + 1;
			if(cand_vertices.size() < lower_bound_for_requested_clique)
			{
//System.out.println("cand_vertices.size(): " + cand_vertices.size());
//System.out.println("lower_bound: " + lower_bound_for_requested_clique); 
//System.out.println("too few vertices to form a clique which contains at least " + lower_bound_for_requested_clique + " vertices");
				continue;
			}
			boolean lower_bound_improved = update_top_level_weights_wrt_new_critical_cliques_in_vertex_list(cand_vertices, lower_bound_for_requested_clique, colored_clique, critical_clique_list, vertices, int_relate_ob, two_param_behav_ob);
			// add those vertices which were not considered in the previous clique enumeration procedure
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.addAll(cand_vertices);
			boolean OK = zero_param_behav_ob.behavior();
			if(lower_bound_improved)
			{
				top_level_weight_wrt_colors_updated_bits.set(i);
				i--; // in order to reconsider the current color
//System.out.println("will shrink cand_vertices according to the newly found top-level weights, and continue to search for a clique whose size is at least as big as " + lower_bound_for_requested_clique);
//System.out.print("colors whose top-level weights have been updated: ");
//for(int j = 0; j < top_level_weights_wrt_colors.size(); j++) 
//if(top_level_weight_wrt_colors_updated_bits.get(j))
//System.out.print((j+1) + "-th\t");
//System.out.println();
			}
// UsrPause.press_enter_to_continue();
		}
System.out.println("eventual top_level_weights_wrt_colors: " + top_level_weights_wrt_colors);
	}

	public void update_top_level_weights_wrt_probabilistic_greedy_cliques(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, WeightBasedPartition weight_based_partition) // 
	{
		for(int i = 0; i <= top_level_weights_wrt_colors.size(); i++)
		{
System.out.println("as to the " + (i + 1) + "-th color, the top weight is " + (i == top_level_weights_wrt_colors.size() ? 0 : top_level_weights_wrt_colors.get(i)) + ", trying to find a tighter bound (probabilistically)++++++++++++++");
// UsrPause.press_enter_to_continue();

			ArrayList<Integer> cand_vertices;
			// prepare cand_vertices and lower_bound for i
			if(i == top_level_weights_wrt_colors.size())
			{
				cand_vertices = weight_based_partition.get_vertex_list_of_weight_at_least(1);
System.out.println("trying to find a clique which is larger than any found one");
			}
			else
				cand_vertices = weight_based_partition.get_vertex_list_of_weight_at_least(top_level_weights_wrt_colors.get(i) + 1);

			int lower_bound_for_requested_clique = i + 1;
			if(cand_vertices.size() < lower_bound_for_requested_clique)
			{
System.out.println("cand_vertices.size(): " + cand_vertices.size());
System.out.println("lower_bound_for_requested_clique: " + lower_bound_for_requested_clique); 
System.out.println("too few vertices to form a clique which contains at least " + lower_bound_for_requested_clique + " vertices");
				continue;
			}
			boolean lower_bound_improved = update_top_level_weights_wrt_one_probabilistic_greedy_clique_in_vertex_list(colored_clique, critical_clique_list, vertices, cand_vertices, lower_bound_for_requested_clique, int_relate_ob);
			if(lower_bound_improved)
			{
				two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
				boolean OK = zero_param_behav_ob.behavior();
				i--;
System.out.println("will shrink cand_vertices according to the newly found top-level weights, and continue to search for a clique whose size is at least as big as " + lower_bound_for_requested_clique);
			}
// UsrPause.press_enter_to_continue();
		}
System.out.println("eventual top_level_weights_wrt_colors: " + top_level_weights_wrt_colors);
	}
	
	public void update_top_level_weights_wrt_enumerated_cliques_adhere_to_vertex_list(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, TreeSet<Integer> cand_remove_vertices_for_clique_reductions_wrt_degree_decrease, WeightedVertex[] vertices, ArrayList<Integer> starting_vertex_list, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob) // apply clique reductions after each enumeration
	{
		for(int v : starting_vertex_list)
		{
// System.out.println("as to the " + (starting_vertex_list.indexOf(v) + 1) + "-th vertex " + v + " from the top weight/degree list++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			update_top_level_weights_wrt_enumerated_cliques_adhere_to_given_vertex(v, colored_clique, critical_clique_list, vertices, int_relate_ob, two_param_behav_ob);
			// add those vertices which were not considered in the previous clique enumeration procedure
			int[] nbs = vertices[v].get_neighbors();
			Integer[] nbs_int_obj_array = Arrays.stream(nbs).boxed().toArray(Integer[]::new);
			ArrayList<Integer> cand_vertices = new ArrayList<>(Arrays.asList(nbs_int_obj_array));
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.addAll(cand_vertices);
			cand_remove_vertices_for_clique_reductions_wrt_degree_decrease.add(v);
			boolean OK = zero_param_behav_ob.behavior();
// UsrPause.press_enter_to_continue();
		}
	}

	private boolean update_top_level_weights_wrt_new_critical_cliques_in_vertex_list(ArrayList<Integer> cand_vertices, int lower_bound_for_requested_clique, ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, TwoParametersBehavior two_param_behav_ob)
	{
// System.out.println("start to look for cliques containing at least " + lower_bound_for_requested_clique + " vertices, dfs");
		ArrayList<Integer> reserved_vertex_list = new ArrayList<Integer>(cand_vertices);
		ArrayDeque<Integer> decision_vertex_stack = new ArrayDeque<Integer>();
		ArrayDeque<Integer> excluded_vertex_stack = new ArrayDeque<Integer>();
		while(true)	
		{
			// shrink cand_vertices to obtain a clique
			int v_to_exclude = 0;
			obtain_undesired_vertex_label:
			// no need to deal with the special case when cand_vertices.size() == 1
			for(int x1 : cand_vertices)
			{
				for(int x2 : cand_vertices)
				{
					if(x1 == x2) continue;
					if(!int_relate_ob.related(x1, x2))
					{
						v_to_exclude = x1; 
						break obtain_undesired_vertex_label;
					} 
				}
			}
			if(v_to_exclude != 0)
			{ 
				// make a decision, i.e., choose one branch			
				decision_vertex_stack.push(v_to_exclude); 
				cand_vertices.remove(Integer.valueOf(v_to_exclude));
				excluded_vertex_stack.push(v_to_exclude);
				if(cand_vertices.size() >= lower_bound_for_requested_clique) continue;
			}

			if(v_to_exclude == 0 && cand_vertices.size() >= lower_bound_for_requested_clique) // I think v_to_exclude == 0 implies cand_vertices.size() >= lower_bound_for_clique. NO!!!!!!!!!!!!!!!!!! If the input is a clique whose size is smaller than this bound, then my assumption does not hold.
			{
				// utilize this clique
				ArrayList<Integer> cand_clique = new ArrayList<Integer>(cand_vertices);
// System.out.println("having obtained a maximal clique of size " + cand_clique.size() + "**: " + cand_clique);
// System.out.println("having obtained a maximal clique of size " + cand_clique.size() + "**, decision_stack_size: " + decision_vertex_stack.size());
// UsrPause.press_enter_to_continue();
				boolean lower_bound_improved = update_top_level_weights_wrt_one_enumerated_clique(colored_clique, critical_clique_list, cand_clique, vertices);
				if(lower_bound_improved)
				{
// System.out.println("start reductions");
					// apply clique reductions
					two_param_behav_ob.behavior(last_top_level_weight_updated_colors, reserved_vertex_list);
// System.out.println("complete reductions");
					return true;
				}				
			}

			do{
				// below change the last decision
				if(decision_vertex_stack.isEmpty()) // all branches covered
				{
// System.out.println("enumerations completed");
					return false;
				}
				int decision_v = decision_vertex_stack.pop(); // erase last decision		
				// back to a decision point
				int v_to_include;
				do{
					v_to_include = excluded_vertex_stack.pop();
					cand_vertices.add(v_to_include);
				}while(v_to_include != decision_v);
				// enter the other branch
				ArrayList<Integer> removed_list = new ArrayList<Integer>();
				for(int x : cand_vertices)
				{
					if(x == decision_v)
					{
						continue;
					}
					if(int_relate_ob.related(x, decision_v))
					{
						continue;
					}
					removed_list.add(x);
				}
				for(int x : removed_list)
				{
					cand_vertices.remove(Integer.valueOf(x)); 
					excluded_vertex_stack.push(x);
				}
			}while(cand_vertices.size() < lower_bound_for_requested_clique);
		} // while(true)
	}
	
	private void update_top_level_weights_wrt_enumerated_cliques_adhere_to_given_vertex(int u, ArrayList<Integer> colored_clique,  ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, TwoParametersBehavior two_param_behav_ob)
	{
// System.out.println("start to enumerate cliques which contains " + u);
		int[] nbs = vertices[u].get_neighbors();
		Integer[] nbs_int_obj_array = Arrays.stream(nbs).boxed().toArray(Integer[]::new);
		ArrayList<Integer> cand_vertices = new ArrayList<>(Arrays.asList(nbs_int_obj_array));
		ArrayList<Integer> reserved_vertex_list = new ArrayList<Integer>(cand_vertices);
		reserved_vertex_list.add(u);
		ArrayDeque<Integer> decision_vertex_stack = new ArrayDeque<Integer>();
		ArrayDeque<Integer> excluded_vertex_stack = new ArrayDeque<Integer>();
		while(true)	
		{
			// shrink cand_vertices to obtain a clique
			int v_to_exclude = 0;
			obtain_undesired_vertex_label:
			// no need to deal with the special case when cand_vertices.size() == 1
			for(int x1 : cand_vertices)
			{
				for(int x2 : cand_vertices)
				{
					if(x1 == x2) continue;
					if(!int_relate_ob.related(x1, x2))
					{
						v_to_exclude = x1; 
						break obtain_undesired_vertex_label;
					} 
				}
			}
			if(v_to_exclude != 0)
			{ 
				// make a decision, i.e., choose one branch			
				decision_vertex_stack.push(v_to_exclude); 
				cand_vertices.remove(Integer.valueOf(v_to_exclude));
				excluded_vertex_stack.push(v_to_exclude);
				continue;
			}
			// a maximal clique found
			// utilize this clique
			ArrayList<Integer> cand_clique = new ArrayList<Integer>(cand_vertices);
			cand_clique.add(u);
// System.out.println("having obtained a maximal clique of size " + cand_clique.size() + "**: " + cand_clique);
// System.out.println("having obtained a maximal clique of size " + cand_clique.size() + "**, decision_stack_size: " + decision_vertex_stack.size());
			boolean lower_bound_improved = update_top_level_weights_wrt_one_enumerated_clique(colored_clique, critical_clique_list, cand_clique, vertices);
			if(lower_bound_improved)
			{
// System.out.println("start reductions");
				// apply clique reductions
				two_param_behav_ob.behavior(last_top_level_weight_updated_colors, reserved_vertex_list);
// System.out.println("complete reductions");
			}
			// below change the last decision
			if(decision_vertex_stack.isEmpty())
			{
// System.out.println("enumerations completed");
				return;
			}
			int decision_v = decision_vertex_stack.pop(); // erase last decision		
			// back to a decision point
			int v_to_include;
			do{
				v_to_include = excluded_vertex_stack.pop();
				cand_vertices.add(v_to_include);
			}while(v_to_include != decision_v);
			// enter the other branch
			ArrayList<Integer> removed_list = new ArrayList<Integer>();
			for(int x : cand_vertices)
			{
				if(x == decision_v)
				{
					continue;
				}
				if(int_relate_ob.related(x, decision_v))
				{
					continue;
				}
				removed_list.add(x);
			}
			for(int x : removed_list)
			{
				cand_vertices.remove(Integer.valueOf(x)); 
				excluded_vertex_stack.push(x);
			}
		} // while(true)
	}

	private ArrayList<Integer> random_weight_size_tuned_greedy_clique(int u, WeightedVertex[] vertices, IntRelate int_relate_ob, double mu)
	// preconditions: 
	// post conditions: return a greedy clique wrt. mu, where u is the smallest vertex id
	{
		int[] nbs = vertices[u].get_neighbors();
		Integer[] nbs_int_obj_array = Arrays.stream(nbs).boxed().toArray(Integer[]::new);
		ArrayList<Integer> cand_vertices = new ArrayList<>(Arrays.asList(nbs_int_obj_array));
		ArrayList<Integer> cand_clique = new ArrayList<Integer>();
		cand_clique.add(u);
// System.out.println("having added " + u + " into cand_clique");	
// System.out.println("its neighbors: " + cand_vertices);
		while(!cand_vertices.isEmpty())
		{
			int best_v = 0;
			double best_benefit = 0;
			ArrayList<Double> hybrid_benefit_list = new ArrayList<Double>();
			ArrayList<Integer> best_hybrid_benefit_v_list = new ArrayList<Integer>();
			for(int v : cand_vertices)
			{
				int connection_benefit = 0; // how many cand vertices will remain if we add v
				for(int x : cand_vertices)
				{
					if(x == v) continue;
					if(!int_relate_ob.related(x, v)) continue;
					connection_benefit++;
				}
				hybrid_benefit_list.add((double) vertices[v].get_weight() * mu + (double) connection_benefit);
			}
			for(int i = cand_vertices.size() - 1; i >= 0; i--)
			{
				int v = cand_vertices.get(i);
				if(Double.compare(hybrid_benefit_list.get(i), best_benefit) > 0)
				{
					best_benefit = hybrid_benefit_list.get(i);
					best_hybrid_benefit_v_list.clear();
					best_hybrid_benefit_v_list.add(v);
				}
				else if(Double.compare(hybrid_benefit_list.get(i), best_benefit) == 0)
				{
					best_hybrid_benefit_v_list.add(v);
				}
			} // for(int v : cand_vertices)
			hybrid_benefit_list.clear();
// System.out.println("best_hybrid_benefit_v_list: " + best_hybrid_benefit_v_list);	
			// since we require v < last_added_vertex, we may obtain no best_hybrid_benefit_v
/*
			if(best_hybrid_benefit_v_list.isEmpty())
			{
				cand_vertices.clear();
				break;
			}
*/
			int rand_best_hybrid_benefit_v = best_hybrid_benefit_v_list.get(rand.nextInt(best_hybrid_benefit_v_list.size()));
			best_hybrid_benefit_v_list.clear();
			cand_clique.add(rand_best_hybrid_benefit_v);
// System.out.println("having added " + rand_best_hybrid_benefit_v + " into cand_clique");		

			for(int i = cand_vertices.size() - 1; i >= 0; i--)
			{
				if(!int_relate_ob.related(rand_best_hybrid_benefit_v, cand_vertices.get(i)))
				{
//System.out.println("remove " + cand_vertices.get(i));
					cand_vertices.remove(i);
				}
			} // for(int i = cand_vertices.size() - 1; i >= 0; i--)
		} // while(!cand_vertices.isEmpty())
// System.out.println("a hybrid-benefit greedy clique found: " + cand_clique);
		return cand_clique;
	}

	private ArrayList<Integer> probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list(ArrayList<Integer> cand_vertices, int lower_bound_for_requested_clique, IntRelate int_relate_ob)
	// preconditions: 
	// post conditions: return a greedy maximal clique whose vertices are selected based on a probability proportional to its missing degree if lower_bound sat.
	{
		while(true)
		{
			ArrayList<Integer> missing_degree_list = new ArrayList<Integer>();
			int missing_degree_sum = 0;
			// compute missing degrees
			for(int v : cand_vertices)
			{
				int missing_degree = 0; // how many cand vertices will be removed if we add v
				for(int x : cand_vertices)
				{
					if(x == v) continue;
					if(int_relate_ob.related(x, v)) continue;
					missing_degree++;
				}
// System.out.println("missing degree of " + v + " is " + missing_degree);
				missing_degree_list.add(missing_degree);
				missing_degree_sum += missing_degree;
			}
			if(missing_degree_sum == 0) // a clique has been found
			{
// System.out.println("having obtained a probabilistic clique of size " + cand_vertices.size() + ": " + cand_vertices);
// UsrPause.press_enter_to_continue();
				ArrayList<Integer> cand_clique = new ArrayList<Integer>();
				if(cand_vertices.size() >= lower_bound_for_requested_clique)
					cand_clique = new ArrayList<Integer>(cand_vertices);					
				return cand_clique;
			}
			// select a random vertex with probability proportional to missing_degree
			int rand_num = rand.nextInt(missing_degree_sum);
			int j = 0;
			int next_bound = missing_degree_list.get(j);
			while(rand_num >= next_bound)
			{
				j++;
				next_bound += missing_degree_list.get(j);
			}
			// exclude it
			int excluded_v = cand_vertices.get(j);
			cand_vertices.remove(Integer.valueOf(excluded_v));
// System.out.println("having excluded " + excluded_v);
		} // while(true)
	}

	private ArrayList<Integer> probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list_2(ArrayList<Integer> cand_vertices, int lower_bound_for_requested_clique, IntRelate int_relate_ob)
	// preconditions: 
	// post conditions: return a greedy maximal clique whose vertices have the min missing degree, breaking ties randomly, if lower_bound sat.
	{
		while(true)
		{
			// compute missing degrees
			int max_missing_degree = 0;
			ArrayList<Integer> best_missing_degree_remove_list = new ArrayList<Integer>();
			for(int v : cand_vertices)
			{
				int missing_degree = 0; // how many cand vertices will be removed if we add v
				for(int x : cand_vertices)
				{
					if(x == v) continue;
					if(int_relate_ob.related(x, v)) continue;
					missing_degree++;
				}
// System.out.println("missing degree of " + v + " is " + missing_degree);
				if(missing_degree > max_missing_degree)
				{
					max_missing_degree = missing_degree;
					best_missing_degree_remove_list.clear();
					best_missing_degree_remove_list.add(v);
				}
				else if(missing_degree == max_missing_degree)
				{
					best_missing_degree_remove_list.add(v);
				}
			}
			if(max_missing_degree == 0)
			{
				ArrayList<Integer> cand_clique = new ArrayList<Integer>();
				if(cand_vertices.size() >= lower_bound_for_requested_clique)
				{
					cand_clique = new ArrayList<Integer>(cand_vertices);
				}
				return cand_clique;
			}
			// select a random vertex with the max missing degree
			int remove_v = best_missing_degree_remove_list.get(rand.nextInt(best_missing_degree_remove_list.size()));
			cand_vertices.remove(Integer.valueOf(remove_v));
// System.out.println("having excluded " + remove_v);
		} // while(true)
	}

private ArrayList<Integer> probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list_3(ArrayList<Integer> cand_vertices, int lower_bound_for_requested_clique, IntRelate int_relate_ob)
	// preconditions: 
	// post conditions: return a greedy maximal clique whose vertices have the min missing degree, breaking ties randomly, if lower_bound sat.
	{
		while(true)
		{
			// compute missing degrees
			int max_missing_degree = 0;
			ArrayList<Integer> best_missing_degree_remove_list = new ArrayList<Integer>();
			TreeMap<Integer, Integer> vertex_missing_degree = new TreeMap<Integer, Integer>();
			for(int v : cand_vertices)
			{
				int missing_degree = 0; // how many cand vertices will be removed if we add v
				for(int x : cand_vertices)
				{
					if(x == v) continue;
					if(int_relate_ob.related(x, v)) continue;
					missing_degree++;
				}
				vertex_missing_degree.put(v, missing_degree);
// System.out.println("missing degree of " + v + " is " + missing_degree);
				if(missing_degree > max_missing_degree)
				{
					max_missing_degree = missing_degree;
					best_missing_degree_remove_list.clear();
					best_missing_degree_remove_list.add(v);
				}
				else if(missing_degree == max_missing_degree)
				{
					best_missing_degree_remove_list.add(v);
				}
			}
			if(max_missing_degree == 0)
			{
				ArrayList<Integer> cand_clique = new ArrayList<Integer>();
				if(cand_vertices.size() >= lower_bound_for_requested_clique)
				{
					cand_clique = new ArrayList<Integer>(cand_vertices);
				}
				return cand_clique;
			}

			ArrayList<Integer> best_second_level_missing_degree_remove_list = new ArrayList<Integer>();
			int min_second_level_missing_degree = cand_vertices.size() * (cand_vertices.size() - 1); // assume complete graph, large enough
// System.out.println("best_missing_degree_remove_list (of size " + best_missing_degree_remove_list.size() +"): " + best_missing_degree_remove_list);
			for(int v : best_missing_degree_remove_list)
			{
				int second_level_missing_degree = 0;
				for(int x : cand_vertices)
				{
					if(x == v) continue;
					if(int_relate_ob.related(x, v)) continue;
					second_level_missing_degree += vertex_missing_degree.get(x);
				}
// System.out.println("second_level_missing_degree of " + v + " is " + second_level_missing_degree);
				if(second_level_missing_degree < min_second_level_missing_degree)
				{
					min_second_level_missing_degree = second_level_missing_degree;
					best_second_level_missing_degree_remove_list.clear();
					best_second_level_missing_degree_remove_list.add(v);
				}
				else if(second_level_missing_degree == min_second_level_missing_degree)
				{
					best_second_level_missing_degree_remove_list.add(v);
				}
			}
// System.out.println("best_second_level_missing_degree_remove_list (of size " + best_second_level_missing_degree_remove_list + "): " + best_second_level_missing_degree_remove_list);
			// select a random vertex with the max missing degree, breaking ties in favor of the min second-level missing degree
			int best_remove_v;
			if(true)
			// if(rand.nextBoolean()) // with p(=0.5) select among the second greedy, with 1-p select among the greedy
				best_remove_v = best_second_level_missing_degree_remove_list.get(rand.nextInt(best_second_level_missing_degree_remove_list.size()));
			else
				best_remove_v = best_missing_degree_remove_list.get(rand.nextInt(best_missing_degree_remove_list.size()));
			cand_vertices.remove(Integer.valueOf(best_remove_v));
// System.out.println("having excluded " + best_remove_v);
//UsrPause.press_enter_to_continue();
		} // while(true)
	}

	private ArrayList<Integer> random_weight_greedy_clique(int u, WeightedVertex[] vertices, IntRelate int_relate_ob)
	{
		int[] nbs = vertices[u].get_neighbors();
		Integer[] nbs_int_obj_array = Arrays.stream(nbs).boxed().toArray(Integer[]::new);
		ArrayList<Integer> cand_vertices = new ArrayList<>(Arrays.asList(nbs_int_obj_array));
		ArrayList<Integer> cand_clique = new ArrayList<Integer>();
		cand_clique.add(u);
//System.out.println("having added " + u + " into the clique");
		while(!cand_vertices.isEmpty())
		{
			int best_v = 0;
			int best_weight = 0;
			ArrayList<Integer> top_weight_v_list = new ArrayList<Integer>();

			for(int v : cand_vertices)
			{
					if(vertices[v].get_weight() > best_weight)
					{
						best_weight = vertices[v].get_weight();
						top_weight_v_list.clear();
						top_weight_v_list.add(v);
					}
					else if(vertices[v].get_weight() == best_weight)
					{
						top_weight_v_list.add(v);
					}
			} // for(int v : cand_vertices)

			int rand_top_weight_v = top_weight_v_list.get(rand.nextInt(top_weight_v_list.size()));
			top_weight_v_list.clear();
			cand_clique.add(rand_top_weight_v);
//System.out.println("having added " + rand_top_weight_v + " into the clique");
			for(int i = cand_vertices.size() - 1; i >= 0; i--)
			{
				if(rand_top_weight_v == cand_vertices.get(i) || !int_relate_ob.related(rand_top_weight_v, cand_vertices.get(i)))
				{
//System.out.println("remove " + cand_vertices.get(i));
					cand_vertices.remove(i);
				}
			}
//System.out.println("cand_vertices: " + cand_vertices);
		} // while(!cand_vertices.isEmpty())
//System.out.println("One weight-greedy clique completed.");
		return cand_clique;
	} // public ArrayList<Integer> weight_greedy_clique(int u, Vertex[] vertices, int seed)

	public boolean update_top_level_weights_wrt_vertex_weight_decrease_clique(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, ArrayList<Integer> cand_clique, WeightedVertex[] vertices)
	// preconditions: cand_clique must be sorted, vertex weight must decrease
	// return true of lower-bound actually improved, false otherwise
	{
//System.out.println("current top_level_weights_wrt_colors: " + top_level_weights_wrt_colors);
		last_top_level_weight_updated_colors.clear();
		if(colored_clique.size() == 0)
		{
			colored_clique.addAll(cand_clique);
		}
		// update top_level_weights
		boolean lower_bound_improved = false;
		for(int i = 0; i < cand_clique.size(); i++)
		{
			int v = cand_clique.get(i);
			if(i < top_level_weights_wrt_colors.size())
			{
				if(top_level_weights_wrt_colors.get(i) < vertices[v].get_weight())
				{
// System.out.println("weight " + top_level_weights_wrt_colors.get(i) + " will be increased to be " + vertices[v].get_weight());
					top_level_weights_wrt_colors.set(i, vertices[v].get_weight());
					last_top_level_weight_updated_colors.add(i);
					lower_bound_improved = true;
				}
			}
			else
			{
				top_level_weights_wrt_colors.add(vertices[v].get_weight());
				last_top_level_weight_updated_colors.add(i);
				lower_bound_improved = true;
// System.out.println("insert a new weight: " + vertices[v].get_weight() + " into top level weights");
			}
		}
		// update colored_clique
		boolean need_to_update_colored_clique = false;
		for(int i = 0; i < cand_clique.size(); i++)
		{
			if(i >= colored_clique.size())
			{
				need_to_update_colored_clique = true;
				break;
			}

			if(vertices[cand_clique.get(i)].get_weight() > vertices[colored_clique.get(i)].get_weight())
			{
				need_to_update_colored_clique = true;
				break;
			}
			else if(vertices[cand_clique.get(i)].get_weight() < vertices[colored_clique.get(i)].get_weight())
			{
				break;
			}
		} // for(int i = 0; i < cand_clique.size(); i++)
		if(need_to_update_colored_clique)
		{
// System.out.println("should be updated");
			colored_clique.clear();
			colored_clique.addAll(cand_clique);
		}
			
		// place cand_clique into critical_clique_list
		if(!sorted_clique_strictly_below_top_level_weights(cand_clique, vertices) && !sorted_clique_covered_by_critical_clique_list(cand_clique, critical_clique_list, vertices))
			critical_clique_list.add(cand_clique); // insert better ones

		if(lower_bound_improved)
		{
			// delete redundant critical cliques
			for(int i = critical_clique_list.size() - 1; i >= 0; i--)
			{
				// ArrayList<Integer> critical_clq = critical_clique_list.get(i);
				if(sorted_clique_dominate_eq(cand_clique, critical_clique_list.get(i), vertices))
				{
					critical_clique_list.remove(i);// delete worse cliques
				}
			}
			for(int i = critical_clique_list.size() - 1; i >= 0; i--)
			{
				if(!sorted_clique_intersect_with_top_level_weights(critical_clique_list.get(i), vertices))
				{
					critical_clique_list.remove(i);// delete redundant cliques
				}
			}

// System.out.println("lower-bound improved with the clique: " + cand_clique);
// System.out.println("top_level_weights: " + top_level_weights_wrt_colors);
		}

		return lower_bound_improved;
	}

	boolean sorted_clique_covered_by_critical_clique_list(ArrayList<Integer> cand_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices)
	{
		for(int i = 0; i < critical_clique_list.size(); i++)
		{
			if(sorted_clique_dominate_eq(critical_clique_list.get(i), cand_clique, vertices))
				return true;
		}
		return false;
	}

	boolean sorted_clique_strictly_below_top_level_weights(ArrayList<Integer> tested_clique, WeightedVertex[] vertices)
	// preconditions: tested_clique should be sorted, vertex weights from big to small
	{
		if(tested_clique.size() > top_level_weights_wrt_colors.size()) return false;
		for(int i = 0; i < tested_clique.size(); i++)
			if(vertices[tested_clique.get(i)].get_weight() >= top_level_weights_wrt_colors.get(i)) return false;
		return true;
	}

	boolean sorted_clique_intersect_with_top_level_weights(ArrayList<Integer> tested_clique, WeightedVertex[] vertices)
	// preconditions: tested_clique should be sorted, vertex weights from big to small
	// preconditions: |C| <= |top_level_weights|
	{
		for(int i = 0; i < tested_clique.size(); i++)
		{
			if(vertices[tested_clique.get(i)].get_weight() == top_level_weights_wrt_colors.get(i)) return true;
		}
		return false;
	}

	//
	boolean sorted_clique_dominate_eq(ArrayList<Integer> clq_1, ArrayList<Integer> clq_2, WeightedVertex[] vertices)
	// preconditions: both cliques should be sorted, vertex weights from big to small
	{
		if(clq_1.size() < clq_2.size()) return false;
		for(int i = 0; i < clq_2.size(); i++)
		{
			if(vertices[clq_1.get(i)].get_weight() < vertices[clq_2.get(i)].get_weight())
			{
				return false;
			}
		}
		return true;		
	}
	// public void update_top_level_weights_wrt_degree_greedy_clique(ArrayList<Integer> top_level_weights_wrt_colors, WeightedVertex[] vertices, LinkedList<Integer> remaining_vertex_list, IntRelate int_relate_ob, int seed)
	public void update_top_level_weights_wrt_degree_greedy_clique(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, ArrayList<Integer> remaining_vertex_list, IntRelate int_relate_ob)
	{
			ArrayList<Integer> top_degree_v_list = top_degree_vertices(vertices, remaining_vertex_list);
			int rand_top_degree_v = top_degree_v_list.get(rand.nextInt(top_degree_v_list.size())); 

			ArrayList<Integer> rand_degree_greedy_clique = random_degree_greedy_clique(rand_top_degree_v, vertices, int_relate_ob);

			Collections.sort(rand_degree_greedy_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
			// System.out.println("Contents of degree_greedy_clique: " + degree_greedy_clique);
			update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, rand_degree_greedy_clique, vertices);
	} // public void update_top_level_weights_wrt_degree_greedy_clique(ArrayList<Integer> top_level_weights_wrt_colors, Vertex[] vertices, int seed)


	public boolean update_top_level_weights_wrt_one_enumerated_clique(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, ArrayList<Integer> enumerated_clique, WeightedVertex[] vertices)
	{
			Collections.sort(enumerated_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
// System.out.println("after sorted by weight: " + enumerated_clique);
			return update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, enumerated_clique, vertices);
	} // public void update_top_level_weights_wrt_one_enumerated_clique(ArrayList<Integer> colored_clique, ArrayList<Integer> cand_clique, WeightedVertex[] vertices)

	public void update_top_level_weights_wrt_weight_size_tuned_greedy_clique_list(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, IntRelate int_relate_ob, ZeroParameterBehavior zero_param_behav_ob, TwoParametersBehavior two_param_behav_ob, DegreeBasedPartition dgr_based_partition, WeightBasedPartition weight_based_partition)
	{
		ArrayList<Integer> starting_vertex_list;
/*
		starting_vertex_list = weight_based_partition.get_vertices_of_maximum_weight();
		starting_vertex_list.addAll(dgr_based_partition.get_vertices_of_maximum_degree());
*/

		starting_vertex_list = weight_based_partition.get_vertex_list_of_weight_at_least(1); // obtain all vertices
		for(int i = starting_vertex_list.size() - 1; i >= 0; i--)
		{
System.out.println("remaining number of vertices for weight-size tuned clique constructions: " + starting_vertex_list.size() + "-------------------------");
			int starting_vertex = starting_vertex_list.get(i);
			starting_vertex_list.remove(i);

			if(vertices[starting_vertex].get_degree() == 0)
			{
				ArrayList<Integer> single_vertex_clique = new ArrayList<Integer>();
				single_vertex_clique.add(starting_vertex);
				boolean lower_bound_improved = update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, single_vertex_clique, vertices);
				if(lower_bound_improved)
				{
					two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
					boolean OK = zero_param_behav_ob.behavior();
				}
				continue;
			}

System.out.println("about to contruct cliques with vertex: " + starting_vertex);

			// preparing parameter values
			double mu_max = vertices[starting_vertex].get_degree(); // weight difference is absolute
// System.out.println("degree: " + vertices[starting_vertex].get_degree());
// System.out.println("mu_max: " + mu_max);
			int max_w = 0;
			for(int nb : vertices[starting_vertex].get_neighbors())
			{
				max_w = StrictMath.max(max_w, vertices[nb].get_weight());
			}
// System.out.println("max_w: " + max_w);

			double mu_min = 1.0 / ((double)max_w); // connection difference is absolute
// System.out.println("mu_min: " + mu_min);

			for(double mu = mu_max; Double.compare(mu, mu_min) > 0; mu /= 1.414) //sqrt(2), instead of 2 to allow better diversification, but more time needed
			{
// System.out.println("mu: " + mu);
				boolean lower_bound_improved = update_top_level_weights_wrt_one_weight_size_tuned_greedy_clique(colored_clique, critical_clique_list, vertices, starting_vertex, int_relate_ob, mu);
				if(lower_bound_improved)
				{
					two_param_behav_ob.behavior(last_top_level_weight_updated_colors, new ArrayList<Integer>());
					boolean OK = zero_param_behav_ob.behavior();
				}
			}  
// UsrPause.press_enter_to_continue();
		}
	}

	private boolean update_top_level_weights_wrt_one_weight_size_tuned_greedy_clique(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, int starting_vertex, IntRelate int_relate_ob, double mu)
	{
			ArrayList<Integer> rand_tuned_greedy_clique = random_weight_size_tuned_greedy_clique(starting_vertex, vertices, int_relate_ob, mu);
			Collections.sort(rand_tuned_greedy_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
// System.out.println("having obtained a sorted tuned clique: " + rand_tuned_greedy_clique);
// UsrPause.press_enter_to_continue();
			return update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, rand_tuned_greedy_clique, vertices);
	} // 

	private boolean update_top_level_weights_wrt_one_probabilistic_greedy_clique_in_vertex_list(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, ArrayList<Integer> cand_vertices, int lower_bound_for_requested_clique, IntRelate int_relate_ob)
	// postconditions: return true if lower_bound_improved, false otherwise
	{
			ArrayList<Integer> probabilistic_greedy_clique = new ArrayList<Integer>(); 
			for(int i = 0; i < MAX_GUESS_NUM; i++)
			{
// System.out.println("the " + (i+1) + "-th attempt to find a clique whose size is at least " + lower_bound_for_requested_clique);
				// probabilistic_greedy_clique = probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list(new ArrayList<Integer>(cand_vertices), lower_bound_for_requested_clique, int_relate_ob);
				// probabilistic_greedy_clique = probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list_2(new ArrayList<Integer>(cand_vertices), lower_bound_for_requested_clique, int_relate_ob);
				probabilistic_greedy_clique = probabilistic_greedy_clique_based_on_missing_degree_in_vertex_list_3(new ArrayList<Integer>(cand_vertices), lower_bound_for_requested_clique, int_relate_ob);
				if(!probabilistic_greedy_clique.isEmpty()) 
				{
System.out.println("having obtained a new critical clique at the " + (i+1) + "-th guess: " + probabilistic_greedy_clique);
UsrPause.press_enter_to_continue();
					break;
				}
			}
			if(probabilistic_greedy_clique.isEmpty())
				return false;
			Collections.sort(probabilistic_greedy_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
System.out.println("having obtained a sorted tuned clique: " + probabilistic_greedy_clique);
// UsrPause.press_enter_to_continue();
			return update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, probabilistic_greedy_clique, vertices);
	} // 

	// public void update_top_level_weights_wrt_weight_greedy_clique(ArrayList<Integer> top_level_weights_wrt_colors, WeightedVertex[] vertices, LinkedList<Integer> remaining_vertex_list, IntRelate int_relate_ob, int seed)
	public void update_top_level_weights_wrt_weight_greedy_clique(ArrayList<Integer> colored_clique, ArrayList<ArrayList<Integer>> critical_clique_list, WeightedVertex[] vertices, ArrayList<Integer> remaining_vertex_list, IntRelate int_relate_ob)
	{
			// ArrayList<Integer> top_weight_v_list = cliq_search.top_weight_vertices(instance_graph);
			ArrayList<Integer> top_weight_v_list = top_weight_vertices(vertices, remaining_vertex_list);
			//System.out.println("Contents of top weight vertex list: " + top_weight_v_list);
			int rand_top_weight_v = top_weight_v_list.get(rand.nextInt(top_weight_v_list.size()));
			ArrayList<Integer> rand_weight_greedy_clique = random_weight_greedy_clique(rand_top_weight_v, vertices, int_relate_ob);
			Collections.sort(rand_weight_greedy_clique, new Comparator<Integer>(){
				public int compare(Integer n1, Integer n2){
					// if(instance_graph.get_vertices()[n1].get_weight() < instance_graph.get_vertices()[n2].get_weight()){
					if(vertices[n1].get_weight() < vertices[n2].get_weight() || (vertices[n1].get_weight() == vertices[n2].get_weight() && n1 < n2)){
						return 1;
					}
					else{
						return -1;
					}
				}
			});
			// System.out.println("Contents of weight_greedy_clique: " + weight_greedy_clique);
			update_top_level_weights_wrt_vertex_weight_decrease_clique(colored_clique, critical_clique_list, rand_weight_greedy_clique, vertices);
	} // public void update_top_level_weights_wrt_weight_greedy_clique(ArrayList<Integer> top_level_weights_wrt_colors, Vertex[] vertices, int seed)

} // class CliqueSearch
