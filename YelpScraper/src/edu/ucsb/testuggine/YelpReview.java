package edu.ucsb.testuggine;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.princeton.cs.algs4.Date;


public class YelpReview {
	String id;
	YelpUser author;
	YelpRestaurant restaurant;
	Float rating;
	
	Integer usefulCounter;
	Integer funnyCounter;
	Integer coolCounter;
	
	String text;
	Element containingElement;
	Date date;
	
	public YelpReview(Element container, YelpRestaurant r) {
		restaurant = r;
		containingElement = container;
		id = mineId();
		author = mineAuthor();
		rating = mineRating();
		usefulCounter = mineUsefulCounter();
		funnyCounter = mineFunnyCounter();
		coolCounter = mineCoolCounter();
		text = mineText();
		date = mineDate();
	}

	private Date mineDate() {
		Elements datez = containingElement.select("span.date.smaller");
		if (datez.isEmpty()) return null;
		String dateStr = datez.first().ownText();
		int firstSlashPos = dateStr.indexOf("/");
		int secondSlashPos = dateStr.indexOf("/", firstSlashPos+1);
		int found = dateStr.indexOf("Updated - ");
		int begin;
		if (found == -1) begin = 0;
		else begin = found + "Updated - ".length();
				
		int month = Integer.valueOf(dateStr.substring(begin, firstSlashPos));
		int day = Integer.valueOf(dateStr.substring(firstSlashPos+1, secondSlashPos));
		int year = Integer.valueOf(dateStr.substring(secondSlashPos+1));
			
		return new Date(month, day, year);
	}

	private String mineId() {
		String idstr = containingElement.attr("id"); // it's "review_<ID>".
		return idstr.substring("review_".length() );
	}

	private YelpUser mineAuthor() {
		Element container = containingElement.select("div.user-passport").first();
		return new YelpUser(container);
	}

	private Float mineRating() {
		Element scoreContainer = containingElement.select("div.rating-container > div > i > img").first();
		String scoreText = scoreContainer.attr("alt");
		scoreText = scoreText.substring(0, scoreText.indexOf(" star rating"));
		Float result = Float.valueOf(scoreText);
		return result;
	}

	private Integer mineUsefulCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element usefulContainer = countersContainer.select("li.useful").first();
		Elements safeContainers = usefulContainer.select("span").get(1).select(":not(:containsOwn(\u00a0))");
		if (safeContainers.isEmpty()) return 0;
		String usefulStr = safeContainers.first().text();
	
		usefulStr = usefulStr.substring(1, usefulStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(usefulStr);
		
		return result;
	}

	private Integer mineFunnyCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element funnyContainer = countersContainer.select("li.funny").first();
		Elements safeContainers = funnyContainer.select("span").get(1).select(":not(:containsOwn(\u00a0))");
		if (safeContainers.isEmpty()) return 0;
		String funnyStr = safeContainers.first().text();
		
		funnyStr = funnyStr.substring(1, funnyStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(funnyStr);
		
		return result;
	}

	private Integer mineCoolCounter() {
		Element countersContainer = containingElement.select("div.rateReview.clearfix > ul").first();
		Element coolContainer = countersContainer.select("li.cool").first();
		Elements safeContainers = coolContainer.select("span").get(1).select(":not(:containsOwn(\u00a0))");
		if (safeContainers.isEmpty()) return 0;
		String coolStr = safeContainers.first().text();
		
		coolStr = coolStr.substring(1, coolStr.length()-1); // removes the parentheses that enclose the counter
		Integer result = Integer.valueOf(coolStr);
		
		return result;
	}

	private String mineText() {
		
		Elements containers = containingElement.select("div.media-story > p.review_comment");
		if (containers.isEmpty()) return "";
		Element container = containers.first();
		String dirty = container.ownText();
		String clean = dirty.replaceAll("\n", "<br/>");
		
		return clean;
	}


	@Override
	public String toString() {
		return "\tYelpReview [id=" + id + ", author=" + author + ", rating="
				+ rating + ", usefulCounter=" + usefulCounter
				+ ", funnyCounter=" + funnyCounter + ", coolCounter="
				+ coolCounter + ", text=" + text + "]\n";
	}
	
}