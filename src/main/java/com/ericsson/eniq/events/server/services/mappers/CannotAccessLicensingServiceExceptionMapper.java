package com.ericsson.eniq.events.server.services.mappers;

import com.ericsson.eniq.events.server.common.exception.CannotAccessLicensingServiceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author: edarbla
 */
@Provider
public class CannotAccessLicensingServiceExceptionMapper implements ExceptionMapper<CannotAccessLicensingServiceException> {

    public Response toResponse(final CannotAccessLicensingServiceException e) {

        String error;
        if (e.getCause() != null) {
            error = "Cannot Access Licensing Service:" + e.getCause().getMessage();
        } else {
            error = "Cannot Access Licensing Service:" + e.getMessage();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).type("text/plain").build();
    }

}
