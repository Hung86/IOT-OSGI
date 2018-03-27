package com.serotonin.bacnet4j.gk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.serotonin.bacnet4j.Network;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.constructed.ServicesSupported;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.Segmentation;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class GKRemoteDevice implements Serializable {
	private static final long serialVersionUID = -8440219041728889750L;
	private final int instanceNumber;
	private final Address address;
	private final Network network;
	private int maxAPDULengthAccepted;
	private Segmentation segmentationSupported;
	private int vendorId;
	private String name;
	private UnsignedInteger protocolVersion;
	private UnsignedInteger protocolRevision;
	private ServicesSupported servicesSupported;
	SequenceOf<ObjectIdentifier> objectIDList;
	//private final Map<ObjectIdentifier, BACnetObject> objects = new HashMap<ObjectIdentifier, BACnetObject>();
	private final List<GKRemoteObject> objects = new CopyOnWriteArrayList<GKRemoteObject>();
	//private GKRemoteObject configuration;
	private Object userData;
	
	//flags
	private boolean isConnect = false;
	private int errorTimes = 0;
	private boolean isQueryDone = false;
	private boolean isQuering = false;
	
	long querytime = -1;

	// public RemoteDevice(int instanceNumber, Address address) {
	// this(instanceNumber, address, null);
	// }
	
	/**
	 * 
	 * @param instanceNumber
	 * @param address
	 * @param network
	 */
	public GKRemoteDevice(int instanceNumber, Address address, Network network) {
	    this.instanceNumber = instanceNumber;
	    this.address = address;
	    this.network = network;
	    isConnect = false;
	    errorTimes = 0;
	    isQueryDone = false;
	    isQuering = false;
	}

	public ObjectIdentifier getObjectIdentifier() {
	    return new ObjectIdentifier(ObjectType.device, instanceNumber);
	}

	@Override
	public String toString() {
	    return "RemoteDevice(instanceNumber=" + instanceNumber + ", address=" + address + ", network=" + network + ")";
	}

	public String toExtendedString() {
	    return "RemoteDevice(instanceNumber=" + instanceNumber + ", address=" + address + ", network=" + network
	            + ", maxAPDULengthAccepted=" + maxAPDULengthAccepted + ", segmentationSupported="
	            + segmentationSupported + ", vendorId=" + vendorId + ", name=" + name + ", servicesSupported="
	            + servicesSupported + ", objects=" + objects + ")";
	}

//	public void setObject(BACnetObject o) {
//	    objects.put(o.getId(), o);
//	}
//
//	public BACnetObject getObject(ObjectIdentifier oid) {
//	    return objects.get(oid);
//	}
//
//	public List<BACnetObject> getObjects() {
//	    return new ArrayList<BACnetObject>(objects.values());
//	}
//
//	public void clearObjects() {
//	    objects.clear();
//	}

    //
    // /
    // / remote object management
    // /
    //
    public GKRemoteObject getObjectRequired(ObjectIdentifier id) throws BACnetServiceException {
        GKRemoteObject o = getObject(id);
        if (o == null)
            throw new BACnetServiceException(ErrorClass.object, ErrorCode.unknownObject);
        return o;
    }

    public List<GKRemoteObject> getObjects() {
        return objects;
    }

    public GKRemoteObject getObject(ObjectIdentifier id) {
//        if (id.getObjectType().intValue() == ObjectType.device.intValue()) {
//            // Check if we need to look into the local device.
//            if (id.getInstanceNumber() == configuration.getInstanceId())
//                return configuration;
//        }

        for (GKRemoteObject obj : objects) {
            if (obj.getId().equals(id))
                return obj;
        }
        return null;
    }

    public GKRemoteObject getObject(String name) {
        // Check if we need to look into the local device.
//        if (name.equals(configuration.getObjectName()))
//            return configuration;

        for (GKRemoteObject obj : objects) {
            if (name.equals(obj.getObjectName()))
                return obj;
        }
        return null;
    }

    public void addObject(GKRemoteObject obj) throws BACnetServiceException {
        if (getObject(obj.getId()) != null)
            throw new BACnetServiceException(ErrorClass.object, ErrorCode.objectIdentifierAlreadyExists);
        obj.validate();
        objects.add(obj);

        // Create a reference in the device's object list for the new object.
//        getObjectList().add(obj.getId());
    }

    public ObjectIdentifier getNextInstanceObjectIdentifier(ObjectType objectType) {
        // Make a list of existing ids.
        List<Integer> ids = new ArrayList<Integer>();
        int type = objectType.intValue();
        ObjectIdentifier id;
        for (GKRemoteObject obj : objects) {
            id = obj.getId();
            if (id.getObjectType().intValue() == type)
                ids.add(id.getInstanceNumber());
        }

        // Sort the list.
        Collections.sort(ids);

        // Find the first hole in the list.
        int i = 0;
        for (; i < ids.size(); i++) {
            if (ids.get(i) != i)
                break;
        }
        return new ObjectIdentifier(objectType, i);
    }

    public void setObject(GKRemoteObject obj){
    	int index = objects.indexOf(obj);
    	if(index != -1){
    		//Modify the Object
    		objects.set(index, obj);
    	}else{
    		//Add new Object
    		objects.add(obj);
    	}
    }
    
    public void removeObject(ObjectIdentifier id) throws BACnetServiceException {
        GKRemoteObject obj = getObject(id);
        if (obj != null)
        	objects.remove(obj);
        else
            throw new BACnetServiceException(ErrorClass.object, ErrorCode.unknownObject);

        // Remove the reference in the device's object list for this id.
//        getObjectList().remove(id);
    }

//    @SuppressWarnings("unchecked")
//    private SequenceOf<ObjectIdentifier> getObjectList() {
//        try {
//            return (SequenceOf<ObjectIdentifier>) configuration.getProperty(PropertyIdentifier.objectList);
//        }
//        catch (BACnetServiceException e) {
//            // Should never happen, so just wrap in a RuntimeException
//            throw new RuntimeException(e);
//        }
//    }
	
    
    //
    // /
    // / remote device management
    // /
    //
	public Address getAddress() {
	    return address;
	}

	public Network getNetwork() {
	    return network;
	}

	public int getMaxAPDULengthAccepted() {
	    return maxAPDULengthAccepted;
	}

	public void setMaxAPDULengthAccepted(int maxAPDULengthAccepted) {
	    this.maxAPDULengthAccepted = maxAPDULengthAccepted;
	}

	public Segmentation getSegmentationSupported() {
	    return segmentationSupported;
	}

	public void setSegmentationSupported(Segmentation segmentationSupported) {
	    this.segmentationSupported = segmentationSupported;
	}

	public int getVendorId() {
	    return vendorId;
	}

	public void setVendorId(int vendorId) {
	    this.vendorId = vendorId;
	}

	public int getInstanceNumber() {
	    return instanceNumber;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public UnsignedInteger getProtocolVersion() {
	    return protocolVersion;
	}

	public void setProtocolVersion(UnsignedInteger protocolVersion) {
	    this.protocolVersion = protocolVersion;
	}

	public UnsignedInteger getProtocolRevision() {
	    return protocolRevision;
	}

	public void setProtocolRevision(UnsignedInteger protocolRevision) {
	    this.protocolRevision = protocolRevision;
	}

	public ServicesSupported getServicesSupported() {
	    return servicesSupported;
	}

	public void setServicesSupported(ServicesSupported servicesSupported) {
	    this.servicesSupported = servicesSupported;
	}

	public Object getUserData() {
	    return userData;
	}

	public void setUserData(Object userData) {
	    this.userData = userData;
	}
	
	public SequenceOf<ObjectIdentifier> getObjectList() {
        return objectIDList;
    }
	
	public void setObjectList(SequenceOf<ObjectIdentifier> objectIDList){
		this.objectIDList = objectIDList;
	}
	
	public boolean getConnect(){
		return isConnect;
	}
	
	public void setConnect(boolean isConnect){
		this.isConnect = isConnect;
	}
	
	public void increaseError(){
		errorTimes++;
	}
	
	public void clearError(){
		errorTimes = 0;
	}
	
	public boolean isQueryDone()
	{
		return isQueryDone;
		
	}
	
	public void setQueryDone(boolean isQueryDone)
	{
		this.isQueryDone = isQueryDone;
		
	}
	
	public boolean isQuering()
	{
		return isQuering;
		
	}
	
	public void setQuering(boolean isQuering)
	{
		this.isQuering = isQuering;
		
	}

	public long getQueryTime(){
		return querytime;
	}
	
	public void setQueryTime(long time){
		querytime = time;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((address == null) ? 0 : address.hashCode());
	    result = prime * result + instanceNumber;
	    result = prime * result + ((network == null) ? 0 : network.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (getClass() != obj.getClass())
	        return false;
	    final GKRemoteDevice other = (GKRemoteDevice) obj;
	    if (address == null) {
	        if (other.address != null)
	            return false;
	    }
	    else if (!address.equals(other.address))
	        return false;
	    if (instanceNumber != other.instanceNumber)
	        return false;
	    if (network == null) {
	        if (other.network != null)
	            return false;
	    }
	    else if (!network.equals(other.network))
	        return false;
	    return true;
	}
}
