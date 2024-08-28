/*
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */

package com.ericsson.eniq.events.server.services.soap;


import javax.xml.namespace.QName;
import java.net.URL;

/**
 * @author ericker
 */
public interface SoapDataService {

    <T> T getQueryService(URL wsdlLocation, QName serviceName, QName portName, Class<T> queryAsAServiceSoapClass);
}
