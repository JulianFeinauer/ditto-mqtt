```
curl -X POST -u devops:foobar -H 'Content-Type: application/json' -d '{
    "targetActorSelection": "/system/sharding/connection",
    "headers": {
    	"aggregate": false
    },
    "piggybackCommand": {
        "type": "connectivity.commands:createConnection",
        "connection": {
            "id": "mqtt-connection",
            "connectionType": "mqtt",
            "connectionStatus": "open",
            "failoverEnabled": true,
            "uri": "ssl://schleuninger:PragMinds2k17@farmer.cloudmqtt.com:23081",
            "sources": [{
                "addresses": [
                    "ditto/incoming"
                ],
                "consumerCount": 1,
                "qos": 2,
                "authorizationContext": [
                    "nginx:mqtt"
                ],
                "replyTarget": {
                    "enabled": true,
                    "address": "{{ header:reply-to }}"
                }
            }],
            "targets": [{
                "address": "ditto/messages/{{ thing:id }}",
                "topics": [
                    "_/_/things/live/messages"
                ],
                "authorizationContext": ["nginx:mqtt"],
                "qos": 0
            }]
        }
    }
}' https://twin.pragmaticindustries.de/devops/piggyback/connectivity?timeout=80000
```

```
curl -X POST -u devops:foobar -H 'Content-Type: application/json' -d '{
    "targetActorSelection": "/system/sharding/connection",
    "headers": {
    	"aggregate": false
    },
    "piggybackCommand": {
        "type": "connectivity.commands:createConnection",
        "connection": {
            "id": "mqtt-events-consumer",
            "connectionType": "mqtt",
            "connectionStatus": "open",
            "failoverEnabled": true,
            "uri": "ssl://ditto:ditto@farmer.cloudmqtt.com:23081",
            "sources": [],
            "targets": [{
                "address": "ditto/events/{{ thing:id }}",
                "topics": [
                    "_/_/things/twin/events"
                ],
                "authorizationContext": ["nginx:mqtt"],
                "qos": 0
            }]
        }
    }
}' https://twin.pragmaticindustries.de/devops/piggyback/connectivity?timeout=80000
```