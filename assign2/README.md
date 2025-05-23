## Running Server and Client

Compile the java classes:
```bash
cd src
javac -cp json-20240303.jar ChatClient.java BotClient.java ChatRoom.java ChatServer.java
```

Make sure the scripts are executable:
```bash
chmod +x ./run_server.sh 
chmod +x ./run_client.sh
```

Run the server script with the port you want to host the server in (ex: 5501):
```bash
./run_server.sh 5501
```

Run the client script every time you want to create a client with the host and server port as arguments (ex: localhost and 5501):
```bash
./run_client.sh localhost 5501
```

If you encounter a "cannot execute: required file not found" when running the server or client shell scripts
(which may happen in WSL), use the following command (replace 'run_server.sh' with 'run_client.sh' if needed) and try again:
```bash
sed -i 's/\r$//' run_server.sh
```

## Connecting as a client
After running the client script, you are prompted to authenticate. Insert your username and password, or token:
```
Connected to chat server at localhost:5501
Server: Welcome to the server! Authenticate using 'TOKEN: <your_token>' or '<username> <password>'.
Enter authentication ('<username> <password>' or 'TOKEN: <your_token>'): alfredo 123
```

