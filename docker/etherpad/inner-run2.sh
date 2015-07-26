#!/usr/bin/env bash
sudo echo $HTTP_USER
sudo echo $HTTP_PASSWORD
sudo echo $HTTP_BASE_URL
sudo echo "wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_URL/settings.zip"
sudo rm -f settings.zip
sudo wget --http-user=$HTTP_USER --http-password=$HTTP_PASSWORD $HTTP_BASE_URL/$SETTINGS_PATH/settings.zip
sudo unzip -o settings.zip
sudo bin/run.sh --root