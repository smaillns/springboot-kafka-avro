Feature: consuming, persist and sending events with retry & DLT
  Background: 
    Given this avro schema:
      """
      {{{[&avro/event.avsc]}}}
      """

  Scenario: we successfully consume, persist and send an event. made an external api call. Nothing has to be retried.
    Given that posting "/posts" will return a status CREATED and:
      """yaml
      id: 101
      """

    When this myevent is published on the test_main_topic topic:
      """
      {{{[&features/fixtures/event.yaml]}}}
      """

    Then within 10000ms the my_events table contains:
      """
      {{{[&features/fixtures/event.yaml]}}}
      """
    And the test_output_topic topic contains this myevent:
      """
      {{{[&features/fixtures/event.yaml]}}}
      """
    And within 5000ms "/posts" has received 1 POST payload
    And payload.body.json.id == 1

    And it is not true that the test_retry_topic topic contains 1 messages
    And it is not true that the test_dlt_topic topic contains 1 messages

  Scenario: when an error occurs, then the event is sent to the retry topic
    until the max number of retries is reached. Then it is sent to the DLT topic.

    When this myevent is published on the test_main_topic topic:
      """
      {{{[&features/fixtures/invalid-event.yaml]}}}
      """
    
    Then within 10000ms the test_retry_topic topic contains this myevent:
      """
      {{{[&features/fixtures/invalid-event.yaml]}}}
      """
    And the test_dlt_topic topic contains this myevent:
      """
      {{{[&features/fixtures/invalid-event.yaml]}}}
      """
    And it is not true that the test_output_topic topic contains this myevent:
      """
      {{{[&features/fixtures/invalid-event.yaml]}}}
      """
