# Device with only definition example
````javascript
{
    "thingId": "org.pragmaticindustries:simulated-plc-2",
    "features": {
        "primary-values": {
            "properties": {
                "definition": {
                    "connectionString": "s7://192.168.167.210/0/0",
                    "scrapeRateMs": 1000,
                    "plcFields": {
                        "position": "%DB444:0.0:REAL",
                        "random": "%DB444:4.0:REAL",
                        "motor-current": "%DB444:8.0:REAL",
                        "process-state": "%DB444:12.0:UINT",
                        "availability": "%DB444:14.0:BOOL",
                        "availability1": "%DB444:14.1:BOOL",
                        "increasing-counter": "%DB444:16.0:DINT",
                        "block_cut_percent": "%DB444:20.0:INT",
                        "remaining_block_cuts": "%DB444:22.0:DINT",
                        "cut_duration_ms": "%DB444:26.0:DINT"
                    }
                },
                "values": {
                }
            }
        }
    }
}
````
# Device with Values Example
```javascript
{
    "thingId": "org.pragmaticindustries:simulated-plc-2",
    "policyId": "org.pragmaticindustries:simulated-plc-2",
    "features": {
        "primary-values": {
            "properties": {
                "definition": {
                    "connectionString": "s7://192.168.167.210/0/0",
                    "scrapeRateMs": 1000,
                    "plcFields": {
                        "position": "%DB444:0.0:REAL",
                        "random": "%DB444:4.0:REAL",
                        "motor-current": "%DB444:8.0:REAL",
                        "process-state": "%DB444:12.0:UINT",
                        "availability": "%DB444:14.0:BOOL",
                        "availability1": "%DB444:14.1:BOOL",
                        "increasing-counter": "%DB444:16.0:DINT",
                        "block_cut_percent": "%DB444:20.0:INT",
                        "remaining_block_cuts": "%DB444:22.0:DINT",
                        "cut_duration_ms": "%DB444:26.0:DINT"
                    }
                },
                "values": {
                    "availability": {
                        "v": true,
                        "t": 1588535457104
                    },
                    "availability1": {
                        "v": false,
                        "t": 1588535457105
                    },
                    "block_cut_percent": {
                        "v": 57,
                        "t": 1588535457105
                    },
                    "cut_duration_ms": {
                        "v": 29,
                        "t": 1588535457105
                    },
                    "increasing-counter": {
                        "v": 40970,
                        "t": 1588535457105
                    },
                    "motor-current": {
                        "v": 23.68844,
                        "t": 1588535457105
                    },
                    "position": {
                        "v": 2863.0784,
                        "t": 1588535457105
                    },
                    "process-state": {
                        "v": 1,
                        "t": 1588535457105
                    },
                    "random": {
                        "v": 0.21280842,
                        "t": 1588535457105
                    },
                    "remaining_block_cuts": {
                        "v": 22,
                        "t": 1588535457105
                    }
                }
            }
        }
    }
}
```

# Vorto

GET Url for mapped Model:
```
https://vorto-dev.eclipse.org/api/v1/models/vorto.private.julian.demo.plc4x.SimulatedPlcTwo:1.0.0/content/simulatedplc
```