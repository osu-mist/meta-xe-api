package edu.oregonstate.mist.metaxe

class Attributes {
    // Name of the XE application
    String applicationName = ""

    // Maps build environment names to app version numbers.
    // For example, "prod" => "9.2"
    Map<String,String> versions = [:]
}
