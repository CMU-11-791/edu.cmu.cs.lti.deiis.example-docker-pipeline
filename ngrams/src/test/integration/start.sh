#!/usr/bin/env bash

# Starts a lappsgrid/tomcat7 container using `target` as Tomcat's webapps directory.

set -e

dir=`pwd`
docker run -d -p 8080:8080 --name tomcat -v $dir/target:/var/lib/tomcat7/webapps lappsgrid/tomcat7:1.2.1



