# Building an Industrial IoT Solution - Open Source

In this post we will show how you can build an Industrial IoT Solution with Gateway / Client and Backend fully based on Open Source Technology.
Industrial IoT means that our things are not the well-known Consumer goods as mobiles, watches or wearables but are industrial machinery. In most situtations machines or automation lines are controlled by PLC Controllers. Thus, to talk to machines you have to talk to PLC controllers.
The PLCs have all sensors and actors of the machines attached, thus you get every information about the machine and could, in theory, even change the operation of the machine.
In this example we focus on 

* describing the machine, its data and its capabilities
* reading the data from the PLC controller
* send the data from the secured shopfloor network to a (cloud) backend
* make the data there accessible to other services for e.g. visualization, further analysis, artificial intelligence, ...

We will show how all points above can be realized with the additional benefits of

* flexibility regarding PLC vendors and protocols
* generally available machine descriptions that could even be shared
* security and fine controlled access control for data of different machines or plants

by relying on the Open Source Projects Apache PLC4X, Eclipse Vorto and Eclipse Ditto.

The overall architecture is shown in Figure 1.

![Figure 1](figure_1.png)

 ## General Architecture
 
[Figure 1](#figure_1) shows the general architecture of the setup. On the Edge we have a gateway running which communicates with the PLC to fetch the necessary data.
The Gateway communicates with a (public) Vorto repository to fetch all information and parameters to get the desired data. When data is acquired the Gateway forwards the data to a Ditto instance where it is then available via HTTP or WebSockets. External Services can query the datasets there.  
 
 ## Details
 
 ### The Model - Vorto
 
In our scenario we expect the "Thing" to be some kind of machinery. To get a semantic description of the data we will collect, we need to have a semantic model of the machine, its properties and its features.
[Eclipse Vorto](https://github.com/eclipse/vorto/) provides the framework for exactly that. 
Eclipse Vorto is an open source project for semantic modelling of IoT devices. The project consists of 3 main components:
- a domain specific language - [Vortolang](https://github.com/eclipse/vorto/blob/development/docs/vortolang-1.0.md) - to describe the characteristics and capabilities of device models
- a [repository](https://vorto.eclipse.org/) to edit, manage, version and distribute the Vorto models
- [plugins](https://vorto.eclipse.org/#/generators) to transform Vorto models into different representations (e.g. JSON Schema, etc.), REST request templates (e.g. for Ditto)

For our use case, we describe the machine in an example vorto file using Vortolang:
 
```
vortolang 1.0
namespace org.apache.plc4x.examples
version 1.0.0
displayname "SimulatedPlcTwo"
description "Functionblock for SimulatedPlcTwo"

functionblock SimulatedPlcTwo {

    configuration {
        position as double
        random as double
        motorCurrent as double
        processState as int
        availability as boolean
    }

}
```
[View it in the official Vorto repository](https://vorto.eclipse.org/#/details/org.apache.plc4x.examples:SimulatedPlcTwo:1.0.0).

As described in our Vorto file, our machine exposes its position as floating-point value and some other properties, which are described in the vortofile.
This description aims towards usage of the data. So someone who later wants to access the data, e.g. for analysis has a semantic description of what the datapoints mean.

For the Gateway, we also want to add information on how to read this information from the machine. It could be, that this differs between different manufactured machines, e.g. due to changes in the machine program. Although the general properties and features of the machine are always the same.
Vorto provides a feature to achieve exactly that: [Function Block Mapping](https://github.com/eclipse/vorto/blob/development/docs/vortolang-1.0.md#function-block-mapping).

The Function Block Mapping allows adding platform- or implementation-specific information to a generic Vorto model. This helps to keep the Vorto models platform-independent and re-usable, as they only contain the semantically relevant information.
It works by enriching properties of the Vorto model with the platform-specific information - the complete model with the enriched properties can be retrieved by adding your target platform as path parameter to the REST request to the standard API: 
```
GET /api/v1/models/{model ID}/content/{target platform}
```

In our case, the information has to be read from a PLC which is connected to the machine. Thus, for each field we need to add information about the PLC, the address / memory location where the information is stored and a poll time, i.e. how often we want to read the value from the PLC and update the twin.

Our exemplary Function Block Mapping looks as follows:

```
vortolang 1.0
namespace org.apache.plc4x.examples
version 1.0.0
displayname "SimulatedPLC"
description "Mapping for SimulatedPLC"

using org.apache.plc4x.examples.SimulatedPlcTwo;1.0.0

functionblockmapping SimulatedPLC {

	targetplatform simulatedPlc
	
	from SimulatedPlcTwo.configuration.position to position with {
	    url: "s7://",
	    rate: "2000",
	    address: "%DB"
	}

}
```
[View it in the official Vorto repository](https://vorto.eclipse.org/#/details/org.apache.plc4x.examples:SimulatedPLC:1.0.0)

Here we introduce three (arbitrary) keys, which we call `url`, `rate` and `address`.
The respective values represent the url of the PLC, the rate with which we will read the respective value in milliseconds and the address inside the PLC.

Vorto automatically merges both models and exposes them via an HTTP GET Endpoint:

```
https://vorto.eclipse.org/api/v1/models/org.apache.plc4x.examples.SimulatedPlcTwo:1.0.0/content/simulatedplc
```

For the two models shown above, this looks

```
{
    "root": {
        "name": "SimulatedPlcTwo",
        "namespace": "vorto.private.julian.demo.plc4x",
        "version": "1.0.0",
        "prettyFormat": "vorto.private.julian.demo.plc4x:SimulatedPlcTwo:1.0.0"
    },
    "models": {
        "vorto.private.julian.demo.plc4x:SimulatedPLC:1.0.0": {
            "targetPlatformKey": null,
            "stereotypes": [],
            "mappingReference": null,
            "vortolang": "1.0",
            "id": {
                "name": "SimulatedPLC",
                "namespace": "vorto.private.julian.demo.plc4x",
                "version": "1.0.0",
                "prettyFormat": "vorto.private.julian.demo.plc4x:SimulatedPLC:1.0.0"
            },
            "type": "Mapping",
            "displayName": null,
            "description": null,
            "category": null,
            "fileName": "SimulatedPLC.mapping",
            "modelType": "ModelInfo",
            "references": [],
            "author": null,
            "creationDate": null,
            "modificationDate": null,
            "lastModifiedBy": null,
            "hasImage": false,
            "state": null,
            "imported": false,
            "visibility": "private",
            "referencedBy": [],
            "platformMappings": {},
            "released": false,
            "fullQualifiedFileName": "vorto.private.julian.demo.plc4x-SimulatedPLC-1.0.0.mapping"
        },
        "vorto.private.julian.demo.plc4x:SimulatedPlcTwo:1.0.0": {
            "targetPlatformKey": "simulatedplc",
            "stereotypes": [],
            "mappingReference": null,
            "vortolang": "1.0",
            "id": {
                "name": "SimulatedPlcTwo",
                "namespace": "vorto.private.julian.demo.plc4x",
                "version": "1.0.0",
                "prettyFormat": "vorto.private.julian.demo.plc4x:SimulatedPlcTwo:1.0.0"
            },
            "type": "Functionblock",
            "displayName": "SimulatedPlcTwo",
            "description": "Functionblock for SimulatedPlcTwo",
            "category": null,
            "fileName": "SimulatedPlcTwo.fbmodel",
            "modelType": "FunctionblockModel",
            "references": [],
            "configurationProperties": [
                {
                    "targetPlatformKey": "simulatedPlc",
                    "stereotypes": [
                        {
                            "name": "position",
                            "attributes": {
                                "address": "%DB",
                                "rate": "2000",
                                "url": "s7://"
                            }
                        }
                    ],
                    "mappingReference": null,
                    "mandatory": true,
                    "name": "position",
                    "description": null,
                    "type": "DOUBLE",
                    "constraints": [],
                    "attributes": [],
                    "multiple": false,
                    "primitive": true
                },
                {
                    "targetPlatformKey": "simulatedPlc",
                    "stereotypes": [],
                    "mappingReference": null,
                    "mandatory": true,
                    "name": "random",
                    "description": null,
                    "type": "DOUBLE",
                    "constraints": [],
                    "attributes": [],
                    "multiple": false,
                    "primitive": true
                },
                {
                    "targetPlatformKey": "simulatedPlc",
                    "stereotypes": [],
                    "mappingReference": null,
                    "mandatory": true,
                    "name": "motorCurrent",
                    "description": null,
                    "type": "DOUBLE",
                    "constraints": [],
                    "attributes": [],
                    "multiple": false,
                    "primitive": true
                },
                {
                    "targetPlatformKey": "simulatedPlc",
                    "stereotypes": [],
                    "mappingReference": null,
                    "mandatory": true,
                    "name": "processState",
                    "description": null,
                    "type": "INT",
                    "constraints": [],
                    "attributes": [],
                    "multiple": false,
                    "primitive": true
                },
                {
                    "targetPlatformKey": "simulatedPlc",
                    "stereotypes": [],
                    "mappingReference": null,
                    "mandatory": true,
                    "name": "availability",
                    "description": null,
                    "type": "BOOLEAN",
                    "constraints": [],
                    "attributes": [],
                    "multiple": false,
                    "primitive": true
                }
            ],
            "statusProperties": [],
            "faultProperties": [],
            "events": [],
            "operations": [],
            "superType": null,
            "fullQualifiedFileName": "vorto.private.julian.demo.plc4x-SimulatedPlcTwo-1.0.0.fbmodel"
        }
    }
}
```

The most important section for the gateway here is the `configurationProperties`, which is an array with all properties and the additional mapping attributes. For the `position` this is 

```
{
    "targetPlatformKey": "simulatedPlc",
    "stereotypes": [
        {
            "name": "position",
            "attributes": {
                "address": "%DB",
                "rate": "2000",
                "url": "s7://"
            }
        }
    ],
    "mappingReference": null,
    "mandatory": true,
    "name": "position",
    "description": null,
    "type": "DOUBLE",
    "constraints": [],
    "attributes": [],
    "multiple": false,
    "primitive": true
}
```

In the next section we will show, how we can write an according gateway, which is able to read data from a wide range of PLCs fully automated with only the data given above in the two Vorto models.
This is achieved by using the Open Source Project [Apache PLC4X](https://plc4x.apache.org).
 
### The Gateway - PLC4X
 
[Apache PLC4X](https://plc4x.apache.org) is a project which aims at providing drivers for all PLC types and other industrial buses with one single programming interface.
Its mission statement is
 
> PLC4X is a set of libraries for communicating with industrial programmable logic controllers (PLCs) using a variety of protocols but with a shared API.

For our demonstrator this has to very important implications if we use PLC4X as communication library. First, we are not tied to a single PLC Vendor with our gateway implementation and second, we dont't have to change a single line of code when switching between different PLC types.
We just use the same API and PLC4X automatically loads the suitable drivers (if they are provided in the path).

// TODO more information here?

Our gateway has 3 main tasks:

* Initially read configuration from Vorto
* Then, in a loop
    * read data from the machines PLC via PLC4X
    * send data to Eclipse Ditto to update the digital twins state
    
The implementation we show here is very simple and straightforward. For productive use one would take other aspects into consideration as more logging, restart capabilities, handling of input errors and so on.
The only input we need for the gateway is the vorto repository, the Function Block Mapping and the Eclipse Ditto connection information as well as a thing id.

In our very simple example we take them as command line arguments:

```
usage: Plc4XVortoDitto
    --ditto-endpoint <ENDPOINT>   Ditto Endpoint
    --mapping <MAPPING>           Vorto Mapping
    --model-name <MODEL>          Vorto Model Name
    --model-version <VERSION>     Vorto Model Version
    --namespace <NAMESPACE>       Vorto Namespace
    --twin-id <TWIN_ID>           Ditto Twin ID
```

The important part from the mapping above is the PLC4X connection strings, addresses and fetch rates.
For each entry we start a scheduled Task that

* connects to the PLC
* reads the field value
* disconnects from the PLC
* sends the data update to Ditto

The following snippet shows how all of this is done.
The respective parameters `url`, `address` and `rate` have been parsed from the ditto mapping that was shown above. 

```
executor.scheduleAtFixedRate(() -> {
    try (PlcConnection connection = new PlcDriverManager().getConnection(url)) {
        PlcReadResponse response = connection.readRequestBuilder()
            .addItem(FIELD_NAME, address)
            .build()
            .execute()
            .get(5, TimeUnit.SECONDS);

        if (response.getResponseCode(FIELD_NAME) != PlcResponseCode.OK) {
            logger.warn("Issue with fetching field value {}, got response {}", address, response.getResponseCode(FIELD_NAME));
            return;
        }

        // Send the Value to Ditto
        sendValueToDitto(dittoClient, thingId, name, type, response);
    } catch (Exception e) {
        logger.warn("Unable to connect to PLC4X / Execute request");
    }
}, rate, rate, TimeUnit.MILLISECONDS);
```

Note: It is pretty inefficient to have every Task handle its own connection as building up the connection
takes time, and you may hit a connection limit at the PLC. So in a real world scenario one would use 
some kind of connection pool. A simple pool is provided by the PLC4X project as [connection-pool](https://github.com/apache/plc4x/tree/develop/plc4j/tools/connection-pool).

### The Backend - Ditto

The final piece for our solution is a system that stores all values that we read from the devices and makes them accessible for other systems that want to use the data.
In a real world use case there are many requirements like

* security
* fine grained access control
* convenient APIs
* allow partial updates (not always update all properties at once)

// TODO Kevin: hast du noch ne Idee hier?

[Eclipse Ditto](https://www.eclipse.org/ditto/) was built exactly for this use case and fulfills all requirements above easily.

// TOOD bissl mehr über Ditto schreiben und warum das so geil ist

In our example there are two things where ditto is involved.
At start of the program we want to check if a device with the given `thingId` is already created in ditto or not.
And if not, we create the device based on the information given from Vorto. In fact, we can use Vortos Ditto Plugin to generate the complete 
message that will register the `thing` described by the Vorto model in Ditto.

We create the thing using the HTTP API as we already get the payload from Vortos Ditto plugin by calling

```
String dittoJsonString = doHttpGet(client, String.format("https://vorto.eclipse.org/api/v1/generators/eclipseditto/models/%s.%s:%s?target=thingJson", namespace, modelName, modelVersion));
```

which gives us the following Ditto message in JSON format

```
{
    "definition": "org.apache.plc4x.examples:VirtualMachineIM:1.0.0",
    "attributes": {
        "modelDisplayName": "VirtualMachine"
    },
    "features": {
        "virtualmachine": {
            "definition": [
                "org.apache.plc4x.examples:VirtualMachine:1.0.0"
            ],
            "properties": {
                "configuration": {
                    "position": 0,
                    "random": 0,
                    "motorCurrent": 0,
                    "processState": 0,
                    "availability": false
                }
            }
        }
    }
}
```
The only thing that we need to add to this JSON structure is a `thingId` key. Then we can send it to the respective `PUT` Endpoint 

```
PutMethod createTwin = new PutMethod("https://" + dittoEndpoint + "api/2/things/" + thingId);
```

which will either create the thing if it does not yet exist or does nothing if it already exists with the respective settings.

As the device now exists in Ditto we can start to fetch data via PLC4X as described above and send the updated properties to Ditto. Ditto offers multiple ways of communication but in this example we use the websocket communication and rely on the [ditto-client](https://github.com/eclipse/ditto-clients) library that is provided
and maintained by the Ditto team to have an easy DSL for communication with Ditto.

In the code above the method `sendValueToDitto(dittoClient, thingId, name, type, response)` was called, so we will look a bit deeper into this method.
PLC4X is pretty clever with its API so it provides a `getObject` method which returns the value in the right type but we need it typed for Ditto.
But, we have the type information from Vorto so we can check and use PLC4Xs strongly typed getters, this is what this method does.
The call `feature.putProperty` is polymorphic so it is important that the argument has the right Java type. 

We use the method `feature.putProperty` which only changes the given path in the property and does not overwrite the complete feature.
This is important as each property is fetched separately in the scheduled tasks and sent independently.

```
private static void sendValueToDitto(DittoClient dittoClient, String thingId, String name, String type, PlcReadResponse response) {
    String propertyPath = "configuration/" + name;
    // Fetch value from PLC4X Response
    if ("DOUBLE".equals(type)) {
        double value = response.getDouble(FIELD_NAME);
        sendInternal(dittoClient, thingId, feature -> feature.putProperty(propertyPath, value));
    } else if ("BOOLEAN".equals(type)) {
        boolean value = response.getBoolean(FIELD_NAME);
        sendInternal(dittoClient, thingId, feature -> feature.putProperty(propertyPath, value));
    } else if ("INT".equals(type)) {
        int value = response.getInteger(FIELD_NAME);
        sendInternal(dittoClient, thingId, feature -> feature.putProperty(propertyPath, value));
    } else {
        throw new NotImplementedException("Currently type " + type + " is not implemented!");
    }
}
```

The final method we have to inspect is `sendInternal(DittoClient dittoClient, String thingId, Function<TwinFeatureHandle, CompletableFuture<Void>> setter)`, which is shown below.
 
```
private static <T> void sendInternal(DittoClient dittoClient, String thingId, Function<TwinFeatureHandle, CompletableFuture<Void>> setter) {
    TwinFeatureHandle twinFeatureHandle = dittoClient.twin()
        .forId(ThingId.of(thingId))
        .forFeature("VirtualMachine".toLowerCase());
    setter.apply(twinFeatureHandle)
        .handle(new BiFunction<Void, Throwable, Object>() {
            @Override
            public Object apply(Void aVoid, Throwable throwable) {
                if (throwable != null) {
                    logger.info("Unable to send update to Ditto");
                    logger.warn("Unable to send update to Ditto", throwable);
                } else {
                    logger.info("Sent update to Ditto!");
                }
                return null;
            }
        });
}
```

It uses the `ditto-client` API to send the property update to Ditto asynchronously.

// TODO how to get values from ditto?
