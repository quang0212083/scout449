package org.s449;

/**
 * A class holding the type and name of a UDF.
 * 
 * @author Stephen Carlson
 */
public class UDF implements java.io.Serializable, Comparable<UDF> {
	private static final long serialVersionUID = 4321978342178349874L;

	/**
	 * A rate 1-10 category.
	 */
	public static final int RATE10 = 0;
	/**
	 * A generalized integer.
	 */
	public static final int INT = 1;
	/**
	 * A boolean. 0=false, anything else=true
	 */
	public static final int BOOL = 2;

	// TODO The types are not yet implemented.

	/**
	 * The field name.
	 */
	private String name;
	/**
	 * The field type.
	 */
	private int type;

	/**
	 * Creates a new user-defined field.
	 * 
	 * @param name the UDF name
	 * @param type the type.
	 */
	public UDF(String name, int type) {
		super();
		this.name = name;
		this.type = type;
	}
	/**
	 * Gets the name of this UDF.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Gets the type of this UDF.
	 *
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	public int hashCode() {
		return name.hashCode() + type;
	}
	public int compareTo(UDF other) {
		return name.compareTo(other.getName());
	}
	public String toString() {
		return name;
	}
}