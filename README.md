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
    * `chs-kafka-api`
3. Boot up the services' containers on docker using tilt `tilt up`.
4. Messages can be produced to the filing-history-delta topic using the instructions given
   in [CHS Delta API](https://github.com/companieshouse/chs-delta-api).

## Environment variables

| Variable                      | Description                                                                                     | Example (from docker-chs-development) |
|-------------------------------|-------------------------------------------------------------------------------------------------|---------------------------------------|
| TRANSACTION_ID_SALT           | The salt used to encode entity ID's into their MongoDB counterpart                              | abc123                                |
| FILING_HISTORY_API_KEY        | The client ID of an API key, with internal app privileges, to call filing-history-data-api with | abc123def456ghi789                    |
| API_LOCAL_URL                 | The host through which requests to the filing-history-data-api are sent                         | http://api.chs.local:4001             |
| BOOTSTRAP_SERVER_URL          | The URL to the kafka broker                                                                     | kafka:9092                            |
| CONCURRENT_LISTENER_INSTANCES | The number of listeners run in parallel for the consumer                                        | 1                                     |
| FILING_HISTORY_DELTA_TOPIC    | The topic ID for filing history delta kafka topic                                               | filing-history-delta                  |
| GROUP_ID                      | The group ID for the service's Kafka topics                                                     | filing-history-delta-consumer         |
| MAX_ATTEMPTS                  | The number of times a message will be retried before being moved to the error topic             | 5                                     |
| BACKOFF_DELAY                 | The incremental time delay between message retries                                              | 100                                   |
| LOGLEVEL                      | The level of log messages output to the logs                                                    | debug                                 |
| HUMAN_LOG                     | A boolean value to enable more readable log messages                                            | 1                                     |
| PORT                          | The port at which the service is hosted in ECS                                                  | 8080                                  |

## Building the docker image

    mvn package -Dskip.unit.tests=true -Dskip.integration.tests=true jib:dockerBuild

## To make local changes

Development mode is available for this service
in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable filing-history-delta-consumer

This will clone the `filing-history-delta-consumer` into the repositories folder. Any changes to the code, or resources
will automatically trigger a rebuild and relaunch.

## Testing

Due to the complex nature of the filing history transformation logic, and the significant number of edge cases,
a `@ParameterisedTest` has been implemented
within `kafka/ConsumerPositiveComprehensiveIT.java` to cover as many of
the [transformation rules](https://github.com/companieshouse/filing-history-delta-consumer/blob/df6ee016f916e303474034ace5c3cc346b50d441/src/main/resources/transform_rules.yml)
as possible.

### IMPORTANT

Before committing any documents to GitHub, please ensure any fields containing sensitive data are
anonymised.
\
\
**Complete the following steps to add another test case**:

1. Create a json file called `<category>/X_delta.json` where `X` is the name of the form/rule under test.
    1. To make json snippets easier to organise, we now separate them based on `<category>`.
        1. This would be the category you find when you locate the form type in
           the `src/main/resources/transform_rules.yml` file.
        2. And can be further cross-checked by looking
           at [CategoryEnum](https://github.com/companieshouse/private-api-sdk-java/blob/2cb6199837bea4342ce3d4717ad2537dd32f235d/generated_sources/src/main/java/uk/gov/companieshouse/api/filinghistory/ExternalData.java#L45).
    2. If a directory matching the `<category>` does not exist, it should be created under the `src/test/resources/data`
       directory.
    3. The file should contain a valid filing history delta with respect to
       the [API specification](https://github.com/companieshouse/private.api.ch.gov.uk-specifications/blob/c2e3f3558e13efba075c23227709448273d5fdfb/src/main/resources/delta/filing-history-delta-spec.yml).
    4. Note the `delta_at` field is at the top level whereas the old Perl backend had it within the `filing_history`
       array.
    5. Deltas can be found within the `fh_deltas` table in Kermit by querying by form
       type, [see confluence page](https://companieshouse.atlassian.net/wiki/spaces/TEAM4/pages/4403200517/SQL+Queries+to+find+Filing+History+deltas).
       Alternatively, they can be found in the `queue`collection in MongoDB or by running the `f_get_one_transaction`
       package in CHIPS.
2. Create a json file called `<category>/X_request_body.json` where `X` is the same as above.
    1. `<category>` would be the same as what was used for the `X_delta.json` file.
    2. The file should contain a valid filing history PUT request body with respect to
       the [API specification](https://github.com/companieshouse/private.api.ch.gov.uk-specifications/blob/1361e79e495b61cdd8101d1814d7d7aeddd8a639/src/main/resources/filing-history/internal-filing-history.json#L55).
    3. As the PUT request body structure is similar to that of a document within the `company_filing_history` collection
       in MongoDB. A quick way to generate a document is to send it through the old backend locally by enabled the
       backend module on tilt. Its easy enough to copy a document and make the following changes:
        1. `data` &rarr; `external_data`.
        2. `_id` &rarr; `external_data.transaction_id`
        3. `_barcode` &rarr; `external_data.barcode`
        4. Delete `external_data.pages`
        5. Delete `external_data.links.document_metadata`
        6. Wrap all other top level fields in `internal_data`
        7. Add `internal_data.delta_at`:
        8. Add `internal_data.updated_by` (value should always be "context_id")
        9. Add `internal_data.transaction_kind` ("top-level" for the most part,
           see `TransactionKindService`)
        10. Add `internal_data.parent_entity_id` (usually an empty string)
        11. Encode the value of `internal_data.entity_id` with the salt `salt`.
        12. Replace the value of `external_data.transaction_id` with the encoded ID.
        13. Replace the suffix of the value of `external_data.links.self` with the encoded ID.
        14. Change MongoDB `ISODate`'s to `ISO_INSTANT` format `.000+0000` &rarr; `Z`. (Instants have trailing zeros
            removed)
3. Finally, add the prefix, `X` used above, to the `@CsvSource` annotation within `ConsumerPositiveComprehensiveIT`.
