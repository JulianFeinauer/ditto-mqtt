# Building an Industrial IoT Solution - Open Source

In this post we will show how you can build an Industrial IoT Solution with Gateway / Client and Backend fully based on Open Source Technology.
Industrial IoT means that our things are not the well known Consumer goods as mobiles, watches or wearables but are industrial machinery. In most situtations machines or automation lines are controlled by PLC Controllers. Thus, to talk to machines you have to talk to PLC controllers.
The PLCs have all sensors and actors of the machines attached, thus you get every information about the machine and could, in theory, even change the operation of the machine.
In this example we focus on 

* describing the machine, its data and its capabilities
* reading the data from the PLC controller
* send the data from the secured shopfloor network to a (cloud) backend
* make the data there accessible to other services for e.g. visualization, further analysis, artifical intelligence, ...

We will show how all points above can be realized with the additional benefits of

* flexibility regarding PLC vendors and protocols
* generally available machine descriptions that could even be shared
* security and fine controlled access control for data of different machines or plants

by relying on the Open Source Projects Apache PLC4X, Eclipse Vorto and Eclipse Ditto.

The overall architecture is shown in Figure 1.

![Figure 1](figure_1.png)

 ## General Architecture
 
[Figure 1](#figure_1) shows the general architecture of the setup. On the Edge we have a gateway running which communicates with the PLC to fetch the necessary data.
The Gateway communicates with a (public) Vorto repository to fetch all informations and parameters to get the desired data. When data is aquired the Gateway forwards the data to a Ditto instance where it is then available via HTTP or WebSockets. External Services can query the datasets there.  
 
 ## Details
 
 ### The Model - Vorto
 
 ### The Gateway - PLC4X
 
 ### The Backend - Ditto