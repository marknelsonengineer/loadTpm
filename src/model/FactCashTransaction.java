package model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import com.avaje.ebean.Model;

@Entity
@Table
public class FactCashTransaction extends Model {
  long skCustomerId;
  long skAccountId;
  Date transactionDate;
  float cashTransaction;

  public static final Model.Finder<Long,FactCashTransaction> find = new Model.Finder<>(Long.class, FactCashTransaction.class);

  public float getCashTransaction() {
    return cashTransaction;
  }

  public void setCashTransaction(float cashTransaction) {
    this.cashTransaction = cashTransaction;
  }

  public long getSkAccountId() {
    return skAccountId;
  }

  public void setSkAccountId(long skAccountId) {
    this.skAccountId = skAccountId;
  }

  public long getSkCustomerId() {
    return skCustomerId;
  }

  public void setSkCustomerId(long skCustomerId) {
    this.skCustomerId = skCustomerId;
  }

  public Date getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(Date transactionDate) {
    this.transactionDate = transactionDate;
  }
}
