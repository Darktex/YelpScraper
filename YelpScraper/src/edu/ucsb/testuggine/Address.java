package edu.ucsb.testuggine;

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
