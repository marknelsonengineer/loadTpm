import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;
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
import java.util.Date;


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

  enum Mode {NEW, UPDATE, NULL}

  static final String filename = "/Users/mark/Desktop/TPC-DI/data/Batch1/CustomerMgmt.xml";
  static XMLEventAllocator allocator;
  static DimCustomer dimCustomer = null;
  static DimCustomer dimCustomerOld = null;
  static String tagContent = null;
  static Mode mode = Mode.NULL;
  static String phone = null;

  static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
  static SimpleDateFormat timeStamp = new SimpleDateFormat("yyyy-M-d'T'H:m:s");
  static Date futureNullDate;
  static Date actionTimestamp;

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
    for (DimCustomer c : DimCustomer.find.all()) {
      c.delete();
    }
  }

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

        if (attr.getValue().equals("NEW")) {
          System.out.println("Start new customer");

          dimCustomer = new DimCustomer();
          mode = Mode.NEW;
        }
        else if (attr.getValue().equals("UPDCUST")) {
          System.out.println("Start Update customer");

          dimCustomer = new DimCustomer();
          mode = Mode.UPDATE;
        }

        attr = event.getAttributeByName(new QName("ActionTS"));
        actionTimestamp = timeStamp.parse(attr.getValue());
      }
      if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Customer") && mode != Mode.NULL) {
        StartElement event = getXMLEvent(xmlr).asStartElement();

        Attribute attr = event.getAttributeByName(new QName("C_ID"));
        System.out.println("Customer ID = [" + attr.getValue() + "]");
        dimCustomer.setCustomerID(Long.parseLong(attr.getValue()));

        if (mode == Mode.UPDATE) {
          dimCustomerOld = DimCustomer.find.where().eq("customer_id", attr.getValue()).eq("is_current", "1").findUnique();
          DimCustomer.copyData(dimCustomerOld, dimCustomer);

          System.out.println("Update customer ID = " + dimCustomerOld.getCustomerID() + "  " + dimCustomer.getCustomerID());

          ////////
          // Set Kimballian Type 2 (Old record)
          dimCustomerOld.setIsCurrent(false);
          dimCustomerOld.setEndDate(actionTimestamp);
          dimCustomerOld.save();
          dimCustomerOld = null;
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
          dimCustomer.setTier(Integer.parseInt(attr.getValue()));
        }

        attr = event.getAttributeByName(new QName("C_DOB"));
        if (attr != null) {
          dimCustomer.setDob(formatter.parse(attr.getValue()));
        }

        ////////
        // Set Kimballian Type 2 (New recrod)
        dimCustomer.setIsCurrent(true);
        dimCustomer.setEffectiveDate(actionTimestamp);
        dimCustomer.setEndDate(futureNullDate);

      }
      else if (eventType == XMLStreamConstants.CHARACTERS) {
        tagContent = xmlr.getText().trim();
      }

      else if (eventType == XMLStreamConstants.END_ELEMENT && xmlr.getLocalName().equals("Action") && mode != Mode.NULL) {
        dimCustomer.save();
        System.out.println("Saved");

        dimCustomer = null;
        mode = Mode.NULL;
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

}
