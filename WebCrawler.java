import javax.swing.text.html.HTML;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

// Link
//  --> look inside data for LINK
//          Link
//              --> look inside data for LINK

public class WebCrawler {

    static Vector<String> visitedURLs;    // to track urls that have been visited. Avoid repeat visits.

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
    public static void hop(String[] args)
    {
        System.out.println("args.length = " + args.length);
        printArgs(args);

    }
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to WebCrawler.java");
        System.out.println("--------------------------");
        hop(args);





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

    //to-do: check that this actually works
    // Note: does url count as visited as soon as you find it? Or do you need to connect with it first? OR connect + check all links first?
    public static boolean addToVisited(String urlFound)
    {
        for(int i = 0; i < visitedURLs.size(); i++)
        {
            if(urlFound == visitedURLs.get(i))
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

        //now parse it...
        int index = 0;
        int count = 0;
        //while(result.length() > 0 && index != -1)
       // {
            index = result.indexOf("<a href=\"http");
            System.out.print(count + ": index for '<a href=\"http' ==> " + index);
            if(index != -1)
            {
                count++;
                result = result.substring(index + 1); // new start index

                int hrefEndIndex = result.indexOf("\">");
                // length of 'a href="' = 8

                String content = result.substring(8, hrefEndIndex + 1);
                System.out.println( " | content: " + content);
                connect(getURL(content));
            }
            // <a href="http://faculty.washington.edu/dimpsey">
            //result = result.substring(index + 1); // new start index
        //}

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
    public static HTML getHTML()
    {


        return null;
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
