import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fanho.media.services.daemon.MovieInfoRetriever;




public class MyTester {

	/**
	 * @param args
	 */
	
	private static String ENDPOINT ="https://www.iping.com/services/iping.asp";
	public static void main(String[] args) throws MalformedURLException, IOException {
	MovieInfoRetrieverService mi = new MovieInfoRetrieverService();
	
	mi.init();
	
	
	}

	/*private void printClassName() {
	
		System.out.println(this.getClass().getM).getName());
		
	}*/

	public void myMethod(){
		CsvTest csvTest = null;
		csvTest = getCSVTest();
		System.out.println("Hey"+csvTest);
	}

	private CsvTest getCSVTest() {
		// TODO Auto-generated method stub
		return new CsvTest();
	}
	
}
