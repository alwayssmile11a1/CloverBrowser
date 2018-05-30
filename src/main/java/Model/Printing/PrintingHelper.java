package Model.Printing;


import Application.Main;
import javafx.print.PrinterJob;
import javafx.scene.web.WebEngine;

public class PrintingHelper implements Runnable {


    private WebEngine webEngine;

    public  PrintingHelper(WebEngine webEngine)
    {
        this.webEngine = webEngine;
    }


    @Override
    public void run() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            System.out.println("printing......");
            if(job.showPrintDialog(Main.getStage())) {
                webEngine.print(job);
                job.endJob();
            }
        }
    }
}
