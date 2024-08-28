package com.ericsson.eniq.events.server.services.mappers;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.ericsson.eniq.events.server.common.exception.ServiceException;
import com.ericsson.eniq.events.server.services.*;

public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {
    public Response toResponse(final ServiceException e) {
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
