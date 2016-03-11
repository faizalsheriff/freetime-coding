package com.netflix.astyanax.examples;
import java.util.List;
import java.util.Map;




public class SOAPTemplate {
	
	
	private static final String SPACE = " ";
	private static final String OPEN_BRACE = "(";
	private static final String COMMA = ",";
	private static final String CLOSE_BRACE = ")";
	private static final String VALUES = "VALUES";
	private static final String WITH = "WITH";

	public boolean insert(String tableName, Map<?, ?> param, List<?> options){
		formInsertQuery(tableName, param, options);
		
		return true;
	}

	
	public String formCreateQuery(String tableName, List<QueryParam> param, List<?> options){
		StringBuilder queryFormer = new StringBuilder();
		StringBuilder primaryKeyFormer = new StringBuilder();
		
		queryFormer.append("CREATE TABLE ").append(tableName).append(SPACE).append(OPEN_BRACE);
		primaryKeyFormer.append(SPACE).append("PRIMARY KEY").append(SPACE).append(OPEN_BRACE);
		
		java.util.Iterator<?> it = param.iterator();
		QueryParam p = null;
		while(it.hasNext()){
			p=(QueryParam)it.next();
			queryFormer.append(p.getName()).append(SPACE).append(p.getType());
			
			if(p.isPrimary()){
			primaryKeyFormer.append(p.getName()).append(COMMA);
			}
			
			//if(it.hasNext()){
				queryFormer.append(COMMA);
			//}
			
		}
		primaryKeyFormer.deleteCharAt(primaryKeyFormer.lastIndexOf(COMMA));
		primaryKeyFormer.append(CLOSE_BRACE);
		
		queryFormer.append(primaryKeyFormer.toString()).append(CLOSE_BRACE);
		
		System.out.println(queryFormer.toString());
	
		if(options!=null && options.size()>0){
			queryFormer.append(SPACE).append(WITH).append(SPACE);
		
			it = options.iterator();
			while(it.hasNext()){
				
				queryFormer.append(it.next()).append(SPACE);
				
			
				if(it.hasNext()){
					queryFormer.append(SPACE);
				}
				
			}
			
		}
	
		System.out.println(queryFormer.toString());
		
		return queryFormer.toString();
	}
	
	public String formInsertQuery(String tableName, Map<?, ?> param, List<?> options) {
		StringBuilder queryFormer = new StringBuilder();
		StringBuilder valueFormer = new StringBuilder();
	
		
		queryFormer.append("INSERT INTO ").append(tableName).append(SPACE).append(OPEN_BRACE);
		valueFormer.append(OPEN_BRACE);
		
		java.util.Iterator<?> it = param.keySet().iterator();
		
		StringBuilder key = new StringBuilder();
		StringBuilder value = new StringBuilder();
		
		while(it.hasNext()){
			key.append(it.next());
			value.append(param.get(key.toString()));
			queryFormer.append(key.toString()).append(COMMA).append(SPACE);
			valueFormer.append(value.toString()).append(COMMA).append(SPACE);
			key.delete(0,(key.length()));
			value.delete(0,(value.length()));
			
		}
		queryFormer.deleteCharAt(queryFormer.lastIndexOf(COMMA));
		valueFormer.deleteCharAt(valueFormer.lastIndexOf(COMMA));
		valueFormer.append(CLOSE_BRACE);
		queryFormer.append(CLOSE_BRACE).append(SPACE).append(VALUES).append(SPACE).append(valueFormer.toString());
		
	/*	String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);",
		          EMP_CF_NAME, COL_NAME_EMPID, COL_NAME_DEPTID, COL_NAME_FIRST_NAME, COL_NAME_LAST_NAME);*/
		System.out.println(queryFormer.toString());
		
		if(options!=null && options.size() > 0){
		queryFormer.append(SPACE).append(WITH).append(SPACE);
		it = options.iterator();
		while(it.hasNext()){
			queryFormer.append(it.next()).append(COMMA);
			
		}
		queryFormer.deleteCharAt(queryFormer.lastIndexOf(COMMA));
		}
		
		
		
		System.out.println(queryFormer.toString());
		return queryFormer.toString();
	}

}
