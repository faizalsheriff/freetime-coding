package com.fanho.restful.response;

import java.util.List;

public class Movie {
	
	private String name;
	private String yearReleased;
	private List<String> starring;
	private String rating;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getYearReleased() {
		return yearReleased;
	}
	public void setYearReleased(String yearReleased) {
		this.yearReleased = yearReleased;
	}
	public List<String> getStarring() {
		return starring;
	}
	public void setStarring(List<String> starring) {
		this.starring = starring;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}

}
