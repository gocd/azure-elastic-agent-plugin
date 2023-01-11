#!/usr/bin/env bash

set -e

java_download_url="https://download.bell-sw.com/java/11.0.17+7/bellsoft-jdk11.0.17+7-linux-amd64.tar.gz"

reset_dir () {
   rm -rf $1
   mkdir -p $1
}

install_java () {
      wget $java_download_url
      reset_dir /var/lib/jdk
      tar -xvf bellsoft-jdk11.0.17+7-linux-amd64.tar.gz -C /var/lib/jdk/
}

echo "Setting up unzip utility"
reset_dir bin
tar -xvf unzip.tar -C bin
bin_dir=`pwd`/bin

agent_dir="/var/lib/go-agent"

echo "Setting up JAVA"
install_java

echo "Installing agent version ${version}"
rm -rf go-agent*/
$bin_dir/unzip go-agent-*.zip
cd go-agent*/
reset_dir $agent_dir
mv * $agent_dir
cd $agent_dir

# write autoregister.properties
mkdir -p config
echo "Creating autoregister.properties file"
(
cat <<EOF
agent.auto.register.key=${autoregister_key}
agent.auto.register.environments=${environment}
agent.auto.register.elasticAgent.pluginId=${plugin_id}
agent.auto.register.elasticAgent.agentId=${agent_id}
EOF
) > ./config/autoregister.properties
