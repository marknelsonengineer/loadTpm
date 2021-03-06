package model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import com.avaje.ebean.Model;

@Entity
@Table
public class DimAccount extends Model {
  @Id
  long sK_AccountId;
  String accountID;
  String brokerId;
  long sK_CustomerId;
  String status;
  String accountDesc;
  String taxStatus;
  Boolean isCurrent;
  Date effectiveDate;
  Date endDate;

  public static void copyData (DimAccount src, DimAccount dst) {
    dst.accountID = src.accountID;
    dst.brokerId = src.brokerId;
    dst.sK_CustomerId = src.sK_CustomerId;
    dst.status = src.status;
    dst.accountDesc = src.accountDesc;
    dst.taxStatus = src.taxStatus;
  }

  public static final Model.Finder<Long,DimAccount> find = new Model.Finder<>(Long.class, DimAccount.class);

  public String getAccountDesc() {
    return accountDesc;
  }

  public void setAccountDesc(String accountDesc) {
    this.accountDesc = accountDesc;
  }

  public String getAccountID() {
    return accountID;
  }

  public void setAccountID(String accountID) {
    this.accountID = accountID;
  }

  public Date getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(Date effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Boolean getIsCurrent() {
    return isCurrent;
  }

  public void setIsCurrent(Boolean isCurrent) {
    this.isCurrent = isCurrent;
  }

  public long getsK_AccountId() {
    return sK_AccountId;
  }

  public void setsK_AccountId(long sK_AccountId) {
    this.sK_AccountId = sK_AccountId;
  }

  public String getBrokerId() {
    return brokerId;
  }

  public void setBrokerId(String brokerId) {
    this.brokerId = brokerId;
  }

  public long getsK_CustomerId() {
    return sK_CustomerId;
  }

  public void setsK_CustomerId(long sK_CustomerId) {
    this.sK_CustomerId = sK_CustomerId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTaxStatus() {
    return taxStatus;
  }

  public void setTaxStatus(String taxStatus) {
    this.taxStatus = taxStatus;
  }
}
