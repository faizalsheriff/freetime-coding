
package com.fanho.media.views.rest;

import java.util.List;

import com.fanho.media.movies.beans.Links;

public class MovieView{
   	private String link_template;
   	private Links links;
   	private List movies;
   	private Number total;

 	public String getLink_template(){
		return this.link_template;
	}
	public void setLink_template(String link_template){
		this.link_template = link_template;
	}
 	public Links getLinks(){
		return this.links;
	}
	public void setLinks(Links links){
		this.links = links;
	}
 	public List getMovies(){
		return this.movies;
	}
	public void setMovies(List movies){
		this.movies = movies;
	}
 	public Number getTotal(){
		return this.total;
	}
	public void setTotal(Number total){
		this.total = total;
	}
}
