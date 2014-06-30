package com.tochange.yang.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class Encrypt
{
    private final static String HEX = "0123456789ABCDEF";

    public static String aesEncrypt(String seed, String cleartext)
            throws Exception
    {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String aesDecrypt(String seed, String encrypted)
            throws Exception
    {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);

        return new String(result);
    }

    public static String customizeEncrypt(String strSrc, String encName)
    {
        MessageDigest md = null;
        String strDes = null;

        byte[] bt = strSrc.getBytes();
        try
        {// MD5,SHA-1,SHA-256,defaultSHA-256

            if (encName == null || encName.equals(""))
            {
                encName = "SHA-256";
            }
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        }
        catch (NoSuchAlgorithmException e)
        {
            return null;
        }
        return strDes;
    }

    public static String Md5(String str)
    {
        if (str != null && !str.equals(""))
        {
            try
            {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                        '9', 'a', 'b', 'c', 'd', 'e', 'f' };
                byte[] md5Byte = md5.digest(str.getBytes("UTF8"));
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < md5Byte.length; i++)
                {
                    sb.append(HEX[(int) (md5Byte[i] & 0xff) / 16]);
                    sb.append(HEX[(int) (md5Byte[i] & 0xff) % 16]);
                }
                str = sb.toString();
            }
            catch (NoSuchAlgorithmException e)
            {
            }
            catch (Exception e)
            {
            }
        }
        return str;
    }

    // *************************qq db SecurityUtile begin
    private static char[] codeKey = { 0, 1, 0, 1 };

    private static int codeKeyLen = 0;

    static Object localObject = null;
    
    public static void testQQDb(Context paramContext){
    	setKey(paramContext);
        SQLiteDatabase db = SQLiteDatabase.openDatabase(
                "/mnt/sdcard/1146870069.db", null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        String sql = "select mCompareSpell,name,richBuffer from Friends where _id > '"
                + 0 + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String name1 = cursor.getString(cursor
                        .getColumnIndex("mCompareSpell"));
                log.e("1:" + xor(name) + " 2:" + xor(name1));
            }
        }
        cursor.close();
        db.close();
    }
    
    public static void setKey(Context paramContext)
    {
        // if ((0 == 0) || (null.length() < codeKey.length)) return;
        try
        {
            localObject = ((TelephonyManager) paramContext
                    .getApplicationContext().getSystemService("phone"))
                    .getDeviceId();
            if ((localObject == null)
                    || (((String) localObject).length() < codeKey.length))
            {
                String str = ((WifiManager) paramContext
                        .getSystemService("wifi")).getConnectionInfo()
                        .getMacAddress();
                localObject = str;
            }
            if ((localObject == null)
                    || (((String) localObject).length() < codeKey.length))
                localObject = "361910168";
            codeKey = ((String) localObject).toCharArray();
            codeKeyLen = codeKey.length;
            return;
        }
        catch (Exception localException)
        {
        }
    }

	private static String xor(String paramString) {
		StringBuffer sb;
		if (paramString == null)
			return null;
		for (;;) {
			char[] arrayOfChar1 = paramString.toCharArray();
			sb = new StringBuffer(arrayOfChar1.length);
			int codeKeyLen = ((String) localObject).length();
			char[] codeKey = ((String) localObject).toCharArray();
			if (codeKeyLen >= arrayOfChar1.length)
				for (int j = 0;; ++j) {
					if (j >= arrayOfChar1.length)
						break;
					sb.append((char) (arrayOfChar1[j] ^ codeKey[j]));
				}
			else
				for (int i = 0; i < arrayOfChar1.length; ++i) {
					sb.append((char) (arrayOfChar1[i] ^ codeKey[(i % codeKeyLen)]));
				}
			label91: if (sb.length() != 0)
				break;
		}
		return sb.toString();
	}

    // **************************** qq db SecurityUtile end

    private static String bytes2Hex(byte[] bts)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bts.length; i++)
        {
            String tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1)
            {
                sb.append('0');
            }
            sb.append(tmp);
        }
        return sb.toString();
    }

    // *******************aes Advanced Encryption Standard Rijndael begin
    private static byte[] getRawKey(byte[] seed) throws Exception
    {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    private static String toHex(String txt)
    {
        return toHex(txt.getBytes());
    }

    private static String fromHex(String hex)
    {
        return new String(toByte(hex));
    }

    private static byte[] toByte(String hexString)
    {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private static String toHex(byte[] buf)
    {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++)
        {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b)
    {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
    // *******************aes Advanced Encryption Standard Rijndael end
}