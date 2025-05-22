#! /bin/bash

# Generate keypairs for server for both RSA and DSA (default)
keytool -genkeypair -dname "cn=server, ou=CPD, o=FEUP, L=Porto, C=PT" -alias serverrsa -keyalg RSA -keystore server.keys -storepass 123456 -validity 365
keytool -genkeypair -dname "cn=server, ou=CPD, o=FEUP, L=Porto, C=PT" -alias server -keyalg DSA -keystore server.keys -storepass 123456 -validity 365

# Generate keypairs for client for both RSA and DSA (default)
keytool -genkeypair -dname "cn=client, ou=CPD, o=FEUP, L=Porto, C=PT" -alias clientrsa -keyalg RSA -keystore client.keys -storepass 123456 -validity 365
keytool -genkeypair -dname "cn=client, ou=CPD, o=FEUP, L=Porto, C=PT" -alias client -keyalg DSA -keystore client.keys -storepass 123456 -validity 365

# Export the server certificates
keytool -export -alias server  -keystore server.keys -storepass 123456 -rfc -file server.cert
keytool -export -alias serverrsa  -keystore server.keys -storepass 123456 -rfc -file serverrsa.cert

# Export the client certificates
keytool -export -alias client -keystore client.keys -storepass 123456 -rfc -file client.cert
keytool -export -alias clientrsa -keystore client.keys -storepass 123456 -rfc -file clientrsa.cert

# Import both server and client certificates to truststore

keytool -import -alias servercert -file server.cert -keystore truststore -storepass 123456
keytool -import -alias serverrsacert -file serverrsa.cert -keystore truststore -storepass 123456
keytool -import -alias clientcert -file client.cert -keystore truststore -storepass 123456
keytool -import -alias clientrsacert -file clientrsa.cert -keystore truststore -storepass 123456
