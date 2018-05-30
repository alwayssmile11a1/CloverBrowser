package Model.HTMLtoPDF;

import com.pdfcrowd.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HTMLtoPDFHelper implements Runnable {

    private String webURL;
    private File file;

    public HTMLtoPDFHelper(String webURL, File file)
    {
        this.webURL = webURL;
        this.file = file;
    }


    @Override
    public void run() {
        try {
            FileOutputStream fileStream = null;

            // create an API client instance
            Pdfcrowd.HtmlToPdfClient client = new Pdfcrowd.HtmlToPdfClient("alwayssmile111a1", "e883e3eaa5a6dba9bd48f60b6ca4f54b");

            // convert a web page and save the PDF to a file
            try {
                fileStream = new FileOutputStream(file);
                byte[] pdf =  client.convertUrl(webURL);
                fileStream.write(pdf);
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
