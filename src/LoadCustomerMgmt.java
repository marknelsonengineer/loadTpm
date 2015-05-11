import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;
import model.DimAccount;
import model.DimCustomer;
import org.avaje.agentloader.AgentLoader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Process the CustomerMgmg.xml file.
 * <p>
 * This XML file represents a historical customer file.  This processor will read the file and focus on two
 * record types NEW and UPDCUST.
 *
 * @throws IOException
 * @throws XMLStreamException
 */
public class LoadCustomerMgmt {

  static final String filename = "/Users/mark/Desktop/TPC-DI/data/Batch1/CustomerMgmt.xml";
  static XMLEventAllocator allocator;
  static DimCustomer dimCustomer = null;
  static List<DimAccount> accounts = null;
  static DimAccount dimAccount = null;
  static String tagContent = null;
  static Mode mode = Mode.NULL;
  static String phone = null;
  static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
  static SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-M-d'T'H:m:s");
  static Date futureNullDate;
  static Date actionTimestamp;

  /**
   * Open an XML file and process the contents.
   *
   * @throws IOException
   * @throws XMLStreamException
   * @throws ParseException
   */
  public static void processCustomerMgmt() throws IOException, XMLStreamException, ParseException {
    futureNullDate = formatter.parse("2999-12-31");
    deleteDatabase();

    XMLInputFactory xmlif = XMLInputFactory.newInstance();

    xmlif.setEventAllocator(new XMLEventAllocatorImpl());
    allocator = xmlif.getEventAllocator();

    XMLStreamReader xmlr = xmlif.createXMLStreamReader(filename, new FileInputStream(filename));

    int eventType = xmlr.getEventType();

    while (xmlr.hasNext()) {
      eventType = xmlr.next();

      if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Action") && mode == Mode.NULL) {
        StartElement event = getXMLEvent(xmlr).asStartElement();

        Attribute attr = event.getAttributeByName(new QName("ActionType"));
        // System.out.println ("Action: " + attr.getValue());


        switch (attr.getValue()) {
          case "NEW":
            System.out.println("Start NEW");
            mode = Mode.NEW;
            break;
          case "ADDACCT":
            System.out.println("Start ADDACCT");
            mode = Mode.ADDACCT;
            break;
          case "UPDCUST":
            System.out.println("Start UPDCUST");
            mode = Mode.UPDCUST;
            break;
          case "UPDACCT":
            System.out.println("Start UPDACCT");
            mode = Mode.UPDACCT;
            break;
          case "CLOSEACCT":
            System.out.println("Start CLOSEACCT");
            mode = Mode.CLOSEACCT;
            break;
          case "INACT":
            System.out.println("Start INACT");
            mode = Mode.INACT;
            break;
          default:
            throw new IllegalArgumentException("Invalid attribute: [" + attr.getValue() + "]");
        }

        attr = event.getAttributeByName(new QName("ActionTS"));
        actionTimestamp = timeStamp.parse(attr.getValue());
      }

      if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("Action") && mode != Mode.NULL) {
        mode = Mode.NULL;
        actionTimestamp = null;
      }


      if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Customer") && mode != Mode.NULL) {
        StartElement event = getXMLEvent(xmlr).asStartElement();

        accounts = new ArrayList<DimAccount>();
        String customerID;

        Attribute attr = event.getAttributeByName(new QName("C_ID"));
        customerID = attr.getValue();
        System.out.println("  Customer ID = [" + customerID + "]");

        if (mode == Mode.NEW || mode == Mode.UPDCUST || mode == Mode.INACT) {
          dimCustomer = new DimCustomer();
          dimCustomer.setCustomerID(Long.parseLong(attr.getValue()));  // I think this gets moved into logic below...

          ////////
          // Kimballian Type 2 (New Customer Record)
          dimCustomer.setIsCurrent(true);
          dimCustomer.setEffectiveDate(actionTimestamp);
          dimCustomer.setEndDate(futureNullDate);
        }

        if (mode == Mode.ADDACCT || mode == Mode.UPDACCT || mode == Mode.CLOSEACCT) {
          dimCustomer = DimCustomer.find.where().eq("customer_id", customerID).eq("is_current", "1").findUnique();
        }

        // Copy the original row before making any changes...
        if (mode == Mode.UPDCUST || mode == Mode.INACT) {
          DimCustomer dimCustomerOld = DimCustomer.find.where().eq("customer_id", customerID).eq("is_current", "1").findUnique();
          DimCustomer.copyData(dimCustomerOld, dimCustomer);

          ////////
          // Kimballian Type 2 (Old Customer Record)
          dimCustomerOld.setIsCurrent(false);
          dimCustomerOld.setEndDate(actionTimestamp);
          dimCustomerOld.save();
          dimCustomerOld = null;
          System.out.println("  Copy old customer record");
        }

        if (mode == Mode.NEW) {
          dimCustomer.setStatus("Active");
        }

        if (mode == Mode.INACT) {
          dimCustomer.setStatus("Inactive");
        }

        attr = event.getAttributeByName(new QName("C_TAX_ID"));
        if (attr != null) {
          dimCustomer.setTaxID(attr.getValue());
        }

        attr = event.getAttributeByName(new QName("C_GNDR"));
        if (attr != null) {
          dimCustomer.setGender(attr.getValue());
        }

        attr = event.getAttributeByName(new QName("C_TIER"));
        if (attr != null) {
          if (!attr.getValue().trim().isEmpty()) {
            dimCustomer.setTier(Integer.parseInt(attr.getValue()));
          }
          else {
            dimCustomer.setTier(null);
          }
        }

        attr = event.getAttributeByName(new QName("C_DOB"));
        if (attr != null) {
          dimCustomer.setDob(formatter.parse(attr.getValue()));
        }

      } // START_ELEMENT "Customer"


      if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("Customer") && mode != Mode.NULL) {
        if (mode == Mode.NEW || mode == Mode.UPDCUST || mode == Mode.INACT) {
          dimCustomer.save();
          System.out.println("  Save customer  UID = [" + dimCustomer.getsK_CustomerID() + "]   Customer ID = [" + dimCustomer.getCustomerID() + "]");
        }

        // Iterate over accounts
        for (DimAccount account : accounts) {
          account.setsK_CustomerId(dimCustomer.getsK_CustomerID());
          account.save();
          System.out.println("    Save account   UID = [" + account.getsK_AccountId() + "]   Account ID = [" + account.getAccountID() + "]");
        }

        //for (DimAccount account : accounts) {  // I'm going for an explicit delete... just in case GC can't sort it out.
        //  accounts.remove(account);
        //}
        accounts = null;
        dimCustomer = null;
      } // END_ELEMENT "Customer"


      if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Account") && mode != Mode.NULL) {
        StartElement event = getXMLEvent(xmlr).asStartElement();

        Attribute attr = event.getAttributeByName(new QName("CA_ID"));
        String accountID = attr.getValue();
        System.out.println("    Account ID = [" + accountID + "]");

        // If we're here, we will always create a new account...
        dimAccount = new DimAccount();
        dimAccount.setAccountID(accountID);

        ////////
        // Kimballian Type 2 (New Account Record)
        dimAccount.setIsCurrent(true);
        dimAccount.setEffectiveDate(actionTimestamp);
        dimAccount.setEndDate(futureNullDate);

        if (mode == Mode.UPDACCT || mode == Mode.CLOSEACCT) {
          DimAccount dimAccountOld = DimAccount.find.where().eq("account_id", accountID).eq("is_current", "1").findUnique();
          DimAccount.copyData(dimAccountOld, dimAccount);

          ////////
          // Kimballian Type 2 (Old Account Record)
          dimAccountOld.setIsCurrent(false);
          dimAccountOld.setEndDate(actionTimestamp);
          dimAccountOld.save();
          dimAccountOld = null;
          System.out.println("    Copy old account record");
        }

        if (mode == Mode.NEW || mode == Mode.ADDACCT) {
          dimAccount.setStatus("Active");
        }

        if (mode == Mode.CLOSEACCT) {
          dimAccount.setStatus("Inactive");
        }

        attr = event.getAttributeByName(new QName("CA_TAX_ST"));
        if (attr != null) {
          dimAccount.setTaxStatus(attr.getValue());
        }

      } // START_ELEMENT "Account"


      if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("Account") && mode != Mode.NULL) {
        accounts.add(dimAccount);

        dimAccount = null;
      }


      if (eventType == XMLStreamConstants.CHARACTERS) {
        tagContent = xmlr.getText().trim();
      }

      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_L_NAME") && mode != Mode.NULL) {
        dimCustomer.setLastName(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_F_NAME") && mode != Mode.NULL) {
        dimCustomer.setFirstName(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_M_NAME") && mode != Mode.NULL) {
        dimCustomer.setMiddleInitial(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_ADLINE1") && mode != Mode.NULL) {
        dimCustomer.setAddressLine1(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_ADLINE2") && mode != Mode.NULL) {
        dimCustomer.setAddressLine2(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_ZIPCODE") && mode != Mode.NULL) {
        dimCustomer.setPostalCode(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_CITY") && mode != Mode.NULL) {
        dimCustomer.setCity(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_STATE_PROV") && mode != Mode.NULL) {
        dimCustomer.setStateProv(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_CTRY") && mode != Mode.NULL) {
        dimCustomer.setCountry(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_PRIM_EMAIL") && mode != Mode.NULL) {
        dimCustomer.seteMail1(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_ALT_EMAIL") && mode != Mode.NULL) {
        dimCustomer.seteMail2(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("CA_B_ID") && mode != Mode.NULL) {
        dimAccount.setBrokerId(tagContent);
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("CA_NAME") && mode != Mode.NULL) {
        dimAccount.setAccountDesc(tagContent);
      }
      else if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("C_PHONE_1") && mode != Mode.NULL) {
        phone = "";
      }
      else if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("C_PHONE_2") && mode != Mode.NULL) {
        phone = "";
      }
      else if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("C_PHONE_3") && mode != Mode.NULL) {
        phone = "";
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_CTRY_CODE") && mode != Mode.NULL) {
        if (!tagContent.trim().isEmpty()) {
          phone += "+" + tagContent;
        }
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_AREA_CODE") && mode != Mode.NULL) {
        if (!tagContent.trim().isEmpty()) {
          phone += " (" + tagContent + ")";
        }
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_LOCAL") && mode != Mode.NULL) {
        if (!tagContent.trim().isEmpty()) {
          phone += " " + tagContent;
        }
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_EXT") && mode != Mode.NULL) {
        if (!tagContent.trim().isEmpty()) {
          phone += " x" + tagContent;
        }
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_PHONE_1") && mode != Mode.NULL) {
        dimCustomer.setPhone1(phone.trim());
        phone = "";
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_PHONE_2") && mode != Mode.NULL) {
        dimCustomer.setPhone2(phone.trim());
        phone = "";
      }
      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("C_PHONE_3") && mode != Mode.NULL) {
        dimCustomer.setPhone3(phone.trim());
        phone = "";
      }

    }
  }

  public static void main(String[] args) throws IOException, XMLStreamException, ParseException {
    System.out.println("Start load TPM");

    ServerConfig config = new ServerConfig();
    config.setName("ics624");

    someApplicationBootupMethod();


    // Define DataSource parameters

    DataSourceConfig postgresDb = new DataSourceConfig();
    postgresDb.setDriver("oracle.jdbc.OracleDriver");
    postgresDb.setUsername("kimball");
    postgresDb.setPassword("Pougsat00");
    postgresDb.setUrl("jdbc:oracle:thin:@//10.0.1.23:1521/ics690");
    postgresDb.setHeartbeatSql("select * from dual;");

    config.setDataSourceConfig(postgresDb);

    // set DDL options...
    config.setDdlGenerate(true);
    config.setDdlRun(true);

    config.setDefaultServer(false);
    config.setRegister(false);

    //config.addPackage("model.*");
    config.addClass(DimCustomer.class);
    config.addClass(DimAccount.class);

// create the EbeanServer instance
    EbeanServer server = EbeanServerFactory.create(config);
    Ebean.register(server, true);


    DimCustomer x = new DimCustomer();
    x.setAddressLine1("I am Sam");
    Ebean.save(x);

    processCustomerMgmt();

    System.out.println("End load TPM");
  }

  private static XMLEvent getXMLEvent(XMLStreamReader reader)
      throws XMLStreamException {
    return allocator.allocate(reader);
  }

  /**
   * Load the agent into the running JVM process.
   */
  public static void someApplicationBootupMethod() {
    if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent", "debug=1;packages=model.**")) {
      System.out.println("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }

    System.out.println("avaje-ebeanorm-agent loaded");
  }

  static void deleteDatabase() {
    for (DimAccount c : DimAccount.find.all()) {
      c.delete();
    }
    for (DimCustomer c : DimCustomer.find.all()) {
      c.delete();
    }
    System.out.println("Database deleted");
  }


  enum Mode {NEW, UPDCUST, ADDACCT, UPDACCT, CLOSEACCT, INACT, NULL}


}
