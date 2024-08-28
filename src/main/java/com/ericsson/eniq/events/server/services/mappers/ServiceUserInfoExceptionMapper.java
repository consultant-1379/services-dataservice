/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.events.server.services.mappers;

import com.ericsson.eniq.events.server.common.exception.ServiceUserInfoException;
import com.ericsson.eniq.events.server.utils.json.JSONUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author ejoegaf
 * @since 2011
 *
 */
@Provider
public class ServiceUserInfoExceptionMapper implements ExceptionMapper<ServiceUserInfoException> {

    /* (non-Javadoc)
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
     */
    @Override
    public Response toResponse(final ServiceUserInfoException e) {
        // check if there is a propagated exception
        String error = null;
        if (e.getCause() != null) {
            error = e.getCause().getMessage();
        } else {
            error = e.getMessage();
        }
        final String jsonResult = JSONUtils.createJSONErrorResult(error);
        return Response.status(Response.Status.OK).entity(jsonResult).type("text/plain").build();
    }

}
