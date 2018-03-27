package com.greenkoncepts.gateway.adapter.modbusconverter;

public class DataPoint {
	public int register = 0;
	public int index = 0;
	public int channelIdx = 0;
	public int subchannelIdx = 0;
	public int datapointIdx = 0;
	public double measureRatio = 1;
	public double consumedRatio = 1;
	public String dataType = "";
	public String measureName = "";
	public String measureUnit = "";
	public double prevmMeasureValue = 0;
	public double measureValue = 0;
	public String rawValue = "0";
	public String consumedName = "";
	public String consumedUnit = "";
	public double consumedValue = 0;
	public boolean hasConsumption = false;

	public String toString() {
		return "Node: channel=" + channelIdx + ", subchannel=" + subchannelIdx + ", datapointIdx=" + datapointIdx + ", register=" + register
				+ ", index=" + index + ", measureName=" + measureName + ", measureUnit=" + measureUnit + ", measureRatio=" + measureRatio + ", hasConsumtion=" + hasConsumption
				+ ", consumedName=" + consumedName + ", consumedUnit=" + consumedUnit + ", consumedRatio=" + consumedRatio + ", dataType" + dataType;
	}
}
