# PlatoonDataUsage

The Data Usage Control module of PLATOON has been developed by modifying the open-source IDS Dataspace Connector . Specifically, version v5.0.1 of the Dataspace Connector has been used as the starting point for the development, extracting the Java packages required for this module. This code has been improved by adding the following new functionalities:
- 	REST services to get, upload and remove the Contract Agreements from the Contract Agreements storage. The format of these Contract Agreements is the one specified by the IDS Information Model . These contracts will be used to apply the Data Usage Control enforcement.
- 	A REST service to apply the Data Usage Control enforcement on the input data according to the Contract Agreements related to the pair consumer-producer indicated as input parameters.
- 	A new policy is supported which indicates if the data contains Personal Data, in which case the module invokes CaPe for each data set belonging to a specific data subject. CaPe will filter out the content that is consented by each data subject. 

This module does not include the Contract Negotiation process. This negotiation process is carried out outside this module and the Contract Agreements are provided to the Usage Control when negotiated.

## 1. Architecture

The Data Usage Control is composed of the following components:
-	Database store, where the Contract Agreements are stored. This database contains the following tables:
    - Contract_store: the whole contract agreement content in JSON-LD format is stored in the “contract_as_string” column.
    - Rule_store: a contract may contain several rules. Each of these rules is stored in this table and the rule content in JSON-LD format is stored in the "rule_content” column.
    -	Access_store: this table stores the number of times a consumer has accessed a specific target/data, in the “num_access” column. This table is used to apply the policy pattern “N Times Usage” 
 


- 	The Contract Agreement Controller, which implements the REST services to get, update and remove the Contract Agreements in the database.
- 	The Usage Control module, which applies the usage control enforcement over the input data according to the rules specified in the corresponding Contract Agreements.
- The REST Controller.



## 2. Enforcement

The steps to be taken to do enforcement are the following:
- 1. Once the consumer and provider connectors have negotiated and established a Contract Agreement, this Contract Agreement is stored in the Data Usage Control by invoking the corresponding REST service.
- 2. The usage control enforcement REST service is invoked before transferring the data from the Provider Connector to the Consumer Connector (parameter consuming=false), and before transferring the data from the Consumer Connector to the Data App (parameter consuming=true). This service will return the data according to the policies defined in the Contract Agreement.

The data Usage Control module supports usage policies written in the IDS Usage Control Language  based on ODRL. The policy patterns supported by the Data Usage Control module are the following ones:
- Allow the Usage of the Data: provides data usage without any restrictions.
- Prohibit the Usage of the Data: prohibits data usage.
- Interval-restricted Data Usage: provides data usage within a specified time interval.
- Duration-restricted Data Usage: allows data usage for a specified time period.
- Role-restricted Data Usage: allows data usage for a specified roles.
- Purpose-restricted Data Usage Policy: allows data usage for specified purpose.
- Restricted Number of Usages: allows data usage for n times. 
- Personal Data: filter out the contents of the data according to the data subject´s consents. To apply this rule, the Usage Control module interacts with CaPe. **


## 3  Installation Guide


### 3.1 Creating Docker image

From root of the project run following command:

```
docker build .

```

This will create docker image; specific tag can also be added to the command simply by using *-t {tag:version}* 

### 3.2 Running the Application

To start up the Platoon Data Usage, run the following command inside the directory "Docker_Tecnalia_DataUsage" of the docker-compose.yml file: docker-compose up -d

### 3.3 Database profiles

There are 2 supported database profiles:

 - PostgreSQL - this profile will require to have configured PostgreSQL as a service, either as installed application or running as docker container. To check DB related properties for PostgreSQL, please check src/main/resources/application-POSTGRES.properties</br>
 When running as SpringBoot application, set -Dprofile=POSTGRES
 - H2 (in memory db) - for faster use of UsageControl dataApp, this profile might be more suitable, since it does not require additional service, like in PostgreSQL profile.</br>
 Property file can be found in src/main/resources/application-H2.properties, and when running as SpringBoot, -Dprofile=H2
 
### 3.4 Policy creation

After starting the application, all information about API is available as Swagger documentation on: https://deh.h2020-demeter-cloud.eu/swagger.

For adding and testing contract agreements, two main controllers are:

 - Contract-agreement-controller - controller used for CRUD operations on contract agreements
 - Enforce-usage-controller-agreement - controller for enforcing existing policies
 
Examples of all policies can be found in [Policies_example](./Policies_example/) folder, from where all of them can be used for testing purpose. **

****NOTE:** Be aware of the dates in policies example, change them according to actual dates. 

## 4 License

Licensed under the Apache 2.0. See LICENSE.txt for more details. 
