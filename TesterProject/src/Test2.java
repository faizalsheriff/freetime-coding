import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class Test2 {

	class ProviderInfo{
		String address1;
		String address2;
		String city;
		String crossStreet;
		String description;
		double distance;
		double lat;
		double lon;
		String name;
		String phone;
		boolean precise;
		String state;
		String url;
		String urlCaption;
		String zip;
	}
	class Details {
		boolean success;
		ProviderInfo[] providers;
		Hashtable errors;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Gson son = new Gson();
		String testStr = "{\"success\":true," +
				"\"providers\":[" +
				"{\"address1\":\"701 Park Ave\"," +
				"\"address2\":null," +
				"\"city\":\"Minneapolis\"," +
				"\"crossStreet\":null," +
				"\"description\":\"test location62641 description of services/prices/hours\"," +
				"\"distance\":0.498675100786243," +
				"\"lat\":44.97279," +
				"\"lon\":-93.262727," +
				"\"name\":\"testlocation62641\"," +
				"\"phone\":\"6125551212\"," +
				"\"precise\":true," +
				"\"state\":\"MN\"," +
				"\"url\":\"http://www.testlocation62641.com?id=62641\"," +
				"\"urlCaption\":\"www.testlocation62641.com\"," +
				"\"zip\":\"554151829\"}," +
				"{" +
				"\"address1\":\"815 Nicollet Mall\"," +
				"\"address2\":null," +
				"\"city\":\"Minneapolis\"," +
				"\"crossStreet\":\"Nicollet Mall & 8th Street\"," +
				"\"description\":\"test location28398 description of services/prices/hours\"," +
				"\"distance\":0.541297938576095," +
				"\"lat\":44.975752," +
				"\"lon\":-93.273174," +
				"\"name\":\"testlocation28398\"," +
				"\"phone\":\"6125551212\"," +
				"\"precise\":true," +
				"\"state\":\"MN\"," +
				"\"url\":\"http://www.testlocation28398.com?id=28398\"," +
				"\"urlCaption\":\"www.testlocation28398.com\"," +
				"\"zip\":\"554022504\"}" +
				"],\"errors\":[]}";
		try {
			Details det = son.fromJson(testStr, Details.class);
			if(det.providers != null) {
				for (ProviderInfo pInfo : det.providers) {
					System.out.println("Address 1 " + pInfo.address1);
					System.out.println("Zip " + pInfo.zip);
					System.out.println("urlCaption " + pInfo.urlCaption);
				}
			}
		} catch(JsonSyntaxException ex) {
			System.out.println("Exception.. " + ex.getMessage());
		}

	}

}
