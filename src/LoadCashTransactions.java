import model.DimAccount;
import model.DimCustomer;
import model.FactCashTransaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mark on 5/10/15.
 */
public class LoadCashTransactions {

  static final String filename = "/Users/mark/Desktop/ICS 624 - Data Management/Project Work/Experiments/TPC/scaleFactor3/Batch1/CashTransaction.txt";
  static SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-M-d H:m:s");
  static SimpleDateFormat oracleDateFormat = new SimpleDateFormat("dd-MMM-yy");

  public static void load() throws IOException, ParseException {
    System.out.println("Start loading cash transactions");

    File inputFile = new File(filename);

    int linesProcessed = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        // System.out.println(line);
        String[] parts = line.split("\\|");

        String accountId = parts[0];
        Date timeStamp = timeStampFormat.parse(parts[1]);
        Float cashTransaction = Float.parseFloat(parts[2]);

        // System.out.println("Account [" + accountId + "]");
        DimAccount dimAccount = DimAccount.find.where().eq("account_id", accountId).betweenProperties("effective_date", "end_date", timeStamp).findUnique();
        DimCustomer dimCustomer = DimCustomer.find.byId(dimAccount.getsK_CustomerId());

        FactCashTransaction factCashTransaction = new FactCashTransaction();
        factCashTransaction.setSkCustomerId(dimCustomer.getsK_CustomerID());
        factCashTransaction.setSkAccountId(dimAccount.getsK_AccountId());
        factCashTransaction.setTransactionDate(timeStamp);
        factCashTransaction.setCashTransaction(cashTransaction);
        factCashTransaction.save();
        linesProcessed++;

        if (linesProcessed % 10 == 0) {
          System.out.println("Processed " + linesProcessed + " lines");
        }
      }
    }


    System.out.println("End loading cash transactions");
  }
}
