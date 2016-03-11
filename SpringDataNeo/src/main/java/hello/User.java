package hello;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

//teammates
@NodeEntity
public class User {
	
	@GraphId
	private Long id;
	
    @Indexed(unique = true)
	private String name;
	
	private int age;
	
	
	
	
	@Fetch
	@RelatedToVia (direction=Direction.BOTH)
	private Set<Like> userLikes;
	
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Set<Like> getUserLikes() {
		return userLikes;
	}

	public void setUserLikes(Set<Like> userLikes) {
		this.userLikes = userLikes;
	}

	public void likeMovie(Movie m){
		if(userLikes == null){
			userLikes = new HashSet<Like>();
			
		}
		
		Like l = new Like (this, m, "05/14/2013");
		userLikes.add(l);
	}
	
	 public User() {}
	    public User(String name) { this.name = name; }
	
}
