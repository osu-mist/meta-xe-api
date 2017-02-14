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

    Attributes getById(String id) {
        def builder = UriBuilder.fromUri(this.esUrl)
        def url = builder.path(id).path("_source").build().toURL()
        String json
        try {
            json = url.getText()
        } catch (FileNotFoundException e) {
            return null
        }

        // TODO: catch IOException, com.fasterxml.jackson.core.JsonParseException,
        // com.fasterxml.jackson.databind.JsonMappingException
        return mapper.readValue(json, Attributes)
    }
}
