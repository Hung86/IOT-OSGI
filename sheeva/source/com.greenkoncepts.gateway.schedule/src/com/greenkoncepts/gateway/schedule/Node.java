package com.greenkoncepts.gateway.schedule;

public class Node {
	private String category;
	private String deviceId;
	private String channelId;
	private String subchannelId;
	
	public Node() {
		this.deviceId = "";
		this.channelId = "";
		this.subchannelId = "";
		this.category = "";
	}
	public Node(String deviceId, String channelId, String subchannelId, String category) {
		this.deviceId = deviceId;
		this.channelId = channelId;
		this.subchannelId = subchannelId;
		this.category = category;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getSubchannelId() {
		return subchannelId;
	}
	public void setSubchannelId(String subchannelId) {
		this.subchannelId = subchannelId;
	}
	
	public String toString() {
		return "Node : category = " +  category + ", device id = " + deviceId + ", channeld id = " + channelId + ", sub channel id = " + subchannelId;
	}
	
}
