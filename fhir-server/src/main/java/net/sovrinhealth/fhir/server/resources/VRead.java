/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.resources;

import static net.sovrinhealth.fhir.server.util.IssueTypeToHttpStatusMapper.issueListToStatus;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationOutcome;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.persistence.SingleResourceResult;
import net.sovrinhealth.fhir.server.spi.operation.FHIRRestOperationResponse;
import net.sovrinhealth.fhir.server.util.FHIRRestHelper;
import net.sovrinhealth.fhir.server.util.RestAuditLogger;

@Path("/")
@Consumes({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
@Produces({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
@RolesAllowed("FHIRUsers")
@RequestScoped
public class VRead extends FHIRResource {
    private static final Logger log = java.util.logging.Logger.getLogger(VRead.class.getName());

    public VRead() throws Exception {
        super();
    }

    @GET
    @Path("{type}/{id}/_history/{vid}")
    public Response vread(@PathParam("type") String type, @PathParam("id") String id, @PathParam("vid") String vid) {
        log.entering(this.getClass().getName(), "vread(String,String,String)");
        Date startTime = new Date();
        Response.Status status = null;
        FHIRRestOperationResponse ior = null;

        try {
            checkInitComplete();
            checkType(type);

            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            SingleResourceResult<? extends Resource> srr = helper.doVRead(type, id, vid, queryParameters);
            if (srr.isSuccess()) {
                status = Status.OK;
                ResponseBuilder response = Response.ok().entity(srr.getResource());
                response = addETagAndLastModifiedHeaders(response, srr.getResource());
                return response.build();
            } else {
                OperationOutcome oo = srr.getOutcome();
                status = issueListToStatus(oo.getIssue());
                return exceptionResponse(oo, status);
            }
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logVersionRead(httpServletRequest,
                        ior != null ? ior.getResource() : null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "vread(String,String,String)");
        }
    }
}
