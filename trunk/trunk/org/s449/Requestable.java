package org.s449;

/**
 * A marker interface marking a class as transmittable over
 *  NPCv2 and NPCv3 non-blocking object stream connections.
 *  Only the primary class being transmitted need implement
 *  this interface; the classes of fields and methods need
 *  only be serializable.
 */
public interface Requestable extends java.io.Serializable {
}