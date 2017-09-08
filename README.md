# Example Docker Pipeline

The Booth/Lincoln example pipeline running in a collection of Docker containers.

This project is structured as a multi-module Maven project, that is, a number of Maven projects grouped together with a single master parent pom file.

# Prerequisites

1. Java 1.8
1. Maven 3.x
1. [Docker](https://docs.docker.com/engine/installation/)
1. [The Lappsgrid Services DSL](https://github.com/lappsgrid-incubator/org.anc.lapps.dsl)

You will also need to add http://oss.sonatype.org as a *SNAPSHOT* to your `~/.m2/settings.xml` file.  An example settings.xml can be found [here](http://downloads.lappsgrid.org/scripts/settings.xml).

# Build

From the main project directory.

```bash
mvn clean package
```

# Run everything

There are two ways to start the Docker containers.

1. The `servers.sh` Bash script  
    ```bash
    ./servers.sh start
    ./servers.sh stop
    ```
1. Using Docker Compose  
    ```bash
    docker-compose up
    docker-compose down
    ```
    
# Run the pipeline

The `pipeline.lsd` script cab be used to run the pipeline of services on the Booth/Lincoln text.

```bash
lsd pipeline.lsd
```

