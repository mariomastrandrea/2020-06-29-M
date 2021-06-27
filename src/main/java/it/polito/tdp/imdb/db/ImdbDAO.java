package it.polito.tdp.imdb.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import it.polito.tdp.imdb.model.Actor;
import it.polito.tdp.imdb.model.Adjacence;
import it.polito.tdp.imdb.model.Director;
import it.polito.tdp.imdb.model.Movie;

public class ImdbDAO {
	
	public List<Actor> listAllActors(){
		String sql = "SELECT * FROM actors";
		List<Actor> result = new ArrayList<Actor>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Actor actor = new Actor(res.getInt("id"), res.getString("first_name"), res.getString("last_name"),
						res.getString("gender"));
				
				result.add(actor);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<Movie> listAllMovies(){
		String sql = "SELECT * FROM movies";
		List<Movie> result = new ArrayList<Movie>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Movie movie = new Movie(res.getInt("id"), res.getString("name"), 
						res.getInt("year"), res.getDouble("rank"));
				
				result.add(movie);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public List<Director> listAllDirectors(){
		String sql = "SELECT * FROM directors";
		List<Director> result = new ArrayList<Director>();
		Connection conn = DBConnect.getConnection();

		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			while (res.next()) {

				Director director = new Director(res.getInt("id"), res.getString("first_name"), res.getString("last_name"));
				
				result.add(director);
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Collection<Director> getAllDirectorsIn(Year selectedYear, 
									Map<Integer, Director> directorsIdMap)
	{
		final String sqlQuery = String.format("%s %s %s %s %s",
				"SELECT *",
				"FROM directors",
				"WHERE id IN (SELECT md.director_id",
							 "FROM movies_directors md, movies m",
							 "WHERE md.movie_id = m.id AND m.year = ?)");
		
		Collection<Director> result = new HashSet<>();

		try 
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setInt(1, selectedYear.getValue());
			ResultSet queryResult = statement.executeQuery();
			
			while (queryResult.next()) 
			{
				int directorId = queryResult.getInt("id");
				
				if(!directorsIdMap.containsKey(directorId))
				{
					Director newDirector = new Director(directorId, 
											queryResult.getString("first_name"), 
											queryResult.getString("last_name"));
					
					directorsIdMap.put(directorId, newDirector);
				}
				
				result.add(directorsIdMap.get(directorId));
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return result;	
		} 
		catch (SQLException sqle) 
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getAllDirectorsIn()", sqle);
		}
	}

	public Collection<Adjacence> getDirectorsAdjacencesIn(Year selectedYear, 
			Map<Integer, Director> directorsIdMap)
	{
		final String sqlQuery = String.format("%s %s %s %s %s %s",
			"SELECT md1.director_id AS director1, md2.director_id AS director2, COUNT(DISTINCT r1.actor_id) AS numActors",
			"FROM movies_directors md1, movies_directors md2, movies m1, movies m2, roles r1, roles r2",
			"WHERE md1.movie_id = m1.id AND md1.movie_id = r1.movie_id AND m1.year = ?",
				"AND md2.movie_id = m2.id AND md2.movie_id = r2.movie_id AND m2.year = ?",
				"AND md1.director_id < md2.director_id AND r1.actor_id = r2.actor_id",
			"GROUP BY md1.director_id, md2.director_id");
		
		Collection<Adjacence> adjacences = new HashSet<>();

		try 
		{
			Connection connection = DBConnect.getConnection();
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			statement.setInt(1, selectedYear.getValue());
			statement.setInt(2, selectedYear.getValue());
			ResultSet queryResult = statement.executeQuery();
			
			while (queryResult.next()) 
			{
				int directorId1 = queryResult.getInt("director1");
				int directorId2 = queryResult.getInt("director2");
				int numActorsInCommon = queryResult.getInt("numActors");

				if(!directorsIdMap.containsKey(directorId1) ||
						!directorsIdMap.containsKey(directorId2))
				{
					System.err.println("Error: director Id not found in IdMap: id1="+directorId1+" id2=" + directorId2);
					continue;
				}
					
				Director director1 = directorsIdMap.get(directorId1);
				Director director2 = directorsIdMap.get(directorId2);

				Adjacence newAdjacence = new Adjacence(director1, director2, (double)numActorsInCommon);
				adjacences.add(newAdjacence);
			}
			
			queryResult.close();
			statement.close();
			connection.close();
			return adjacences;	
		} 
		catch (SQLException sqle) 
		{
			sqle.printStackTrace();
			throw new RuntimeException("Dao error in getDirectorsAdjacencesIn()", sqle);
		}
	}	
}
