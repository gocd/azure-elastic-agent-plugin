# Create an elastic agent profile for Azure

<hr/>

Note: [GoCD 19.3.0 introduced](https://www.gocd.org/releases/#19-3-0) support for a concept called "cluster profiles". This plugin (as of version 1.0.0-5) does not support cluster profiles. So, it is necessary to directly add a dummy cluster profile to the GoCD configuration manually. This is a one-time activity. The snippet that needs to be added is below, along with an image which shows the position in which it needs to be added:

```xml
<elastic>
  <clusterProfiles>
    <clusterProfile id="no-op-azure-cluster" pluginId="com.thoughtworks.gocd.elastic-agent.azure" />
  </clusterProfiles>
</elastic>
```

![Adding a dummy cluster profile](readme-screenshots/dummy-cluster-config-xml.png?raw=true "Adding a dummy cluster profile")

<hr/>

The elastic agent profile provides an ability to configure an Azure Instance with the required OS, size and other parameters.

* Navigate to **_Admin > Elastic Agent Configurations_** in the main menu
* Click the **_+ Elastic Agent Profile_** button to create a new elastic agent profile

![List elastic profiles](readme-screenshots/azure/elastic_profile/profile_listing.png?raw=true "Elastic Profile")

**_Note:_** *Configuration marked with (\*) are mandatory*


## Azure instance configuration

![Creating an elastic profile](readme-screenshots/azure/elastic_profile/elastic_profile_1.png?raw=true "Elastic Profile")

1. Provide a name for the elastic profile to be created.

2. **Operating system\*:** Choose the platform or the operating system to be installed on the Azure instance.

3. Provide one of the two options to setup the platform on the instance.

    a. **Image URN:** Specify the base [image urn](https://docs.microsoft.com/en-us/azure/virtual-machines/windows/cli-ps-findimage#terminology) from Azure market place for specific version of OS to be installed.

    b. **Custom Image Id:** Optionally if you have a custom image built with all the necessary dependencies associated with your subscription, provide the resource id here.

4. **Size\*:** Size of the virtual machine which defines machine configuration like number of CPU cores, memory, storage. [Available sizes](https://docs.microsoft.com/en-us/azure/cloud-services/cloud-services-sizes-specs)

5. **OS Disk Storage type\*:** Choose the storage type of OS disk [Standard SSD | Premium SSD | Standard HDD] based on the agent vm's responsibility.

6. **OS Disk size (in GB):** This field is to capture the os disk size for the agent.

7. **Custom Script:** Post provision scripts such as environment variable configuration, custom software installation scripts can be provided here.
                       Shell script for Linux and Powershell script for windows are supported. The script will be run as System user on windows.

8. **Agent Idle timeout (in minutes):** Agent virtual machines will be kept idle for this period(specified in minutes) before termination, after job completion. This setting overrides the value configured in Plugin settings.

9. **Subnet name:** Name of the subnet in which the agent virtual machine has to be created. This value has to be one of the subnets configured in plugin settings. If this field is blank, agent virtual machine will be created in one of the subnets provided in the Plugin settings.
