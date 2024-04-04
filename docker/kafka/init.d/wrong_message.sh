TOPIC=testing.kafka-env

echo Sending a Wrong message to topic ${TOPIC}

echo '666:{ "data": "WRONG!!!" }' |
  kafka-console-producer \
     --bootstrap-server broker:9092 \
     --topic ${TOPIC} \
     --property ignore.error=true \
     --property parse.key=true \
     --property key.separator=:
