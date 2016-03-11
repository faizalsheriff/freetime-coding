import java.util.Properties;
	import javax.mail.Message;
	import javax.mail.MessagingException;
	import javax.mail.PasswordAuthentication;
	import javax.mail.Session;
	import javax.mail.Transport;
	import javax.mail.internet.InternetAddress;
	import javax.mail.internet.MimeMessage;
	 
public class MailSender {

	 
	

		public static void main(String[] args) {
			Properties props = new Properties();
			props.put("mail.smtp.host", "mail.messagingengine.com");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "465");
	 
			Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("faizalsheriff@fastmail.fm","zaheer$$9");
					}
				});
	 
			try {
	 
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress("root@www.mywinks.org"));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse("faizal.sherriff@gmail.com"));
				message.setSubject("Testing Subject");
				//message.setText(FTLHelloWorld.getMessage());
				message.setContent(FTLHelloWorld.getMessage(), "text/html");
				
				Transport.send(message);
				
				System.out.println("Done");
	 
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
	
}
