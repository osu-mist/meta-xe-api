package edu.oregonstate.mist.metaxe

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.InheritConstructors
import groovy.transform.TypeChecked

@TypeChecked
class XEAppDAO {
    private File xeAppsFile

    XEAppDAO(String xeAppsFileName) {
        xeAppsFile = new File(xeAppsFileName)
    }

    Attributes getById(String id) {
        Map<String, Attributes> allAttributes = getAllAttributes(xeAppsFile)
        allAttributes?.get(id)
    }

    List<Attributes> search(String q, String instance, String version) {
        Map<String, Attributes> allAttributes = getAllAttributes(xeAppsFile)
        Map<String, Attributes> filteredAttributes = allAttributes.findAll { key, value ->
            (!q || key.contains(q)) &&
                    (!instance || value.versions.containsKey(instance)) &&
                    (!version || value.versions.containsValue(version))
        }
        filteredAttributes.values().asList()
    }

    static Map<String, Attributes> getAllAttributes(File xeAppsFile) throws XeParsingException {
        Map<String, Attributes> allAttributes
        ObjectMapper mapper = new ObjectMapper()
        try {
            allAttributes = mapper.readValue(
                    xeAppsFile, new TypeReference<Map<String, Attributes>>() {}
            ) as Map<String, Attributes>
        } catch (IOException e) {
            throw new XeParsingException("Error parsing ${xeAppsFile.getPath()}. Exception: ${e}")
        }
        allAttributes
    }
}

@InheritConstructors
class XeParsingException extends Exception {}
