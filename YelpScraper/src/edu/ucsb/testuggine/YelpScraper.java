package edu.ucsb.testuggine;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.princeton.cs.algs4.Date;
import edu.princeton.cs.introcs.StdOut;



public class YelpScraper {

	MySQLConnection db;
	int current; // The current restaurant number the scraper is on
	int startFrom; // The restaurant to start scraping from

	public YelpScraper() {
		db = new MySQLConnection();
		current = 0;
		startFrom = 0;
	}

	public YelpScraper(int startFrom) {
		db = new MySQLConnection();
		current = 0;
		this.startFrom = startFrom;
	}

	/** Accepted input: http://www.yelp.com/sm/los-angeles-ca-us-restaurants/g
	 * 	or http://www.yelp.com/sm/los-angeles-ca-us-restaurants
	 * 	or http://www.yelp.com/sm/los-angeles-ca-us-restaurants/ 
	 * @throws SQLException */
	public void organizeIndexPages(String baseUrl) throws SQLException {

		String url = stripIndexUrl(baseUrl); 

		for (int i = 'a'; i <= 'z'; i++) {
			char c = (char) i;
			String urlToVisit = url + "/" + c;
			visitIndexPage(urlToVisit);
		}
	}

	/** This method deals with returning the url in a format that organizeIndexPages() can process */
	private String stripIndexUrl(String baseUrl) {
		String url;
		int startingPos = baseUrl.indexOf("www.yelp.com/sm/");
		int firstSlashPos = baseUrl.indexOf("/", startingPos + "www.yelp.com/sm/".length() + 1);
		if (firstSlashPos == -1) url = baseUrl;
		else url = "http://" + baseUrl.substring(startingPos, firstSlashPos); // mb +1 to include slash?
		return url;
	}


	/** Index pages contain a list of links, ordered alphabetically like volumes
	 * in a old encyclopedia. Each link points to a list of restaurants of max size 80.
	 * An index page contains all the restaurants starting with a certain letter.
	 * If there are < 80 restaurants, then the list is immediately shown without any intermediate passage.
	 *  */
	private void visitIndexPage(String cityIndexUrl) throws SQLException {
		Document page = getHTMLFromPage(cityIndexUrl);
		String baseUrl = "http://www.yelp.com";

		if (!isIndexPage(page)) {
			current += visitRestaurants(cityIndexUrl);
		}
		else { // Otherwise, this is just an index page. We visit each 80-restaurants list from each link.
			Elements linkContainers = page.select("div#super-container > div.container.sitemap-biz-by-group")
					.select("a[href]");
			if (linkContainers.isEmpty()) return;
			for (int i = 1; i < linkContainers.size() - 1 ; i++) { // The last one *must* be treated differently
				String urlToVisit = linkContainers.get(i).attr("href");
				if (current + 80 > startFrom) current += visitRestaurants(baseUrl + urlToVisit);
				else current += 80;
			}
			String lastOnesUrl = linkContainers.last().attr("href");
			current += visitRestaurants(baseUrl + lastOnesUrl); // This is the last one, and we always visit it.
		}
	}

	private boolean isIndexPage(Document page) { // if the query is successful, then we have an index page
		Elements linkContainers = page.select("div#super-container > div.container.sitemap-biz-by-group");
		return (!linkContainers.isEmpty());
	}


	private int visitRestaurants(String indexPageUrl) throws SQLException {
		Document indexPage = getHTMLFromPage(indexPageUrl);
		Elements pageResults = indexPage.select("div#super-container > div.container.sitemap-biz-by-letter" +
				" > div > div > ol > li");
		int totalRestaurants = pageResults.size();
		if (current + totalRestaurants < startFrom) return totalRestaurants; // if we are still far from the starting point, leave
		
		for (int j = (current < startFrom)? startFrom - current: 0; j < totalRestaurants; j++) { // j is either 0 or some index if it must start from a point
			Element restaurantDiv = pageResults.get(j);

			String restaurantPostfix = restaurantDiv.select("h2 > a[href]").first().attr("href").toString();
			String restaurantUrl = stripTags("http://www.yelp.com" + restaurantPostfix);
			if (restaurantUrl.contains("#"))
				restaurantUrl = restaurantUrl.substring(0, restaurantUrl.indexOf("#"));
			if (restaurantUrl.length() > 80) { 
				continue;  // Kinda last resort. We just give up on it. To not screw up the numbering, we keep the number of those restaurants assigned. We will just skip from e.g. number 21 to number 23.  	
			}
			Document restaurantPage = getHTMLAndSaveInDB(restaurantUrl);
			YelpRestaurant yr = new YelpRestaurant(restaurantPage, restaurantUrl);
			StdOut.println((current+j) + ") " + yr.getName() + " (" + yr.totalNumberOfReviews + " reviews)");
			if (yr.hasCategories() && yr.hasScore() && !isAlreadyInDB(yr)) {
				yr.mineReviewsAndDumpToDB(restaurantPage, this); // We save a lot of time by doing it ONLY at this point
				writeToDB(yr);
			}
			else StdOut.println("\t\tAlready in DB. Skipping...");
			yr = null; // Should be useless
		}
		return totalRestaurants;
	}

	/** Mines all the restaurants of a given city between pageStart and pageEnd */
	public void mineCity(String city, int start, int end) throws SQLException {
		String urlName = city.replaceAll("-", "+");
		String base = "http://www.yelp.com/search?find_desc=restaurant&find_loc=";
		String url = base + urlName;
		Document doc = getHTMLAndSaveInDB(url);
		String totalStr = doc.select("div#pager_top > strong.pager_total").first().text();
		Integer totalRestaurants = Integer.valueOf(totalStr);
		if (end < start) end = totalRestaurants;

		for (int i = start; i < end; ) { // i grows by 10 at a time as there are 10 restaurants per page
			Elements pageResults = null;
			if (i > 0) {
				String pageUrl = url + "&start=" + i;
				pageResults = getHTMLAndSaveInDB(pageUrl).select("div#businessresults > div.businessresult.clearfix");
			}
			else pageResults = doc.select("div#businessresults > div.businessresult.clearfix");
			for (Element restaurantDiv : pageResults) {
				String restaurantPostfix = restaurantDiv.select("a[id^=bizTitleLink]").first().attr("href").toString();
				String restaurantUrl = stripTags("http://www.yelp.com" + restaurantPostfix);
				Document restaurantPage = getHTMLAndSaveInDB(restaurantUrl);
				YelpRestaurant yr = new YelpRestaurant(restaurantPage, restaurantUrl);
				StdOut.println(i + ") " + yr.getName() + " (" + yr.totalNumberOfReviews + " reviews)");
				i++;
				if (yr.hasCategories() && yr.hasScore() && !isAlreadyInDB(yr)) {
					yr.mineReviewsAndDumpToDB(restaurantPage, this); // We save a lot of time by doing it ONLY at this point
					writeToDB(yr);
				}
				else StdOut.println("\t\tAlready in DB. Skipping...");
				yr = null; // Should be useless
			}
		}
	}

	private String stripTags(String _url) {
		Integer index;
		if ((index = _url.indexOf("#query")) == -1)
			return _url;
		else
			return _url.substring(0, index);
	}

	protected static Document getHTMLFromPage(String url) {
		System.setProperty("http.proxyHost", "deltoro");
		System.setProperty("http.proxyPort", "3128");

		Document doc = null;
		boolean success = false;

		while (!success) {
			try {
				String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
				doc = Jsoup.connect(url).timeout(60 * 1000).userAgent(ua).get();
				success = true;
				break;
			} catch (IOException e) {
				System.out.println("Failed to connect to " + url + "\n\tRetrying...");
			}
		}
		return doc;
	}

	protected Document getHTMLAndSaveInDB(String url) throws SQLException {
		Document doc = getHTMLFromPage(url);
		String alreadyExistsCheckQuery = "SELECT * FROM `HTMLcache` WHERE `url` = ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, url); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (alreadyExistsRes.first() ) return doc;
		String insertionQuery = "INSERT INTO `restaurant_reviews`.`HTMLcache` (`url`, `date`, `HTML`) " +
				"VALUES (?, CURRENT_TIMESTAMP, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);
		prep.setString(1, url);
		prep.setString(2, doc.html()); // Name CANNOT be null!

		prep.executeUpdate();
		prep.close();

		return doc;
	}

	void writeToDB(YelpRestaurant r) throws SQLException {
		String insertionQuery = "INSERT INTO  `YelpRestaurant` ("
				+ "`id` , `name` ,`category` ,`addressNum` ,`addressStreet` ,`addressCity` ,"
				+ "`addressRegion` ,`addressZip` ,`website` ,`avgRating` ,`totalNumOfReviews`, `phoneNumber`)"
				+ "VALUES (? ,  ?,  ?,  "
				+ "?,  ?,  ?,  ?,  ?,  "
				+ "?,  ?,  ?, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		prep.setString(1, r.hashString());
		prep.setString(2, r.name); // Name CANNOT be null!
		safeInsert(prep, 3, r.category);
		safeInsert(prep, 4, r.address.number);
		safeInsert(prep, 5, r.address.street);
		safeInsert(prep, 6, r.address.city);
		safeInsert(prep, 7, r.address.region);
		safeInsert(prep, 8, r.address.zip);
		safeInsert(prep, 9, r.website);
		prep.setFloat(10, r.rating);
		prep.setInt(11, r.totalNumberOfReviews);
		safeInsert(prep, 12, r.phoneNumber);

		if (!isAlreadyInDB(r)) {
			StdOut.println("----\n" + prep + "\n--------");
			prep.execute();
		} else
			StdOut.println("Restaurant " + r.name
					+ " already in the DB. Skipping...");
		for (YelpReview rvw : r.reviews)
			writeToDB(rvw);
		prep.close();
	}

	void writeToDB(YelpReview rev) throws SQLException {

		String alreadyExistsCheckQuery = "SELECT * FROM  `YelpReview` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, rev.id);
		ResultSet alreadyExistsRes = checkStatement.executeQuery(); // if it's already there, don't insert
		String insertionQuery = "INSERT INTO `YelpReview` " +
				"(`id`, `author_id`, `restaurant_id`, `rating`, `usefulCounter`, `funnyCounter`, " +
				"`coolCounter`, `text`, `date`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

		PreparedStatement prep = db.con.prepareStatement(insertionQuery);

		prep.setString(1, rev.id); // Always null, handled by DB

		writeToDb(rev.author);
		prep.setString(2, rev.author.getId()); // Name CANNOT be null!

		prep.setString(3, rev.restaurant.hashString());
		prep.setFloat(4, rev.rating);
		prep.setInt(5,  rev.usefulCounter);
		prep.setInt(6,  rev.funnyCounter);
		prep.setInt(7,  rev.coolCounter);
		safeInsert(prep, 8, rev.text);
		safeInsert(prep, 9, mySQLformat(rev.date));

		if (!alreadyExistsRes.first()) {
			prep.execute();
		}
		prep.close();
	}

	private String mySQLformat(Date d) {
		return d.year() + "-" + d.month() + "-" + d.day();
	}

	void writeToDb(YelpUser u) throws SQLException {

		String alreadyExistsCheckQuery = "SELECT * FROM  `YelpUser` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, u.id);
		ResultSet alreadyExistsRes = checkStatement.executeQuery(); // if it's already there, don't insert
		String insertionQuery = "INSERT INTO `YelpUser` (`id`, `userName`, " +
				"`friendsCount`, `reviewsCount`) " +
				"VALUES (?, ?, ?, ?);";
		PreparedStatement prep = db.con.prepareStatement(insertionQuery);
		prep.setString(1, u.id); 
		prep.setString(2, u.userName); // Name CANNOT be null!
		prep.setInt(3, u.friendsCount);
		prep.setInt(4, u.reviewsCount);

		if (!alreadyExistsRes.first()) {
			prep.execute();
		}
		prep.close();
	}

	private boolean isAlreadyInDB(YelpRestaurant r) throws SQLException {
		String alreadyExistsCheckQuery = "SELECT * FROM  `YelpRestaurant` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, r.hashString()); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (!alreadyExistsRes.first() ) return false;
		return true;
	}

	private static void safeInsert(PreparedStatement prep, int pos, String field)
			throws SQLException { // JDBC sends an empty string instead of a
		// NULL value.
		if (field.isEmpty())
			prep.setString(pos, null);
		else
			prep.setString(pos, field);
	}
	// NO MAIN ANYMORE, use Main class to launch it.

}
