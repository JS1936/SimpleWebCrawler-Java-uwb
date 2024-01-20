import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/*
 * WebCrawl.java is a simple web crawler that uses HTTP and HTML.
 * - Requires a starting url and a limit on number of url hops.
 * - Follows a depth-first search pattern for urls.
 * - Search concludes when: (1) limit on number of hops is met OR
 *                          (2) a link has no new accessible options
 *
 * Project 1: Web Crawler
 * CSS 436  : Cloud Computing
 * Professor: Robert Dimpsey
 * By:        Jennifer Stibbins
 */
public class WebCrawl {

    static Vector<String> visitedURLs = new Vector<String>(); // to track urls that have been visited. Avoid repeat visits.
    static int currHop = 0; // always starts at 0, expected to increment over time
    static int numHops = 0; // default, expected to change once


    // Optional: Can be helpful for debugging. Prints arguments given via command line.
    public static void printArgs(String[] args) {
        for(String arg: args)
        {
            System.out.println("ARG = " + arg);
        }
    }

    // Optional: Can be helpful for debugging. Prints all visited URLs so far. Does not mention responseCode info.
    public static void printVisitedURLs() {
        System.out.println("----Visited URLs:-----");
        for(String url : visitedURLs)
        {
            System.out.println(url);
        }
        System.out.println("----------------------");
    }

    // Starts the web crawler by validating arguments and connecting to given url (hop 0).
    public static void start(String[] args) throws IOException {
        // 1. Check number of arguments
        if(args.length != 2)
        {
            System.err.println("Error: Expect 2 args (url, num_hops). Actual: " + args.length);
            return;
        }
        System.out.println("\t# arguments:\tOK");

        // 2. Check url and num_hops
        URL url = getURL(args[0]);
        numHops = getNumHops(args[1]);

        // 3. Only continue if input is valid. If invalid, do not connect.
        if(!isValidInput(url, numHops))
        {
            return;
        }
        //System.out.println("Attempting to connect to : " + url.toString());
        connect(url);
    }


    // Command line input, expect: 2 args, args[0] = starting url, args[1] = integer >= 0 (num_hops)
    // Starts web crawler. If web crawler reaches limit num_hops, then prints all urls visited in chronological order.
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to WebCrawler.java");
        System.out.println("--------------------------");
        start(args);
        printVisitedURLs();
    }

    // Returns connection.
    // Special case example: 301 (redirect)
    private static HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if(connection == null)
        {
            System.err.println("Error: connection is null");
        }
        else //connection is not null
        {
            //System.out.println("connection = " + connection.toString());
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if(responseCode == 200)
            {
                System.out.println("\nConnection made. URL = " + url.toString());
                addToVisited(url.toString());
                InputStream is = connection.getInputStream(); //LOOK HERE
                //System.out.println("is = " + is.toString());
                parse(is);
                //is.close();
            }
            else
            {
                System.out.println("Connection error | responseCode " + responseCode + " | " + url.toString());

                //404: not found

                if(responseCode == 301) //301: redirect
                {
                    String newLocation = connection.getHeaderField("Location");
                    System.out.println("--> new location = " + newLocation);
                    URL newURL = getURL(newLocation);
                    connect(newURL);
                }
                return null;
            }
        }
        return connection;
    }

    // Returns true if urlFound has been already visited. Otherwise, returns false.
    // Note: <url> and <url>/ are considered to be the same.
    public static boolean alreadyVisited(String urlFound) {
        if(visitedURLs.size() == 0) // urlFound is automatically unvisited
        {
            return false;
        }
        for(String url: visitedURLs)
        {
            String urlFoundAddSlash = urlFound + "/";
            String urlFoundRemoveLast = urlFound.substring(0, urlFound.length() - 1);
            if(url.equals(urlFound) || url.equals(urlFoundAddSlash) || url.equals(urlFoundRemoveLast))
            {
                return true; // urlFound is already visited
            }
        }
        return false; // urlFound is not already visited

    }

    // Pre: urlFound is not already in visitedURLs
    // Note: When adding a new url to visitedURLs, currHop increments by one.
    public static void addToVisited(String urlFound) {
        System.out.println("(" + currHop + ") " + urlFound);
        visitedURLs.add(urlFound);
        currHop++;
    }

    //check if specifying whole path or not (todo)
    //what if there are no new links to be found? (todo)
    //is the number off hops off by one (doing one extra)? (todo)
    // Note: Does not check if specifying whole path or not. Expects whole path.
    private static void parse(InputStream is) throws IOException {

        String result = "";
        while(true)
        {
            int var = is.read();
            if(var == -1) { break; }
            result += (char)(var);
        }
        //System.out.println("result = " + result); // gives html

        //Try writing it to file
        BufferedWriter bw = new BufferedWriter(new FileWriter("output" + currHop + ".txt"));
        bw.write(result);
        bw.close();

        //now parse it...
        int index = 0;
        int count = 0;
        while(result.length() > 0 && index != -1 && currHop < numHops)
        {
            index = result.indexOf("<a href=\"http");
            System.out.println("index = " + index);
            if(index != -1)
            {
                count++;
                result = result.substring(index + 9); // new start index

                int hrefEndIndex = result.indexOf("\"");//result.indexOf("\">"); //Look hERE
                String content = "";
                if(hrefEndIndex > 0)
                {
                    content = result.substring(0, hrefEndIndex);
                }
                System.out.println( " | content: " + content);
                if(content.length() > 0 && !alreadyVisited(content))
                {
                    connect(getURL(content));
                }
            }
            else // FIX THIS... / FINISH THIS... (explain the approach after seeing -1)
            {
                System.out.println("EXIT because index is -1");
                System.exit(0);
            }
        }
    }

    // Given string "str", tries to convert it into URL format.
    // Returns the url (returns null if URL is malformed)
    private static URL getURL(String str) {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            System.err.println("\turl:\t\tBAD. MalformedURLException. String could not become url");
            return null; //not System.exit(0); 1 bad url doesn't stop program from going
        }
        //System.out.println("\turl:\t\tOK (" + url.toString() + ")");
        return url;
    }

    // Given string "str", tries to convert it to int value num_hops.
    // If invalid conversion, num_hops defaults to -1.
    // Returns num_hops
    private static int getNumHops(String str) {
        int num_hops;
        try {
            num_hops = Integer.valueOf(str);
        } catch (NumberFormatException e)
        {
            System.err.println("\tnum_hops:\tBAD. NumberFormatException. String could not become int");
            return -1; //invalid //could do System.exit(0) instead
        }
        if(num_hops < 0)
        {
            System.err.println("\tnum_hops: \tBAD. expect >= 0, actual: " + num_hops);
            return -1; //invalid //could do System.exit(0) instead
        }
        System.out.println("\tnum_hops:\tOK");
        return num_hops;

    }

    // Pre: # args = 2
    // Returns true if url is URL AND num_hops is integer >= 0. Otherwise, returns false.
    private static boolean isValidInput(URL url, int num_hops) {
        // Check argument types: validate URL and int
        if(url == null) {
            System.err.println("Error: BAD url.");
            return false;
        }
        if(num_hops == -1) { // < 0
            System.err.println("Error: BAD num_hops.");
            return false;
        }
        return true;
    }

}
