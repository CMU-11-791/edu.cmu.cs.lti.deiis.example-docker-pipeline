#!/usr/bin/env bash
root=`pwd`
webapps=/var/lib/tomcat7/webapps

function usage() {
	 echo "USAGE: $0 [start|list|stop|help]"
}

if [ -z "$1" ] ; then
	usage
	exit 
fi

case $1 in
	start)
		docker run -d -p 8080:8080 --name prepare -v $root/prepare-data/target:/$webapps lappsgrid/tomcat7:1.2.3 
		docker run -d -p 8081:8080 --name tokenizer -v $root/tokenizer/target:/$webapps lappsgrid/tomcat7:1.2.3 
		docker run -d -p 8082:8080 --name ngrams -v $root/ngrams/target:/$webapps lappsgrid/tomcat7:1.2.3 
		docker run -d -p 8083:8080 --name ranker -v $root/ranker/target:/$webapps lappsgrid/tomcat7:1.2.3 
		docker run -d -p 8084:8080 --name evaluator -v $root/evaluator/target:/$webapps lappsgrid/tomcat7:1.2.3 
		docker run -d -p 8085:8080 --name summarize -v $root/summarize/target:/$webapps lappsgrid/tomcat7:1.2.3 
		;;
	stop)
		docker rm -f $(docker ps -q)
		;;
	list)	
		docker ps 
		;;
	help)
		usage
		;;
	*)
		echo "Unknown option: $1"
		usage
		;;
esac



