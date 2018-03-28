package com.ktc.control.wlan;

public class UdpMsg {
	public static String IP;// 4
	public static String gateway_ip;// 4
	public static String MAC;// 6
	public static String model_name = "5345393030312d542d33314600000000";// 16
	public static String hostname = "64656661756c74000000000000000000";// 16
	public static String subnet_mac;// 4
	public static String DNS1; 
	public static String DNS2;

	public static String getIp() {
		return IP;
	}

	public static void setIp(String ip) {
		UdpMsg.IP = ip;
	}

	public static void setMAC(String mAC) {
		UdpMsg.MAC = mAC;
	}

	public static String getMAC() {
		return MAC;
	}

	public static String getGateway_ip() {
		return gateway_ip;
	}

	public static void setGateway_ip(String gateway_ip) {
		UdpMsg.gateway_ip = gateway_ip;
	}

	public static String getSubnet_mac() {
		return subnet_mac;
	}

	public static void setSubnet_mac(String subnet_mac) {
		UdpMsg.subnet_mac = subnet_mac;
	}
	
	public static String getDNS1() {
		return DNS1;
	}
	
	public static void setDNS1(String dns1) {
		UdpMsg.DNS1 = dns1;
	}
	
	public static String getDNS2() {
		return DNS2;
	}
	
	public static void setDNS2(String dns2) {
		UdpMsg.DNS2 = dns2;
	}

	public static String getMSG() {
		// UDP datagram length 300Byte
		String msg = "0101060092da" + "000000000000" + IP + "000000000000"
				+ "0000" + gateway_ip + MAC + "00000000" + "000000000000"
				+ model_name + "5345393030312d542d33"
				+ "31460000000000000000000000000000"
				+ "00000000504834363000000000000000"
				+ "0000000001011f0250726f5765625372"
				+ "7620766572312e303133000000000000"
				+ "53000000000000000000000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000" + "000000000000"
				+ subnet_mac + "000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000"
				+ "00000000000000000000000000000000" + "000000000000";
		return msg;
	}

}
