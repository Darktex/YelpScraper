package edu.ucsb.testuggine;

public class RatingDistribution {
	public Integer oneCount;
	public Integer twoCount;
	public Integer threeCount;
	public Integer fourCount;
	public Integer fiveCount;
	
	/**
	 * @param oneCount How many reviews with score "1"
	 * @param twoCount How many reviews with score "2"
	 * @param threeCount How many reviews with score "3"
	 * @param fourCount How many reviews with score "4"
	 * @param fiveCount How many reviews with score "5"
	 */
	
	public RatingDistribution(Integer oneCount, Integer twoCount,
			Integer threeCount, Integer fourCount, Integer fiveCount) {
		this.oneCount = oneCount;
		this.twoCount = twoCount;
		this.threeCount = threeCount;
		this.fourCount = fourCount;
		this.fiveCount = fiveCount;
	}
	
	public RatingDistribution(Integer[] data) {
		if (data.length != 5) throw new Error("Tried to create a RatingDistribution with " + data.length + " elements instead of 5");
		oneCount = data[0];
		twoCount = data[1];
		threeCount = data[2];
		fourCount = data[3];
		fiveCount = data[4];
	}

	@Override
	public String toString() {
		return "RatingDistribution [1 star=" + oneCount + ", 2 stars="
				+ twoCount + ", 3 stars=" + threeCount + ", 4 stars="
				+ fourCount + ", 5 stars=" + fiveCount + "]";
	}

	public Integer[] getDistribution() {
		Integer[] result = new Integer[5];
		result[0] = oneCount;
		result[1] = twoCount;
		result[2] = threeCount;
		result[3] = fourCount;
		result[4] = fiveCount;
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
		RatingDistribution other = (RatingDistribution) obj;
		if (fiveCount == null) {
			if (other.fiveCount != null)
				return false;
		} else if (!fiveCount.equals(other.fiveCount))
			return false;
		if (fourCount == null) {
			if (other.fourCount != null)
				return false;
		} else if (!fourCount.equals(other.fourCount))
			return false;
		if (oneCount == null) {
			if (other.oneCount != null)
				return false;
		} else if (!oneCount.equals(other.oneCount))
			return false;
		if (threeCount == null) {
			if (other.threeCount != null)
				return false;
		} else if (!threeCount.equals(other.threeCount))
			return false;
		if (twoCount == null) {
			if (other.twoCount != null)
				return false;
		} else if (!twoCount.equals(other.twoCount))
			return false;
		return true;
	}
	
	
	
	
	
	
}
