jmx-extraction
==============

All-command-line-configured utility for grabbing the values of named MBean properties to stdout

By default does not show status to stdout - this is logged to /tmp/jmx-extractor.log


    $ tail -F /tmp/jmx-extractor.log


Design Summary
--------------

- invoked using cron or sensu's scheduler
    - non-memory resident during inactive times - exits when complete or timed out
- one invocation per MBean per JMX connection
    - any number of properties of that MBean
- prints to stdout by default
    - extensible to make other renderers (e.g. to a file)
- all config is visible on command line for ease of logging and modularity
    - Host logging details: tier, app, utility, hostname
    - JMX service details: host, port, jndi, user/pass
    - MBean metric details: domain, matched string in key, metrics to log
- all error or logging messages do NOT appear on stdout


Usage
=====

Compile
-------

      $ mvn -U clean package


in development
--------------

Print all values of all attributes on all MBeans:


      $ java -jar target/jmx-extraction-1.0-SNAPSHOT.jar     \
        -H localhost -P 18063 -J /jmxrmi                     \


Accessing only MBeans with "Pooled" in their canonical names, printing only the values of the named attributes:

      $ java -jar target/jmx-extraction-1.0-SNAPSHOT.jar     \
        -H localhost -P 18063 -J /jmxrmi                     \
        -k Pooled                                            \
        -c "numConnections,numIdleConnections,numBusyConnections,threadPoolNumActiveThreads,threadPoolNumIdleThreads"


in production
-------------

Change the route to the jar file, and use the same arguments

Usage listing
-------------

    usage: java -jar jmx-extraction.jar
      -H,--jmxHost <arg>         the host serving the JMX interface
      -J,--jmxJndi <arg>         the JNDI name of the JMX interface
      -P,--jmxPort <arg>         the port for the JMX interface
      -U,--jmxUser <arg>         OPTIONAL username for a secured JMX interface
      -W,--jmxPassword <arg>     OPTIONAL password for a secured JMX interface
      -k,--keyMatch <arg>        OPTIONAL substring used to find the MBean
      -c,--csvAttributes <arg>   OPTIONAL comma-separated list of attributes to monitor


Parameter description
=====================

Sample output
-------------

One metric per line:


     (com.mchange.v2.c3p0:identityToken=2sodxj945wawqglpchmh|2d7b8b45,\
        name=FooC3P0PooledDataSource,type=PooledDataSource) \
        name=numIdleConnections value=0


JMX connection parameters
-------------------------

- -H or --jmxHost
     - the JMX host name
     - example: localhost
- -P or --jmxPort
     - the JMX port which is listening for requests
     - example: 18063
- -J or --jmxJndi
     - the JMX host name
     - example: /jmxrmi
- -U or --jmxUser
     - optional
     - the JMX user name for a secured connection
     - example: admin
- -W or --jmxPassword
     - optional
     - the JMX password for a secured connection
     - example: start123

MBean specification parameters
------------------------------

- -k or --keyMatch
     - a substring appearing in a portion of key/value, specifying an MBean to search
     - example: Billy (see above C3P0 example)
     - if omitted, will search for all beans and all properties
- -c or --csvAttributes
     - the comma-separated list of attributes we want from this MBean
     - example: "numConnections,numIdleConnections,numBusyConnections,threadPoolNumActiveThreads,threadPoolNumIdleThreads"


Development Notes
=================

Compiling
---------

All done in Maven


     $ mvn -U clean package


Appendix: Expose the JMX on the service
=======================================

For example, how does jetty expose its MBeans so this utility can read from them?

Add these flags to the java

      -Dcom.sun.management.jmxremote.port=9999 \
      -Dcom.sun.management.jmxremote \
      -Dcom.sun.management.jmxremote.local.only=false


if running locally, disable the security


      -Dcom.sun.management.jmxremote.authenticate=false \
      -Dcom.sun.management.jmxremote.ssl=false

More reading:
- [how to activate jmx on my jvm for access with jconsole](http://stackoverflow.com/questions/856881/how-to-activate-jmx-on-my-jvm-for-access-with-jconsole)


Labelling your C3P0
-------------------

To make it easier to distinguish c3p0 instances, you can name each pool.

In src/main/resources/c3p0.properties :
- set the property "c3p0.dataSourceName" to a name specific to this application's data source
- "com.mchange.v2.c3p0.management.RegistryName" configures name of the entire c3p0 registry

Example:


     c3p0.dataSourceName=FooC3P0PooledDataSource
     com.mchange.v2.c3p0.management.RegistryName=FooC3P0PooledDataSourceRegistry
