package com.journaldev.spring;

import java.util.ArrayList;
import java.util.List;


import org.springframework.util.MimeType;

public class MyMessageConverter extends org.springframework.http.converter.json.MappingJackson2HttpMessageConverter{
	
	
   private List medias = new ArrayList();
   
   
   
   public void myinit(){
	   System.out.println("my init is called");
	   medias.addAll(super.getSupportedMediaTypes());
	   
	   
	   MimeType mymedia =  new MimeType("application","json",DEFAULT_CHARSET);
	   
	   
	   
   }
   
   @Override
   public List getSupportedMediaTypes(){
	   System.out.println("My supported medias is called");
	   return medias;
   }

}
