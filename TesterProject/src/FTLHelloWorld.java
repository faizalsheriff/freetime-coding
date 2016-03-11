

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FTLHelloWorld {
	
	public static void main(String[] args) {

		Configuration cfg = new Configuration();
		try {
			
			Template template = cfg.getTemplate("src/helloworld.ftl");
			
			// Build the data-model
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("message", "Hello World!");

			//List parsing 
			List<String> countries = new ArrayList<String>();
			countries.add("India");
			countries.add("United States");
			countries.add("Germany");
			countries.add("France");
			countries.add("Brazil");
			
			data.put("countries", countries);
			data.put("name", "faizy");
			data.put("following","50");

			
			// Console output
			Writer out = new OutputStreamWriter(System.out);
			template.process(data, out);
			out.flush();

			System.out.println("it is get message now");
			System.out.println(getMessage());
			
		/*	// File output
			Writer file = new FileWriter (new File("C:\\FTL_helloworld.txt"));
			template.process(data, file);
			file.flush();
			file.close();*/
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static String getMessage(){
		try{
		Configuration cfg = new Configuration();
		Template template = cfg.getTemplate("src/app.ftl");
		
		// Build the data-model
		Map<String, Object> data = new HashMap<String, Object>();
		/*data.put("message", "Hello World!");

		//List parsing 
		List<String> countries = new ArrayList<String>();
		countries.add("India");
		countries.add("United States");
		countries.add("Germany");
		countries.add("France");
		
		data.put("countries", countries);*/
		data.put("name", "Faizyie");
		data.put("following", "50");
		data.put("followers", "10");
		
		// Console output
		Writer out = new StringWriter();
		template.process(data, out);
		return out.toString();
		}catch(Exception e){
			return "Well you are in your own zone lollz";
		}
	}
}
