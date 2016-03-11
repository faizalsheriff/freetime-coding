package hello;

import java.io.File;

import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

@Configuration
@EnableNeo4jRepositories
public class Application extends Neo4jConfiguration implements CommandLineRunner {

    @Bean
    EmbeddedGraphDatabase graphDatabaseService() {
        return new EmbeddedGraphDatabase("accessingdataneo4j.db");
    }

    @Autowired
    PersonRepository personRepository;
    
    @Autowired
    MovieRepository movieRepository;
    
    @Autowired
    UserRepository userRepository;
    
    
 
    

    @Autowired
    GraphDatabase graphDatabase;

    public void run(String... args) throws Exception {
        Person greg = new Person("Greg");
        Person roy = new Person("Roy");
        Person craig = new Person("Craig");

        
        Movie matrix1 = new Movie("Matrix1");
        Movie matrix2 = new Movie("Matrix2");
        Movie matrix3 = new Movie("Matrxi3");
        
        Artists artist1 = new Artists("Keanu Reeves");
        Artists artist2 = new Artists("Trinity");
        Artists artist3 = new Artists("Morpheous");
        
        User faizy = new User("Faizy");
    	User sherry = new User("Sherry");
        
        
        System.out.println("Before linking up with Neo4j...");
        for (Person person : new Person[]{greg, roy, craig}) {
            System.out.println(person);
        }

        Transaction tx = graphDatabase.beginTx();
        try {
            personRepository.save(greg);
            personRepository.save(roy);
            personRepository.save(craig);
            
            greg = personRepository.findByName(greg.name);
            greg.worksWith(roy);
            greg.worksWith(craig);
            personRepository.save(greg);

            roy = personRepository.findByName(roy.name);
            roy.worksWith(craig);
            // We already know that roy works with greg
            personRepository.save(roy);
            
            // We already know craig works with roy and greg

            tx.success();
        } finally {
            tx.finish();
        }
        
        
        tx = graphDatabase.beginTx();
        try {
        	
        	System.out.println("About to save");
        	movieRepository.save(matrix1);
        	
        	movieRepository.save(matrix2);
        	movieRepository.save(matrix3);
            
        	matrix1 = movieRepository.findByName(matrix1.getName());
            matrix1.actsIn(artist1);
            matrix1.actsIn(artist2);
            matrix1.actsIn(artist3);
            //greg.worksWith(craig);
            movieRepository.save(matrix1);

            //roy = personRepository.findByName(roy.name);
            //roy.worksWith(craig);
            // We already know that roy works with greg
           // personRepository.save(roy);
            
            // We already know craig works with roy and greg

            tx.success();
        } finally {
            tx.finish();
        }
        
        
        
        tx = graphDatabase.beginTx();
        try {
        	
        	System.out.println("About to save user");
        	
        	
        	faizy.likeMovie(matrix1);
        	faizy.likeMovie(matrix3);
        	
        	
        	
        	sherry.likeMovie(matrix2);
        	sherry.likeMovie(matrix3);
        	
        	
        	userRepository.save(sherry);
        	
        	userRepository.save(faizy);
        	//userRepository.save(matrix3);
            
        	//matrix1 = userRepository.findByName(matrix1.getName());
            //matrix1.actsIn(artist1);
           // matrix1.actsIn(artist2);
            //matrix1.actsIn(artist3);
            //greg.worksWith(craig);
            //userRepository.save(matrix1);

            //roy = personRepository.findByName(roy.name);
            //roy.worksWith(craig);
            // We already know that roy works with greg
           // personRepository.save(roy);
            
            // We already know craig works with roy and greg

            tx.success();
        } finally {
            tx.finish();
        }
        
        

        System.out.println("Lookup each person by name...");
        for (String name: new String[]{greg.name, roy.name, craig.name}) {
            System.out.println(personRepository.findByName(name));
        }

        System.out.println("Looking up who works with Greg...");
        for (Person person : personRepository.findByTeammatesName("Greg")) {
            System.out.println(person.name + " works with Greg.");
        }
        
        
        System.out.println("Lookup each movie by name...");
        for (String name: new String[]{matrix1.getName(), matrix2.getName(), matrix3.getName()}) {
            System.out.println(movieRepository.findByName(name).getName());
            
            if(movieRepository.findByName(name).getArtists()!=null && movieRepository.findByName(name).getArtists().size() > 0){
            	for (Artists a: movieRepository.findByName(name).getArtists()){
            		System.out.println("Artist Name"+a.getName());
            		if(a.getMovies()!=null && a.getMovies().size() > 0){
            			for(Movie m : a.getMovies()){
            			System.out.println("Movies printing bi driectional"+m.getName());
            			}
            		}else{
            			System.out.println("Movies printing bi driectional nomovie found for "+a.getName());
            		}
            		
            	}
            	System.out.println();
            }else{
            	System.out.println("Empty Artist");
            }
            
        }
        
        System.out.println("Find user liked movies of faizy");
        faizy = userRepository.findByName("Faizy");
        
        if(faizy.getUserLikes()!=null && faizy.getUserLikes().size()>0){
        	for (Like l :faizy.getUserLikes() ){
        		System.out.println(l.getUser().getName()+" likes "+l.getMovie().getName());
        	}
        }else{
        	System.out.println("User did not like anything");
        }

    }

    public static void main(String[] args) throws Exception {
        FileUtils.deleteRecursively(new File("accessingdataneo4j.db"));

        SpringApplication.run(Application.class, args);
    }

}
