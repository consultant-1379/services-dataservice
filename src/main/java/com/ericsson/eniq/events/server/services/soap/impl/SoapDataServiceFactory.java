/*
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.eniq.events.server.services.soap.impl;


import javax.ejb.Stateless;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * @author ericker
 */
@Stateless
public class SoapDataServiceFactory {

    public <T> T getQueryService(final URL wsdlLocation, final QName serviceName, final QName portName, final Class<T> queryAsAServiceSoapClass) {
        final Service s = new SoapService(wsdlLocation, serviceName);

        return s.getPort(portName, queryAsAServiceSoapClass);
    }

    private class SoapService extends Service {
        public SoapService(URL wsdlDocumentLocation, QName serviceName) {
            super(wsdlDocumentLocation, serviceName);
        }
    }
}
