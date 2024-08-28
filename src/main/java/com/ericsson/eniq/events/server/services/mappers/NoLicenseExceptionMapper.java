package com.ericsson.eniq.events.server.services.mappers;

import com.ericsson.eniq.events.server.common.exception.NoLicenseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * @author edarbla
 * @since February 2012
 *
 */
@Provider
public class NoLicenseExceptionMapper implements ExceptionMapper<NoLicenseException> {

    public Response toResponse(final NoLicenseException e) {

        final String error = "Licensing Error:" + e.getMessage();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).type("text/plain").build();

    }

}
