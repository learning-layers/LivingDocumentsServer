#!/bin/bash
sudo echo $HTTP_USER
sudo echo $HTTP_PASSWORD
sudo echo $HTTP_BASE_URL
sudo echo $SETTINGS_PATH
sudo echo "wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_URL/$SETTINGS_PATH/https/keystore.jks"
cd /opt/ldserver
sudo wget -N --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_BASE_URL/$SETTINGS_PATH/https/keystore.jks