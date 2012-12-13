package edu.ucsb.testuggine;


import org.jsoup.nodes.Element;


public class YelpUser {
	String id;
	String userName;
	Integer friendsCount;
	Integer reviewsCount;
	//private RatingDistribution ratingDistribution;
	
	private Element container;
	
	public YelpUser(Element passport) {
		container = passport;
		id = mineId();
		userName = mineUserName();
		friendsCount = mineFriendsCount();
		reviewsCount = mineReviewsCount();
	}

	private String mineId() {
		Element cont = container.select("ul.user-passport-info > li.user-name > a").first();
		String linkAndId = cont.attr("href");
		String idStr = linkAndId
				.substring(
						linkAndId.indexOf("?userid=") + "?userid=".length(),
						linkAndId.length()
				);
		return idStr;
	}
	
	private String mineUserName() {
		Element cont = container.select("ul.user-passport-info > li.user-name > a").first();
		String name = cont.text();
		return name;
	}

	private Integer mineFriendsCount() {
		Element statsContainer = container.select("ul.user-stats").first();
		String strFriendsCount = statsContainer.select("li.friend-count > span").first().ownText().trim();
		return Integer.valueOf(strFriendsCount);
	}

	private Integer mineReviewsCount() {
		Element statsContainer = container.select("ul.user-stats").first();
		String strFriendsCount = statsContainer.select("li.review-count > span").first().ownText().trim();
		return Integer.valueOf(strFriendsCount);
	}
	
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "YelpUser [id=" + id + ", userName=" + userName
				+ ", friendsCount=" + friendsCount + ", reviewsCount="
				+ reviewsCount + "]";
	}
	
	
}
