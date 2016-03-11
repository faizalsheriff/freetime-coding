package hello;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface MovieRepository extends GraphRepository<Movie> {

    Movie findByName(String name);

    

}
