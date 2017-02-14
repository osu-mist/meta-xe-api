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
import javax.ws.rs.core.UriInfo
import java.util.regex.Pattern

@Path("/xeapps")
@Produces(MediaType.APPLICATION_JSON)
@groovy.transform.TypeChecked
class XEAppsResource extends Resource {
    private XEAppDAO dao

    XEAppsResource(XEAppDAO dao) {
        this.dao = dao
    }

    // Get a specific application by ID.
    @GET
    @Path("{id}")
    ResultObject get(@PathParam("id") String id) {
        // TODO: sanitize id

        Attributes attrib = this.dao.getById(id)
        if (attrib == null) {
            throw new NotFoundException()
        }

        new ResultObject(
            data: new ResourceObject(
                id: id,
                type: "app",
                attributes: attrib,
                links: [:],
            ),
        )
    }
    
    // Get all applications, their versions, and what instances they're deployed in.
    @GET
    ResultObject search() {}
}
