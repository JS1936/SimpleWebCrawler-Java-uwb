import javax.swing.text.html.HTML;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

// Link
//  --> look inside data for LINK
//          Link
//              --> look inside data for LINK

public class WebCrawler {

    static Vector<String> visitedURLs = new Vector<String>();    // to track urls that have been visited. Avoid repeat visits.
    static int currHop = 0; //start at 1 or 0?
    static int numHops = 0; //default
    //add static int currURL = 0?

    // Command line input,
    //      expect: 2 args, args[0] = starting url, args[1] = integer >= 0 (num_hops)

    //Abstract out, so main calls "hop" or something similar
    public static void printArgs(String[] args)
    {
        for(String arg: args)
        {
            System.out.println("ARG = " + arg);
        }
    }

    public static void printVisitedURLs()
    {
        System.out.println("----Visited URLs:-----");
        for(String url : visitedURLs)
        {
            System.out.println(url);
        }
        System.out.println("----------------------");
    }

    // "preFirstHop"
    public static void main2(String[] args) throws IOException {
        // 1. Check number of arguments
        if(args.length != 2)
        {
            System.err.println("Error: Expect 2 args (url, num_hops). Actual: " + args.length);
            return;
        }
        System.out.println("\t# arguments:\tOK");

        //2. Check url and num_hops
        URL url = getURL(args[0]);
        Integer num_hops = getNumHops(args[1]);
        numHops = num_hops;
        Integer argc = args.length;

        // Only continue if input is valid.
        if(!isValidInput(url, num_hops))
        {
            return;
        }
        //System.out.println("Attempting to connect to : " + url.toString());
        connect(url);
    }
    // 1. Validate args
    // 2. Connect
    // 3. If connect, then get next link
    //          1. Validate args...
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to WebCrawler.java");
        System.out.println("--------------------------");
        System.out.println("args.length = " + args.length);
        printArgs(args);
        main2(args);
        printVisitedURLs();
    }

    private static HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if(connection == null)
        {
            System.err.println("Error: connection is null");
        }
        else //connection is not null
        {
            System.out.println("connection = " + connection.toString());
            connection.setRequestMethod("GET");
            //connection.setInstanceFollowRedirects(true); //try this

            int responseCode = connection.getResponseCode();
            //System.out.println("ResponseCode = " + responseCode);
            if(responseCode == 200)
            {
                System.out.println("\nConnection made. URL = " + url.toString());
                addToVisited(url.toString());
                InputStream is = connection.getInputStream(); //LOOK HERE
                System.out.println("is = " + is.toString());
                //connection.getInstanceFollowRedirects()
                //System.out.println("hop 0 = " + url.toString());
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
                //else {
                    return null;
                //}
            }
//            InputStream is = connection.getInputStream(); //LOOK HERE
//            System.out.println("is = " + is.toString());
//            //connection.getInstanceFollowRedirects()
//            //System.out.println("hop 0 = " + url.toString());
//            parse(is);
//            //is.close();
        }
        return connection;
    }
    //return null; //?
    //String result = "";
//                    while(is.available() > 0)
//                    {
//                        result += (char)(is.read());
//                    }
//                    System.out.println("error result = \n" + result); //gives html


    //storage, / option...
    //NOTE: make it so http://blah and https://blah register as same thing
    //NOTE: make it so BLAH/ and BLAH register as same thing

    // if length is different by 1
    //      - is one http<?> and other https?
    //      - is one BLAH and other BLAH/?
//why is printVisitedURLs getting called twice as often?
    public static boolean alreadyVisited(String urlFound)
    {
        ///System.out.println("-----Checking if url is already visited: " + urlFound);
        //printVisitedURLs();
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
                return true; // match: is already visited
            }
        }
        // Not already visited
        return false;

    }
    ///to-do: check that this actually works
    /// Note: does url count as visited as soon as you find it? Or do you need to connect with it first? OR connect + check all links first?
    ///fix

    // Pre: urlFound is not already in visitedURLs
    public static void addToVisited(String urlFound)
    {
        System.out.println("(" + currHop + ") " + urlFound);
        visitedURLs.add(urlFound);
        currHop++;

    }

    //check if specifying whole path or not (todo)
    private static void parse(InputStream is) throws IOException {
        //System.out.println("is = " + is.toString());
        System.out.println("is.available() = " + is.available());
        String result = "";
        //while(is.available() > 0)
        //{
        //    result += (char)(is.read());
        //}
        while(true)
        {
            int var = is.read();
            //System.out.println("var = " + var);
            if(var == -1)
            {
                break;
            }
            result += (char)(var);
        }
        //System.out.println("result = " + result);
        ///System.out.println("result = \n" + result); //gives html

        //Try writing it to file
        //BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
        //bw.write(result);
        //bw.close();

        //now parse it...
        int index = 0;
        int count = 0;
        while(result.length() > 0 && index != -1 && currHop < numHops)
        {
            index = result.indexOf("<a href=\"http");
            //System.out.print(count + ": index for '<a href=\"http' ==> " + index);
            ///System.out.print("Hop " + count + ": " + index);
            if(index != -1)
            {
                count++;
                result = result.substring(index + 9); // new start index

                int hrefEndIndex = result.indexOf("\"");//result.indexOf("\">"); //Look hERE
                //if(index == -1)
                //{
                //    System.
                //}
                // length of 'a href="' = 8

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
                //printVisitedURLs();
            }
            // <a href="http://faculty.washington.edu/dimpsey">
            //result = result.substring(index + 1); // new start index
        }

    }
    /*
    //Given a URL, checks the current response code for the HttpURLConnection connection.
    //Expect: response code is 200 (valid/OK). Otherwise, invalid connection.
    private void printResponseCodeSuccessFail(URL url) throws IOException {
        System.out.println("Response code: " + this.connection.getResponseCode()); //expect: 200
        if(this.connection.getResponseCode() == 200) //response is valid/OK
        {
            System.out.println("Connection made. URL: " + url.toString());
        }
        else
        {
            System.out.println("Error: connection to api has invalid response");
        }
    }
     */

    // If valid, expect: args[0] = url, args[1] = int >= 0
    /*
    private static boolean isValidArgs(String[] args)
    {
        // 1. Check number of arguments
        if(args.length != 2)
        {
            System.err.println("Error: Expect 2 args (url, num_hops). Actual: " + args.length);
            return false;
        }
        System.out.println("\t# arguments:\tOK");

        //2. Check url and num_hops
        URL url = getURL(args[0]);
        Integer num_hops = getNumHops(args[1]);
        Integer argc = args.length;

        // Only continue if input is valid.
        if(!isValidInput(url, num_hops))
        {
            return false;
        }
        return true;
    }
     */
    private static URL getURL(String str)
    {
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

    private static Integer getNumHops(String str)
    {
        Integer num_hops;
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
    // Returns true if url is URL AND num_hops is integer >= 0.
    // Otherwise, returns false.
    private static boolean isValidInput(URL url, Integer num_hops)
    {
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

// while num_hops > 0
//      do hop, print
//      hop--
