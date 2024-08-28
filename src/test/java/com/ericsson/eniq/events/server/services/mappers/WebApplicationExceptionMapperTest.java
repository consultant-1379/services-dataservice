/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import static org.junit.Assert.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;

import org.junit.Test;

import com.ericsson.eniq.events.server.services.*;
import com.ericsson.eniq.events.server.utils.DateTimeUtils;

/**
 * @author etomcor
 * @since June 2010
 *
 */
public class WebApplicationExceptionMapperTest extends ExceptionMapperBaseTest {

    private final String TEST_PARSE_DATE = "This is not a date";

    
    @Test
    public void testMapExceptionWithCause() {
        WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper() ;
        Response response = mapper.toResponse(new WebApplicationException(new IllegalArgumentException("web app exception")));
        
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("web app exception",(String)response.getEntity());

        assertContentType(response, "text", "plain");
    }

    @Test
    public void testMapExceptionWithoutCause() {
        WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper() ;
        Response response = mapper.toResponse(new WebApplicationException());
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertNull(response.getEntity());
        assertContentType(response, "text", "plain");
    }

    @Test(expected = WebApplicationException.class)
    public void testParseErrorCausesException() {
        DateTimeUtils.parseDateTimeFormat(TEST_PARSE_DATE);
    }

    @Test
    public void testParseExceptionPropagates() {
        try {
            DateTimeUtils.parseDateTimeFormat(TEST_PARSE_DATE);
        } catch (Exception e) {
            assertEquals(e.getMessage(), String.format("java.text.ParseException: Unparseable date: \"%s\"",
                    TEST_PARSE_DATE));
        }
    }
    
}
