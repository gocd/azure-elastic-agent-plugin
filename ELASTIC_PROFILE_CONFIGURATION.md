# Create an elastic agent profile for Azure
The elastic agent profile provides an ability to configure an Azure Instance with the required OS, size and other parameters.

* Navigate to **_Admin > Elastic Agent Profiles_** in the main menu
* Click the **_Add_** button to create a new elastic agent profile

![Alt text](readme-screenshots/azure/elastic_profile/profile_listing.png?raw=true "Elastic Profile")

**_Note:_** *Configuration marked with (\*) are mandatory*

* Choose the Plugin ID and provide the name for the elastic profile to be created.

![Alt text](readme-screenshots/azure/elastic_profile/elastic_profile_1.png?raw=true "Elastic Profile")

#Azure instance configuration

![Alt text](readme-screenshots/azure/elastic_profile/elastic_profile_2.png?raw=true "Elastic Profile")

1. **Operating system\*:** Choose the platform or the operating system to be installed on the Azure instance.

2. Provide one of the two options to setup the platform on the instance.

    a. **Image URN:** Specify the base [image urn](https://docs.microsoft.com/en-us/azure/virtual-machines/windows/cli-ps-findimage#terminology) from Azure market place for specific version of OS to be installed.

    b. **Custom Image Id:** Optionally if you have a custom image built with all the necessary dependencies associated with your subscription, provide the resource id here.

3. **Size\*:** Size of the virtual machine which defines machine configuration like number of CPU cores, memory, storage. [Available sizes](https://docs.microsoft.com/en-us/azure/cloud-services/cloud-services-sizes-specs)

4. **OS Disk Storage type\*:** Choose the storage type of OS disk [Standard SSD | Premium SSD | Standard HDD] based on the agent vm's responsibility.

5. **OS Disk size (in GB):** This field is to capture the os disk size for the agent.

6. **Custom Script:** Post provision scripts such as environment variable configuration, custom software installation scripts can be provided here.
                       Shell script for Linux and Powershell script for windows are supported. The script will be run as System user on windows.

7. **Agent Idle timeout (in minutes):** Agent virtual machines will be kept idle for this period(specified in minutes) before termination, after job completion. This setting overrides the value configured in Plugin settings.

8. **Subnet name:** Name of the subnet in which the agent virtual machine has to be created. This value has to be one of the subnets configured in plugin settings. If this field is blank, agent virtual machine will be created in one of the subnets provided in the Plugin settings.
