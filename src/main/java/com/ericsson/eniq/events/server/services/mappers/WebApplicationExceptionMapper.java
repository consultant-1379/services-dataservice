/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author etomcor
 * @since June 010
 *
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    public Response toResponse(final WebApplicationException e) {
        // check if there is a propagated exception
        String error = null;
        if (e.getCause() != null) {
            error = e.getCause().getMessage();
        } else {
            error = e.getMessage();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).type("text/plain").build();
    }
}
