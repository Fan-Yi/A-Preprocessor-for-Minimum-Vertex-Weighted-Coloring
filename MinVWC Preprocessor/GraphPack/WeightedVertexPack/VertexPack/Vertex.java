package GraphPack.WeightedVertexPack.VertexPack;

public class Vertex{
	
	protected int neighbors[];
	protected int adj_edges[];
	protected	int degree;


	public Vertex()
	{
		neighbors = null;
		adj_edges = null;
		degree = 0;
	}

	public Vertex(int[] nbs, int[] adj_edgs, int d)
	{
		neighbors = nbs;
		adj_edges = adj_edgs;
		degree = d;
	}

	public void set_neighbors(int[] nbs)
	{
		neighbors = nbs;
	}

	public void set_adj_edges(int[] adj_edgs)
	{
		adj_edges = adj_edgs;
	}

	public void allocate_neighborhood_space(int dgr)
	{
		neighbors = new int[dgr];
		adj_edges = new int[dgr];
	}

	public void add_neighbor(int name, int index)
	{
		neighbors[index] = name;
	}

	public void add_adj_edge(int name, int index)
	{
		adj_edges[index] = name;
	}

	public int[] get_neighbors()
	{
		return neighbors;
	}

	public int[] get_adj_edges()
	{
		return adj_edges;
	}

	public void set_degree(int d)
	{
		degree = d;
	}

	public int get_degree()
	{
		return degree;
	}

}
