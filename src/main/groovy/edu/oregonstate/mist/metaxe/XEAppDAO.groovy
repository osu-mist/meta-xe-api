package edu.oregonstate.mist.metaxe

import javax.ws.rs.core.UriBuilder
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonMappingException

@groovy.transform.TypeChecked
class XEAppDAO {
    private URI esUrl
    private ObjectMapper mapper = new ObjectMapper()

    XEAppDAO(URI elasticsearchUrl) {
        this.esUrl = elasticsearchUrl
    }

    ESResult getById(String id) {
        def builder = UriBuilder.fromUri(this.esUrl)
        def url = builder.path('{id}').build(id).toURL()

        println "get by id: ${url}"

        InputStream jsonStream
        try {
            jsonStream = url.newInputStream()
        } catch (FileNotFoundException e) {
            // 404 -> return null
            return null
        } catch (IOException e) {
            // some other http error
            throw new ElasticsearchException(url, e)
        }

        // Catch JsonMappingException and JsonParseException here,
        // as otherwise jersey will catch them and interpret them
        // as an error in the client request
        //
        // Catch IOException as well, because why not
        try {
            mapper.readValue(jsonStream, ESResult) // return
        } catch (IOException e) {
            // read error
            throw new ElasticsearchException(url, e)
        } catch (JsonMappingException e) {
            throw new ElasticsearchException(url, e)
        } catch (JsonParseException e) {
            throw new ElasticsearchException(url, e)
        }
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
            must.add([wildcard: [applicationName: "*" + q.toLowerCase() + "*"]])
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

        // Catch JsonMappingException and JsonParseException,
        // as otherwise jersey will catch them and interpret them
        // as an error in the client request
        //
        // Catch IOException as well, because why not
        try {
            def conn = post(url, [
                "query": query,
                "from": (pageNumber - 1) * pageSize,
                "size": pageSize
            ])
            def jsonStream = conn.getInputStream() // checks status code
            def results = mapper.readValue(jsonStream, ESSearchResults)
            results.hits // return
        } catch (IOException e) {
            // http exception or read error
            throw new ElasticsearchException(url, e)
        } catch (JsonMappingException e) {
            throw new ElasticsearchException(url, e)
        } catch (JsonParseException e) {
            throw new ElasticsearchException(url, e)
        }
    }

    URLConnection post(URL url, def data) {
        def conn = url.openConnection()
        conn.doOutput = true
        conn.doInput = true
        // output stream is the *input* to the server (request body)
        // input stream is the *output* from the server (response body)
        mapper.writeValue(conn.getOutputStream(), data)
        conn // return
    }
}

class ElasticsearchException extends RuntimeException {
    ElasticsearchException(URL url, Throwable cause) {
        super("error from elasticsearch query ${url}".toString(), cause)
    }
}
