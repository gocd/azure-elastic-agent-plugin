#!/usr/bin/env bash

set -e

java_download_url="https://api.adoptium.net/v3/binary/version/jdk-17.0.5%2B8/linux/x64/jre/hotspot/normal/eclipse?project=jdk"

reset_dir () {
   rm -rf $1
   mkdir -p $1
}

install_java () {
      wget $java_download_url --output-document=jre17.tar.gz 
      reset_dir /var/lib/jdk
      tar -xvf jre17.tar.gz -C /var/lib/jdk/
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
