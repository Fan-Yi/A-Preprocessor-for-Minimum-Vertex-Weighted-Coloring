package GraphPack.WeightedVertexPack;

import GraphPack.WeightedVertexPack.VertexPack.Vertex;

public class WeightedVertex extends Vertex{

	protected int weight;

	public WeightedVertex()
	{
		super();
	}

	public WeightedVertex(int[] nbs, int[] adj_edgs, int d, int w)
	{
		super(nbs, adj_edgs, d);
		weight = 0;
	}

	public int get_weight()
	{
		return weight;
	}

	public void set_weight(int w)
	{
		weight = w;
	}
}
