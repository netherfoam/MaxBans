package org.maxgamer.maxbans.sync;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import org.maxgamer.maxbans.MaxBans;
import java.util.Random;

public class SyncUtil
{
    public static final String PASSWORD_SALT = "fuQJ7_q#eF78A&D";
    private static char[] chars;
    private static Random r;
    
    static {
        SyncUtil.r = new Random();
        SyncUtil.chars = "abcdefghijklmnopqrstuvwrxyzABCDEFGHIJKLMNOPQRSTUVWRXYZ0123456789".toCharArray();
    }
    
    public static boolean isDebug() {
        return MaxBans.instance.getConfig().getBoolean("sync.debug");
    }
    
    public static String getRandomString(final int len) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            sb.append(SyncUtil.chars[SyncUtil.r.nextInt(SyncUtil.chars.length)]);
        }
        return sb.toString();
    }
    
    private static String convertedToHex(final byte[] data) {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            int halfOfByte = data[i] >>> 4 & 0xF;
            int twoHalfBytes = 0;
            do {
                if (halfOfByte >= 0 && halfOfByte <= 9) {
                    buf.append((char)(48 + halfOfByte));
                }
                else {
                    buf.append((char)(97 + (halfOfByte - 10)));
                }
                halfOfByte = (data[i] & 0xF);
            } while (twoHalfBytes++ < 1);
        }
        return buf.toString();
    }
    
    public static String MD5(final String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md2 = new byte[64];
        md.update(text.getBytes("ISO-8859-1"), 0, text.length());
        md2 = md.digest();
        return convertedToHex(md2);
    }
    
    public static String encrypt(final String text, final String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MD5(String.valueOf(MD5(text)) + salt);
    }
}
