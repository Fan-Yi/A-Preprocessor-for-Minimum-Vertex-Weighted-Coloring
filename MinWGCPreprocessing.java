// using directories
import java.io.*;
import java.util.*;

import DelFolderPack.DeleteFolder;
import UsrPausePack.UsrPause;
import GraphPack.WGCReducedGraph;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;

import CliqueSearchPack.CliqueSearch;
import BucketPack.DegreeBasedPartition;

import UsrPausePack.UsrPause;

class MinWGCPreprocessing
{
	private static boolean check_and_deal_with_preexisting_folder(File out_folder)
	{
		if(!out_folder.exists())
			return true;

		System.out.println(out_folder.getName() + " exists");
		System.out.println("Do you want to delete this folder together with all its contents? (y/n)");

		char c = 0;
		do{
			try{
				c = (char)System.in.read();					
			}catch(IOException e)
			{
				System.out.println("Input error occurs when reading a character from the user: " + e);
			}		
		}while(c != 'y' && c != 'n');

		if(c == 'y')
		{
			DeleteFolder df = new DeleteFolder();
			df.rmdir(out_folder);
		}
		else
		{
			// keep
			return false;
		}

		return true;				

	}

	public static void main(String args[])
	{

		// First, ensure there are 2 args
    if (args.length != 2) {
      throw new IllegalArgumentException("Exactly 2 arguments required!");
    }

		// deal with input folder		
		String instance_folder_name = args[0];
		System.out.println("Instance Folder Name: " + instance_folder_name);

		File instance_folder = new File(instance_folder_name);
		if(!instance_folder.isDirectory())
		{
			System.out.println(instance_folder_name + " is not a directory.");
			return;
		}

		String instance_file_name_list_str[] = instance_folder.list();

		// deal with output folder		
		String output_instance_folder_name = args[1];
		File output_instance_folder = new File(output_instance_folder_name);
		if(!check_and_deal_with_preexisting_folder(output_instance_folder))
		{
			System.out.println(output_instance_folder_name + " keeps unchanged.");
			return;
		}
		
		if(!output_instance_folder.mkdir())
		{
			System.out.println(output_instance_folder_name + " cannot be created");
			return;
		}

		// enumerate each instance file in the input folder
		for(String instance_file_name : instance_file_name_list_str)
		{
			// construct a graph
			WGCReducedGraph instance_graph = new WGCReducedGraph(instance_folder_name, instance_file_name);

			//instance_graph.show_graph();

			// Clique Reduction Rule
			instance_graph.apply_clique_reductions();

/*
			System.out.println("there remain " + instance_graph.get_remaining_vertex_list().size() + " vertices after reductions.");
			System.out.println("instance_graph.get_remaining_vertex_list().size(): " + instance_graph.get_remaining_vertex_list().size());
			System.out.println("instance_graph.get_removed_vertex_list().size(): " + instance_graph.get_removed_vertex_list().size());
*/
			double rate = (double)instance_graph.get_remaining_vertex_list().size() / (instance_graph.get_remaining_vertex_list().size() + instance_graph.get_removed_vertex_list().size());
			System.out.println("remain rate is " + String.format("%.2f", rate));

			// instance_graph.show_graph();		

			String output_instance_file_name = output_instance_folder_name + "/" + instance_file_name;
			// open output file
			File output_instance_file = new File(output_instance_file_name);
			try{
				FileWriter fw = new FileWriter(output_instance_file);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("p edge " + instance_graph.get_vertex_num() + " " + instance_graph.get_remaining_edge_list().size() + "\n"); 
				// use the original vertex_num for the ease of later procedures

				/* should write something */
				for(int v = 1; v <= instance_graph.get_vertex_num(); v++)
				{
					bw.write("v " + v + " " + instance_graph.get_vertices()[v].get_weight() + "\n");
				}

				int output_edge_num = 0;
				for(int e = 0; e < instance_graph.get_edge_num(); e++)
				{
					int v1, v2;
					int[] v_array = new int[2];
					instance_graph.get_edges()[e].get_vertices(v_array);
					v1 = v_array[0]; v2 = v_array[1];
					if(!instance_graph.is_connected(v1, v2))
						continue;
					bw.write("e " + v1 + " " + v2 + "\n");
					output_edge_num++;					
				}

				bw.flush();
				fw.close();

				if(output_edge_num != instance_graph.get_remaining_edge_list().size())
				{
					System.out.println("output_edge_num: " + output_edge_num);
					System.out.println("remaing_edge_list.size(): " + instance_graph.get_remaining_edge_list().size());
					System.out.println("something is wrong in the preprocessment.");
					System.exit(1);
					System.out.println("removed_vertex_list: " + instance_graph.get_removed_vertex_list());
					System.out.println("remaining_vertex_list: " + instance_graph.get_remaining_vertex_list());
					System.out.println("removed_edge_list: " + instance_graph.get_removed_edge_list());
					System.out.println("remaining_edge_list: " + instance_graph.get_remaining_edge_list());
					System.exit(1);
				}
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}
				
		}			
	} // for(String instance_file_name : instance_file_name_list_str)
}
