
package Model.Download;


import javafx.scene.control.ProgressIndicator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
import java.nio.file.Paths;


public class DownloadThread extends Thread {

    private String filePath;
    private String title = "Unknown";
    private static final int BUFFER_SIZE = 4096;
    private HttpURLConnection connection;
    private  ProgressIndicator indicator;
    public DownloadThread(HttpURLConnection connection, String path, String title, ProgressIndicator indicator) {
        this.filePath = path;
        if (title != null)
            this.title = title;

        this.connection = connection;
        this.indicator = indicator;
    }

    /**
     * @param contentType is a string which tells the types of file
     * @param url         is a string which refers to the download link of file
     * @return
     * @throws URISyntaxException    url syntax not valid
     * @throws MalformedURLException not a url
     *                               /**
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    private File createFile(String contentType, String url) throws URISyntaxException, MalformedURLException {
        String fileTitle = title;
        System.out.println(Paths.get(new URI(url).getPath()).getFileName().toString());
        if ((url.length() - url.lastIndexOf('.')) == 4) {
            System.out.println("plain url.");
            fileTitle = url.substring(url.lastIndexOf("/") + 1, url.length());
        } else {

            String[] ext = contentType.split("/");
            System.out.println(ext[1]);
            fileTitle = fileTitle + "." + ext[1];
            System.out.println(fileTitle);
        }
        File downloadFile = new File(filePath + File.separator + fileTitle);
        if (!downloadFile.exists()) {
            try {
                downloadFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Cannot create File to store.");
                //				System.exit(0);
            }
        }
        return downloadFile;
    }

    @Override
    public void run() {
        try {
            System.out.println("Download started on link  " + connection.getURL().toString());

            BufferedInputStream in = new BufferedInputStream(connection.getInputStream()); // open the input stream on the established tcp connection.
            FileOutputStream out = new FileOutputStream(createFile(connection.getContentType(), connection.getURL().toString())); // create a file and open the output stream to write on file.
            int size = connection.getContentLength(); // to get the total size of the file being downloaded it will be helpful making the GUI like progress bar.
            int len;
            int progress = 0; // to update the GUI progress bar.
            byte[] buffer = new byte[BUFFER_SIZE]; // byte array to get the content from the input stream.
            while ((len = in.read(buffer, 0, BUFFER_SIZE)) != -1) { // getting content from the input stream and saving into the buffer byte array.
                out.write(buffer, 0, len); // writing the bytes to the file.
                //			out.write(buffer);
                progress += len; // update progress variable
                System.out.println("Downloaded bytes " + progress / 1024 + " KB" + "| Remaining  bytes  " + (size - progress) / 1024 + " KB");
                indicator.setProgress(progress/(float)size);
            }
            out.flush(); // empty the buffer.
            in.close(); // close opened streams
            out.close();
            System.out.println("Download Complete . ");


        } catch (SocketTimeoutException e) {
            System.out.println("Connection is not established...");
        } catch (FileNotFoundException e) {
            System.err.println("Error While Downloading : file not found . ");
            e.printStackTrace();
        } catch (ConnectException ex) {
            System.out.println("Connect Exception.");
        } catch (SSLHandshakeException e) {
            System.err.println("Error while SSL handshake.");
        } catch (UnknownHostException e) {
            System.err.println("Error while downloading : Unknown Host Exception.");
        } catch (IOException e) {
            System.err.println("input output exception .");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            System.out.println("Exception occurred in filTitle URL.");
            e.printStackTrace();
        }


    }

}