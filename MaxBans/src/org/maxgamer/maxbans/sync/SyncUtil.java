package org.maxgamer.maxbans.sync;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.maxgamer.maxbans.MaxBans;

public class SyncUtil{
	/** The salt to add to all passwords */
	public static final String PASSWORD_SALT = "fuQJ7_q#eF78A&D";
	
	/** The alphabet. A-Z, a-z, 0-9. */
	private static char[] chars;
	private static Random r = new Random();
	
	static{
		chars = "abcdefghijklmnopqrstuvwrxyzABCDEFGHIJKLMNOPQRSTUVWRXYZ0123456789".toCharArray();
	}
	
	public static boolean isDebug(){
		return MaxBans.instance.getConfig().getBoolean("sync.debug");
	}
	
	/**
	 * Returns a random string of A-Z, a-z and 0-9 with the given length.
	 * @param len The length of the required string
	 * @return A string random letters/numbers of the given length.
	 */
	public static String getRandomString(int len){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < len; i++){ sb.append(chars[r.nextInt(chars.length)]); }
		return sb.toString();
	}
	
	/** Converts the given bytes into letters. */
    private static String convertedToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < data.length; i++) { 
            int halfOfByte = (data[i] >>> 4) & 0x0F;
            int twoHalfBytes = 0;
            
            do { 
                if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
                    buf.append( (char) ('0' + halfOfByte) );
                }
                else {
                    buf.append( (char) ('a' + (halfOfByte - 10)) );
                }
                halfOfByte = data[i] & 0x0F;
            } while(twoHalfBytes++ < 1);
        } 
        return buf.toString();
    }
    
    /** Converts the given text into MD5 format - Do not use this for encrypting passwords. Use {@link SyncServer#encrypt(String, String)} instead.*/
    public static String MD5(String text)  throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5 = new byte[64];
        md.update(text.getBytes("ISO-8859-1"), 0, text.length());
        md5 = md.digest();
        return convertedToHex(md5);
    } 
    
    /**
     * Encrypts the given text.  The format is:<br/>
     * <i>String encrypted = MD5(MD5(text) + salt);</i><br/><br/>
     * 
     * In other words, encrypt the text in MD5. Then append the salt.
     * Then, encrypt the result in MD5 again. This result is the final
     * result.
     * @param text The text to encrypt
     * @param salt The salt to add
     * @return The text, encrypted.
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String encrypt(String text, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException{
    	return MD5(MD5(text) + salt);
    }
}