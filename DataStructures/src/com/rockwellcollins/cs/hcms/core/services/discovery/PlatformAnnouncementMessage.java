
package com.rockwellcollins.cs.hcms.core.services.discovery;

import java.util.zip.CRC32;

/**
 * This class encapsulates the platform announcement message, its message attributes and converts
 * the attributes to a byte array that can be transmitted in a network packet. 
 * 
 * @author gdneshei
 *
 */
public class PlatformAnnouncementMessage {

	public final static int MESSAGE_TYPE_PLATFORM_ID = 1;
	public final static int MESSAGE_VERSION = 1;
	public final static int PLATFORM_ID_VENUE = 3;
	
	private int msgType 		= PlatformAnnouncementMessage.MESSAGE_TYPE_PLATFORM_ID;
	private int msgVersion		= PlatformAnnouncementMessage.MESSAGE_VERSION;
	private int msgPlatform	= PlatformAnnouncementMessage.PLATFORM_ID_VENUE;
	private String sourceName	= "PlatformAnnouncer";
	
	private byte[] byteArray;	/* data of the UDP packet */  
	
	/**
	 * Default constructor
	 * 
	 */
	PlatformAnnouncementMessage( ){

	}
	
	
	/**
	 * Set the byte array given a data packet.  This is for parsing packets. 
	 * At this time the crc is not checked on packets that are received. 
	 *  
	 * @param name
	 */
	public void setData( byte[] data ){
		byteArray = data.clone();
		msgType  = 0;

		int i;

		/* message type */
		for( i=0; i < 4; i++ ){
			msgType += ( byteArray[ i + 4] << (8*(3-i)));
		}
		/* message version */
		for( i=0; i < 4; i++ ){
			msgVersion += ( byteArray[ i + 8] << (8*(3-i)));
		}
		/* platform id */
		for( i=0; i < 4; i++ ){
			msgPlatform += ( byteArray[ i + 12] << (8*(3-i)));
		}
		/* LRU name */
		for( i=0; i < 20  && ( byteArray[i] != 0); i++ ){
			sourceName += ( byteArray[ i + 16] );
		}
		
		/* no crc check at this time */
		
	}

	/**
	 * Set the LRU source name to be put in the platform announcement message.
	 * @param name
	 */
	public void setSourceName( String name ){
		sourceName = name;
	}
	
	/**
	 *  Return the platform announcement message type.
	 * @return integer representing the message type.
	 */
	public int getMessageType(){
		return msgType;
	}
	
	/**
	 *  Return the platform announcement message in a byte array.
	 * @return byte array containing the platform announcement message.
	 */
	public byte[] toByteArray(){
		return byteArray;
	}

	/**
	 * Return the byte value from a given value at the given offset.  
	 * @param value  Value containing the byte to return.
	 * @param offset  Offset used to locate byte in value. 
	 * @return a byte. 
	 */
	public byte byteAt( int value, int offset ){
		return(  (byte) (( value >>> (8*offset) ) & 0xFF ) );
	}

	/**
	 * Return the byte value from a given value at the given offset.  
	 * @param value  Value containing the byte to return.
	 * @param offset  Offset used to locate byte in value. 
	 * @return a byte. 
	 */
	public byte byteAt( long value, int offset ){
		return(  (byte) (( value >>> (8*offset) ) & 0xFF ) );
	}
	
	/**
	 * Return the byte value from a given value at the given offset.  
	 * @param value  Value containing the byte to return.
	 * @param offset  Offset used to locate byte in value. 
	 * @return a byte. 
	 */
	public byte byteAt( String value, int offset ){
		return(  (byte)  value.charAt(offset) );
	}
	
	/**
	 * Update the byteArray with the platform announcement message attributes.
	 * Numbers are stored in big-endian order (network order).
	 * CRC must be generated for packets that are sent. 
	 * 
	 */
	public void update(){
		
		int dataLength  = 0;
		long crc32 = 0; 
		int b = 0;
		int i = 0;

		dataLength = (4 * 4 ) + 20;
		byteArray = new byte[dataLength + 4];

		/* data length */
		for ( i = 4; i > 0; i--){
			byteArray[b++]	= byteAt( dataLength, i-1 );   
		}	
		
		/* message type */
		for ( i = 4; i > 0; i--){
			byteArray[b++]	= byteAt( msgType, i-1);   
		}
		
		/* message version */
		for ( i = 4; i > 0; i--){
			byteArray[b++]	= byteAt( msgVersion, i-1);
		}
		
		/* platform ID */
		for ( i = 4; i > 0; i--){
			byteArray[b++]	= byteAt( msgPlatform, i-1);
		}
		
		/* sourceName, null padded to 20 chars */
		for ( i=0; i < 20; i++){ 
			if ( i < sourceName.length() && i < 19 ){
				byteArray[b++] = byteAt( sourceName, i );
			}
			/* pad with nulls */
			else{
				byteArray[b++]	= (byte) 0;
			}
		}
		
		/* calculate crc on current data */
		CRC32 crc32Object = new CRC32();
		
		crc32Object.update( byteArray, 0, b );
		crc32 = crc32Object.getValue();
		
		/* crc32 */
		for ( i = 4; i > 0; i--){
			byteArray[b++]	= byteAt( crc32, i-1);   
		}
	}
}
