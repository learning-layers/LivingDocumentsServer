#!/bin/bash
sudo echo $HTTP_USER
sudo echo $HTTP_PASSWORD
sudo echo $HTTP_BASE_URL
sudo echo $SETTINGS_PATH
sudo echo "wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_URL/$SETTINGS_PATH/https/ssl.key.insecure"
cd /etc/nginx/ssl
sudo wget -N --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_BASE_URL/$SETTINGS_PATH/https/ssl.key.insecure
sudo echo "wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_URL/$SETTINGS_PATH/https/summary.crt"
sudo wget -N --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_BASE_URL/$SETTINGS_PATH/https/summary.crt