#!/bin/bash
script=$(
  cd $(dirname $0)
  pwd
)
config_dir=${HOME}/program/learn/flink/flink-learn
echo "run script ${script}"
FLINK_HOME="/Users/wangyang/program/learn/flink-1.13.3"
JobInfo="/Users/wangyang/program/learn/flink/flink-learn/mysql-cdc/target/mysql-cdc-1.0.0-jar-with-dependencies.jar"
app_name="mysql_sync_to_mysql"
host_name=$(hostname -f)

# checkpoint 配置
checkpoint_dir="file:///Users/wangyang/program/learn/flink/flink-learn/mysql-cdc/checkpoints"
# 数据库配置
db_host="127.0.0.1"
db_port="3307"
db_databases="learnjdbc"
db_table_list="learnjdbc.students"
db_user="root"
db_user_pass="123456"
log_dir=${HOME}/program/learn/flink/flink-learn/logs

#Check if the application is running before start
echo "Check Application for ${app_name} if running before start, Please wait..."
sh $config_dir/check_flink_job_running.sh "${app_name}"
if [ $? -eq 1 ]; then
  echo "The Application for ${app_name} is running. Skip start."
  exit 1
fi
#exit 1
nohup $FLINK_HOME/bin/flink run -c com.akazone.stream.mysql2mysql $JobInfo \
  --app_name "${app_name}" \
  --db_host "${db_host}" \
  --db_port "${db_port}" \
  --db_databases "${db_databases}" \
  --db_table_list "${db_table_list}" \
  --db_user "${db_user}" \
  --db_user_pass "${db_user_pass}" \
  --checkpoint_dir "${checkpoint_dir}" >${log_dir}/${app_name}.log 2>&1 &

echo "Check Application for ${app_name} if running after start, Please wait..."
sleep 10
sh $config_dir/check_flink_job_running.sh "${app_name}"
# shellcheck disable=SC2181
if [ $? -eq 0 ]; then
  echo "ERROR-Yarn Application Start Failed,AppName: ${app_name},Host: ${host_name},Script: ${script}"
  exit 1
else
  echo "WARN-Yarn Application Start Successful,AppName: ${app_name},Host: ${host_name},Script: ${script}"
fi
#nohup $FLINK_HOME/bin/flink run $JobInfo \
#--app_name="${app_name}" \
#--checkpoint_dir="${checkpoint_dir}"
#  > ${log_dir}/${app_name}.log 2>&1 &
