package com.tochange.yang.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encrypt
{
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

    public static String Encrypt(String strSrc, String encName)
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

    public static String bytes2Hex(byte[] bts)
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
}