@echo off
java -Djavax.net.ssl.keyStore=server.keys -Djavax.net.ssl.keyStorePassword=123456 ChatServer %1