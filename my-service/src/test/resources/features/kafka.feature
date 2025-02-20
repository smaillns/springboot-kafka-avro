Feature: consuming, persist and sending events with retry & DLT
  Background: 
    Given this avro schema:
      """
      {{{[&avro/event.avsc]}}}
      """
  Scenario: we successfully consume, persist and send an event
    When this myevent is published on the test_main_topic topic:
      """
      id: 1
      label: a label
      """
    
    Then within 10000ms the my_events table contains:
      """
      id: 1
      label: a label
      """ 
    And the test_output_topic topic contains this myevent:
      """
      id: 1
      label: a label
      """
    And it is not true that the test_retry_topic topic contains 1 messages
    
    Scenario: when an error occurs, then the event is sent to the retry topic
      When this myevent is published on the test_main_topic topic:
        """
        id: 0
        label: a label
        """
      
      Then within 10000ms the test_retry_topic topic contains this myevent:
        """
        id: 0
        label: a label
        """
      And it is not true that the test_main_topic topic contains this myevent:
        """
        id: 0
        label: a label
        """
      
      
      