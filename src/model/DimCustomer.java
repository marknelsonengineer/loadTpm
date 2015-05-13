package model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import com.avaje.ebean.Model;

@Entity
@Table
public class DimCustomer extends Model {
@Id
  long sK_CustomerID;
  long customerID;
  String taxID;
  String lastName;
  String firstName;
  String middleInitial;
  String gender;
  Integer tier;
  Date dob;
  String addressLine1;
  String addressLine2;
  String postalCode;
  String city;
  String stateProv;
  String country;
  String phone1;
  String phone2;
  String phone3;
  String eMail1;
  String eMail2;
  String status;
  long current_sK_Customer_id;
  Date effectiveDate;
  Date endDate;


  public static void copyData (DimCustomer src, DimCustomer dst) {
    dst.customerID = src.customerID;
    dst.taxID = src.taxID;
    dst.lastName = src.lastName;
    dst.firstName = src.firstName;
    dst.middleInitial = src.middleInitial;
    dst.gender = src.gender;
    dst.tier = src.tier;
    dst.dob = src.dob;
    dst.addressLine1 = src.addressLine1;
    dst.addressLine2 = src.addressLine2;
    dst.postalCode = src.postalCode;
    dst.city = src.city;
    dst.stateProv = src.stateProv;
    dst.country = src.country;
    dst.phone1 = src.phone1;
    dst.phone2 = src.phone2;
    dst.phone3 = src.phone3;
    dst.eMail1 = src.eMail1;
    dst.eMail2 = src.eMail2;
    dst.status = src.status;
  }

  public static final Finder<Long,DimCustomer> find = new Finder<>(Long.class, DimCustomer.class);

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public long getCustomerID() {
    return customerID;
  }

  public void setCustomerID(long customerID) {
    this.customerID = customerID;
  }

  public Date getDob() {
    return dob;
  }

  public void setDob(Date dob) {
    this.dob = dob;
  }

  public Date getEffectiveDate() {
    return effectiveDate;
  }

  public void setEffectiveDate(Date effectiveDate) {
    this.effectiveDate = effectiveDate;
  }

  public String geteMail1() {
    return eMail1;
  }

  public void seteMail1(String eMail1) {
    this.eMail1 = eMail1;
  }

  public String geteMail2() {
    return eMail2;
  }

  public void seteMail2(String eMail2) {
    this.eMail2 = eMail2;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public long getCurrent_sK_Customer_id() {
    return current_sK_Customer_id;
  }

  public void setCurrent_sK_Customer_id(long current_sK_Customer_id) {
    this.current_sK_Customer_id = current_sK_Customer_id;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMiddleInitial() {
    return middleInitial;
  }

  public void setMiddleInitial(String middleInitial) {
    this.middleInitial = middleInitial;
  }

  public String getPhone1() {
    return phone1;
  }

  public void setPhone1(String phone1) {
    this.phone1 = phone1;
  }

  public String getPhone2() {
    return phone2;
  }

  public void setPhone2(String phone2) {
    this.phone2 = phone2;
  }

  public String getPhone3() {
    return phone3;
  }

  public void setPhone3(String phone3) {
    this.phone3 = phone3;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public long getsK_CustomerID() {
    return sK_CustomerID;
  }

  public void setsK_CustomerID(long sK_CustomerID) {
    this.sK_CustomerID = sK_CustomerID;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

  public String getTaxID() {
    return taxID;
  }

  public void setTaxID(String taxID) {
    this.taxID = taxID;
  }

  public Integer getTier() {
    return tier;
  }

  public void setTier(Integer tier) {
    this.tier = tier;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
