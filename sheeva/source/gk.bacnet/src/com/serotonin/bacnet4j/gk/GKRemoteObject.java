package com.serotonin.bacnet4j.gk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.bacnet4j.exception.BACnetRuntimeException;
import com.serotonin.bacnet4j.exception.BACnetServiceException;
import com.serotonin.bacnet4j.obj.ObjectProperties;
import com.serotonin.bacnet4j.obj.PropertyTypeDefinition;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.BaseType;
import com.serotonin.bacnet4j.type.constructed.PriorityArray;
import com.serotonin.bacnet4j.type.constructed.PriorityValue;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.SequenceOf;
import com.serotonin.bacnet4j.type.enumerated.BinaryPV;
import com.serotonin.bacnet4j.type.enumerated.ErrorClass;
import com.serotonin.bacnet4j.type.enumerated.ErrorCode;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Date;
import com.serotonin.bacnet4j.type.primitive.Null;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.Time;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

public class GKRemoteObject implements Serializable {
    private static final long serialVersionUID = -569892306207282576L;

    private final GKRemoteDevice remoteDevice;
    private final ObjectIdentifier id;
    private final Map<PropertyIdentifier, Encodable> properties = new HashMap<PropertyIdentifier, Encodable>();
//    private final List<ObjectCovSubscription> covSubscriptions = new ArrayList<ObjectCovSubscription>();

    public GKRemoteObject(GKRemoteDevice remoteDevice, ObjectIdentifier id) {
        this.remoteDevice = remoteDevice;

        if (id == null)
            throw new IllegalArgumentException("object id cannot be null");
        this.id = id;

        try {
            setProperty(PropertyIdentifier.objectName, new CharacterString(id.toString()));

            // Set any default values.
            List<PropertyTypeDefinition> defs = ObjectProperties.getPropertyTypeDefinitions(id.getObjectType());
            for (PropertyTypeDefinition def : defs) {
                if (def.getDefaultValue() != null)
                    setProperty(def.getPropertyIdentifier(), def.getDefaultValue());
            }
        }
        catch (BACnetServiceException e) {
            // Should never happen, but wrap in an unchecked just in case.
            throw new BACnetRuntimeException(e);
        }
    }

    public ObjectIdentifier getId() {
        return id;
    }

    public int getInstanceId() {
        return id.getInstanceNumber();
    }

    public String getObjectName() {
        CharacterString name = getRawObjectName();
        if (name == null)
            return null;
        return name.getValue();
    }

    public CharacterString getRawObjectName() {
        return (CharacterString) properties.get(PropertyIdentifier.objectName);
    }

    public String getDescription() {
        CharacterString name = (CharacterString) properties.get(PropertyIdentifier.description);
        if (name == null)
            return null;
        return name.getValue();
    }
    
    //
    // /
    // / Get property
    // /
    //
    public Encodable getProperty(PropertyIdentifier pid) throws BACnetServiceException {
        if (pid.intValue() == PropertyIdentifier.objectIdentifier.intValue())
            return id;
        if (pid.intValue() == PropertyIdentifier.objectType.intValue())
            return id.getObjectType();

        // Check that the requested property is valid for the object. This will throw an exception if the
        // property doesn't belong.
        ObjectProperties.getPropertyTypeDefinitionRequired(id.getObjectType(), pid);

        // Do some property-specific checking here.
        if (pid.intValue() == PropertyIdentifier.localTime.intValue())
            return new Time();
        if (pid.intValue() == PropertyIdentifier.localDate.intValue())
            return new Date();

        return properties.get(pid);
    }

    public Encodable getPropertyRequired(PropertyIdentifier pid) throws BACnetServiceException {
        Encodable p = getProperty(pid);
        if (p == null)
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.unknownProperty);
        return p;
    }

    public Encodable getProperty(PropertyIdentifier pid, UnsignedInteger propertyArrayIndex)
            throws BACnetServiceException {
        Encodable result = getProperty(pid);
        if (propertyArrayIndex == null)
            return result;

        if (!(result instanceof SequenceOf<?>))
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.propertyIsNotAnArray);

        SequenceOf<?> array = (SequenceOf<?>) result;
        int index = propertyArrayIndex.intValue();
        if (index == 0)
            return new UnsignedInteger(array.getCount());

        if (index > array.getCount())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.invalidArrayIndex);

        return array.get(index);
    }

    public Encodable getPropertyRequired(PropertyIdentifier pid, UnsignedInteger propertyArrayIndex)
            throws BACnetServiceException {
        Encodable p = getProperty(pid, propertyArrayIndex);
        if (p == null)
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.unknownProperty);
        return p;
    }

    //
    // /
    // / Set property
    // /
    //
    public void setProperty(PropertyIdentifier pid, Encodable value) throws BACnetServiceException {
        ObjectProperties.validateValue(id.getObjectType(), pid, value);
        setPropertyImpl(pid, value);

        // If the reinquish default was set, make sure the present value gets updated as necessary.
        if (pid.equals(PropertyIdentifier.relinquishDefault))
            setCommandableImpl((PriorityArray) getProperty(PropertyIdentifier.priorityArray));
    }

    @SuppressWarnings("unchecked")
    public void setProperty(PropertyIdentifier pid, int indexBase1, Encodable value) throws BACnetServiceException {
        ObjectProperties.validateSequenceValue(id.getObjectType(), pid, value);
        SequenceOf<Encodable> list = (SequenceOf<Encodable>) properties.get(pid);
        if (list == null) {
            list = new SequenceOf<Encodable>();
            setPropertyImpl(pid, list);
        }
        list.set(indexBase1, value);
    }

    public void setProperty(PropertyValue value) throws BACnetServiceException {
        PropertyIdentifier pid = value.getPropertyIdentifier();

        if (pid.intValue() == PropertyIdentifier.objectIdentifier.intValue())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.writeAccessDenied);
        if (pid.intValue() == PropertyIdentifier.objectType.intValue())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.writeAccessDenied);
        if (pid.intValue() == PropertyIdentifier.priorityArray.intValue())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.writeAccessDenied);
        if (pid.intValue() == PropertyIdentifier.relinquishDefault.intValue())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.writeAccessDenied);

        if (ObjectProperties.isCommandable((ObjectType) getProperty(PropertyIdentifier.objectType), pid))
            setCommandable(value.getValue(), value.getPriority());
        else if (value.getValue() == null) {
            if (value.getPropertyArrayIndex() == null)
                removeProperty(value.getPropertyIdentifier());
            else
                removeProperty(value.getPropertyIdentifier(), value.getPropertyArrayIndex());
        }
        else {
            if (value.getPropertyArrayIndex() != null)
                setProperty(pid, value.getPropertyArrayIndex().intValue(), value.getValue());
            else
                setProperty(pid, value.getValue());
        }
    }

    public void setCommandable(Encodable value, UnsignedInteger priority) throws BACnetServiceException {
        int pri = 16;
        if (priority != null)
            pri = priority.intValue();

        PriorityArray priorityArray = (PriorityArray) getProperty(PropertyIdentifier.priorityArray);
        priorityArray.set(pri, createCommandValue(value));
        setCommandableImpl(priorityArray);
    }

    private void setCommandableImpl(PriorityArray priorityArray) throws BACnetServiceException {
        PriorityValue priorityValue = null;
        for (PriorityValue priv : priorityArray) {
            if (!priv.isNull()) {
                priorityValue = priv;
                break;
            }
        }

        Encodable newValue = getProperty(PropertyIdentifier.relinquishDefault);
        if (priorityValue != null)
            newValue = priorityValue.getValue();

        setPropertyImpl(PropertyIdentifier.presentValue, newValue);
    }

    private void setPropertyImpl(PropertyIdentifier pid, Encodable value) {
        //Encodable oldValue = properties.get(pid);
        properties.put(pid, value);

        //remove COV subscriptions
//        if (!ObjectUtils.isEqual(value, oldValue)) {
//            // Check for subscriptions.
//            if (ObjectCovSubscription.sendCovNotification(id.getObjectType(), pid)) {
//                synchronized (covSubscriptions) {
//                    long now = System.currentTimeMillis();
//                    ObjectCovSubscription sub;
//                    for (int i = covSubscriptions.size() - 1; i >= 0; i--) {
//                        sub = covSubscriptions.get(i);
//                        if (sub.hasExpired(now))
//                            covSubscriptions.remove(i);
//                        else
//                            sendCovNotification(sub, now);
//                    }
//                }
//            }
//        }
    }

    private PriorityValue createCommandValue(Encodable value) throws BACnetServiceException {
        if (value instanceof Null)
            return new PriorityValue((Null) value);

        ObjectType type = (ObjectType) getProperty(PropertyIdentifier.objectType);
        if (type.equals(ObjectType.accessDoor))
            return new PriorityValue((BaseType) value);
        if (type.equals(ObjectType.analogOutput) || type.equals(ObjectType.analogValue))
            return new PriorityValue((Real) value);
        if (type.equals(ObjectType.binaryOutput) || type.equals(ObjectType.binaryValue))
            return new PriorityValue((BinaryPV) value);
        return new PriorityValue((UnsignedInteger) value);
    }
    
    public void validate() throws BACnetServiceException {
        // Ensure that all required properties have values.
        List<PropertyTypeDefinition> defs = ObjectProperties.getRequiredPropertyTypeDefinitions(id.getObjectType());
        for (PropertyTypeDefinition def : defs) {
            if (getProperty(def.getPropertyIdentifier()) == null)
                throw new BACnetServiceException(ErrorClass.property, ErrorCode.other, "Required property not set: "
                        + def.getPropertyIdentifier());
        }
    }

    public void removeProperty(PropertyIdentifier pid) throws BACnetServiceException {
        PropertyTypeDefinition def = ObjectProperties.getPropertyTypeDefinitionRequired(id.getObjectType(), pid);
        if (def.isRequired())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.writeAccessDenied);
        properties.remove(pid);
    }

    public void removeProperty(PropertyIdentifier pid, UnsignedInteger propertyArrayIndex)
            throws BACnetServiceException {
        PropertyTypeDefinition def = ObjectProperties.getPropertyTypeDefinitionRequired(id.getObjectType(), pid);
        if (!def.isSequence())
            throw new BACnetServiceException(ErrorClass.property, ErrorCode.invalidArrayIndex);
        SequenceOf<?> sequence = (SequenceOf<?>) properties.get(pid);
        if (sequence != null)
            sequence.remove(propertyArrayIndex.intValue());
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
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
        final GKRemoteObject other = (GKRemoteObject) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        return true;
    }


}
