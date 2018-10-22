package edu.oregonstate.mist.metaxe

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.TypeChecked

@TypeChecked
class XEAppDAO {
    private File xeAppsFile
    private ObjectMapper mapper = new ObjectMapper()

    XEAppDAO(String xeAppsFileName) {
        xeAppsFile = new File(xeAppsFileName)
    }

    Attributes getById(String id) {
        Map<String, Attributes> allAttributes = getAllAttributes()
        allAttributes?.get(id)
    }

    List<Attributes> search(String q, String instance, String version) {
        Map<String, Attributes> allAttributes = getAllAttributes()
        Map<String, Attributes> filteredAttributes = allAttributes.findAll { key, value ->
            (q == null || key.contains(q)) &&
                    (instance == null || value.versions.containsKey(instance)) &&
                    (version == null || value.versions.containsValue(version))
        }
        filteredAttributes.values().asList()
    }

    private Map<String, Attributes> getAllAttributes() {
        Map<String, Attributes> allAttributes
        try {
            allAttributes = mapper.readValue(
                    xeAppsFile, new TypeReference<Map<String, Attributes>>() {}
            ) as Map<String, Attributes>
        } catch (Exception e) {
            throw new Exception("Error parsing ${xeAppsFile.getPath()}. Exception: ${e}")
        }
        allAttributes
    }
}
