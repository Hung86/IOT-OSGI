package com.greenkoncepts.gateway.api.bridge;

import java.util.List;
import java.util.Map;

import com.greenkoncepts.gateway.api.adapter.Adapter;
import com.greenkoncepts.gateway.api.task.ITaskExecute;

public interface BridgeMaster {

	public Map<String, Long> getBridgeStatus();
	
	public Map<String, String> getGatewayStatus();
	public Map<String, String> getCurrentBridgeSettings();
	public Map<String, String> getBridgeSettingsBy(String mode);
	public int getSendingInterval();
	public boolean updateBridgeSettings(Map<String, String> settings);
	public boolean updateGatewayStatus(Long totalReadData,Long totalSentData);
	public List<Adapter> getAdapterServices() ;
	public ITaskExecute getTaskExecute();
	
	
	
	/**
	 * Get Bridge name
	 * @return bridge name
	 */
	public String getBridgeName();
	
	public void clearBufferStorage(String location);
	
	public long getUsableStorage(String location);
	
	public void adapterSendDeviceState(String adapter, String data);
		
}
