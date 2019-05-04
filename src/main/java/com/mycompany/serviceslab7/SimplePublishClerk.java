package com.mycompany.serviceslab7;

import org.uddi.api_v3.*;
import org.apache.juddi.api_v3.*;
import org.apache.juddi.v3.client.config.UDDIClerk;
import org.apache.juddi.v3.client.config.UDDIClient;

public class SimplePublishClerk {

        private static UDDIClerk clerk = null;

        public SimplePublishClerk() {
                try {
                        UDDIClient uddiClient = new UDDIClient("META-INF/uddi.xml");
                        clerk = uddiClient.getClerk("default");
                        if (clerk==null)
                                throw new Exception("the clerk wasn't found, check the config file!");
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public void publish() {
                try {
                        // Creating the parent business entity that will contain our service.
                        BusinessEntity myBusEntity = new BusinessEntity();
                        Name myBusName = new Name();
                        myBusName.setValue("My Business");
                        myBusEntity.getName().add(myBusName);
                        // Adding the business entity to the "save" structure, using our publisher's authentication info and saving away.
                        BusinessEntity register = clerk.register(myBusEntity);
                        if (register == null) {
                                System.out.println("Save failed!");
                                System.exit(1);
                        }
                        String myBusKey = register.getBusinessKey();
                        System.out.println("myBusiness key:  " + myBusKey);

                        // Creating a service to save.  Only adding the minimum data: the parent business key retrieved from saving the business 
                        // above and a single name.
                        BusinessService myService = new BusinessService();
                        myService.setBusinessKey(myBusKey);
                        Name myServName = new Name();
                        myServName.setValue("IFMOTestService");
                        myService.getName().add(myServName);

                        // Add binding templates, etc...
                        BindingTemplate myBindingTemplate = new BindingTemplate();
                        AccessPoint accessPoint = new AccessPoint();
                        accessPoint.setUseType(AccessPointType.END_POINT.toString());
                        accessPoint.setValue("http://0.0.0.0:8081/EmployeeService");
                        myBindingTemplate.setAccessPoint(accessPoint);
                        BindingTemplates myBindingTemplates = new BindingTemplates();
                        //optional but recommended step, this annotations our binding with all the standard SOAP tModel instance infos
                        myBindingTemplate = UDDIClient.addSOAPtModels(myBindingTemplate);
                        myBindingTemplates.getBindingTemplate().add(myBindingTemplate);
                        myService.setBindingTemplates(myBindingTemplates);
                        // Adding the service to the "save" structure, using our publisher's authentication info and saving away.
                        BusinessService svc = clerk.register(myService);
                        if (svc==null){
                                System.out.println("Save failed!");
                                System.exit(1);
                        }
                        
                        String myServKey = svc.getServiceKey();
                        System.out.println("my Service key:  " + myServKey);

                        clerk.discardAuthToken();
                        // Now you have a business and service via 
                        // the jUDDI API!
                        System.out.println("Success!");

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}