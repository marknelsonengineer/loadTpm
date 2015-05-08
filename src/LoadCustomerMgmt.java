import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;

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
import java.util.logging.Logger;

/**
 * Implement a StAX Pull Parsing XML Parser.
 */

public class LoadCustomerMgmt {

  static XMLEventAllocator allocator;
  static final String filename = "/Users/mark/Desktop/TPC-DI/data/Batch1/CustomerMgmt.xml";


  /**
   * Process the CustomerMgmg.xml file.
   *
   * This XML file represents a historical customer file.  This processor will read the file and focus on two
   * record types NEW and UPDCUST.
   *
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void processCustomerMgmt() throws IOException, XMLStreamException {
    XMLInputFactory xmlif = XMLInputFactory.newInstance();

    xmlif.setEventAllocator(new XMLEventAllocatorImpl());
    allocator = xmlif.getEventAllocator();

    XMLStreamReader xmlr = xmlif.createXMLStreamReader(filename, new FileInputStream(filename));

    int eventType = xmlr.getEventType();

    while (xmlr.hasNext()) {
      eventType = xmlr.next();
      // Get all "Action" elements as XMLEvent object
      if (eventType == XMLStreamConstants.START_ELEMENT && xmlr.getLocalName().equals("Action")) {
        StartElement event = getXMLEvent(xmlr).asStartElement();
        // System.out.println ("EVENT: " + event.toString());

        Attribute attr = event.getAttributeByName(new QName("ActionType"));
        // System.out.println ("Action: " + attr.getValue());

        if (attr.getValue().equals("NEW")) {
          System.out.println ("Process new customer");

        }
        else if (attr.getValue().equals("UPDCUST")) {
          System.out.println ("Update customer");

        }
      }
    }

  }

  public static void main(String[] args) throws IOException, XMLStreamException {
    System.out.println("Start load TPM");

    processCustomerMgmt();

    System.out.println("End load TPM");
  }

  private static XMLEvent getXMLEvent(XMLStreamReader reader)
      throws XMLStreamException {
    return allocator.allocate(reader);
  }

}
