# GoCD Microsoft Azure Elastic Agent Plugin

This [Elastic Agent](https://docs.gocd.org/current/configuration/elastic_agents.html) plugin for *[Microsoft Azure](https://azure.microsoft.com/)* allows you to run elastic agents on Windows Azure virtual machines.
The plugin takes care of spinning up, shutting down virtual machines and installing go-agent based on the need of your deployment pipeline, thus removing bottlenecks and reducing the cost of your agent infrastructure. The plugin, based on the user-defined configuration, decides whether to spin up a new virtual machine or re-use an existing one.

The plugin can be configured to use a Azure virtual network, resource group and region where the virtual machines will be created. It supports both **Windows** and **Linux** virtual machines. The elastic profiles can be configured to use an image from Azure marketplace or a custom image from your subscription along with the size of the VM, disk space and more.

Once the builds finish, the virtual machine will be terminated or kept idle for a configured amount of time. This allows for more efficient usage of resources and reduces the overhead of creating virtual machines for each job. The VM will be terminated after the idle time and a fresh one will be created once required by a new job.

Table of Contents
=================

  * [Building the code base](#building-the-code-base)
  * [Install and configure the plugin](/docs/INSTALL.md)
    * [Prerequisites](/docs/INSTALL.md#prerequisites)
    * [Installation](/docs/INSTALL.md#installation)
    * [Configuration](/docs/INSTALL.md#configuring-the-azure-elastic-agent-plugin)
        * [Configure Plugin Settings](/docs/PLUGIN_SETTINGS.md)
        * [Configure Elastic Profile](/docs/ELASTIC_PROFILE_CONFIGURATION.md)
  * [Troubleshooting](/docs/TROUBLESHOOT.md)

## Building the code base

To build the jar, run `./gradlew clean check assemble`

## License

```plain
Copyright 2020 Thoughtworks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
