package simpleapiclient;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.util.ServiceException;

public class SimpleClient {

	public static void main(String[] args) {
		
		System.out.println("hello world");
		try {
			getAllGmailContacts("faizalsheriffk@gmail.com","zaheer$$9");
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static  List<ContactDTO> getAllGmailContacts(String gmailId,String password)
			throws ServiceException, IOException {
			    
		//	 InstrumentDTO contactDTO;
			 String name;
			 List<ContactDTO> myGmailContactList= new ArrayList<ContactDTO>();
			 
			 ContactsService myService = new ContactsService("My Honks"); 
			 myService.setUserCredentials(gmailId, password);


			    URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/"+gmailId+"/full");


			   
			    ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);  
			   
			    for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			       ContactEntry entry = resultFeed.getEntries().get(i);
			       System.out.println("\t" + entry.getTitle().getPlainText());
			       name=entry.getTitle().getPlainText();
			    
			       for (Email email : entry.getEmailAddresses()) {  
			    
			      System.out.println("Friend Name:"+name+" and emailAddress:"+email.getAddress());


			         /*if(name != null && name.trim().length()>0){
			           contactDTO=new InstrumentDTO(name, email.getAddress());
			         }else{
			           contactDTO=new InstrumentDTO(email.getAddress(), email.getAddress());
			         }
			         myGmailContactList.add(contactDTO);
			       }
			       LogManager.info(this, resultFeed.getTitle().getPlainText()+" list size (Gmail contact list):"+myGmailContactList.size());*/  
			   }
			    //return myGmailContactList;
			}
				return myGmailContactList;


}


}




class ContactDTO {


 private String key;
 private String value;
 
 public ContactDTO() {
  
 }
 public ContactDTO(String key, String value) {
  super();
  this.key = key;
  this.value = value;
 }
 public String getKey() {
  return key;
 }
 public void setKey(String key) {
  this.key = key;
 }
 public String getValue() {
  return value;
 }
 public void setValue(String value) {
  this.value = value;
 }
 
 public String toString() {
  StringBuilder builder = new StringBuilder();
  builder.append("ContactDTO [key=").append(key).append(", value=")
    .append(value).append("]");
  return builder.toString();
 }
 
 
}

