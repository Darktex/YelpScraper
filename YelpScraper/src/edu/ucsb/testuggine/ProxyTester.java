package edu.ucsb.testuggine;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.princeton.cs.introcs.StdOut;

public class ProxyTester {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		System.setProperty("http.proxyHost", "deltoro");
		System.setProperty("http.proxyPort", "3128");
		Document doc = null;
		boolean success = false;

		String url = "http://www.whatismyip.com";

		while (!success) {
			try {
				String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
				doc = Jsoup.connect(url).timeout(60 * 1000).userAgent(ua).get();
				success = true;
				break;
			} catch (IOException e) {
				System.out.println("URL constructor failed to connect. Retrying...");
			}
		}
		Element ipdiv = doc.select("div#greenip").first();
		StdOut.println(ipdiv.text());
	}


}

