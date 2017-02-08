#!/bin/bash
XE_USER=$1
XE_HOST=$2
ES_USER=$3
ES_HOST=$4

BANNER_HOME="/san/smf/ban/ban9/lcl"

for instance in dev2 devl prod
do
    # Get all war files into a variable as a list
    WAR_FILES=$(ssh -l $XE_USER $XE_HOST 'find '"$BANNER_HOME"' -type f  -wholename "*/'"$instance"'/*/current/dist/*.war" -printf "%f\n"')
    echo $WAR_FILES
    for item in $WAR_FILES 
    do 
        APP=$(echo $item | awk -F '-' '{print $1}')
        VERSION=$(echo $item | awk -F '-' '{print $2}' | sed 's/.war//')

        # Get response code when trying to GET a specific app
        CHECK_APP=$(ssh -l $ES_USER $ES_HOST 'curl -XGET -w %{http_code} localhost:9200/xe/apps/'"$APP"' | tail -c 3')

        if [ "$CHECK_APP" == "404" ]
        # If entry doesn't exist, create it
        then
            ssh -l $ES_USER $ES_HOST "curl -XPUT "localhost:9200/xe/apps/$APP" -d '{
                "applicationName": "'"$APP"'",
                "versions": {
                    "'"$instance"'": "'"$VERSION"'"
                }
            }'"
        else
        # Else update it
            ssh -l $ES_USER $ES_HOST "curl -XPOST "localhost:9200/xe/apps/$APP/_update" -d '{
                "doc": {
                    "versions": {
                        "'"$instance"'": "'"$VERSION"'"
                      }
                }
            }'"
        fi
    done
done
