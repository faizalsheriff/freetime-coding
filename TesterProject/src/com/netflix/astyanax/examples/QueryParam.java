package com.netflix.astyanax.examples;

public class QueryParam {
	
	private String name;
	private String type;
	private boolean isPrimary;
	public QueryParam(String string, String string2, boolean b) {
	
		
		this.name=string;
		this.type=string2;
		this.isPrimary=b;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isPrimary() {
		return isPrimary;
	}
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

}
