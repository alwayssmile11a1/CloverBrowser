package Model.Download;

import javafx.scene.control.ProgressIndicator;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.util.Properties;

public class DownloadHelper {


    private DownloadHelper()
    {

    }


    public static void startDownload(HttpURLConnection connection, String title, ProgressIndicator indicator) {

        DownloadThread thread = new DownloadThread(connection, downloadFolder(), title, indicator);
        System.setProperty("java.net.preferIPv4Stack", "true");
        thread.start();
    }


    private static void setProxy() {
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("http.proxyHost", "172.16.0.2");
        systemProperties.setProperty("http.proxyPort", "8080");
        systemProperties.setProperty("https.proxyHost", "172.16.0.2");
        systemProperties.setProperty("https.proxyPort", "8080");

    }

    /*
     * Method create the Folder Downloads in the home/ if does not exists .To store the Download Stuff
     * */

    /**
     * @return
     */
    public static String downloadFolder() {
        File home = new File(System.getProperty("user.home"));
        File folder = new File(home, "Downloads");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    public static HttpURLConnection isDownloadable(String webURL)
    {

        try {
            URL obj; // create url object for the given string
            obj = new URL(webURL);

            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            if (webURL.startsWith("https")) {
                System.out.println("Establishing https URL connection. . .");
                connection = (HttpsURLConnection) obj.openConnection();
            }
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setUseCaches(true);
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.connect();
            int requestinfo = connection.getResponseCode();
            String contentType = connection.getContentType();
            if (requestinfo == connection.HTTP_OK && canDownload(contentType)) {

                return connection;
            }
            else
            {
                connection.disconnect();
                System.out.println("Cannot download File response code from server: " +requestinfo);
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;

    }


    private static boolean canDownload(String contentType){
        System.out.println(contentType);
        if(contentType.contains("application")){
            return true;
        }else if (contentType.contains("video")){
            return true;
        }else if (contentType.contains("audio")){
            return true;
        }else if(contentType.contains("image")){
            return true;
        }
        return false;
    }

}