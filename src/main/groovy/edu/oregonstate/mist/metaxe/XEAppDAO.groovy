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
        println "getByID: ${url}"
        String json
        try {
            json = url.getText()
        } catch (FileNotFoundException e) {
            return null
        }

        // TODO: catch IOException, com.fasterxml.jackson.core.JsonParseException,
        // com.fasterxml.jackson.databind.JsonMappingException
        return mapper.readValue(json, ESResult)
    }

    List<ESResult> search(
        String q, String instance, String version,
        int pageNumber, int pageSize
    ) {
        def builder = UriBuilder.fromUri(this.esUrl)
        def url = builder.path("_search").build().toURL()
        println "search: ${url}"

        def must = []
        def filter = []

        if (q) {
            must.add([match: [applicationName: [query: q.toLowerCase(), fuzziness: 2]]])
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
                "from": (pageNumber-1) * pageSize,
                "size": pageSize
            ])
            def results = mapper.readValue(jsonStream, ESSearchResults)
            results.hits.hits
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
        return conn.getInputStream()
    }

}
