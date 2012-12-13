package edu.ucsb.testuggine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.princeton.cs.introcs.Out;
import edu.princeton.cs.introcs.StdOut;


public class YelpRestaurant {
	String name;
	String category;
	Address address;
	String phoneNumber;
	String website;
	Float rating; // should probably be an enum, but meh
	ArrayList<YelpReview> reviews;
	// private RatingDistribution ratingDistribution;
	String url;
	Integer totalNumberOfReviews;
	
	private Element restaurantInfoContainer; // used by the mineX() methods.
	
	public YelpRestaurant(Document doc, String _url) {
		restaurantInfoContainer = doc.select("div#bizBox").first();
		url = _url;
		name = mineName();
		category = mineCategory();
		address = mineAddress();
		phoneNumber = minePhoneNumber();
		website = mineWebsite();
		rating = mineScore();
		totalNumberOfReviews = mineTotalNumberOfReviews(doc.select(
				"div#bizReviews").first());
		reviews = new ArrayList<YelpReview>(); // For performance reasons, 
		// this won't be populated until calling class specifically asks for it.
		
	}

	public void mineReviewsAndDumpToDB(Document startingDoc, YelpScraper yelpScraper) throws SQLException {
		
		ArrayList<YelpReview> result = new ArrayList<YelpReview>();
		Element container = startingDoc.select("div#bizReviews").first();

		for (int i = 0; 40 * i < totalNumberOfReviews; i++) { // 40 reviews per
																// page.
			// We start with 1-40, so if we have 40 reviews we stop, if we have
			// 41 we continue.
			Element pageContainer = container; // changes with page
			if (i > 0) { // to get comments in page i > 0, we need another
							// connection.
				String targetPage = url + "?start=" + Integer.toString(40 * i);
				Document doc = yelpScraper.getHTMLAndSaveInDB(targetPage);
				pageContainer = doc.select("div#bizReviews").first();
			}
			Elements reviewcontainers = pageContainer
					.select("div#reviews-other > ul > li.review");
			for (Element reviewcontainer : reviewcontainers) {
				result.add(new YelpReview(reviewcontainer, this));
			}
		}
		reviews = result;
	}

	private String mineName() {
		Element nameDiv = restaurantInfoContainer.select(
				"div#bizInfoHeader > h1").first();
		return nameDiv.text();
	}

	private String mineCategory() {
		Elements categoryContainers = restaurantInfoContainer
				.select("p#bizCategories > span#cat_display > a");
		if (!categoryContainers.isEmpty())
			return categoryContainers.first().text();
		else
			return "";
	}

	private Address mineAddress() {
		Element addressContainer = restaurantInfoContainer.select("address")
				.first();
		String numAndStreet = addressContainer.select(
				"span[itemprop=streetAddress]").text();
		String[] split = splitNumAndStreet(numAndStreet);

		String city = addressContainer.select("span[itemprop=addressLocality]")
				.text();
		String region = addressContainer.select("span[itemprop=addressRegion]")
				.text();
		String zipcode = addressContainer.select("span[itemprop=postalCode]")
				.text();

		Address result = new Address(split[0], split[1], zipcode, city, region);
		return result;
	}

	private String minePhoneNumber() {
		Elements phoneContainer = restaurantInfoContainer
				.select("span#bizPhone");
		if (!phoneContainer.isEmpty()) {
			String phoneText = phoneContainer.first().text();
			phoneText = phoneText.replaceAll("\\D", ""); // Numbers are saved as
															// a large INT
			return phoneText;
		} else
			return "";
	}

	private String mineWebsite() {
		Elements urlContainer = restaurantInfoContainer.select("div#bizUrl");
		if (!urlContainer.isEmpty()) {
			String url = urlContainer.first().text();
			if (url.length() > 98) {
				url = url.contains("#")?url.substring(0, url.indexOf("#")):""; // if the URL is too long, it gets dropped
			}
			return url;
		}
			
		else
			return "";
	}
	

	private Float mineScore() {
		Elements scoreContainers = restaurantInfoContainer
				.select("div#bizRating > div > div.rating > i > img");
		if (scoreContainers.isEmpty())
			return new Float(0);
		String scoreText = scoreContainers.first().attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" star rating"));
		Float result = Float.valueOf(scoreText);
		return result;
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

	@SuppressWarnings("unused")
	private ArrayList<YelpReview> mineReviews(Element container) {
		ArrayList<YelpReview> result = new ArrayList<YelpReview>();

		for (int i = 0; 40 * i < totalNumberOfReviews; i++) { // 40 reviews per
																// page.
			// We start with 1-40, so if we have 40 reviews we stop, if we have
			// 41 we continue.
			Element pageContainer = container; // changes with page
			if (i > 0) { // to get comments in page i > 0, we need another
							// connection.
				String targetPage = url + "?start=" + Integer.toString(40 * i);
				Document doc = YelpScraper.getHTMLFromPage(targetPage);
				
				pageContainer = doc.select("div#bizReviews").first();
			}
			Elements reviewcontainers = pageContainer
					.select("div#reviews-other > ul > li.review");
			for (Element reviewcontainer : reviewcontainers) {
				result.add(new YelpReview(reviewcontainer, this));
			}
		}

		return result;
	}

	private Integer mineTotalNumberOfReviews(Element container) {
		if (container == null)
			return 0;
		Elements containers = container.select("h2#total_reviews");
		if (containers.isEmpty())
			return 0;
		String containerStr = containers.first().ownText();
		// format: if multiple, "<space> TOTAL <space> reviews for <RESTAURANT>"
		// if single, "One review for <RESTAURANT>"
		if (containerStr.contains("One"))
			return 1;
		String trimmedStr = containerStr.substring(0,
				containerStr.indexOf(" reviews for "));
		return Integer.valueOf(trimmedStr);
	}
	
	@Override
	public String toString() {
		return "Restaurant [\nname=" + name + ", category=" + category
				+ ", address=" + address + ", phoneNumber=" + phoneNumber
				+ ", website=" + website + ", score=" + rating
				+ ".\nReviews:\n" + reviews + "\n]\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((website == null) ? 0 : website.hashCode());
		return result;
	}
	
	public String hashString() {
		return name + hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YelpRestaurant other = (YelpRestaurant) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (website == null) {
			if (other.website != null)
				return false;
		} else if (!website.equals(other.website))
			return false;
		return true;
	}

	public boolean hasCategories() {
		return !category.isEmpty();
	}

	public boolean hasScore() {
		return !(rating.equals(new Float(0)));
	}

	public Integer totalReviews() {
		return totalNumberOfReviews;
	}
	
	public static void main(String args[]) throws SQLException {
		int totalReviewsMined = 0;
		int totalRestaurantsMined = 0;
		YelpScraper scraper = new YelpScraper();

		Out Out = new Out("/Users/davide/Desktop/consolelog.txt");

		for (String arg : args) {
			
			Document d = YelpScraper.getHTMLFromPage(arg);
			YelpRestaurant q = new YelpRestaurant(d, arg);
			
			Out.println(q);
			scraper.writeToDB(q);
			totalReviewsMined += q.totalReviews();
			totalRestaurantsMined++;
		}
		StdOut.println("Total reviews mined: " + totalReviewsMined +
				".\nTotal restaurants mined: " + totalRestaurantsMined);
	}

	public String getName() {
		return name;
	}

}
