<#-- @ftlvariable name="go_server_url" type="java.lang.String" -->
<#-- @ftlvariable name="jre_feature_version" type="java.lang.String" -->
#!/usr/bin/env bash

set -e

agent_dir="/var/lib/go-agent"
default_jre_dir="/var/lib/jdk/${jre_feature_version}-jre"

if [ ! -x "$(command -v java)" ]; then
     export GO_JAVA_HOME=$default_jre_dir
     export PATH=$PATH:$default_jre_dir/bin
     echo "GO_JAVA_HOME set to: $GO_JAVA_HOME"
     echo "PATH set to $PATH"
fi

cd $agent_dir
if [ -f ./bin/go-agent ]; then
  echo "Setting GO_SERVER_URL url in wrapper-config/wrapper-properties.conf"
  echo "wrapper.app.parameter.100=-serverUrl" > wrapper-config/wrapper-properties.conf
  echo "wrapper.app.parameter.101=${go_server_url}" >> wrapper-config/wrapper-properties.conf

  ./bin/go-agent start
elif [ -f agent.sh ]; then
  echo "Setting GO_SERVER_URL: $GO_SERVER_URL"
  export GO_SERVER_URL=${go_server_url}
  echo "Starting agent"
  nohup bash agent.sh &
else
  echo "Unknown directory structure."
fi
