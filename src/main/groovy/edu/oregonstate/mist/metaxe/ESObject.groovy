package edu.oregonstate.mist.metaxe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class ESSearchResults {
    ESHits hits
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ESHits {
    Integer total
    List<ESResult> hits
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ESResult {
    @JsonProperty("_index")
    String index

    @JsonProperty("_type")
    String type

    @JsonProperty("_id")
    String id

    @JsonProperty("_source")
    ESObject source
}

class ESObject {
    // Name of the XE application
    String applicationName = ""

    // Maps build enviroment names to app version numbers.
    // For example, "prod" => "9.2"
    List<ESVersion> versions = []
}

class ESVersion {
    String instance
    String version
}
