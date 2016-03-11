package hello;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity(type="like")
public class Like {

	
	public Like(User user, Movie m, String s) {
		this.user = user;
		this.movie = m;
		this.since = s;
	}
	
	 public Like() {
		 
	 }
	    
	
    @GraphId
	private Long id;

	private String since;
	
	public String getSince() {
		return since;
	}

	public void setSince(String since) {
		this.since = since;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}




	@StartNode
	
	private User user;
	
	
	@EndNode
	private Movie movie;
}
