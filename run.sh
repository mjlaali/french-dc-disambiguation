#!/bin/bash

java -Xmx2g -cp target/french.connective-disambiguation-2.1.0-SNAPSHOT.jar:target/dependency/* ca.concordia.clac.discourse.FrConnectiveClassifier "$@"
