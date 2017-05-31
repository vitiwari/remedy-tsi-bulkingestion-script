# remedy-meter-script
Script for ingestion of Remedy Incidents and Change tickets into Truesight Intelligence as events.

## Prerequisite 
	- Java Sdk 1.8
	- maven
	
## How to build ? 
	- Clone this repository.
	  $ git clone https://github.com/vitiwari/remedy-meter-script/
	- Change the directory.
	  $ cd remedy-meter-script
	- Run maven install
	  $ mvn install
	- You can find the build jar file as remedy-meter-script-0.0.1-SNAPSHOT-full.jar  

## How to run ?
	- Copy jar file to the same location as file changeTemplate.json, incidentTemplate.json
	- Change the incidentTemplate.json/changeTemplate.json configuration (based on description below)
	- Run jar file
	  ####java -jar remedy-meter-script-0.0.1-SNAPSHOT-full.jar <incident> <change>
## Configuration
   The configuration file contains three major sections.
   - "config":{}
   - "payload":{}
   - "@placeholder" : {}
   1) The config element contains all the required configurations to run this script.

||config field and value 								|| Details/comments						||
|    "config": {									   	|                                       |
|		"remedyHostName":"clm-XXX-XXXXX.bmc.com",      | Provide the remedy Host name          |
|  		"remedyPort":"",                                | Provide the remedy port (Not required)|
|  		"remedyUserName":"XXXXXX",                    | Provide the remedy UserName           |
|   	"remedyPassword":"XXXXXX",                    | Provide the remedy Password           |
|   	"tsiEventEndpoint" : "https://api.truesight-staging.bmc.com/v1/events",| TSI events endpoint based on credentials |
|  		"tsiApiToken":"XXXXXXXXXXXXXXXXXXXXXXXXXXXXX",| TSI API Token                |
|  		"chunkSize":5,                                | No of tickets read and ingested in one chunk |
|  		"conditionFields":[1000000564,3],             | List of fields to create condition (see blow for details)|
| 		"startDateTime":"2017-01-01 00:00 AM GMT+1:00",| Start Date of Remedy conditionFields   |
| 		"endDateTime":"2017-05-29 00:00 AM GMT+1:00",| end date of remedy conditionFields 		|
|  		"retryConfig":3,                            | Retry configuration, in case of failure   |
| 		"waitMsBeforeRetry":5000                    | Time in ms to wait before next retry		|
|	},                                              |    end									|