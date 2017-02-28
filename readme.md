# Meta XE API

API for checking XE app deployment status.

### Generate Keys

HTTPS is required for Web APIs in development and production. Use `keytool(1)` to generate public and private keys.

Generate key pair and keystore:

    $ keytool \
        -genkeypair \
        -dname "CN=Jane Doe, OU=Enterprise Computing Services, O=Oregon State University, L=Corvallis, S=Oregon, C=US" \
        -ext "san=dns:localhost,ip:127.0.0.1" \
        -alias doej \
        -keyalg RSA \
        -keysize 2048 \
        -sigalg SHA256withRSA \
        -validity 365 \
        -keystore doej.keystore

Export certificate to file:

    $ keytool \
        -exportcert \
        -rfc \
        -alias "doej" \
        -keystore doej.keystore \
        -file doej.pem

Import certificate into truststore:

    $ keytool \
        -importcert \
        -alias "doej" \
        -file doej.pem \
        -keystore doej.truststore

## Gradle

This project uses the build automation tool Gradle. Use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to download and install it automatically:

    $ ./gradlew

The Gradle wrapper installs Gradle in the directory `~/.gradle`. To add it to your `$PATH`, add the following line to `~/.bashrc`:

    $ export PATH=$PATH:/home/user/.gradle/wrapper/dists/gradle-2.4-all/WRAPPER_GENERATED_HASH/gradle-2.4/bin

The changes will take effect once you restart the terminal or `source ~/.bashrc`.

## Tasks

List all tasks runnable from root project:

    $ gradle tasks

### IntelliJ IDEA

Generate IntelliJ IDEA project:

    $ gradle idea

Open with `File` -> `Open Project`.

### Configure

Copy [configuration-example.yaml](configuration-example.yaml) to `configuration.yaml`. Modify as necessary, being careful to avoid committing sensitive data.

### Build

Build the project:

    $ gradle build

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

### Run

Run the project:

    $ gradle run

## Incorporate Updates from the Skeleton

Fetch updates from the skeleton:

    $ git fetch skeleton

Merge the updates into your codebase as before.
Note that changes to CodeNarc configuration may introduce build failures.

    $ git checkout feature/abc-124-branch
    $ git merge skeleton/master
    ...
    $ git commit -v


## Resources

The Web API definition is contained in the [Swagger specification](swagger.yaml).

The following examples demonstrate the use of `curl` to make authenticated HTTPS requests.

### GET /

This resource returns build and runtime information:

    $ curl \
    > --cacert doej.pem \
    > --user "username:password" \
    > "https://localhost:8080/api/v0/?pretty=true"
    {
        "name" : "meta-xe-api",
        "time" : "2017-02-23 15:29:35-0800",
        "unixTime" : 1487892575248,
        "commit" : "4290452",
        "documentation" : "swagger.yaml"
    }

NOTE: you should only specify a certificate with --cacert for local testing.
Production servers should use a real certificate
issued by a valid certificate authority.

### GET /xeapps/:id

Return information about a single XE application:

    % curl \
    > --cacert doej.pem
    > --user "username:password"
    > "http://localhost:8080/api/v0/xeapps/ExampleApp?pretty=true"
    {
      "links" : {
        "self" : "https://api.oregonstate.edu/v1/xeapps/ExampleApp"
      },
      "data" : {
        "id" : "ExampleApp",
        "type" : "xeapp",
        "attributes" : {
          "applicationName" : "ExampleApp",
          "versions" : {
            "dev2" : "9.2",
            "devl" : "9.3",
            "prod" : "9.2"
          }
        },
        "links" : {
          "self" : "https://api.oregonstate.edu/v1/xeapps/ExampleApp"
        }
      }
    }

### GET /xeapps

Search for apps.

    % curl \
    > --cacert doej.pem
    > --user "username:password"
    > "http://localhost:8092/api/v0/xeapps?q=Example&pretty=true"
    {
      "links" : {
        "self" : "https://api.oregonstate.edu/xeapps?q=Example&page%5Bnumber%5D=1&page%5Bsize%5D=10",
        "first" : "https://api.oregonstate.edu/xeapps?q=Example&page%5Bnumber%5D=1&page%5Bsize%5D=10",
        "last" : "https://api.oregonstate.edu/xeapps?q=Example&page%5Bnumber%5D=1&page%5Bsize%5D=10",
        "next" : null,
        "prev" : null
      },
      "data" : [ {
        "id" : "ExampleApp",
        "type" : "xeapp",
        "attributes" : {
          "applicationName" : "ExampleApp",
          "versions" : {
            "dev2" : "9.2",
            "devl" : "9.3",
            "prod" : "9.2"
          }
        },
        "links" : {
          "self" : "https://api.oregonstate.edu/v1/xeapps/ExampleApp"
        }
      } ]
    }
