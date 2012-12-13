package edu.ucsb.testuggine;
import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Main {

    @Option(name="-r",usage="recursively run something")
    private boolean recursive;
	
	@Option(name="-start",usage="Resume from a previously-visited restaurant number")
	private int startingPoint = 0;

	@Option(name="-index",usage="Use index pages. You must provide the link to it, e.g. http://www.yelp.com/sm/los-angeles-ca-us-restaurants/a/")
	private String indexLink = "";
	
	@Option(name="-city",usage="Just give a city name. Format is lowercase and dash-spaced (e.g. los-angeles). Check for duplicates: Yelp will default the choice to the biggest city with that name")
	private String cityName = "";
	
	@Option(name="-end",usage="End at this restaraunt's number. Works only with -city")
	private int end = -1;

	// receives other command line parameters than options
	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static void main(String[] args) throws IOException, SQLException {
		new Main().doMain(args);
	}

	public void doMain(String[] args) throws IOException, SQLException {
		
		for (String arg : args)
			arguments.add(arg);
		
		CmdLineParser parser = new CmdLineParser(this);

		// if you have a wider console, you could increase the value;
		// here 80 is also the default
		parser.setUsageWidth(800);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			// you can parse additional arguments if you want.
			// parser.parseArgument("more","args");

			// after parsing arguments, you should check
			// if enough arguments are given.
			if( arguments.isEmpty() )
				throw new CmdLineException("No argument is given");

		} catch( CmdLineException e ) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java YelpScraper.jar [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("  Usage: java YelpScraper.jar "+parser.printExample(ALL));

			return;
		}

		YelpScraper y = null;
		
		if (cityName.equals("") && !indexLink.matches(".*www.yelp.com/sm/.*/?.*"))
			System.err.println("  Usage: java YelpScraper.jar "+parser.printExample(ALL));

		if (startingPoint > 0) {
			y = new YelpScraper(startingPoint);
		}
		
		if (!cityName.equals("")) {
			y = new YelpScraper();
			y.mineCity(cityName, startingPoint, end);
		}
		
		if (indexLink.matches(".*www.yelp.com/sm/.*/?.*")) {
			if (end > -1) { 
				System.err.println("-end not supported with -index.");
				System.err.println("  Usage: java YelpScraper.jar "+parser.printExample(ALL));
			}
			y = new YelpScraper(startingPoint);
			y.organizeIndexPages(indexLink);
		}
			
	}
}