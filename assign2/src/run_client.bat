@echo off
java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=123456 ChatClient %1 %2