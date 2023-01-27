/*
 * (C) Copyright IBM Corp. 2016, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.server.resources;

import static net.sovrinhealth.fhir.server.spi.operation.FHIROperationUtil.checkAndVerifyOperationAllowed;
import static net.sovrinhealth.fhir.server.util.FHIRRestHelper.getRequestBaseUri;
import static net.sovrinhealth.fhir.server.util.IssueTypeToHttpStatusMapper.issueListToStatus;

import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import net.sovrinhealth.fhir.core.FHIRMediaType;
import net.sovrinhealth.fhir.exception.FHIROperationException;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.model.resource.Resource;
import net.sovrinhealth.fhir.model.util.FHIRUtil;
import net.sovrinhealth.fhir.server.spi.operation.FHIROperationContext;
import net.sovrinhealth.fhir.server.util.FHIRRestHelper;
import net.sovrinhealth.fhir.server.util.RestAuditLogger;

@Path("/")
@Consumes({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
@Produces({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
@RolesAllowed({"FHIRUsers", "FHIROperationAdmin"})
@RequestScoped
public class Operation extends FHIRResource {
    private static final Logger log = java.util.logging.Logger.getLogger(Operation.class.getName());

    public Operation() throws Exception {
        super();
    }

    @Context
    protected HttpHeaders httpHeaders;

    @Context
    protected SecurityContext securityContext;

    @GET
    @Path("${operationName}")
    public Response invoke(@PathParam("operationName") String operationName) {
        log.entering(this.getClass().getName(), "invoke(String)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createSystemOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.GET);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, null, null, null,
                    null, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, null, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, null, null, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String)");
        }
    }

    @POST
    @Path("${operationName}")
    public Response invoke(@PathParam("operationName") String operationName, Resource resource) {
        log.entering(this.getClass().getName(), "invoke(String,Resource)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createSystemOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, "POST");
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, null, null, null,
                    resource, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, null, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, null, null, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,Resource)");
        }
    }

    @DELETE
    @Path("${operationName}")
    public Response invokeDelete(@PathParam("operationName") String operationName) {
        // Support for calling a HTTP DELETE for a System-Level Operation calls.

        log.entering(this.getClass().getName(), "invokeDelete(String)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext = FHIROperationContext.createSystemOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, "DELETE");
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result =
                    helper.doInvoke(operationContext, null, null, null, null, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, null, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, null, null, null, startTime, new Date(),
                        status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,Resource)");
        }
    }

    @GET
    @Path("{resourceTypeName}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("operationName") String operationName) {
        log.entering(this.getClass().getName(), "invoke(String,String)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createResourceTypeOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.GET);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, null, null,
                    null, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, null, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String)");
        }
    }

    @POST
    @Path("{resourceTypeName}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("operationName") String operationName, Resource resource) {
        log.entering(this.getClass().getName(), "invoke(String,String,Resource)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createResourceTypeOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.POST);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, null, null,
                    resource, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, null, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String,Resource)");
        }
    }

    @GET
    @Path("{resourceTypeName}/{logicalId}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("logicalId") String logicalId,
            @PathParam("operationName") String operationName) {
        log.entering(this.getClass().getName(), "invoke(String,String,String)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createInstanceOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.GET);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, logicalId, null,
                    null, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, logicalId, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String,String)");
        }
    }

    @POST
    @Path("{resourceTypeName}/{logicalId}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("logicalId") String logicalId,
            @PathParam("operationName") String operationName, Resource resource) {
        log.entering(this.getClass().getName(), "invoke(String,String,String,Resource)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createInstanceOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.POST);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, logicalId, null,
                    resource, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, logicalId, null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String,String,Resource)");
        }
    }

    @GET
    @Path("{resourceTypeName}/{logicalId}/_history/{versionId}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("logicalId") String logicalId,
            @PathParam("versionId") String versionId,
            @PathParam("operationName") String operationName) {
        log.entering(this.getClass().getName(), "invoke(String,String,String,String)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createInstanceOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.GET);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, logicalId, versionId,
                    null, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, logicalId, versionId,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String,String,String)");
        }
    }

    @POST
    @Path("{resourceTypeName}/{logicalId}/_history/{versionId}/${operationName}")
    public Response invoke(@PathParam("resourceTypeName") String resourceTypeName,
            @PathParam("logicalId") String logicalId,
            @PathParam("versionId") String versionId,
            @PathParam("operationName") String operationName, Resource resource) {
        log.entering(this.getClass().getName(), "invoke(String,String,String,String,Resource)");
        Date startTime = new Date();
        Response.Status status = null;

        try {
            checkInitComplete();
            checkType(resourceTypeName);
            checkAndVerifyOperationAllowed(operationName);

            FHIROperationContext operationContext =
                    FHIROperationContext.createInstanceOperationContext(operationName);
            operationContext.setProperty(FHIROperationContext.PROPNAME_URI_INFO, uriInfo);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_HEADERS, httpHeaders);
            operationContext.setProperty(FHIROperationContext.PROPNAME_SECURITY_CONTEXT, securityContext);
            operationContext.setProperty(FHIROperationContext.PROPNAME_HTTP_REQUEST, httpServletRequest);
            operationContext.setProperty(FHIROperationContext.PROPNAME_METHOD_TYPE, HttpMethod.POST);

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl(), getSearchHelper(), getFhirVersion());
            Resource result = helper.doInvoke(operationContext, resourceTypeName, logicalId, versionId,
                    resource, uriInfo.getQueryParameters());
            Response response = buildResponse(operationContext, resourceTypeName, result);
            status = Response.Status.fromStatusCode(response.getStatus());
            return response;
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logOperation(httpServletRequest, operationName, resourceTypeName, logicalId, versionId,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "invoke(String,String,String,String,Resource)");
        }
    }

    private Response buildResponse(FHIROperationContext operationContext, String resourceTypeName, Resource resource)
            throws Exception {
        // The following code allows the downstream application to change the response code
        // This enables the 202 accepted to be sent back
        Response.Status status = Response.Status.OK;
        Object o = operationContext.getProperty(FHIROperationContext.PROPNAME_STATUS_TYPE);
        if (o != null) {
            if (o instanceof Integer) {
                Object ox = operationContext.getProperty(FHIROperationContext.PROPNAME_RESPONSE);
                return (Response) ox;
            } else {
                // Else we're on a status
                status = (Response.Status) o;
                if (Response.Status.ACCEPTED.equals(status)) {
                    // This change is for BulkData operations which manipulate the response code.
                    // Operations that return Accepted need to implement their own approach.
                    Object ox = operationContext.getProperty(FHIROperationContext.PROPNAME_RESPONSE);
                    return (Response) ox;
                }
            }
        }

        URI locationURI =
                (URI) operationContext.getProperty(FHIROperationContext.PROPNAME_LOCATION_URI);
        if (locationURI != null) {
            return Response.status(status)
                    .location(toUri(buildAbsoluteUri(getRequestBaseUri(resourceTypeName), locationURI.toString())))
                    .entity(resource)
                    .build();
        }
        return Response.status(status).entity(resource).build();
    }
}
