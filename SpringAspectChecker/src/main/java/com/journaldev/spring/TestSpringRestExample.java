package com.journaldev.spring;



import java.util.List;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.journaldev.spring.controller.EmpRestURIConstants;
import com.journaldev.spring.model.Employee;
import com.fanho.media.movies.beans.MovieView;

public class TestSpringRestExample {

	//public static final String SERVER_URI = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=3mm9wqu9fpzdtakd2s7q87nj&q=Toy+Story+3&page_limit=1";
	public static final String SERVER_URI = "http://www.omdbapi.com/?t=True%20Grit&y=1969";
	
	public static void main(String args[]){
		
		
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("servlet-context.xml");
		com.journaldev.spring.MyMessageConverter msgConverter = (MyMessageConverter) context.getBean("jsonMessageConverter");
		
	
		
		//context.getBean("");
		//testGetDummyEmployee();
		System.out.println("*****");
		//testCreateEmployee();
		System.out.println("*****");
		//testGetEmployee();
		System.out.println("*****");
		testGetAllEmployee(msgConverter);
	}

	private static void testGetAllEmployee(MyMessageConverter msgConverter) {
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
		
		converters.add(msgConverter);
		for(HttpMessageConverter convert:converters){
			System.out.println(convert.getClass());
			/*if(convert instanceof com.journaldev.spring.MyMessageConverter){
				System.out.println("jackson mapping is added");
				List medias = convert.getSupportedMediaTypes();
				medias.add("text/javascript;charset=ISO-8859-1");
			}*/
			List medias = convert.getSupportedMediaTypes();
					for(Object media: medias){
						
						System.out.println(media.toString());
						
					}
			
		}
		
		System.out.println("^&^&^^&^&"+converters.size());
		converters.add(msgConverter);
	
		//restTemplate
		//we can't get List<Employee> because JSON convertor doesn't know the type of
		//object in the list and hence convert it to default JSON object type LinkedHashMap
		MovieView emps = restTemplate.getForObject(SERVER_URI, MovieView.class);
		System.out.println("running minw"+emps);
		/*for(LinkedHashMap map : emps){
			System.out.println("ID="+map.get("id")+",Name="+map.get("name")+",CreatedDate="+map.get("createdDate"));;
		}*/
	}

	private static void testCreateEmployee() {
		RestTemplate restTemplate = new RestTemplate();
		Employee emp = new Employee();
		emp.setId(1);emp.setName("Pankaj Kumar");
		Employee response = restTemplate.postForObject(SERVER_URI+EmpRestURIConstants.CREATE_EMP, emp, Employee.class);
		printEmpData(response);
	}

	private static void testGetEmployee() {
		RestTemplate restTemplate = new RestTemplate();
		Employee emp = restTemplate.getForObject(SERVER_URI+"/rest/emp/1", Employee.class);
		printEmpData(emp);
	}

	private static void testGetDummyEmployee() {
		RestTemplate restTemplate = new RestTemplate();
		Employee emp = restTemplate.getForObject(SERVER_URI+EmpRestURIConstants.DUMMY_EMP, Employee.class);
		printEmpData(emp);
	}
	
	public static void printEmpData(Employee emp){
		System.out.println("ID="+emp.getId()+",Name="+emp.getName()+",CreatedDate="+emp.getCreatedDate());
	}
}
