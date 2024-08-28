package com.ericsson.eniq.events.server.services.mappers;

import static org.junit.Assert.*;
import javax.ws.rs.core.*;

import com.ericsson.eniq.events.server.common.exception.NoLicenseException;
import org.junit.Test;


/**
 * @author edarbla
 * @since February 2012
 *
 */
public class NoLicenseExceptionMapperTest extends ExceptionMapperBaseTest {

    @Test
    public void testMapException() {
        NoLicenseExceptionMapper mapper = new NoLicenseExceptionMapper() ;
        Response response = mapper.toResponse(new NoLicenseException("No Licenses Found"));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Licensing Error:No Licenses Found",response.getEntity());
        assertContentType(response, "text", "plain");
    }

}
