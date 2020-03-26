# GoCD Microsoft Azure Elastic Agent Plugin

The plugin needs to be configured with elastic profile configurations in order to create VMs on azure.

Table of Contents
=================

  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
  * [Configuration](#configuring-the-azure-elastic-agent-plugin)

## Prerequisites

### Go Server
* The GoCD server version **18.10.0** or higher.

### Azure subscription
* You will need a valid [Azure subscription](https://azure.microsoft.com/en-us/free/) with privileges to create Virtual machines inside it.
* There should be a [virtual network](https://docs.microsoft.com/en-us/azure/virtual-network/virtual-networks-overview) created in the azure subscription, go-agent virtual machines will be created inside it.
  
  A virtual network enables Azure resources, such as virtual machines (VM), to communicate privately with each other and with the internet. Please note the EA created will need to communicate with your GoCD server.
  
  `az network vnet create --name gocd-virtual-network --resource-group gocd-resource-group --subnet-name default`
  
  After the successful creation of the virtual network please note the Virtual Network ID from the response which will be similar to 
  
  ```json
  { 
    "id": "/subscriptions/3986fa0c-d463-4qf7-b508-cc38db097f58/resourceGroups/gocd-resource-group/providers/Microsoft.Network/virtualNetworks/gocd-virtual-network"
  }
  ```
  
  Refer [azure documentation](https://docs.microsoft.com/en-us/azure/virtual-network/quick-create-cli) for managing your Virtual Networks
      
* Create a new [resource group](https://docs.microsoft.com/en-us/azure/azure-stack/azure-stack-key-features#resource-groups) which will contain all the go-agent virtual machines.
  A resource group is a container that holds related resources for an Azure solution. GoCD uses the resource group to create as a container in which it creates your Azure EA.
  
  `az group create --name gocd-resource-group --location "Central US"`
  
  On successful creation of a Resource Group
  
  ```json  
  {
      "id": "/subscriptions/3386fa3c-d463-4cf7-b508-cc38db097e58/resourceGroups/gocd-resource-group",
      "location": "centralus",
      "managedBy": null,
      "name": "gocd-resource-group",
      "properties": {
          "provisioningState": "Succeeded"
      },
      "tags": null
  }
  ```
  
  Refer [azure documentation](https://docs.microsoft.com/en-us/azure/azure-resource-manager/cli-azure-resource-manager#create-a-resource-group) for managing your resource groups.
  
* Create a [service principal](https://docs.microsoft.com/en-us/cli/azure/create-an-azure-service-principal-azure-cli?view=azure-cli-latest) with access to create network interfaces, virtual machines, network security groups and managed disks in the region of the virtual network. The service principal credentials are required to setup the plugin.
  
  Service principals are separate identities that can be associated with an account. The service principal [clientid/secret] is used for sdk authentication
  
  `az ad sp create-for-rbac --name ServicePrincipalName --password PASSWORD`
  
  On successful creation of a Service Principal
  
  ```json
  {
      "appId": "f6a9bfde-414f-4700-a10c-07eafb7f1eaa",
      "displayName": "ServicePrincipalName",
      "name": "http://ServicePrincipalName",
      "password": "PASSWORD",
      "tenant": "cf03984b-4fa1-465f-b731-wecea9eece05"
  }
  ```
  Refer [azure documentation](https://docs.microsoft.com/en-us/cli/azure/create-an-azure-service-principal-azure-cli?view=azure-cli-latest) for creating a service principal.

  By default a service principal will have a [Contributor](https://docs.microsoft.com/en-us/azure/role-based-access-control/built-in-roles#contributor) role. For more fine grained access control, ensure that the service principal has the following access:

<pre class="highlight shell">
  <code>
  Microsoft.Compute/virtualMachines/*
  Microsoft.Network/networkInterfaces/*
  Microsoft.Compute/disks/*
  Microsoft.Compute/locations/*
  Microsoft.Resources/subscriptions/resourceGroups/read
  Microsoft.Network/virtualNetworks/read
  Microsoft.Network/virtualNetworks/subnets/read
  Microsoft.Network/virtualNetworks/subnets/join/*
  Microsoft.Network/networkSecurityGroups/read"
  Microsoft.Network/networkSecurityGroups/join/*
  </code>
</pre>

  You can create a [custom role](https://docs.microsoft.com/en-us/azure/role-based-access-control/custom-roles) using the below template. Ensure this role is [assigned to the service principal](https://docs.microsoft.com/en-us/cli/azure/create-an-azure-service-principal-azure-cli?view=azure-cli-latest#manage-service-principal-roles).
  
<pre class="highlight shell">
  <code>
  {
    "Actions": [
        "Microsoft.Compute/virtualMachines/*",
        "Microsoft.Network/networkInterfaces/*",
        "Microsoft.Compute/disks/*",
        "Microsoft.Compute/locations/*",
        "Microsoft.Resources/subscriptions/resourceGroups/read",
        "Microsoft.Network/virtualNetworks/read",
        "Microsoft.Network/virtualNetworks/subnets/read",
        "Microsoft.Network/virtualNetworks/subnets/join/*",
        "Microsoft.Network/networkSecurityGroups/read",
        "Microsoft.Network/networkSecurityGroups/join/*",
        "Microsoft.Resources/subscriptions/locations/read"
    ],
    "AssignableScopes": [
        "/subscriptions/{subscriptionId}"
    ],
    "DataActions": [],
    "Description": "Role for service principal used for GoCD Azure elastic agent plugin",
    "IsCustom": true,
    "Name": "gocd-azure-plugin-scope",
    "NotActions": [],
    "NotDataActions": []
}
  </code>
</pre>

### Some useful commands

1. az group list : List resource groups for a user account
2. az account list-locations : List of allowed locations for deploying resources for an account
3. az network vnet list : List of virtual networks for a user account 
4. az ad sp list --display-name <service-principal-name> : Details of the service principal

### Related help topics

1. [Getting Started with Microsoft Azure](https://azure.microsoft.com/en-in/get-started/)
2. [Azure virtual machines](https://azure.microsoft.com/en-us/services/virtual-machines/)
3. [Azure service principal](https://docs.microsoft.com/en-us/azure/active-directory/develop/app-objects-and-service-principals)
4. [Custom roles](https://docs.microsoft.com/en-us/azure/role-based-access-control/custom-roles)

## Installation

* Copy the file `build/libs/azure-elastic-agent-plugin-VERSION.jar` to the GoCD server under `${GO_SERVER_DIR}/plugins/external`
and restart the server.
* The `GO_SERVER_DIR` is usually `/var/lib/go-server` on **Linux** and `C:\Program Files\Go Server` on **Windows**.

## Configuring the Azure Elastic Agent Plugin

1. Configure the GoCD Azure Elastic Agent [Plugin Settings](PLUGIN_SETTINGS.md): The plugin settings are used to provide global level configurations for the plugin. Configurations such as Azure credentials, Azure settings like Virtual Network, region, Go Server configuration are provided in plugin settings.

    ![Alt text](readme-screenshots/azure-plugin-settings.png?raw=true "azure-plugin-settings")

2. Configure an [Elastic Profile](ELASTIC_PROFILE_CONFIGURATION.md) for GoCD Azure Elastic Agent Plugin: The Elastic Agent Profile is used to define the configuration of the Azure instance. The profile is used to configure the platform, VM image, size and the custom scripts to be run on the instance.

    ![Alt text](readme-screenshots/azure-elastic-profile.png?raw=true "azure-elastic-profile")

3. Assign the Elastic Profile to a job

    ![Alt text](readme-screenshots/assign-elastic-profile.png?raw=true "assign-elastic-profile")


