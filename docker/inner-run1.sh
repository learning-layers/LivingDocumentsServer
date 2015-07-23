#!/bin/bash
sudo echo $HTTP_USER
sudo echo $HTTP_PASSWORD
sudo echo $HTTP_BASE_URL
sudo echo "wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_URL/keystore.jks"
cd /opt/ldserver
sudo wget -N --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_BASE_URL/keystore.jks