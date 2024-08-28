/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.ericsson.eniq.events.server.templates.exception.ResourceInitializationException;
import com.ericsson.eniq.events.server.templates.utils.TemplateUtils;

/**
 * @author etomcor
 * @since June 2010
 *
 */
public class ResourseInitializationExceptionMapperTest extends ExceptionMapperBaseTest {
    @Test
    public void testMapExceptionWithCause() {
        final ResourceInitializationExceptionMapper mapper = new ResourceInitializationExceptionMapper();
        final Response response = mapper.toResponse(new ResourceInitializationException(
                "Failed to initialize resource", new IOException("File not found")));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Failed to initialize resource : File not found", response.getEntity());
        assertContentType(response, "text", "plain");
    }

    @Test
    public void testMapExceptionWithCauseNoMessage() {
        final ResourceInitializationExceptionMapper mapper = new ResourceInitializationExceptionMapper();
        final Response response = mapper.toResponse(new ResourceInitializationException(
                "Failed to initialize resource", new IOException()));
        assertNotNull(response);
        assertEquals(INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Failed to initialize resource", response.getEntity());
        assertContentType(response, "text", "plain");
    }

    @Test(expected = ResourceInitializationException.class)
    public void testTemplateDoesNotExist() {
        (new TemplateUtils()).getQueryFromTemplate("q_template_does_not_exist.vm".toString());
    }

}
