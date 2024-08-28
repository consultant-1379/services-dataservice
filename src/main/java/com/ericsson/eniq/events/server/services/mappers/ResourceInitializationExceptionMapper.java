/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2010 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.ericsson.eniq.events.server.templates.exception.ResourceInitializationException;

/**
 * @author etomcor
 * @since June 2010
 *
 */
@Provider
public class ResourceInitializationExceptionMapper implements ExceptionMapper<ResourceInitializationException> {
    public Response toResponse(final ResourceInitializationException e) {
        
        final StringBuilder sb = new StringBuilder();
        
        sb.append(e.getMessage() != null ? e.getMessage() : "");
        
        if (e.getCause() != null) {
            final String message = e.getCause().getMessage();
            sb.append(message != null ? " : "+message : "");
        } 

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(sb.toString()).type("text/plain").build();
    }
}
