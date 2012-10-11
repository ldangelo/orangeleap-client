package com.orangeleap.soap.client;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;
//import java.util.Formatter.DateTime;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import com.orangeleap.orangeleap.services3.FindConstituentsRequest;
import com.orangeleap.orangeleap.services3.FindConstituentsResponse;
import com.orangeleap.orangeleap.services3.OrangeLeap;
import com.orangeleap.orangeleap.services3.SaveOrUpdateConstituentRequest;
import com.orangeleap.orangeleap.services3.SaveOrUpdateGiftRequest;
import com.orangeleap.orangeleap.services3.SaveOrUpdateRecurringGiftRequest;
import com.orangeleap.orangeleap.typesv3.AbstractCustomizableEntity;
import com.orangeleap.orangeleap.typesv3.AbstractCustomizableEntity.CustomFieldMap;
import com.orangeleap.orangeleap.typesv3.AbstractCustomizableEntity.CustomFieldMap.Entry;
import com.orangeleap.orangeleap.typesv3.ActivationType;
import com.orangeleap.orangeleap.typesv3.Address;
import com.orangeleap.orangeleap.typesv3.CommunicationHistory;
import com.orangeleap.orangeleap.typesv3.Constituent;
import com.orangeleap.orangeleap.typesv3.CustomField;
import com.orangeleap.orangeleap.typesv3.DistributionLine;
import com.orangeleap.orangeleap.typesv3.Email;
import com.orangeleap.orangeleap.typesv3.Gift;
import com.orangeleap.orangeleap.typesv3.PaymentSource;
import com.orangeleap.orangeleap.typesv3.PaymentType;
import com.orangeleap.orangeleap.typesv3.RecurringGift;
import com.orangeleap.orangeleap.typesv3.Site;
import com.orangeleap.testws.WSClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;

/**
 * Hello world!
 *
 */
public class TestWS 
{
  private CommandLine cmd;
  private Options options;
  private Constituent constituents[];
  private String eventId;

  private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "wstest", options );		
		System.exit(-1);
	}

  public void init( String[] args )
  {
    options = new Options();

    options.addOption("h",false,"Display help message.");
    options.addOption("hostname",true,"Host name running Orange Leap Soap Service");
    options.addOption("port",true,"Port number running Orange Leap Soap Service");
    options.addOption("count",true,"Number of constituents to create");
    options.addOption("username",true,"User name");
    options.addOption("password",true,"Password");
    options.addOption("sitename",true,"Site name");
    options.addOption("eventid",true,"Event Id to attach");
    options.addOption("servicename",true,"Service name where OrangeLeap is running.");
	Parser p = new PosixParser();
		
    try {
      cmd = p.parse(options, args);
    } catch (ParseException e) {
      System.err.print(e.getLocalizedMessage());
      System.exit(-1);
    }
	
    if (cmd.hasOption("h")) {
      printHelp();
    } 
	

    try {
      String host = cmd.hasOption("hostname")? cmd.getOptionValue("hostname") : "localhost";
      String service = cmd.hasOption("servicename") ? cmd.getOptionValue("servicename") : "orangeleap";
      String port = cmd.hasOption("port") ? cmd.getOptionValue("port") : "8080";
      int n = cmd.hasOption("count") ? Integer.valueOf(cmd.getOptionValue("count")) : 3;


      constituents = new Constituent[n];

      String user = cmd.hasOption("username") ? cmd.getOptionValue("username") : "nolan";
      String site = cmd.hasOption("site") ? cmd.getOptionValue("site") : "company1";
      String password = cmd.hasOption("password") ? cmd.getOptionValue("password") : "ryan";
      eventId = cmd.hasOption("eventid") ? cmd.getOptionValue("eventid") : "1";
      
      
      String wsdlUrl = "http://"+host+":"+port+"/" + service + "/services3.1/orangeleap.wsdl";
      System.out.println(wsdlUrl);
      
      System.out.println("Started: "+new Date());
      long t0 = System.currentTimeMillis();
      
      WSClient client = new WSClient();
      
      OrangeLeap oleap = client.getOrangeLeap(wsdlUrl, user+"@"+site, password);
      
      for (int i = 0; i < n; i++) {
        
        Constituent constituent = testCreateConstituent(oleap, site, i);
        constituents[i] = constituent;
        Gift gift = testCreateGift(oleap, constituent, new BigDecimal(i));
        
        if (progress(i,n) != progress(i-1,n)) System.out.println(progress(i,n));
        
      }

      testE3ScenerioOne(oleap,constituents[0],new BigDecimal(200.00));
      testE3ScenerioTwo(oleap,constituents[0],new BigDecimal(6000.00));
      testE3ScenerioThree(oleap, constituents[0],new BigDecimal(5000.00));

      // find a constituent
      FindConstituentsRequest request = new FindConstituentsRequest();
      FindConstituentsResponse response = null;
      
      
      request.setFirstName("Leo");
      request.setLastName("DAngelo");
      response = oleap.findConstituents(request);
      
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof SOAPFaultException) {
        SOAPFaultException sfe = (SOAPFaultException)e;
        Throwable t = ((SOAPFaultException)e).getCause();
        SOAPFault fault = sfe.getFault();
        
        if (fault.hasDetail()) {
          Detail d = fault.getDetail();
          Iterator<DetailEntry> it = d.getDetailEntries();
          while (it.hasNext()) {
            DetailEntry dte = it.next();
            System.out.println(dte.getTextContent());
          }
        }
        
      }
    }
  }
  
  private int progress(int i, int n) {
    return (int)Math.round(i*1f/n*100);
  }
  
  private Constituent testCreateConstituent(OrangeLeap oleap, String siteName, int index) {
    
    Constituent constituent = new Constituent();
    Site site = new Site();
    site.setName(siteName);
	
    constituent.setFirstName("John "+index);
    constituent.setLastName("Smith "+index+"."+Math.round(Math.random()*10000));
    constituent.setConstituentType("individual");
    constituent.setSite(site);
    constituent.setCustomFieldMap(new CustomFieldMap());
    
    addAddress(constituent, "100 Main Street, Ste. "+index);
    addEmail(constituent, "john"+index+"@example.com");
    
    setCustomField(constituent, "individual.birthDate", "01/01/2001");
	
    SaveOrUpdateConstituentRequest request = new SaveOrUpdateConstituentRequest();
    request.setConstituent(constituent);
    
    return oleap.saveOrUpdateConstituent(request).getConstituent();
	
  }
  
  
  private void addEmail(Constituent constituent, String emailAddress) {
    
    Email email = new Email();
	
    email.setEmailAddress(emailAddress);
    email.setPrimary(true);  
    email.setActivationStatus(ActivationType.PERMANENT);
    email.setReceiveCorrespondence(true);
    email.setEmailDisplay(emailAddress);
    email.setCustomFieldMap(new CustomFieldMap());
    
    constituent.getEmails().add(email);
	
  }
  
  private void addAddress(Constituent constituent, String addressLine1) {
    
    Address address = new Address();
	
    address.setAddressLine1(addressLine1);
    address.setCity("Dallas");
    address.setStateProvince("TX");
    address.setCountry("US");
    address.setPostalCode("75001");
    address.setCustomFieldMap(new CustomFieldMap());
    address.setPrimary(true);
    address.setActivationStatus(ActivationType.PERMANENT);
    address.setReceiveCorrespondence(true);
    
    constituent.getAddresses().add(address);
  }
  
  
  
  private Gift testCreateGift(OrangeLeap oleap, Constituent constituent, BigDecimal amt) {
    
    amt = amt.add(new BigDecimal(100));
	
    Gift gift = new Gift();
    Address address = new Address();
    PaymentSource paymentSource = new PaymentSource();
    CustomFieldMap fieldMap = new CustomFieldMap();
	
    gift.setCustomFieldMap(fieldMap);
    address.setAddressLine1("123 Main Street");
    address.setCity("San Diego");
    address.setStateProvince("CA");
    address.setPostalCode("92121");
    address.setCountry("USA");
    address.setInactive(false);
	
    paymentSource.setConstituentId(constituent.getConstituentId());
    paymentSource.setPaymentType(PaymentType.CREDIT_CARD);
    paymentSource.setCreditCardHolderName("LEO DANGELO");
    paymentSource.setCreditCardType("Visa");
    paymentSource.setCreditCardNumber("4111111111111111");
    paymentSource.setCreditCardExpirationYear(2013);
    paymentSource.setCreditCardExpirationMonth(1);
    paymentSource.setCreditCardSecurityCode("199");
    //		paymentSource.setAddress(address);
    
    gift.setAmount(amt);
    gift.setConstituentId(constituent.getConstituentId());
    gift.setDeductible(false);
    gift.setDeductibleAmount(amt);
    gift.setPaymentSource(paymentSource);
    gift.setCurrencyCode("USD");
    gift.setPaymentType(PaymentType.CREDIT_CARD);
    //		gift.setAddress(address);
    Site site = new Site();
    site.setName(constituent.getSite().getName());
    gift.setSite(site);
	
    DistributionLine dLine = new DistributionLine();
    dLine.setCustomFieldMap(fieldMap);
    dLine.setAmount(amt);
    dLine.setPercentage(new BigDecimal(100.0));
    dLine.setProjectCode("generalFundPresident");
    gift.getDistributionLines().add(dLine);
	
    setCustomField(gift,"giftCategory","Cash");
	
    SaveOrUpdateGiftRequest request = new SaveOrUpdateGiftRequest();
    request.setGift(gift);
    request.setConstituentId(constituent.getConstituentId());
    
    return oleap.saveOrUpdateGift(request).getGift();
	
  }	

  //
  // Deposit 200.00 bind a constituent and a trip
  private Gift testE3ScenerioOne(OrangeLeap oleap, Constituent constituent, BigDecimal amt) {
    
    //    amt = amt.add(new BigDecimal(200));
	
    Gift gift = new Gift();
    Address address = new Address();
    PaymentSource paymentSource = new PaymentSource();
    CustomFieldMap fieldMap = new CustomFieldMap();
	
    gift.setCustomFieldMap(fieldMap);
    address.setAddressLine1("123 Main Street");
    address.setCity("San Diego");
    address.setStateProvince("CA");
    address.setPostalCode("92121");
    address.setCountry("USA");
    address.setInactive(false);
	
    paymentSource.setConstituentId(constituent.getConstituentId());
    paymentSource.setPaymentType(PaymentType.CREDIT_CARD);
    paymentSource.setCreditCardHolderName("LEO DANGELO");
    paymentSource.setCreditCardType("Visa");
    paymentSource.setCreditCardNumber("4111111111111111");
    paymentSource.setCreditCardExpirationYear(2013);
    paymentSource.setCreditCardExpirationMonth(1);
    paymentSource.setCreditCardSecurityCode("199");
    //		paymentSource.setAddress(address);
    
    gift.setAmount(amt);
    gift.setConstituentId(constituent.getConstituentId());
    gift.setDeductible(false);
    gift.setDeductibleAmount(amt);
    gift.setPaymentSource(paymentSource);
    gift.setCurrencyCode("USD");
    gift.setPaymentType(PaymentType.CREDIT_CARD);
    //		gift.setAddress(address);
    Site site = new Site();
    site.setName(constituent.getSite().getName());
    gift.setSite(site);
	
    DistributionLine dLine = new DistributionLine();
    dLine.setCustomFieldMap(fieldMap);
    dLine.setAmount(amt);
    dLine.setPercentage(new BigDecimal(100.0));
    dLine.setProjectCode("generalFundPresident");
    setCustomField(dLine,"event",eventId);
    gift.getDistributionLines().add(dLine);
	
    setCustomField(gift,"giftCategory","Cash");
	
    SaveOrUpdateGiftRequest request = new SaveOrUpdateGiftRequest();
    request.setGift(gift);
    request.setConstituentId(constituent.getConstituentId());
    
    return oleap.saveOrUpdateGift(request).getGift();
	
  }	
  
  //
  // Deposit 6000.00 bind a constituent and a trip per person
  private Gift testE3ScenerioTwo(OrangeLeap oleap, Constituent constituent, BigDecimal amt) {
    
    //    amt = amt.add(new BigDecimal(6000.00));
	
    Gift gift = new Gift();
    Address address = new Address();
    PaymentSource paymentSource = new PaymentSource();
    CustomFieldMap fieldMap = new CustomFieldMap();
	
    gift.setCustomFieldMap(fieldMap);
    address.setAddressLine1("123 Main Street");
    address.setCity("San Diego");
    address.setStateProvince("CA");
    address.setPostalCode("92121");
    address.setCountry("USA");
    address.setInactive(false);
	
    paymentSource.setConstituentId(constituent.getConstituentId());
    paymentSource.setPaymentType(PaymentType.CREDIT_CARD);
    paymentSource.setCreditCardHolderName("LEO DANGELO");
    paymentSource.setCreditCardType("Visa");
    paymentSource.setCreditCardNumber("4111111111111111");
    paymentSource.setCreditCardExpirationYear(2013);
    paymentSource.setCreditCardExpirationMonth(1);
    paymentSource.setCreditCardSecurityCode("199");
    //		paymentSource.setAddress(address);
    
    gift.setAmount(amt);
    gift.setConstituentId(constituent.getConstituentId());
    gift.setDeductible(false);
    gift.setDeductibleAmount(amt);
    gift.setPaymentSource(paymentSource);
    gift.setCurrencyCode("USD");
    gift.setPaymentType(PaymentType.CREDIT_CARD);
    //		gift.setAddress(address);
    Site site = new Site();
    site.setName(constituent.getSite().getName());
    gift.setSite(site);
	
    //
    // first distribution line
    DistributionLine dLine = new DistributionLine();
    dLine.setCustomFieldMap(fieldMap);
    dLine.setAmount(amt.divide(new BigDecimal(3.0)));
    dLine.setPercentage(new BigDecimal(100.0/3.0));
    dLine.setProjectCode("generalFundPresident");

    // attach to an event (trip)
    setCustomField(dLine,"event",eventId);  

    // make the gift "on behalf of another constituent"
    setCustomField(dLine,"reference",constituents[0].getId().toString());

    gift.getDistributionLines().add(dLine);

    //
    // second distribution line
    DistributionLine dLine2 = new DistributionLine();
    dLine2.setCustomFieldMap(fieldMap);
    dLine2.setAmount(amt.divide(new BigDecimal(3.0)));
    dLine2.setPercentage(new BigDecimal(100.0/3.0));
    dLine2.setProjectCode("generalFundPresident");

    // attach to an event (trip)
    setCustomField(dLine2,"event",eventId);

    // make the gift "on behalf of another constituent"
    setCustomField(dLine2,"reference",constituents[1].getId().toString());

    gift.getDistributionLines().add(dLine2);

    //
    // third distribution line
    DistributionLine dLine3 = new DistributionLine();
    dLine3.setCustomFieldMap(fieldMap);
    dLine3.setAmount(amt.divide(new BigDecimal(3.0)));
    dLine3.setPercentage(new BigDecimal(100.0/3.0));
    dLine3.setProjectCode("generalFundPresident");
    
    // attach to an event
    setCustomField(dLine3,"event",eventId);

    // make the gift "on behalf of another constituent"
    setCustomField(dLine3,"reference",constituents[2].getId().toString());

    gift.getDistributionLines().add(dLine3);
	
    setCustomField(gift,"giftCategory","Cash");
	
    SaveOrUpdateGiftRequest request = new SaveOrUpdateGiftRequest();
    request.setGift(gift);
    request.setConstituentId(constituent.getConstituentId());
    
    return oleap.saveOrUpdateGift(request).getGift();
	
  }	

  //
  // Deposit 6000.00 bind a constituent and a trip per person
  private RecurringGift testE3ScenerioThree(OrangeLeap oleap, Constituent constituent, BigDecimal amt) throws DatatypeConfigurationException {
    
    //    amt = amt.add(new BigDecimal(6000.00));
	
    RecurringGift gift = new RecurringGift();
    Address address = new Address();
    PaymentSource paymentSource = new PaymentSource();
    CustomFieldMap fieldMap = new CustomFieldMap();
	
    gift.setCustomFieldMap(fieldMap);
    address.setAddressLine1("123 Main Street");
    address.setCity("San Diego");
    address.setStateProvince("CA");
    address.setPostalCode("92121");
    address.setCountry("USA");
    address.setInactive(false);
	
    paymentSource.setConstituentId(constituent.getConstituentId());
    paymentSource.setPaymentType(PaymentType.CREDIT_CARD);
    paymentSource.setCreditCardHolderName("LEO DANGELO");
    paymentSource.setCreditCardType("Visa");
    paymentSource.setCreditCardNumber("4111111111111111");
    paymentSource.setCreditCardExpirationYear(2013);
    paymentSource.setCreditCardExpirationMonth(1);
    paymentSource.setCreditCardSecurityCode("199");
    //		paymentSource.setAddress(address);
    Date today = new Date();
    DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
    XMLGregorianCalendar cal = dataTypeFactory.newXMLGregorianCalendarDate(today.getYear(),today.getMonth(),today.getDay(),today.getTimezoneOffset());
    cal.setMinute(0);
    cal.setHour(12);
    cal.setSecond(0);

    Duration fiveMonths = dataTypeFactory.newDurationYearMonth(true,cal.getYear(),5);
    gift.setAmountPerGift(amt.divide(new BigDecimal(5)));
    gift.setFrequency("monthly");
    gift.setConstituentId(constituent.getConstituentId());
    gift.setStartDate(cal);

    //
    // advance 5 months
    cal.add(fiveMonths);

    gift.setEndDate(cal);
    gift.setInactive(false);
    gift.setPaymentSource(paymentSource);
    gift.setCurrencyCode("USD");
    gift.setPaymentType(PaymentType.CREDIT_CARD);
    //		gift.setAddress(address);

    //
    // first distribution line
    DistributionLine dLine = new DistributionLine();
    dLine.setCustomFieldMap(fieldMap);
    dLine.setAmount(amt.divide(new BigDecimal(5.0)));
    dLine.setPercentage(new BigDecimal(100.0));
    dLine.setProjectCode("generalFundPresident");

    // attach to an event (trip)
    setCustomField(dLine,"event",eventId);  

    gift.getDistributionLines().add(dLine);

    SaveOrUpdateRecurringGiftRequest request = new SaveOrUpdateRecurringGiftRequest();
    request.setRecurringgift(gift);
    request.setConstituentId(constituent.getConstituentId());
    
    return oleap.saveOrUpdateRecurringGift(request).getRecurringgift();
	
  }	
    
  
  private void setCustomField(Constituent constituent, String name, String value) {
    
    CustomFieldMap fieldMap = constituent.getCustomFieldMap();
	
    CustomField cf = new CustomField();
    cf.setName(name);
    cf.setValue(value);
    cf.setEntityType("constituent");
    cf.setEntityId(constituent.getId());
    cf.setSequenceNumber(0);
	
    
    boolean found = false;
    List<Entry> list = fieldMap.getEntry();
    for (Entry entry:list) {
      if (entry.getKey().equals(name)) {
        entry.setValue(cf);
        found = true;
      }
    }
    if (!found) {
      Entry entry = new Entry();
      entry.setKey(name);
      entry.setValue(cf);
      fieldMap.getEntry().add(entry);
    }
	
  }	
  
  private void setCustomField(DistributionLine dLine, String name, String value) {
    CustomFieldMap fieldMap = dLine.getCustomFieldMap();
	
    CustomField cf = new CustomField();
    cf.setName(name);
    cf.setValue(value);
    cf.setEntityType("distributionline");
    cf.setEntityId(dLine.getId());
    cf.setSequenceNumber(0);
	
    
    boolean found = false;
    List<Entry> list = fieldMap.getEntry();
    for (Entry entry:list) {
      if (entry.getKey().equals(name)) {
        entry.setValue(cf);
        found = true;
      }
    }
    if (!found) {
      Entry entry = new Entry();
      entry.setKey(name);
      entry.setValue(cf);
      fieldMap.getEntry().add(entry);
    }
  }

  private void setCustomField(Gift gift, String name, String value) {
    
    CustomFieldMap fieldMap = gift.getCustomFieldMap();
	
    CustomField cf = new CustomField();
    cf.setName(name);
    cf.setValue(value);
    cf.setEntityType("gift");
    cf.setEntityId(gift.getId());
    cf.setSequenceNumber(0);
	
    
    boolean found = false;
    List<Entry> list = fieldMap.getEntry();
    for (Entry entry:list) {
      if (entry.getKey().equals(name)) {
        entry.setValue(cf);
        found = true;
      }
    }
    if (!found) {
      Entry entry = new Entry();
      entry.setKey(name);
      entry.setValue(cf);
      fieldMap.getEntry().add(entry);
    }
	
  }	
}
