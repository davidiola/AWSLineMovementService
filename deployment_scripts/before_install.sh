#!/bin/bash

yes | sudo yum install java-1.8.0-openjdk-devel # includes compiler
yes | sudo yum remove java-1.7.0-openjdk

# add cron job to run prog every 5 min
crontab -l > mycron
echo "*/5 * * * * cd /home/ec2-user/; sudo ./gradlew run" >> mycron
crontab mycron
rm mycron