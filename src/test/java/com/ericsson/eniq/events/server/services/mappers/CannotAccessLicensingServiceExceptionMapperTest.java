package com.ericsson.eniq.events.server.services.mappers;

import com.ericsson.eniq.events.server.common.exception.CannotAccessLicensingServiceException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author edarbla
 */
public class CannotAccessLicensingServiceExceptionMapperTest extends ExceptionMapperBaseTest {

    @Test
    public void testCannotAccessLicenseManagerMapperException() {
        Exception e = new Exception("Cannot Access License Manager");
        CannotAccessLicensingServiceExceptionMapper mapper = new CannotAccessLicensingServiceExceptionMapper() ;
        Response response = mapper.toResponse(new CannotAccessLicensingServiceException(e));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Cannot Access Licensing Service:Cannot Access License Manager",response.getEntity());
        assertContentType(response, "text", "plain");
    }

}
