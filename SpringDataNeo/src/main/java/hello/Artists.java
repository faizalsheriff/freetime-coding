package hello;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;


@NodeEntity
public class Artists {
	
	@GraphId
	private Long id;
	
	private String name;
	
	@RelatedTo(type = "ACTED_IN", direction=Direction.BOTH)
	@Fetch
    Set<Movie> movies;
	
	 public Artists() {}
	    public Artists(String name) { this.name = name; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Movie> getMovies() {
		return movies;
	}

	public void setMovies(Set<Movie> movies) {
		this.movies = movies;
	}
    

	
	
}
