/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import static org.junit.Assert.*;

import java.util.*;

import javax.ws.rs.core.*;

import com.ericsson.eniq.events.server.common.exception.ServiceException;
import org.junit.Test;

import com.ericsson.eniq.events.server.services.*;

/**
 * @author etomcor
 * @since June 2010
 *
 */
public class ServiceExceptionMapperTest extends ExceptionMapperBaseTest {
    @Test
    public void testMapExceptionWithCause() {
        ServiceExceptionMapper mapper = new ServiceExceptionMapper() ;
        Response response = mapper.toResponse(new ServiceException(new IllegalArgumentException("Bad argument")));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Bad argument",(String)response.getEntity());
        assertContentType(response, "text", "plain");
    }


    @Test
    public void testMapExceptionNoCause() {
        ServiceExceptionMapper mapper = new ServiceExceptionMapper() ;
        Response response = mapper.toResponse(new ServiceException("service error"));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("service error",(String)response.getEntity());
        assertContentType(response, "text", "plain");
    }
}
