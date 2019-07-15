package GraphPack.DynamicSimpleUndirectedVertexWeightedGraphPack;

import java.util.*;
import java.io.*;
import java.lang.StrictMath;

import GraphPack.SimpleUndirectedVertexWeightedGraphPack.SimpleUndirectedVertexWeightedGraph;
import DegreeBucketPack.DegreeBasedPartition;
import WeightBucketPack.WeightBasedPartition;

public class DynamicSimpleUndirectedVertexWeightedGraph extends SimpleUndirectedVertexWeightedGraph{
/*	
	protected LinkedList<Integer> removed_vertex_list = new LinkedList<Integer>();
	protected LinkedList<Integer> remaining_vertex_list = new LinkedList<Integer>();
	protected LinkedList<Integer> removed_edge_list = new LinkedList<Integer>();
	protected LinkedList<Integer> remaining_edge_list = new LinkedList<Integer>();
*/

/*
**********************************Notice**************************************
*I am very confused why using ArrayList for the data sturctures below is much faster
*than using LinkedList. I need to remove elements frequently. 
*I never locate, delete, insert or modify an element via indexes.
*I simply remove elements by their identity.
*Why ArrayList is a better choice?????????
*/

	private class PositiveIntegerMap{

		private TreeMap<Integer, Integer> big_to_small;
		private ArrayList<Integer> small_to_big;
	
		public PositiveIntegerMap()
		{
			small_to_big = new ArrayList<Integer>(remaining_vertex_list.size() + 1);
			big_to_small = new TreeMap<Integer, Integer>();

			BitSet vertex_exists_bits = new BitSet(vertex_num + 1);
			for(int v : remaining_vertex_list)
			{
				vertex_exists_bits.set(v);
			}

			small_to_big.add(0);
			for(int v = 1; v <= vertex_num; v++)
			{
				if(vertex_exists_bits.get(v))
				{
					small_to_big.add(v);
				}
			}
			vertex_exists_bits.clear();
			
			for(int i = 1; i <= remaining_vertex_list.size(); i++)
			{
				big_to_small.put(small_to_big.get(i), i);
			}
		} // public PositiveIntegerMap()

		public int get_original_id_from_new_id(int new_id)
		{
			try{
				return small_to_big.get(new_id);
			}catch(IndexOutOfBoundsException e)
			{
				System.out.println("Exception occurs when computing the original vertex id, may because of the absence of construction function: " + e);
				return 0;
			}
		}

		public int get_new_id_from_original_id(int org_id)
		{
			try{
				return big_to_small.get(org_id);
			}catch(IndexOutOfBoundsException e)
			{
				System.out.println("Exception occurs when computing the original vertex id, may because of the absence of construction function: " + e);
				return 0;
			}
		}
	};

	protected ArrayList<Integer> removed_vertex_list = new ArrayList<Integer>();
	protected ArrayList<Integer> remaining_vertex_list = new ArrayList<Integer>();
	protected ArrayList<Integer> removed_edge_list = new ArrayList<Integer>();
	protected ArrayList<Integer> remaining_edge_list = new ArrayList<Integer>();

	private PositiveIntegerMap id_map_in_shrinking;

	protected DegreeBasedPartition dgr_based_partition;
	protected WeightBasedPartition weight_based_partition;

	public DynamicSimpleUndirectedVertexWeightedGraph(String instance_folder_name, String instance_file_name)
	{
		super(instance_folder_name, instance_file_name);
		for(int v = 1; v <= vertex_num; v++)
		{
			remaining_vertex_list.add(v);
		}
		for(int e = 0; e < edge_num; e++)
		{
			remaining_edge_list.add(e);
		}

		dgr_based_partition = new DegreeBasedPartition(get_max_degree(), vertex_num, remaining_vertex_list, vertices);
		weight_based_partition = new WeightBasedPartition(get_max_vertex_weight(), vertex_num, remaining_vertex_list, vertices);
	}

	public String vertex_and_edge_num_change_info_to_string()
	{
		String output_string = new String();

		output_string += " & " + vertex_num + " & " + edge_num; 
		output_string += " & " + remaining_vertex_list.size() + " & " + remaining_edge_list.size();

		return output_string;
	}

	public String toString(boolean shrunk)
	{
		if(shrunk)
		{ 
			id_map_in_shrinking = new PositiveIntegerMap();
		}

		String output_string = new String();
		// headline
		if(shrunk)
		{
			output_string += ("p edge " + remaining_vertex_list.size() + " " + remaining_edge_list.size() + "\n");
		}
		else
		{
			output_string += ("p edge " + vertex_num + " " + remaining_edge_list.size() + "\n");
		}
		// v lines
		if(shrunk)
		{
			for(int i = 1; i <= remaining_vertex_list.size(); i++)
			{
				output_string += ("v " + i + " " + vertices[id_map_in_shrinking.get_original_id_from_new_id(i)].get_weight() + "\n");
			}
		}
		else
		{
			for(int v = 1; v <= vertex_num; v++)
			{
				output_string += ("v " + v + " " + vertices[v].get_weight() + "\n");
			}
		}
		// e lines
		int output_edge_num = 0;
		for(int e = 0; e < edge_num; e++)
		{
			int v1, v2;
			int[] v_array = new int[2];
			edges[e].get_vertices(v_array);
			v1 = v_array[0]; v2 = v_array[1];
			if(!is_connected(v1, v2))
				continue;
			if(shrunk)
			{
				output_string += ("e " + id_map_in_shrinking.get_new_id_from_original_id(v1) + " " + id_map_in_shrinking.get_new_id_from_original_id(v2) + "\n");
			}
			else
			{
				output_string += ("e " + v1 + " " + v2 + "\n");
			}
			output_edge_num++;					
		}

if(output_edge_num != remaining_edge_list.size())
{
	System.out.println("output_edge_num: " + output_edge_num);
	System.out.println("remaing_edge_list.size(): " + remaining_edge_list.size());
	System.out.println("something is wrong in the preprocessment.");
	System.exit(1);
	System.out.println("removed_vertex_list: " + removed_vertex_list);
	System.out.println("remaining_vertex_list: " + remaining_vertex_list);
	System.out.println("removed_edge_list: " + removed_edge_list);
	System.out.println("remaining_edge_list: " + remaining_edge_list);
	System.exit(1);
}

		return output_string;
	}

	protected void delete_edge_hash_id_from_set(long v1, long v2)
	{
		long edge_hash_id = unordered_pair_id_encode(v1, v2);
		edge_hash_id_set.remove(edge_hash_id);
	}

	public ArrayList<Integer> get_remaining_vertex_list()
	{
		return remaining_vertex_list;
	}

	public ArrayList<Integer> get_removed_vertex_list()
	{
		return removed_vertex_list;
	}

	public ArrayList<Integer> get_remaining_edge_list()
	{
		return remaining_edge_list;
	}

	public ArrayList<Integer> get_removed_edge_list()
	{
		return removed_edge_list;
	}

/*
	public LinkedList<Integer> get_remaining_vertex_list()
	{
		return remaining_vertex_list;
	}

	public LinkedList<Integer> get_removed_vertex_list()
	{
		return removed_vertex_list;
	}

	public LinkedList<Integer> get_remaining_edge_list()
	{
		return remaining_edge_list;
	}

	public LinkedList<Integer> get_removed_edge_list()
	{
		return removed_edge_list;
	}
*/	

	public void remove_vertex_from_graph(int u)
	{
/*
for(int i = 0; i < vertices[u].get_degree(); i++)
{
	System.out.println(vertices[u].get_neighbors()[i] + " is found to be " + u + "\'s neighbor");
}
*/
/*
if(!remaining_vertex_list.contains(u))
{
	System.out.println(u + " should be in the graph but not");
	System.exit(1);
}
else
{
	System.out.println(u + " is in the graph indeed");
}

if(!dgr_based_partition.check_partitions(vertex_num, vertices, remaining_vertex_list, removed_vertex_list))
{
	System.out.println("dgr-based partition incorrect++++");
	System.exit(1);
}
else
{
	System.out.println("dgr-based partition correct++++^^^^");
}
*/
		// maintain partitions
		weight_based_partition.place_out_vertex_from_graph(u, vertices);

		dgr_based_partition.place_out_vertex_from_graph(u, vertices);
		for(int j = 0; j < vertices[u].get_degree(); j++)
		{
// System.out.println("dealing with " + u + "\'s neighbor " + vertices[u].get_neighbors()[j]);
			dgr_based_partition.degree_dec_by_one(vertices[u].get_neighbors()[j], vertices);
		}


		// maintain dynamic vertex sets
		for(Iterator<Integer> int_iterator = remaining_vertex_list.iterator(); int_iterator.hasNext();)
		{
			if(int_iterator.next() == u)
			{
				int_iterator.remove();
				break;
			}
		}
		removed_vertex_list.add(u);
/*
if(!dgr_based_partition.check_partitions(vertex_num, vertices, remaining_vertex_list, removed_vertex_list))
{
	System.out.println("dgr-based partition incorrect~~~~");
	System.exit(1);
}
else
{
	System.out.println("dgr-based partition correct~~~~");
}
*/
		// remove from static graph
		boolean max_degree_can_be_decreased = false;
		int[] nbs = vertices[u].get_neighbors();
		for(int nb_of_u : nbs)
		{
// System.out.println("considering neighbor " + nb_of_u + "**************************************");
// System.out.println("First show");
// dgr_based_partition.show_partitions();
			for(int i = 0; i < vertices[nb_of_u].get_degree(); i++)
			{
				if(vertices[nb_of_u].get_neighbors()[i] == u)
				{
					vertices[nb_of_u].get_neighbors()[i] = vertices[nb_of_u].get_neighbors()[vertices[nb_of_u].get_neighbors().length - 1];
					vertices[nb_of_u].set_neighbors(Arrays.copyOf(vertices[nb_of_u].get_neighbors(), vertices[nb_of_u].get_neighbors().length - 1));

					// maintain edge lists
					int e_to_del = vertices[nb_of_u].get_adj_edges()[i];
					for(int j = 0; j < remaining_edge_list.size(); j++)
					{
						if(remaining_edge_list.get(j) == e_to_del)
						{
							remaining_edge_list.remove(j);
							break;
						}
					}
					removed_edge_list.add(e_to_del);

					vertices[nb_of_u].get_adj_edges()[i] = vertices[nb_of_u].get_adj_edges()[vertices[nb_of_u].get_adj_edges().length - 1];
					vertices[nb_of_u].set_adj_edges(Arrays.copyOf(vertices[nb_of_u].get_adj_edges(), vertices[nb_of_u].get_adj_edges().length - 1));
					delete_edge_hash_id_from_set(u, nb_of_u);

					break;
				}
			}
			if(vertices[nb_of_u].get_degree() == get_max_degree())
			{
				max_degree_can_be_decreased = true;
			}
			vertices[nb_of_u].set_degree(vertices[nb_of_u].get_degree() - 1);
/*
if(!dgr_based_partition.check_partitions(vertex_num, vertices, remaining_vertex_list, removed_vertex_list))
{
	System.out.println("dgr-based partition incorrect====");
	System.exit(1);
}
else
{
	System.out.println("dgr-based partition correct====");
}
*/
// System.out.println("Second show");
// dgr_based_partition.show_partitions();
		} // for(int nb_of_u : nbs)
/*
if(!dgr_based_partition.check_partitions(vertex_num, vertices, remaining_vertex_list, removed_vertex_list))
{
	System.out.println("dgr-based partition incorrect****");
	System.exit(1);
}
else
{
	System.out.println("dgr-based partition correct****");
}
*/
		// max_vertex_weight
		if(vertices[u].get_weight() == get_max_vertex_weight())
		{
			set_max_vertex_weight(0);
			for(int v : remaining_vertex_list)
			{
				set_max_vertex_weight(StrictMath.max(get_max_vertex_weight(), vertices[v].get_weight()));
			}
		}
		// min_vertex_weight
		if(vertices[u].get_weight() == get_min_vertex_weight())
		{
			set_min_vertex_weight(get_max_vertex_weight());
			for(int v : remaining_vertex_list)
			{
				set_min_vertex_weight(StrictMath.min(get_min_vertex_weight(), vertices[v].get_weight()));
			}
		}
		// max_degree
		if(vertices[u].get_degree() == get_max_degree() || max_degree_can_be_decreased)
		{
			set_max_degree(0);
			for(int v : remaining_vertex_list)
			{
				set_max_degree(StrictMath.max(get_max_degree(), vertices[v].get_degree()));
			}
		}
	} // public void remove_vertex_from_graph(int u)
	
	public void show_graph()
	{
		System.out.println("p edge " + vertex_num + " " + edge_num);
		for(int v = 1; v <= vertex_num; v++)
		{
			System.out.println("v " + v + " " + vertices[v].get_weight());
		}
		System.out.println("neighbr lists:");
		for(int v : remaining_vertex_list)
		{
			System.out.print(v + ": ");
			for(int i = 0; i < vertices[v].get_degree(); i++)
				System.out.print(vertices[v].get_neighbors()[i] + "\t");
			System.out.println();
		}
		System.out.println();
	}
}
