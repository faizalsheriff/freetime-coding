import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class json1 {

//	class accessTokenInfo {
//		String access_token;
//		String token_type;
//		String expires_in;
//		String id_token;
//		String refresh_token;
//	}
	public static void main(String[] args)
	{
		Gson son = new Gson();
		String testStr = "{\"access_token\" : \"ya29.AHES6ZT_n1cSBxGudxgNtn11JIFdGs4p2efY_S8720XUwwq6EA66Vw\"," +
				"  \"token_type\" : \"Bearer\",  " +
				"\"expires_in\" : 3600, " +
				" \"id_token\" : \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjIwYmQyMWNjZDljZDA5OWZkMDJkMzZjNjI1OThmOTY2MzFhOTlhMzMifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwidmVyaWZpZWRfZW1haWwiOiJ0cnVlIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiaWQiOiIxMTM5NzkyNjg5MjcyMzYxNTAxMjEiLCJzdWIiOiIxMTM5NzkyNjg5MjcyMzYxNTAxMjEiLCJjaWQiOiI2NTAyMTQyMjYwMjMtbWpwMWx0MDZ2cTNldWkzZzdrbXN1YXZ2NjA3YnY0YXQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhenAiOiI2NTAyMTQyMjYwMjMtbWpwMWx0MDZ2cTNldWkzZzdrbXN1YXZ2NjA3YnY0YXQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2NTAyMTQyMjYwMjMtbWpwMWx0MDZ2cTNldWkzZzdrbXN1YXZ2NjA3YnY0YXQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6ImZhaXphbC5zaGVycmlmZkBnbWFpbC5jb20iLCJ0b2tlbl9oYXNoIjoiUU03Tm4wb1Q2Y3BDcFZ5Z3htOWpPQSIsImF0X2hhc2giOiJRTTdObjBvVDZjcENwVnlneG05ak9BIiwiaWF0IjoxMzY5MzMyNjI0LCJleHAiOjEzNjkzMzY1MjR9.JOE0aXY_0I7UMbOvE_lesGf7BBSRDRb4ytQYd0LvzXyehOzQlTPV3db5smbul0iNrrqVSynoMlW9N0xH2p0m3wj8ax_o1fESC_epeGCZpb9SokO_nKPnJ7ntrlHeLLHgJfGJURkT4FJ4W-YrLlmpyNJah1yEZL8W9V2uoMbmEW8\"," +
				"\"refresh_token\":\"\"}";
		
		try
		{
			System.out.println("Starting to parse json");
			AccessTokenInfo accesstoken_info = son.fromJson(testStr, AccessTokenInfo.class);
			System.out.println(accesstoken_info.getAccess_token());
			System.out.println(accesstoken_info.getToken_type());
			System.out.println(accesstoken_info.getExpires_in());
			System.out.println(accesstoken_info.getId_token());
			System.out.println(accesstoken_info.getRefresh_token());
			System.out.println("Done......");
		}catch(JsonSyntaxException ex) {
			System.out.println("Exception.. " + ex.getMessage());
		}
	}
}
