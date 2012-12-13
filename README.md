YelpScraper
===========

A scraper that mines all the reviews in Yelp and writes them in a database. While the code is open, the DB connection is not provided.

USAGE:

Trying the program without arguments will make it list all options.

Two use cases are supported:

1) Mine all the restaurants (with their reviews) in a (small, max 1000 restaurants) city, starting from restaurant #37 and finishing at restaurant #84: java -jar YelpScraper.jar -city los-angeles -start 37 -end 84

2) Mine all the restaurants (with their reviews) in a large city by making use of Yelp's directories, starting from restaurant #37 (-end not supported here): java -jar YelpScraper.jar -index <CITY INDEX URL>


(you can use a subset of these commands and print them in any order).

I recommend launching this with java -Xmx4g, but the more the better.
