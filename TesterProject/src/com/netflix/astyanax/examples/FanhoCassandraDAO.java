package com.netflix.astyanax.examples;

import java.util.Set;

import org.reflections.Reflections;

import com.netflix.astyanax.entitystore.DefaultEntityManager;
import com.netflix.astyanax.entitystore.EntityManager;

public class FanhoCassandraDAO {
	
	
	private String repoPath;
	private boolean isCreate;
    private CassandaraEntityManager cm;
    
    public FanhoCassandraDAO(){
    	if(isCreate){
    		create();
    	}
    }
	
	public void create(){
		
		 EntityManager<Class, String> entityManager = null;
		 
		 
		 Reflections reflections = new Reflections(repoPath);

		 Set<Class<? extends Object>> allClasses = 
		     reflections.getSubTypesOf(Object.class);
		 
		 for(Class iden:allClasses)
			    new DefaultEntityManager.Builder<Class, String>()
			    .withEntityType(iden)
			    .withKeyspace(cm.getKeyspace())
			    .withColumnFamily(iden.getSimpleName())
			    .build();
	}
	
/*	public void insert(String tableName, List<QueryParam> param, List<String> options){
	

		
	}
	
	
    public void update(String tableName, List<QueryParam> param, List<String> options){
		
	}*/
	
    
    
    public void update(String tableName){
		
	}

}
