package it.polito.tdp.imdb.model;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	private Collection<List<Director>> bestPaths;
	private int maxNumDirectors;
	private Map<List<Director>, Integer> totCommonActors;
	
	
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


	public void computeBestDirectorsPathFrom(Director startDirector, int numMaxActors)
	{
		//initialization
		this.bestPaths = new HashSet<>();
		this.maxNumDirectors = Integer.MIN_VALUE;
		this.totCommonActors = new HashMap<>();
		
		List<Director> partialSolution = new ArrayList<>();
		Set<Director> partialSolutionSet = new HashSet<>();	//overhead to increase performance
		partialSolution.add(startDirector);
		partialSolutionSet.add(startDirector);
		
		int currentCommonActors = 0;
		
		this.recursiveBestPathComputation(partialSolution, partialSolutionSet, 
				currentCommonActors, numMaxActors);
	}


	private void recursiveBestPathComputation(List<Director> partialSolution, 
			Set<Director> partialSolutionSet, int currentCommonActors, int numMaxActors)
	{
		Director lastAdded = partialSolution.get(partialSolution.size() - 1);
		boolean flag = false; //it indicates if this is(not) a terminal node in a path
		
		for(var nextEdge : this.graph.edgesOf(lastAdded))
		{
			int commonActors = (int)this.graph.getEdgeWeight(nextEdge);
			Director adjacentDirector = Graphs.getOppositeVertex(this.graph, nextEdge, lastAdded);
						
			if(partialSolutionSet.contains(adjacentDirector) || 
					currentCommonActors + commonActors > numMaxActors)
				continue;	//this node must not be explored
			
			flag = true;	//explore new nodes
			partialSolution.add(adjacentDirector);
			partialSolutionSet.add(adjacentDirector);
			this.recursiveBestPathComputation(partialSolution, partialSolutionSet,
										currentCommonActors + commonActors, numMaxActors);	//recursive call
			
			partialSolution.remove(partialSolution.size() - 1); //backtracking
			partialSolutionSet.remove(adjacentDirector);
		}
		
		if(!flag)
		{
			//lastAdded is a terminal node in a path
			int numDirectors = partialSolution.size();
			
			if(numDirectors >= this.maxNumDirectors)
			{
				if(numDirectors > this.maxNumDirectors)
				{
					this.bestPaths = new HashSet<>();
					this.maxNumDirectors = numDirectors;
					this.totCommonActors = new HashMap<>();
				}
				
				List<Director> bestPath = new ArrayList<>(partialSolution);
				this.bestPaths.add(bestPath);	//adding new best solution
				this.totCommonActors.put(bestPath, currentCommonActors);
			}
		}
	}
	
	public Map<List<Director>, Integer> getTotCommonActors()
	{
		return this.totCommonActors;
	}
	
	public Collection<List<Director>> getBestPaths()
	{
		return this.bestPaths;
	}
}
