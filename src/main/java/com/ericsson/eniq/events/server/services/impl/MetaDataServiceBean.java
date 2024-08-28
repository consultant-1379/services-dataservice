package com.ericsson.eniq.events.server.services.impl;

import static com.ericsson.eniq.events.server.common.ApplicationConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import com.ericsson.eniq.events.server.auth.rbac.AccessControlInterceptor;
import com.ericsson.eniq.events.server.licensing.LicensingInterceptor;
import com.ericsson.eniq.events.server.services.MetaDataService;

/**
 * The intension is that this EJB will return valid JSON metadata string for a valid GUI request.
 *
 * @author edeccox
 * @author estepdu
 * @author ehaoswa
 * @author eromsza
 *
 * @since Mar 2010
 */

@Stateless
//@TransactionManagement(TransactionManagementType.BEAN)
@Local(MetaDataService.class)
@Interceptors({ LicensingInterceptor.class, AccessControlInterceptor.class })
public class MetaDataServiceBean implements MetaDataService {

    @Override
    public String getUIMetaData() throws IOException {
        return readFileAsString(METADATA_FILE);
    }

    /**
     * Reads in a file from class path.
     *
     * @param filePath      name of file to open. The file can reside
     *                      anywhere in the class path
     */
    private String readFileAsString(final String filePath) throws java.io.IOException {
        final StringBuilder fileData = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
                .getResourceAsStream(filePath)));
        final char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

}
