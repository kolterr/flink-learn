#!/bin/bash
Host="http://localhost:8081"

if [ "$1" == "" ]; then
  echo "Empty App Name"
  exit 1
fi
jobsLength= curl --request GET -s "$Host/jobs/overview" | jq ".jobs" | jq '.[] | select(.name == "mysql_sync_to_mysql") | select(.state == "RUNNING") | length'
echo "$jobsLength"
if [ -z $jobsLength ]; then
  echo 12331
  exit 0
else
  exit 1
fi
