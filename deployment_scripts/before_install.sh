#!/bin/bash

yes | sudo yum install java-1.8.0-openjdk-devel # includes compiler
yes | sudo yum remove java-1.7.0-openjdk
