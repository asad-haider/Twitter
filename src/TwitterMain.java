import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Asad on 8/25/2016.
 */
public class TwitterMain {

    static String TWITTER_CONSUMER_KEY = "zLsU9zLm0bBbXFEgYVndGuMGj";
    static String TWITTER_CONSUMER_SECRET = "KBHpU2YDc60kRUcKTLmFviMJiL2RtAfv0Ml7k86jjnL7L3h0zY";

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    public static void main (String args[]) throws Exception {
        try {

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(TWITTER_CONSUMER_KEY)
                    .setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET)
                    .setOAuthAccessToken(null)
                    .setOAuthAccessTokenSecret(null);
            TwitterFactory tf = new TwitterFactory(cb.build());
            Twitter twitter = tf.getInstance();

            AccessToken accessToken = null;

            File file = new File("token.txt");
            if (file.exists()){
                Scanner in = new Scanner(new FileReader(file));

                ArrayList<String> tokenData = new ArrayList<>();

                while (in.hasNext()){
                    tokenData.add(in.nextLine());
                }
                System.out.println(tokenData.toString());

                twitter.setOAuthAccessToken(new AccessToken(tokenData.get(0), tokenData.get(1)));

            }else{
                RequestToken requestToken = twitter.getOAuthRequestToken();
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while (null == accessToken) {
                    System.out.println("Open the following URL and grant access to your account:");
                    System.out.println(requestToken.getAuthorizationURL());
                    System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
                    String pin = br.readLine();
                    try {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken();
                        }
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            System.out.println("Unable to get the access token.");
                        } else {
                            te.printStackTrace();
                        }
                    }
                }
                //persist to the accessToken for future reference.
                storeAccessToken((int) twitter.verifyCredentials().getId(), accessToken);
            }

//            Remove250Followers(twitter, "youazee", 5);
//            ListUsers(twitter, "SMAsadHyder", "Followers");
            ListUsers(twitter, "SMAsadHyder", "Following");

//            Status status = twitter.updateStatus("From stored dataa");
//            System.out.println("Successfully updated the status to [" + status.getText() + "].");
//            System.exit(0);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void storeAccessToken(int useId, AccessToken accessToken) {
        //store accessToken.getToken()
        //store accessToken.getTokenSecret()

        try(FileWriter fw = new FileWriter("token.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.write(accessToken.getToken() + "\n");
            out.write(accessToken.getTokenSecret());

            //more code
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    private static void RemoveFollowers(Twitter twitter, String currentUserId, int limit) throws TwitterException {
        long lCursor = -1;
        IDs friendsIDs = twitter.getFriendsIDs(currentUserId, lCursor);
        System.out.println(twitter.showUser(currentUserId).getName());
        System.out.println("==========================");
        int counter = 1;

        long[] userIDs = friendsIDs.getIDs();

        for (long i : userIDs)
        {
            System.out.println("Unfollowed: " + twitter.showUser(i).getName());
            twitter.destroyFriendship(twitter.showUser(i).getId());

            if (counter++ >= limit){
                break;
            }
        }
    }

    private static void FollowUserFollowers(Twitter twitter, String userId, int limit) throws TwitterException {
        IDs friendsIDs = GetUserFollowersList(twitter, userId);
        System.out.println(twitter.showUser(userId).getName());
        System.out.println("==========================");
        int counter = 1;

        long[] userIDs = friendsIDs.getIDs();
        System.out.println("Total Followers: " + userIDs.length);

        for (long i : userIDs)
        {
            System.out.println("Now Following: " + twitter.showUser(i).getName());
            twitter.createFriendship(twitter.showUser(i).getId());

            if (counter++ >= limit){
                break;
            }
        }

    }

    private static void FollowUserFollowings(Twitter twitter, String userId, int limit) throws TwitterException {
        IDs followingIDS = GetUserFollowingList(twitter, userId);
        System.out.println(twitter.showUser(userId).getName());
        System.out.println("==========================");
        int counter = 1;

        long[] followingIDs = followingIDS.getIDs();
        System.out.println("Total Following: " + followingIDs.length);

        for (long i : followingIDs)
        {
            System.out.println("Now Following: " + twitter.showUser(i).getName());
            twitter.destroyFriendship(twitter.showUser(i).getId());

            if (counter++ >= limit){
                break;
            }
        }
    }

    private static void ListUsers(Twitter twitter, String userId, String userType) throws TwitterException {

        IDs userIDs = null;

        if (userType.equals("Followers")){
            userIDs = GetUserFollowersList(twitter, userId);
        }else{
            userIDs = GetUserFollowingList(twitter, userId);
        }

        System.out.println(twitter.showUser(userId).getName());
        System.out.println("==================================");

        long[] userLongIds = userIDs.getIDs();
        System.out.println("Total " + userType + " : " + userLongIds.length);

        for (long i : userLongIds)
        {
            System.out.println("User: " + twitter.showUser(i).getName());
        }
    }

    private static IDs GetUserFollowersList(Twitter twitter, String userId) throws TwitterException {
        long lCursor = -1;
        IDs friendsIDs = twitter.getFriendsIDs(userId, lCursor);
        return friendsIDs;
    }

    private static IDs GetUserFollowingList(Twitter twitter, String userId) throws TwitterException {
        long lCursor = -1;
        IDs followingIDS = twitter.getFollowersIDs(userId, lCursor);
        return followingIDS;
    }



}