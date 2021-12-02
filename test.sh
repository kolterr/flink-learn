#!/bin/bash
str='[{"jid":"c67f6d5b3b28f060f1a3a29bea0fc313","name":"mysql_sync_to_mysql1","state":"RUNNING","start-time":1638430674240,"end-time":-1,"duration":6271419,"last-modification":1638430676112,"tasks":{"total":1,"created":0,"scheduled":0,"deploying":0,"running":1,"finished":0,"canceling":0,"canceled":0,"failed":0,"reconciling":0,"initializing":0}},{"jid":"c67f6d5b3b28f060f1a3a29bea0fc313","name":"mysql_sync_to_mysql","state":"RUNNING","start-time":1638430674240,"end-time":-1,"duration":6271419,"last-modification":1638430676112,"tasks":{"total":1,"created":0,"scheduled":0,"deploying":0,"running":1,"finished":0,"canceling":0,"canceled":0,"failed":0,"reconciling":0,"initializing":0}}]'
jobsLength= echo  $str | jq '.[] | select(.name == "mysql_sync_to_mysql12331") | select(.state == "RUNNING") | length'
echo $jobsLength;

#{name: .[].name,state: .[].state}