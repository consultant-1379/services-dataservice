package com.ericsson.eniq.events.server.services.mappers;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Ignore;

@Ignore
public class ExceptionMapperBaseTest {
    static final int INTERNAL_SERVER_ERROR = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

    static final String CONTENT_TYPE_ID = "Content-Type";

    void assertContentType(final Response response, final String type, final String subType) {
        final MediaType mediaType = (MediaType) response.getMetadata().getFirst(CONTENT_TYPE_ID);
        assertEquals(type, mediaType.getType());
        assertEquals(subType, mediaType.getSubtype());
    }

}
