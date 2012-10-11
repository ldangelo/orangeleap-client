package com.orangeleap.testws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import com.orangeleap.orangeleap.services3.OrangeLeap;
import com.orangeleap.orangeleap.services3.OrangeLeapService;
import com.orangeleap.testws.PWCallbackHandler;

public class WSClient  {
	
	public OrangeLeap getOrangeLeap(String wsdlUrl,String username, String password) {
		OrangeLeapService orangeLeapService;
		try {
			orangeLeapService = new OrangeLeapService(new URL(wsdlUrl)); //,new QName("http://www.orangeleap.com/orangeleap/services/1.0", "OrangeLeapService"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		OrangeLeap orangeLeapPort = orangeLeapService.getOrangeLeapSoap11();

    	
        Map outProps = new HashMap();
		Client client = org.apache.cxf.frontend.ClientProxy.getClient(orangeLeapPort);
		org.apache.cxf.endpoint.Endpoint cxfEndpoint = client.getEndpoint();
	
		PWCallbackHandler pwHandler = new PWCallbackHandler(username,password);
		outProps.put(WSHandlerConstants.ACTION,WSHandlerConstants.USERNAME_TOKEN);
		outProps.put(WSHandlerConstants.USER, username);
		outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		outProps.put(WSHandlerConstants.PW_CALLBACK_REF, pwHandler);
		outProps.put(WSHandlerConstants.ADD_UT_ELEMENTS,WSConstants.NONCE_LN + " " + WSConstants.CREATED_LN);

		WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
		cxfEndpoint.getOutInterceptors().add(wssOut);
		return orangeLeapPort;
	}
}

