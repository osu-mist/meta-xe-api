package edu.oregonstate.mist.metaxe

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import groovy.transform.TypeChecked

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder
import java.util.regex.Pattern

@Path("xeapps")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@TypeChecked
class XEAppsResource extends Resource {
    private XEAppDAO dao
    private URI myEndpointUri

    private final String JSONAPI_TYPE = "xeapp"
    private static Pattern sanitizeRegex = ~/[^A-Za-z0-9.\-]/

    XEAppsResource(XEAppDAO dao, URI endpointUri) {
        this.dao = dao
        this.myEndpointUri = endpointUri
        this.endpointUri = endpointUri
    }

    /**
     * Get a specific application by ID.
     *
     * @param id the application id
     */
    @Timed
    @GET
    @Path("{id}")
    ResultObject getById(@PathParam("id") String id) {
        id = sanitize(id)

        Attributes attributes = this.dao.getById(id)
        if (attributes == null) {
            throw new NotFoundException()
        }

        new ResultObject(
            data: createResourceObject(attributes),
            links: [
                self: this.urlFor(attributes.applicationName)
            ]
        )
    }

    private ResourceObject createResourceObject(Attributes attributes) {
        new ResourceObject(
            id: attributes.applicationName,
            type: JSONAPI_TYPE,
            attributes: new Attributes(
                applicationName: attributes.applicationName,
                versions: attributes.versions
            ),
            links: [
                self: this.urlFor(attributes.applicationName)
            ]
        )
    }

    private String urlFor(String id) {
        UriBuilder.fromUri(this.myEndpointUri).path("xeapps/{id}").build(id).toString()
    }

    /**
     * Get all applications, their versions, and what instances they're deployed in.
     *
     * @param q query string; will match all applications which contain this
     *          string as a substring
     * @param instance filter by deployed instance (prod, devl, dev2)
     * @param version filter by deployed version
     * @return a ResultObject with the list of results
     */
    @Timed
    @GET
    ResultObject search(
        @QueryParam("q")        String q,
        @QueryParam("instance") String instance,
        @QueryParam("version")  String version
    ) {
        q = sanitize(q)
        instance = sanitize(instance)
        version = sanitize(version)

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

        List<Attributes> results = this.dao.search(q, instance, version)

        if (results.isEmpty()) {
            return new ResultObject(
                    data: [],
                    links: getPaginationLinks(params, 0)
            )
        }

        int pageSize = this.getPageSize()
        int pageNumber = this.getPageNumber()
        int startIdx = (pageNumber - 1) * pageSize
        int endIdx = (pageNumber * pageSize) - 1
        endIdx = results.size() - 1 >= endIdx ? endIdx : results.size() - 1

        List<Attributes> paginatedResults
        try {
            paginatedResults = results[startIdx..endIdx]
        } catch (IndexOutOfBoundsException ignore) {
            // Page out of bounds. Return empty data object with first/last links and null values
            // for self, next, and prev links
            return new ResultObject(
                    data: [],
                    links: getPaginationLinks(params, results.size(), false)
            )
        }

        new ResultObject(
            data: paginatedResults.collect { createResourceObject(it) },
            links: getPaginationLinks(params, results.size())
        )
    }

    /**
     * Sanitize strips everything except
     * ascii letters, numbers, hyphen, and period.
     */
    private static String sanitize(String s) {
        if (s != null) {
            sanitizeRegex.matcher(s).replaceAll('')
        } else {
            null
        }
    }

    private Map<String,String> getPaginationLinks(Map<String,String> params, int totalHits,
                                                  boolean inBounds = true) {
        def pageNumber = this.getPageNumber()
        int pageSize = this.getPageSize()
        int lastPage = totalHits != 0 ? (totalHits + pageSize - 1).intdiv(pageSize).toInteger() : 1

        [
            self: inBounds ? getPaginationUrl(params, pageNumber, pageSize) : null,
            first: getPaginationUrl(params, 1, pageSize),
            last: getPaginationUrl(params, lastPage, pageSize),
            next: inBounds && pageNumber < lastPage ?
                getPaginationUrl(params, pageNumber + 1, pageSize) : null,
            prev: inBounds && pageNumber > 1 ?
                getPaginationUrl(params, pageNumber - 1, pageSize) : null,
        ]
    }

    private String getPaginationUrl(Map<String,String> params, int pageNumber, int pageSize) {
        params = new LinkedHashMap(params)
        params["pageNumber"] = pageNumber.toString()
        params["pageSize"] = pageSize.toString()
        getPaginationUrl(params, "xeapps")
    }
}
