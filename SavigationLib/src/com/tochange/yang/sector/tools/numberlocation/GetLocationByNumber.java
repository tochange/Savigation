package com.tochange.yang.sector.tools.numberlocation;

import java.io.File;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.text.TextUtils;

import com.tochange.yang.lib.log;
import com.tochange.yang.sector.tools.AppUtils;

public class GetLocationByNumber {

    /**
     * 主方法用来得到归属地信息 
    */
	public static String getCallerInfo(String incomingNumber,Context context){
    	String result = "";
    	String searchNum = getSearchNum(incomingNumber);
    	if(TextUtils.isEmpty(searchNum) == false){
    		result = getLocation(searchNum,context);
			log.d( "location:" + result);
		}
    	return result;
	  }
	/**
	 * 
	 *对号码进行处理，因为文件里存的号码只有7位或者只有区号，而不是整个号码 
	 */
    public static String getSearchNum(String PhoneNumber) {
		String searchNum = "";
		//0755-82720818,remove "-"
	    searchNum = PhoneNumber.replace("-", "");
		searchNum = searchNum.replace(" ", "");
		if(searchNum.startsWith("+86")){
			searchNum = searchNum.replace("+86", "");
		}
		//if(PhoneNumber.matches("^(0[1-2])\\d{1}\\-?\\d{0,8}$|^(0[3-9])\\d{2}\\-?\\d{0,8}$|^(13|15|14|18)\\d{5,9}$")){
		if (searchNum.matches("^[0-9]*[1-9][0-9]*$")) {
			int len = searchNum.length();
			// telephone number start with '0' like 075582720818
			// mobile phone number not start with '0'
			if (searchNum.startsWith("0")) {
				if (len == 12 || len == 11 || len == 10) {
					//中国区号中小于29就是特别行政区的区号，不可能出现290的
					//{"10","20","21","22","23","24","25","27","28","29"}
					if (Integer.parseInt(searchNum.substring(1, 3)) <= 29) {
						searchNum = searchNum.substring(1, 3);
					} else {
						searchNum = searchNum.substring(1, 4);
					}
				}
			} else {
				// the database only store number like '1367002' have 7 digit
				if (len == 11) {
					searchNum = searchNum.substring(0, 7);
				} else {
				}
			}
		} else {
			searchNum = "";
		}
		log.d( "after num:" + searchNum);
		return searchNum;
	}
    /**
     * 从c++层获得归属地信息
     * */
    public static String getLocation(String number,Context context){
    	byte[] result = NativeFunction.getLocationFromJni(context.getFilesDir() + File.separator + AppUtils.PHONELOCATION_FILENAME, number);
		String location = "";
		try {
			location = new String(result, "GB2312");
		} catch (UnsupportedEncodingException e) {
			log.e(""+e.toString());
		}
    	return location;
    }
}
