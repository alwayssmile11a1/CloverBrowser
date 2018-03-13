package Model.HTMLtoPDF;

import com.pdfcrowd.Client;
import com.pdfcrowd.PdfcrowdError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HTMLtoPDFHelper {

    public static void execute(String webUri, File file) {

        try {
            FileOutputStream fileStream = null;

            // create an API client instance
            Client client = new Client("alwayssmile11a1", "6499245d8b9b135e0dc97b4fa1b19595");

            // convert a web page and save the PDF to a file
            try {
                fileStream = new FileOutputStream(file);
                client.convertURI(webUri, fileStream);
                fileStream.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }


        } catch (PdfcrowdError why) {
            System.err.println(why.getMessage());
        } catch (IOException exc) {
            // handle the exception
        }

    }

}
