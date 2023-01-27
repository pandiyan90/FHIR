/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.resources;

import static net.sovrinhealth.fhir.server.util.FHIRRestHelper.getRequestBaseUri;
import static net.sovrinhealth.fhir.server.util.IssueTypeToHttpStatusMapper.issueListToStatus;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import net.sovrinhealth.fhir.config.FHIRRequestContext;
import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.core.HTTPReturnPreference;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.Resource;
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
public class Create extends FHIRResource {
    private static final Logger log = java.util.logging.Logger.getLogger(Create.class.getName());

    /**
     * This HL7-defined extension header supports "conditional create", allowing a client to create a new resource only if some equivalent
     * resource does not already exist on the server.
     * The client defines what equivalence means in this case by supplying a FHIR search query using an HL7 defined extension header "If-None-Exist" as shown:
     * <pre>
     * If-None-Exist: [search parameters]
     * </pre>
     * The header value matches the FHIR search syntax (what would be in the URL following the "?").
     */
    private static final String HEADERNAME_IF_NONE_EXIST = "If-None-Exist";

    public Create() throws Exception {
        super();
    }

    @POST
    @Path("{type}")
    public Response create(@PathParam("type") String type, Resource resource, @HeaderParam(HEADERNAME_IF_NONE_EXIST) String ifNoneExist) {
        log.entering(this.getClass().getName(), "create(String,Resource)");
        Date startTime = new Date();
        Response.Status status = null;
        FHIRRestOperationResponse ior = null;

        try {
            checkInitComplete();
            checkType(type);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            ior = helper.doCreate(type, resource, ifNoneExist);

            ResponseBuilder response =
                    Response.created(toUri(buildAbsoluteUri(getRequestBaseUri(type), ior.getLocationURI().toString())));
            resource = ior.getResource();

            HTTPReturnPreference returnPreference = FHIRRequestContext.get().getReturnPreference();
            if (resource != null && HTTPReturnPreference.REPRESENTATION == returnPreference) {
                response.entity(resource);
            } else if (ior.getOperationOutcome() != null && HTTPReturnPreference.OPERATION_OUTCOME == returnPreference) {
                response.entity(ior.getOperationOutcome());
            }
            response = addETagAndLastModifiedHeaders(response, resource);
            status = ior.getStatus();
            response.status(status);

            return response.build();
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logCreate(httpServletRequest,
                        ior != null ? ior.getResource() : null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "create(String,Resource)");
        }
    }
}
