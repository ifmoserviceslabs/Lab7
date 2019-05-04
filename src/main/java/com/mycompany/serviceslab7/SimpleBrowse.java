package com.mycompany.serviceslab7;

import java.util.List;
import org.apache.juddi.api_v3.AccessPointType;
import org.apache.juddi.v3.client.UDDIConstants;
import org.apache.juddi.v3.client.config.UDDIClient;
import org.apache.juddi.v3.client.transport.Transport;
import org.uddi.api_v3.AccessPoint;
import org.uddi.api_v3.AuthToken;
import org.uddi.api_v3.BindingTemplate;
import org.uddi.api_v3.BindingTemplates;
import org.uddi.api_v3.BusinessService;
import org.uddi.api_v3.CategoryBag;
import org.uddi.api_v3.Description;
import org.uddi.api_v3.DiscardAuthToken;
import org.uddi.api_v3.FindService;
import org.uddi.api_v3.GetAuthToken;
import org.uddi.api_v3.GetServiceDetail;
import org.uddi.api_v3.KeyedReference;
import org.uddi.api_v3.Name;
import org.uddi.api_v3.ServiceDetail;
import org.uddi.api_v3.ServiceInfo;
import org.uddi.api_v3.ServiceList;
import org.uddi.v3_service.UDDIInquiryPortType;
import org.uddi.v3_service.UDDISecurityPortType;

public class SimpleBrowse {

        private static UDDISecurityPortType security = null;
        private static UDDIInquiryPortType inquiry = null;

        public SimpleBrowse() {
                try {
        	// create a manager and read the config in the archive; 
                        // you can use your config file name
                        UDDIClient client = new UDDIClient("META-INF/uddi.xml");
        	// a UDDIClient can be a client to multiple UDDI nodes, so 
                        // supply the nodeName (defined in your uddi.xml.
                        // The transport can be WS, inVM, RMI etc which is defined in the uddi.xml
                        Transport transport = client.getTransport("default");
                        // Now you create a reference to the UDDI API
                        security = transport.getUDDISecurityService();
                        inquiry = transport.getUDDIInquiryService();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public String FindServiceAndReturnURL() {
            String url = "";    
            try {
                        String token = GetAuthKey("uddi", "uddi");
                        ServiceInfo serviceInfo = findService("IFMOTestService", token);
                        BusinessService service = getServiceInfo(serviceInfo.getServiceKey(), token);
                        PrintServiceDetail(service);
                        url = getServiceURL(service);
                        security.discardAuthToken(new DiscardAuthToken(token));
                        return url;
                } catch (Exception e) {
                        e.printStackTrace();
                }
            return url;
        }

        private ServiceInfo findService(String name, String token) throws Exception {
            FindService fs = new FindService();
            fs.setAuthInfo(token);
            org.uddi.api_v3.FindQualifiers fq = new org.uddi.api_v3.FindQualifiers();
            fq.getFindQualifier().add(UDDIConstants.APPROXIMATE_MATCH);
            fs.setFindQualifiers(fq);
            Name searchname = new Name();
            searchname.setValue(name);
            fs.getName().add(searchname);
            ServiceList foundServices = inquiry.findService(fs);
            List<ServiceInfo> services = foundServices.getServiceInfos().getServiceInfo();
            if(services.isEmpty())
                throw new Exception("Service with given name was not found");
            return services.get(0);
        }
        
        private String CatBagToString(CategoryBag categoryBag) {
                StringBuilder sb = new StringBuilder();
                if (categoryBag == null) {
                        return "no data";
                }
                for (int i = 0; i < categoryBag.getKeyedReference().size(); i++) {
                        sb.append(KeyedReferenceToString(categoryBag.getKeyedReference().get(i)));
                }
                for (int i = 0; i < categoryBag.getKeyedReferenceGroup().size(); i++) {
                        sb.append("Key Ref Grp: TModelKey=");
                        for (int k = 0; k < categoryBag.getKeyedReferenceGroup().get(i).getKeyedReference().size(); k++) {
                                sb.append(KeyedReferenceToString(categoryBag.getKeyedReferenceGroup().get(i).getKeyedReference().get(k)));
                        }
                }
                return sb.toString();
        }

        private String KeyedReferenceToString(KeyedReference item) {
                StringBuilder sb = new StringBuilder();
                sb.append("Key Ref: Name=").
                        append(item.getKeyName()).
                        append(" Value=").
                        append(item.getKeyValue()).
                        append(" tModel=").
                        append(item.getTModelKey()).
                        append(System.getProperty("line.separator"));
                return sb.toString();
        }

        private void PrintServiceDetail(BusinessService get) {
                if (get == null) {
                        return;
                }
                System.out.println("Name " + ListToString(get.getName()));
                System.out.println("Desc " + ListToDescString(get.getDescription()));
                System.out.println("Key " + (get.getServiceKey()));
                System.out.println("Cat bag " + CatBagToString(get.getCategoryBag()));
                if (!get.getSignature().isEmpty()) {
                        System.out.println("Item is digitally signed");
                } else {
                        System.out.println("Item is not digitally signed");
                }
                PrintBindingTemplates(get.getBindingTemplates());
        }

        private void PrintBindingTemplates(BindingTemplates bindingTemplates) {
                if (bindingTemplates == null) {
                        return;
                }
                for (int i = 0; i < bindingTemplates.getBindingTemplate().size(); i++) {
                        System.out.println("Binding Key: " + bindingTemplates.getBindingTemplate().get(i).getBindingKey());
                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint() != null) {
                                System.out.println("Access Point: " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getValue() + " type " + bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType());
                                if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType() != null) {
                                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.END_POINT.toString())) {
                                                System.out.println("Use this access point value as an invocation endpoint.");
                                        }
                                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.BINDING_TEMPLATE.toString())) {
                                                System.out.println("Use this access point value as a reference to another binding template.");
                                        }
                                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.WSDL_DEPLOYMENT.toString())) {
                                                System.out.println("Use this access point value as a URL to a WSDL document, which presumably will have a real access point defined.");
                                        }
                                        if (bindingTemplates.getBindingTemplate().get(i).getAccessPoint().getUseType().equalsIgnoreCase(AccessPointType.HOSTING_REDIRECTOR.toString())) {
                                                System.out.println("Use this access point value as an Inquiry URL of another UDDI registry, look up the same binding template there (usage varies).");
                                        }
                                }
                        }

                }
        }

        private enum AuthStyle {

                HTTP_BASIC,
                HTTP_DIGEST,
                HTTP_NTLM,
                UDDI_AUTH,
                HTTP_CLIENT_CERT
        }

        private String GetAuthKey(String username, String password) {
                try {

                        GetAuthToken getAuthTokenRoot = new GetAuthToken();
                        getAuthTokenRoot.setUserID(username);
                        getAuthTokenRoot.setCred(password);

                        // Making API call that retrieves the authentication token for the user.
                        AuthToken rootAuthToken = security.getAuthToken(getAuthTokenRoot);
                        System.out.println(username + " AUTHTOKEN = (don't log auth tokens!");
                        return rootAuthToken.getAuthInfo();
                } catch (Exception ex) {
                        System.out.println("Could not authenticate with the provided credentials " + ex.getMessage());
                }
                return null;
        }

        private String ListToString(List<Name> name) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < name.size(); i++) {
                        sb.append(name.get(i).getValue()).append(" ");
                }
                return sb.toString();
        }

        private String ListToDescString(List<Description> name) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < name.size(); i++) {
                        sb.append(name.get(i).getValue()).append(" ");
                }
                return sb.toString();
        }

        private BusinessService getServiceInfo(String serviceKey, String token) throws Exception {
            GetServiceDetail gsd = new GetServiceDetail();
            gsd.setAuthInfo(token);
            gsd.getServiceKey().add(serviceKey);
            ServiceDetail serviceDetail = inquiry.getServiceDetail(gsd);
            if(serviceDetail.getBusinessService().isEmpty())
                throw new Exception("Service with given key does not exist");
            return serviceDetail.getBusinessService().get(0);
        }
        
        private String getServiceURL(BusinessService service) throws Exception{
            for(BindingTemplate template: service.getBindingTemplates().getBindingTemplate()){
                AccessPoint accessPoint = template.getAccessPoint();
                if(accessPoint.getUseType().equalsIgnoreCase(AccessPointType.END_POINT.toString()))
                    return accessPoint.getValue();
            }
            throw new Exception("Endpoint address for given address was not found");
        }
}