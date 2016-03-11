package hello;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface ArtistRepository extends GraphRepository<Artists> {

	Artists findByArtist(String name);

    

}
