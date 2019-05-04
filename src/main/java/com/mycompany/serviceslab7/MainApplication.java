package com.mycompany.serviceslab7;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class MainApplication {
    private static SOAPMessage createSoapRequest() throws Exception{
		 MessageFactory messageFactory = MessageFactory.newInstance();
		 SOAPMessage soapMessage = messageFactory.createMessage();
		 SOAPPart soapPart = soapMessage.getSOAPPart();
    	         SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
    	         soapEnvelope.addNamespaceDeclaration("ser", "http://serviceslab3maven.ifmo.com/");
		 SOAPBody soapBody = soapEnvelope.getBody();
		 SOAPElement soapElement = soapBody.addChildElement("ser:getEmployees");
		 soapMessage.saveChanges();
		 System.out.println("----------SOAP Request------------");
		 soapMessage.writeTo(System.out);
		 return soapMessage;
	 }
	 private static void createSoapResponse(SOAPMessage soapResponse) throws Exception  {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = soapResponse.getSOAPPart().getContent();
		System.out.println("\n----------SOAP Response-----------");
		StreamResult result = new StreamResult(System.out);
		transformer.transform(sourceContent, result);
	 }
         
         private static void callService(String url){
             try{
                    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                    SOAPConnection soapConnection = soapConnectionFactory.createConnection();
                    SOAPMessage soapRequest = createSoapRequest();
                    //hit soapRequest to the server to get response
                    SOAPMessage soapResponse = soapConnection.call(soapRequest, url);
                    createSoapResponse(soapResponse);
                    soapConnection.close();
		} catch (Exception e) {
		     e.printStackTrace();
		}
         }
         
         private static void publishService(){
            SimplePublishClerk sp = new SimplePublishClerk();
            sp.publish();
        }
         
         private static void findAndCallService(){
             SimpleBrowse b = new SimpleBrowse();
             String url = b.FindServiceAndReturnURL();
             callService(url);
         }
         
	 public static void main(String args[]){
            if (args.length == 0){
                System.out.println("There were no commandline arguments passed!");
                return;
            }
            if("publish".equals(args[0])){
                publishService();
            }
            else if("find".equals(args[0])){
                findAndCallService();
            }
	 }
}
