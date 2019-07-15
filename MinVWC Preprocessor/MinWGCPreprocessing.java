// using directories
import java.io.*;
import java.util.*;

import DelFolderPack.DeleteFolder;
import UsrPausePack.UsrPause;
import GraphPack.WGCReducedGraph;
import GraphPack.WeightedVertexPack.VertexPack.Vertex;

import CliqueSearchPack.CliqueSearch;
import DegreeBucketPack.DegreeBasedPartition;

import UsrPausePack.UsrPause;


class MinWGCPreprocessing
{
	final static boolean REQUEST_SHRUNK_GRAPH = true;

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
			Date date = new Date();
			long start_elap_milliseconds_from_past = date.getTime();

			// construct a graph
			WGCReducedGraph instance_graph = new WGCReducedGraph(instance_folder_name, instance_file_name);

			// Below are Australasian AI Conference submitted algorithms

			instance_graph.apply_reductions_with_enumerated_cliques_containing_top_weight_vertices();
			instance_graph.apply_reductions_with_enumerated_cliques_containing_top_degree_vertices();			
			instance_graph.apply_reductions_with_new_critical_cliques();			
			instance_graph.apply_post_reductions();

			// Below are ICTAI submitted algorithms

			// instance_graph.apply_shadow_reductions(true);


			double rate = (double)instance_graph.get_remaining_vertex_list().size() / (instance_graph.get_remaining_vertex_list().size() + instance_graph.get_removed_vertex_list().size());
			System.out.println("remain rate is " + String.format("%.4f", rate));

			String output_instance_file_name = output_instance_folder_name + "/" + instance_file_name;
			// open output file
			File output_instance_file = new File(output_instance_file_name);
			try{
				FileWriter fw = new FileWriter(output_instance_file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(instance_graph.toString(REQUEST_SHRUNK_GRAPH));
				bw.flush();
				fw.close();
			}catch(IOException e)
			{
				System.out.println("Exception occurs when writing a line to file: " + e);
			}

			date = new Date();
			long end_elap_milliseconds_from_past = date.getTime();
			long elap_time = end_elap_milliseconds_from_past - start_elap_milliseconds_from_past;

			System.out.println("The time past is " + (double)elap_time / 1000 + "s");

			String latex_source_string = new String();
			if(instance_file_name.substring(instance_file_name.length() - 5, instance_file_name.length()).equals(".dimw"))
				latex_source_string += instance_file_name.substring(0, instance_file_name.length() - 5);
			else
				latex_source_string += instance_file_name;
			latex_source_string += instance_graph.vertex_and_edge_num_change_info_to_string();
			latex_source_string += " & " + String.format("%.4f", rate);
			latex_source_string += " & " + (double)elap_time / 1000;
			System.out.println(latex_source_string);
		} // for(String instance_file_name : instance_file_name_list_str)			
	} // for(String instance_file_name : instance_file_name_list_str)
}
