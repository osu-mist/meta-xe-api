package edu.oregonstate.mist.metaxe

import javax.ws.rs.core.UriBuilder
import com.fasterxml.jackson.databind.ObjectMapper

@groovy.transform.TypeChecked
class XEAppDAO {
    private URI esUrl
    private ObjectMapper mapper = new ObjectMapper()

    XEAppDAO(URI elasticsearchUrl) {
        this.esUrl = elasticsearchUrl
    }

    ESResult getById(String id) {
        def builder = UriBuilder.fromUri(this.esUrl)
        def url = builder.path(id).build().toURL()

        String json
        try {
            json = url.getText()
        } catch (FileNotFoundException e) {
            return null
        }

        // TODO: catch IOException, com.fasterxml.jackson.core.JsonParseException,
        // com.fasterxml.jackson.databind.JsonMappingException
        mapper.readValue(json, ESResult) // return
    }

    ESHits search(
        String q, String instance, String version,
        int pageNumber, int pageSize
    ) {
        def builder = UriBuilder.fromUri(this.esUrl)
        def url = builder.path("_search").build().toURL()

        def must = []
        def filter = []

        // Search for any application whose name contains q as a substring.
        // Elasticsearch recommends against using a wildcard at the beginning
        // of a search term because it may be slow, but we only have a handful
        // of applications, so we can afford it.
        if (q) {
            must.add([wildcard: [applicationName: "*" + q + "*"]])
        }

        if (instance) {
            filter.add([term: ["versions.instance": instance.toLowerCase()]])
        }

        if (version) {
            filter.add([term: ["versions.version": version.toLowerCase()]])
        }

        def query = [
            "bool": [
                must: must,
                filter: filter,
            ]
        ]

        // XXX do something sensible with exeptions
        try {
            def jsonStream = post(url, [
                "query": query,
                "from": (pageNumber - 1) * pageSize,
                "size": pageSize
            ])
            def results = mapper.readValue(jsonStream, ESSearchResults)
            results.hits // return
        } catch (IOException e) {
            throw new RuntimeException(e.toString())
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            throw new RuntimeException(e.toString())
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            throw new RuntimeException(e.toString())
        }
    }

    InputStream post(URL url, def data) {
        def conn = url.openConnection()
        conn.doOutput = true
        conn.doInput = true
        // output stream is the *input* to the server (request body)
        // input stream is the *output* from the server (response body)
        mapper.writeValue(conn.getOutputStream(), data)
        conn.getInputStream() // return
    }

}
