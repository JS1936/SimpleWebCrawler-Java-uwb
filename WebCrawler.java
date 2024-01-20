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
    }
    /*public static void hop(String[] args)
    {
        System.out.println("args.length = " + args.length);
        printArgs(args);

    }*/
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
        //hop(args);
        System.out.println("args.length = " + args.length);
        printArgs(args);

        main2(args);




    }
    //Given a URL, checks the current response code for the HttpURLConnection connection.
    //Expect: response code is 200 (valid/OK). Otherwise, invalid connection.
    //private void printResponseCodeSuccessFail(URL url) throws IOException {

    //private void endProgramIfNullConnection() throws IOException {

    private static HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if(connection == null)
        {
            System.err.println("Error: connection is null");
        }
        else
        {
            System.out.println("connection = " + connection.toString());
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("ResponseCode = " + responseCode);
            if(responseCode == 200)
            {
                System.out.println("Connection made. URL = " + url.toString());
            }
            else
            {
                System.out.println("Connection error: invalid response.");
            }
            InputStream is = connection.getInputStream();
            //System.out.println("hop 0 = " + url.toString());
            parse(is);




        }
        return connection;
    }

    //storage, / option...
    //NOTE: make it so http://blah and https://blah register as same thing
    //NOTE: make it so BLAH/ and BLAH register as same thing

    // if length is different by 1
    //      - is one http<?> and other https?
    //      - is one BLAH and other BLAH/?
//why is printVisitedURLs getting called twice as often?
    public static boolean alreadyVisited(String urlFound)
    {
        System.out.println("-----Checking if url is already visited: " + urlFound);
        //printVisitedURLs();
        if(visitedURLs.size() == 0)
        {
            System.out.println("New url found! --> " + urlFound);
            addToVisited(urlFound);
            return false;
        }
        for(String url: visitedURLs)
        {
            System.out.println(url + " VS \n" + urlFound);
            int difference = url.length() - urlFound.length();
            if(difference > 2) //EX: could need 's' and '/'
            {
                return false;
            }

            String urlFoundAddSlash = urlFound + "/";
            String urlFoundRemoveLast = urlFound.substring(0, urlFound.length() - 1);
            String urlFoundAddS = urlFound.substring(0,4) + "s" + urlFound.substring(4, urlFound.length());
            String urlFoundRemoveS = urlFound.substring(0,4) + urlFound.substring(5, urlFound.length());
            System.out.println("urlFoundAddSlash   = " + urlFoundAddSlash);
            System.out.println("urlFoundRemoveLast = " + urlFoundRemoveLast);
            System.out.println("urlFoundAddS       = " + urlFoundAddS);
            System.out.println("urlFoundRemoveS    = " + urlFoundRemoveS);
            if(url.equals(urlFound))
            {
                System.out.println("url == urlFound");
                return true;
            }
            else if(url.equals(urlFoundAddSlash))
            {
                System.out.println("url == urlFoundAddSlash");
                return true;
            }
            else if(url.equals(urlFoundRemoveLast))
            {
                System.out.println("url == urlFoundRemoveLast");
                return true;
            }
            else if(url.equals(urlFoundAddS))
            {
                System.out.println("url == urlFoundAddS");
                return true;
            }
            else if(url.equals(urlFoundRemoveS)) //Note: may not actually have https. Could become http//, removed :. Invalid link now.
            {
                System.out.println("url == urlFoundRemoveS");
                return true;
            }
            else //not a match for this one
            {

            }
        }
        // Not already visited
        System.out.println("New url found! --> " + urlFound);
        addToVisited(urlFound);
        //printVisitedURLs();
        return false;

    }
    //to-do: check that this actually works
    // Note: does url count as visited as soon as you find it? Or do you need to connect with it first? OR connect + check all links first?
    //fix
    public static boolean addToVisited(String urlFound)
    {
        int lengthFound = urlFound.length();

        for(int i = 0; i < visitedURLs.size(); i++)
        {
            String urlCurr = visitedURLs.get(i);
            int lengthCurrVisited = urlCurr.length();
            if((lengthFound - lengthCurrVisited) == 1)
            {
                urlFound.substring(0, lengthFound - 1); //take of "/", if exists
            }
            else if((lengthFound - lengthCurrVisited) == -1)
            {
                urlFound += "/"; // add "/", just in case
            }
            if(urlFound == urlCurr) //visitedURLs.get(i)
            {
                System.out.println(urlFound + " already was visited. Do not add to visitedURLs");
                return false;
            }
        }
        System.out.print("visitedURLs.size() = " + visitedURLs.size() + " -->");
        visitedURLs.add(urlFound);
        System.out.println(visitedURLs.size());
        return true;

    }
    private static void parse(InputStream is) throws IOException {
        System.out.println("is = " + is.toString());
        System.out.println("is.available() = " + is.available());
        String result = "";
        while(is.available() > 0)
        {
            result += (char)(is.read());
        }
        System.out.println("result = \n" + result); //gives html

        //Try writing it to file
        BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
        bw.write(result);
        bw.close();

        //now parse it...
        int index = 0;
        int count = 0;
        while(result.length() > 0 && index != -1)
        {
            index = result.indexOf("<a href=\"http");
            System.out.print(count + ": index for '<a href=\"http' ==> " + index);
            if(index != -1)
            {
                count++;
                result = result.substring(index + 9); // new start index

                int hrefEndIndex = result.indexOf("\"");//result.indexOf("\">"); //Look hERE
                // length of 'a href="' = 8

                String content = result.substring(0, hrefEndIndex);
                System.out.println( " | content: " + content);
                if(!alreadyVisited(content))
                {
                    connect(getURL(content));
                }
                printVisitedURLs();
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
    private static URL getURL(String str)
    {
        URL url;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            System.err.println("\turl:\t\tBAD. MalformedURLException. String could not become url");
            return null; //could do System.exit(0) instead
        }
        System.out.println("\turl:\t\tOK");
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
        if(num_hops == -1) {
            System.err.println("Error: BAD num_hops.");
            return false;
        }
        return true;
    }

}

// while num_hops > 0
//      do hop, print
//      hop--
