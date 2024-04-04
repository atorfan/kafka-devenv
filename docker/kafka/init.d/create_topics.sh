#!/usr/bin/env bash

function create_topic {

  echo "Creating topic ${1}"

  kafka-topics \
    --bootstrap-server broker:9092 \
    --if-not-exists \
    --create \
    --topic "${1}" \
    --partitions 1 | grep -v 'WARNING: Due to limitations in metric names'

}

create_topic 'testing.kafka-env'
