# Configure plugin settings

* Navigate to **_Admin > Plugins_** in the main menu
* Click on gear icon next to the `GoCD Elastic Agent Plugin for Azure` and enter the required plugin configuration.

![Alt text](readme-screenshots/azure/plugin_settings/plugin_listing.png?raw=true "Plugin Settings")

**_Note:_** *Configuration marked with (\*) are mandatory*

## Go Server configuration

![Alt text](readme-screenshots/azure/plugin_settings/go_server_configuration.png?raw=true "Go Server configuration")

1. **GoCD Server URL\*:** This is used by the instance to register with GoCD server. Server hostname must resolve in your container. Don't use `localhost`.

2. **Agent autoregister timeout (in minutes)\*:** If an agent running on a virtual machine created by this plugin does not register with this server within the specified timeout period, the plugin will assume that the instance failed to startup and will be terminated.

3. **Agent idle timeout (in minutes):** Agent virtual machines will be kept idle for this period before termination, after job completion. This setting can be overridden at the elastic profile.

## Azure configuration

![Alt text](readme-screenshots/azure/plugin_settings/azure_configuration.png?raw=true "Azure configuration")

1. **Resource Group name\*:** All the agent virtual machines(and its dependencies) will be created in this resource group.

2. **Region name\*:** The agent virtual machines are created within the specified Azure region. Read more about [Azure Regions](https://azure.microsoft.com/en-in/global-infrastructure/locations/)

3. **Virtual Network Id\*:**  Enter the  resource id of the virtual network in which all the agent virtual machines are created. Use Azure client to get the Virtual Network Id of an existing network.

4. **Subnet name(s)\*:** Enter comma separated subnet names. If multiple subnet names are specified, the plugin chooses a random subnet and creates the agent in it. User can override this from elastic profile.

5. **Network Security Group Id:**  A network security group contains several default security rules that allow or deny traffic to or from resources. Enter the resource id of the network security group to be assigned to the virtual machine.


## Azure Credentials

A service principal is required by an application to deploy or configure resources through Azure Resource Manager in Azure Stack.

Create the service principal using the following [guide](https://docs.microsoft.com/en-us/cli/azure/ad/sp?view=azure-cli-latest#az-ad-sp-create-for-rbac)

Note down the `Client Id`,`Secret`, `Domain/Tenant Id` from the response and enter the below fields.

![Alt text](readme-screenshots/azure/plugin_settings/credentials.png?raw=true "Azure Credentials")


## Platform Settings

These settings applied based on the platform chosen for the agent virtual machine at Elastic profile level.

For Linux username/sshkey and for Windows username/password fields are captured and applied based on the profile chosen
at the Elastic profile level.

![Alt text](readme-screenshots/azure/plugin_settings/platform_settings.png?raw=true "Platform Settings")
