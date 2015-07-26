#!/bin/bash
cd /opt/ldserver
sed -i "s#SSL_KEYSTORE_FILE#${SSL_KEYSTORE_FILE}#g" ./application.properties
sed -i "s#SSL_KEYSTORE_PASSWORD#${SSL_KEYSTORE_PASSWORD}#g" ./application.properties
sed -i "s#SSL_KEY_PASSWORD#${SSL_KEY_PASSWORD}#g" ./application.properties
java -jar ld-boot-1.0.0.jar --spring.config.location application.properties