package edu.oregonstate.mist.metaxe

import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.api.Resource

import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject

import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.UriInfo
import java.util.regex.Pattern

@Path("/xeapps")
@Produces(MediaType.APPLICATION_JSON)
@groovy.transform.TypeChecked
class XEAppsResource extends Resource {
    private XEAppDAO dao
    private URI myEndpointUri

    private final String JSONAPI_TYPE = "xeapp"

    XEAppsResource(XEAppDAO dao, URI endpointUri) {
        this.dao = dao
        this.myEndpointUri = endpointUri
        this.endpointUri = endpointUri
    }

    // Get a specific application by ID.
    @GET
    @Path("{id}")
    ResultObject getById(@PathParam("id") String id) {
        // TODO: sanitize id

        ESResult es = this.dao.getById(id)
        if (es == null) {
            throw new NotFoundException()
        }

        new ResultObject(
            data: mapESObject(es),
            links: [
                self: this.urlFor(es.id)
            ]
        )
    }

    ResourceObject mapESObject(ESResult es) {
        new ResourceObject(
            id: es.id,
            type: JSONAPI_TYPE,
            attributes: new Attributes(
                applicationName: es.source.applicationName,
                versions: es.source.versions.collectEntries { v ->
                    [ (v.instance): v.version ]
                }
            ),
            links: [
                self: this.urlFor(es.id)
            ],
        )
    }

    String urlFor(String id) {
        UriBuilder.fromUri(this.myEndpointUri).path("xeapps/{id}").build(id).toString()
    }

    // Get all applications, their versions, and what instances they're deployed in.
    @GET
    ResultObject search(
        @QueryParam("q")        String q,        // app name
        @QueryParam("instance") String instance, // prod, devl, dev2
        @QueryParam("version")  String version   // deployed version
    ) {
        // XXX sanitize?
        def results = this.dao.search(q, instance, version, this.pageNumber, this.pageSize)

        def params = [:]
        if (q) {
            params.q = q
        }
        if (instance) {
            params.instance = instance
        }
        if (version) {
            params.version = version
        }

        new ResultObject(
            data: results.collect { mapESObject(it) },
            links: getSearchLinks(params),
        )
    }

    private getSearchLinks(Map<String,String> params) {
        def pageNumber = this.getPageNumber()
        def pageSize = this.getPageSize()
        return [
            self: getPaginationUrl(params, pageNumber, pageSize),
            next: getPaginationUrl(params, pageNumber+1, pageSize),
            prev: pageNumber > 1 ? getPaginationUrl(params, pageNumber-1, pageSize) : null,
        ]
    }

    private String getPaginationUrl(Map<String,String> params, int pageNumber, int pageSize) {
        params = new LinkedHashMap(params)
        params["pageNumber"] = pageNumber.toString()
        params["pageSize"] = this.pageSize.toString()
        getPaginationUrl(params)
    }
}
