package com.ktc.utils;

/**
 * transform 16 number into HEX string
 * 
 * @author zengjf
 * 
 */
public class KtcHexUtil {

	private static StringBuffer mStringBuffer;
	/**
	 * bytes to hex string
	 * @param byte[] b byte array
	 * @return String split Bytes with " "
	 */
	public static String byte2HexStr(byte[] b,int i) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < i; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);

		}
		return sb.toString().toUpperCase();
	}
	public static String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
		}
		return sb.toString().toUpperCase();
	}
	
	public static final String bytesToHexString(byte[] bArray) {
		  StringBuffer sb = new StringBuffer(bArray.length);
		  String sTemp;
		  for (int i = 0; i < bArray.length; i++) {
		   sTemp = Integer.toHexString(0xFF & bArray[i]);
		   if (sTemp.length() < 2)
		    sb.append(0);
		   sb.append(sTemp.toUpperCase());
		  }
		  return sb.toString();
		 }

	/**
	 * 16 string array to bytes array
	 * @param hexString
	 * @return byte[]
	 */
	public static byte[] hexStr2ByteArray(String hexString) {
		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}


	/**   
	 * string to hex str
	 * @param String source ascii str
	 * @return String divide every Byte, for ex:[61 6C 6B]
	 */      
	public static String str2HexStr(String str){      
	  
	    char[] chars = "0123456789ABCDEF".toCharArray();      
	    StringBuilder sb = new StringBuilder("");    
	    byte[] bs = str.getBytes();      
	    int bit;      
	        
	    for (int i = 0; i < bs.length; i++)    
	    {      
	        bit = (bs[i] & 0x0f0) >> 4;      
	        sb.append(chars[bit]);      
	        bit = bs[i] & 0x0f;      
	        sb.append(chars[bit]);    
	        
	    }      
	    return sb.toString().trim();      
	} 
	
    /*public static String string2HexString(String strPart) {   
         StringBuffer hexString = new StringBuffer();   
         for (int i = 0; i < strPart.length(); i++) {   
             int ch = (int) strPart.charAt(i);   
             String strHex = Integer.toHexString(ch);   
             hexString.append(strHex);   
         }   
        return hexString.toString();   
     }*/

    /**
     * 16进制转换成为string类型字符串
     * @param s
     * @return
     */
    public static String hexStrToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

	/**
	 * change IP to byte[]
	 * 
	 * @param ipAddr
	 * @return byte[]
	 */
	public static byte[] ipToBytesByReg(String ipAddr) { 
		byte[] ret = new byte[4];
		try {
			String[] ipArr = ipAddr.split("\\.");
			ret[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
			ret[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
			ret[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
			ret[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);
			return ret;
		} catch (Exception e) {
			throw new IllegalArgumentException(ipAddr + " is invalid IP");
		}
	}
	
	/**
	 * TODO 按指定len按0补充后缀
	 * @param int len
	 * @return String
	 */
	public static String getExtraEnding(int len){
		if(mStringBuffer == null){
			mStringBuffer = new StringBuffer();
		}else{
			mStringBuffer.delete(0, mStringBuffer.length());
		}
		for(int i = 0 ; i < len ; i++){
			mStringBuffer.append("0");
		}
		return mStringBuffer.toString();
	}
	
	/**
     * string to ASCII code, for example "BENQ"->"42454E51" 
     * @param bin String
     * @return 
     */
    public static String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
}
