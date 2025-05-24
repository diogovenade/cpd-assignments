#!/bin/bash
java -cp .:json-20240303.jar -Djavax.net.ssl.keyStore=server.keys -Djavax.net.ssl.keyStorePassword=123456 ChatServer $1