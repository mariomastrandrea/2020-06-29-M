package it.polito.tdp.imdb.model;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import it.polito.tdp.imdb.db.ImdbDAO;

public class Model 
{
	private final ImdbDAO dao;
	private Graph<Director, DefaultWeightedEdge> graph;
	private final Map<Integer, Director> directorsIdMap;
	
	
	public Model()
	{
		this.dao = new ImdbDAO();
		this.directorsIdMap = new HashMap<>();
	}
	
	
	public void createGraph(Year selectedYear)
	{
		this.graph = GraphTypeBuilder.<Director, DefaultWeightedEdge>undirected()
									.allowingMultipleEdges(false)
									.allowingSelfLoops(false)
									.weighted(true)
									.edgeClass(DefaultWeightedEdge.class)
									.buildGraph();
		
		//add vertices
		Collection<Director> vertices = 
				this.dao.getAllDirectorsIn(selectedYear, this.directorsIdMap);
		Graphs.addAllVertices(this.graph, vertices);
		
		//add edges
		Collection<Adjacence> directorsAdjacences = 
				this.dao.getDirectorsAdjacencesIn(selectedYear, this.directorsIdMap);
		
		for(var adjacence : directorsAdjacences)
		{
			Director d1 = adjacence.getDirector1();
			Director d2 = adjacence.getDirector2();
			double weight = adjacence.getNumActors();
			
			Graphs.addEdge(this.graph, d1, d2, weight);
		}
	}
	
	public int getNumVertices() { return this.graph.vertexSet().size(); }
	public int getNumEdges() { return this.graph.edgeSet().size(); }
	
	public List<Director> getOrderedDirectors()
	{
		List<Director> orderedDirectors = new ArrayList<>(this.graph.vertexSet());
		orderedDirectors.sort((d1, d2) -> {
			return (d1.getFirstName() + d1.getLastName()).compareTo(d2.getFirstName() + d2.getLastName());
		});
		
		return orderedDirectors;
	}

	public boolean isGraphCreated()
	{
		return this.graph != null;
	}


	public Map<Director, Integer> getAdjacentDirectorsTo(Director selectedDirector)
	{
		Map<Director, Integer> adjacentDirectors = new HashMap<>();
		
		if(this.graph == null || !this.graph.containsVertex(selectedDirector))
			return null;
		
		for(var edge : this.graph.edgesOf(selectedDirector))
		{
			int commonActors = (int)this.graph.getEdgeWeight(edge);
			Director adjacentDirector = Graphs.getOppositeVertex(this.graph, edge, selectedDirector);
			
			adjacentDirectors.put(adjacentDirector, commonActors);
		}
		
		return adjacentDirectors;
	}

}
