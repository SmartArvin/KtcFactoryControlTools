package com.ktc.control.wlan;

public class Constant {

	public static String quest_ip=null;//MDA local IP
	
	public static boolean Reportflag=false;//send UDP response enable

	public static int TCPPort=4660;//TCP port
	
	public static final int Port=55954;//UDP request receive port

	public static final String GETCMD = "67";//get cmd

	public static final Object SETCMD = "73";//set cmd
	
	public static final String[] language_list={
		"en","fr","es","zh","zh","pt","de","nl","pl","ru",
		"cs","da","sv","it","ro","nb","fi","el","tr","ar",
		"ja","th","ko","hu","fa","vi"
    };
	
	public static final String[] country_list={
        "US","FR","US","TW","CN","PT","DE","NL","PL","RU",
        "CZ","DK","SE","IT","RO","NO","FI","GR","TR","EG",
        "JP","TH","KR","HU","IR","VN"
    };

	

}
