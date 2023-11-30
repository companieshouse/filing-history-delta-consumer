# `filing-history-delta-consumer`

## Summary

The ``filing-history-delta-consumer`` handles the processing of filing history deltas by:

* consuming them, in the forms of `ChsDelta` Kafka messages, from the `filing-history-delta` Kafka topic,
* deserialising them and transforming them into a structure suitable for a request to `filing-history-data-api`, and
* sending the request internally while performing any error handling.

The service is implemented in Java 21 using Spring Boot 3.2

## Error handling

The table below describes the topic a Kafka message is published to when an API error response is received, given the
number of attempts to process that message. The number of attempts is incremented when processed from the main or
retry topic. Any runtime exceptions thrown during the processing of a message are handled by publishing the message
immediately to the <br>`filing-history-delta-filing-history-delta-consumer-invalid` topic and are not retried.

| API Response | Attempt          | Topic published to                                         |
|--------------|------------------|------------------------------------------------------------|
| 2xx          | any              | _does not republish_                                       |
| 4xx          | any              | filing-history-delta-filing-history-delta-consumer-invalid |
| 5xx          | < max_attempts   | filing-history-delta-filing-history-delta-consumer-retry   |
| 5xx          | \>= max_attempts | filing-history-delta-filing-history-delta-consumer-error   |

## System requirements

* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads)
* [Maven](https://maven.apache.org/download.cgi)
* [Apache Kafka](https://kafka.apache.org/)

## Building and Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the
   README.
2. Enable the following services using the command `./bin/chs-dev services enable <service>`.
   * `chs-delta-api`
   * `filing-history-delta-consumer`
   * `filing-history-data-api`
3. Boot up the services' containers on docker using tilt `tilt up`.
4. Messages can be produced to the filing-history-delta topic using the instructions given
   in [CHS Delta API](https://github.com/companieshouse/chs-delta-api).

## Environment variables

| Variable                      | Description                                                                         | Example (from docker-chs-development) |
|-------------------------------|-------------------------------------------------------------------------------------|---------------------------------------|
| CHS_API_KEY                   | The client ID of an API key with internal app privileges                            | abc123def456ghi789                    |
| API_LOCAL_URL                 | The host through which requests to the filing-history-data-api are sent             | http://api.chs.local:4001             |
| SERVER_PORT                   | The server port of this service                                                     | 9090                                  |
| BOOTSTRAP_SERVER_URL          | The URL to the kafka broker                                                         | kafka:9092                            |
| CONCURRENT_LISTENER_INSTANCES | The number of listeners run in parallel for the consumer                            | 1                                     |
| FILING_HISTORY_DELTA_TOPIC    | The topic ID for filing history delta kafka topic                                   | filing-history-delta                  |
| GROUP_ID                      | The group ID for the service's Kafka topics                                         | filing-history-delta-consumer         |
| MAX_ATTEMPTS                  | The number of times a message will be retried before being moved to the error topic | 5                                     |
| BACKOFF_DELAY                 | The incremental time delay between message retries                                  | 100                                   |
| LOGLEVEL                      | The level of log messages output to the logs                                        | debug                                 |
| HUMAN_LOG                     | A boolean value to enable more readable log messages                                | 1                                     |

## Building the docker image

    mvn compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/filing-history-delta-consumer

## To make local changes

Development mode is available for this service
in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable filing-history-delta-consumer

This will clone the `filing-history-delta-consumer` into the repositories folder. Any changes to the code, or resources
will automatically trigger a rebuild and relaunch.
