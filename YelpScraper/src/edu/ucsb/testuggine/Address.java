package edu.ucsb.testuggine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Yelp follows this schema for postal addresses: http://schema.org/PostalAddress */

public class Address {
	
	public String number;
	public String street;
	public String zip;
	public String city;
	public String region; // State in the US. Did not find it elsewhere, tried a few restaurants in Italy.
	/**
	 * @param number
	 * @param street
	 * @param zip
	 * @param city
	 * @param country
	 */
	public Address(String number, String street, String zip, String city,
			String region) {
		this.number = number;
		this.street = street;
		this.zip = zip;
		this.city = city;
		this.region = region;
	}
	
	public Address(String numberAndStreet, String zip, String city, String region) {
		String[] split = splitNumAndStreet(numberAndStreet);
		this.number = split[0];
		this.street = split[1];
		this.zip = zip;
		this.city = city;
		this.region = region;
	}
	
	/** Number is in return value[0], street name is in [1] */
	private static String[] splitNumAndStreet(String address) {
		if (address.contains("Ste")) { // Some address indicate the suite. I
										// will have it removed
			address = address.substring(0, address.indexOf("Ste"));
		}
		String[] result = new String[2];
		Pattern numberFirst = Pattern.compile("(\\d+)\\s+(\\D+(\\s \\D+)?)");
		Pattern streetFirst = Pattern.compile("(\\D+(\\s \\D+)?)\\s+(\\d+)");

		Matcher numFirstMatcher = numberFirst.matcher(address);
		Matcher streetFirstMatcher = streetFirst.matcher(address);

		if (numFirstMatcher.matches() && !streetFirstMatcher.matches()) { // Num
																			// first
			result[0] = numFirstMatcher.group(1);
			result[1] = numFirstMatcher.group(2);
		} else if (!numFirstMatcher.matches() && streetFirstMatcher.matches()) { // Address
																					// first
			result[0] = streetFirstMatcher.group(3); // the group (\\d+) is opened
													// third
			result[1] = streetFirstMatcher.group(1);
		}

		else { // Both match or none match, i.e. unknown format.
			result[0] = "0"; // Something wrong, we give up splitting
			result[1] = address;
		}

		return result;
	}
	
	@Override
	public String toString() {
		return "Address [" + number + " " + street + "\n"
				+ zip + " " + city + ", " + region + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
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
		Address other = (Address) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}
	
	
	
	
	
}
