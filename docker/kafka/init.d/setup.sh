#!/usr/bin/env bash


if [ -f /tmp/kafka_is_ready ]
then
  echo "KAFKA TOPICS ALREADY CREATED"
  exit 0
fi

kafka-topics --bootstrap-server broker:9092 --list

if [ $? -ne 0 ]
then
  echo "ERROR RETRIEVING TOPICS LIST!!!"
  exit 1
fi

sh /tmp/data/create_topics.sh

echo
echo "KAFKA CREATE TOPICS HAS FINISHED !!!"
echo

touch /tmp/kafka_is_ready
