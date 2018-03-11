package Model;

import com.pdfcrowd.Client;
import com.pdfcrowd.PdfcrowdError;

import java.io.FileOutputStream;
import java.io.IOException;

public class HTMLtoPDF  {

    public void execute() {
        try {
            FileOutputStream fileStream;

            // create an API client instance
            Client client = new Client("username", "apikey");

            // convert a web page and save the PDF to a file
            fileStream = new FileOutputStream("example.pdf");
            client.convertURI("http://example.com/", fileStream);
            fileStream.close();


        } catch (PdfcrowdError why) {
            System.err.println(why.getMessage());
        } catch (IOException exc) {
            // handle the exception
        }
    }

}
