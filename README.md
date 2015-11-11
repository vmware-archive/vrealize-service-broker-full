#vrealize-service-broker

POC service broker that exposes vRealize blueprint services to cloud foundry.

##Prerequisits
###Environment
* The vrealize-service-broker requires a PCF that supports the 2.7 service broker API (PCF 1.6 and above). More information on this can be found [here](https://docs.pivotal.io/pivotalcf/services).
* vrealize-service-broker makes use of a mongodb datastore to hold metadata and binding information. This can be provided by installing the (mongodb tile)[https://network.pivotal.io/products/p-mongodb] into your PCF environment.
* The vrealize-service-broker has been developed against the vR 7.0 beta, current as of the ime of this writing (Nov. 11, 2015). The vRAutomation REST API end-points must be accessible to the broker.
* A MariaDB blueprint should be created and made available to a vR user, and this vR user credentials will be used to communicate with the vR RERT API by the broker.
* A local mongodb service should be installed and run on the development environment. Tips on how to set this up can be found [here](https://spring.io/guides/gs/accessing-data-mongodb/).

###Certificates
The vrA API requires SSL: if you are using a self-signed certificate you will need to install it into your local JDK certificate vault. Information on how to do this can be found [here](http://alvinalexander.com/java/java-keytool-keystore-certificates).

This command will display the cert: 
```bash
openssl s_client -connect <your.vra.api.server>:443 -showcerts
```
Copy the certificate text into a file called vra.cer
Load the cert via this command:
```bash
sudo keytool -keystore <path to your cacerts file> -importcert -alias vra -file <path to your vra.cer file>
```
This certificate must be provided to the buildpack that will be used during deployment of the broker as well. Information on this can be found [here](https://github.com/cloudfoundry/java-buildpack). 

Generally, you will fork the java buildpack, add a copy of the cacerts file you updated above, and check the forked buildpack into an accessible github repository. Details on this process canbe found [here](https://johnpfield.wordpress.com/2014/09/19/customizing-the-cloud-foundry-java-buildpack/).

##Building
You will need to download, build and maven-install the appropriate version of the [spring-boot-cf-service-broker] (https://github.com/cloudfoundry-community/spring-boot-cf-service-broker). As of the time of this writing, this would be the code in branch "async-cleanup".

```bash
cd ~/<your workspace dir>
git clone https://github.com/cloudfoundry-community/spring-boot-cf-service-broker.git
cd spring-boot-cf-service-broker
git checkout async-cleanup
mvn clean install
```
Then checkout and build this project
```bash
cd ~/<your workspace dir>
git clone https://github.com/cf-platform-eng/vrealize-service-broker.git
cd vrealize-service-broker
mvn clean install -dskipTests=true
```

##Testing
Provided the tasks above have been completed, test are configured by editing the test.properties file in the src/main/resources directory. Also, ensure that a local mongo db is installed (as per prerequisits, above).

Tests are run in the usual maven manner:
```bash
mvn clean install
```

##Deploying to PCF
Create a mongodb service in your PCF environment:
```bash
cf cs p-mongodb development vra-broker-repo
```
Edit the manifest.yml file:
```
---
applications:
- name: vrealize-service-broker
  memory: 512M
  instances: 1
  path: target/vrealize-service-broker-0.1.0.jar
  buildpack: <url to your forked java buildpack goes here>
  services: [ vra-broker-repo ]
  env:
    SERVICE_URI: <vR endpoint URL>
    VRA_USER_ID: <vR user name>
    VRA_USER_PASSWORD: <vR password>
    VRA_TENANT: <vR tenant>
```
Push the app:
```bash
cf push
```

##Registering the Broker
Once the app is pushed successfully, register and validate the the broker as follows:
###Read the broker security token from the logs
Run the following command and look for the broker's security password (there will be a new password generated each time the broker is pushed or restarted).
```bash
cf logs vrealize-service-broker --recent
```
Look for a log entry such as:
```
OUT Using default security password: bbc452a4-27e6-4d7b-b23a-4232ee5575345
```
###Register the Broker Instance
General information about this task can be found [here](https://docs.cloudfoundry.org/services/managing-service-brokers.html).
Get the url of the broker application via this command:
```bash
$ cf apps
Getting apps in org yourOrg / space dev as admin...
OK

name                      requested state   instances   memory   disk   urls   
vrealize-service-broker   started           1/1         512M     1G     vrealize-service-broker.your.host   
```
Register the broker:
```bash
cf create-service-broker vrealize-service-broker user <the broker password> <the broker url>
```
List service brokers and verify the vr service broker shows up:
```bash
$ cf service-brokers
Getting service brokers as admin...

name                      url   
p-mongodb                 https://mongodb.host:443   
vrealize-service-broker   http://vrealize-service-broker.host
```
Enable service access:
```bash
$ cf service-access
Getting service access as admin...
broker: p-mongodb
   service     plan          access   orgs   
   p-mongodb   development   all         

broker: vrealize-service-broker
   service      plan         access   orgs   
   MariaDB      MariaDB      none
   
$ cf enable-service-access MariaDB
```
Check out the services available in the broker:
```bash
$ cf marketplace
Getting services from marketplace in org jgordon / space dev as admin...
OK

service     plans         description   
MariaDB     MariaDB       Created by Merlin Glynn Nov 1, 2015
...
```

##Use the broker
Deploy a simple database application to PCF and have it bind to the services. 

One such app is the quote-service app that can be found [here](https://github.com/cf-platform-eng/quote-service/tree/mysql). Follow the instructions on the github site (for the "mysql" branch) to use this example project to demo the broker.

##Tear down the broker
To tear the broker down, delete the services bound to it, unregister the broker, and then delete the broker app itself.
```bash
$ cf delete quote-service
$ cf cf delete-service testds
$ cf delete-service-broker vrealize-service-broker
$ cf delete vrealize-service-broker
```
If the broker becomes "stuck," [see this](https://docs.cloudfoundry.org/services/managing-service-brokers.html#purge-service) to knock it out.

