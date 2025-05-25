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

Run the server script with the port you want to host the server in (ex: 12345):
```bash
./run_server.sh 12345
```

Run the client script every time you want to create a client with the host and server port as arguments (ex: localhost and 5501):
```bash
./run_client.sh localhost 12345
```

If you encounter a "cannot execute: required file not found" when running the server or client shell scripts
(which may happen in WSL), use the following command (replace 'run_server.sh' with 'run_client.sh' if needed) and try again:
```bash
sed -i 's/\r$//' run_server.sh
```

## Connecting as a client
After running the client script, you are prompted to authenticate. Insert your username and password, or token, in the indicated format:
```
Connected to chat server at localhost:12345
Server: Welcome to the server! Authenticate using 'TOKEN: <your_token>' or '<username> <password>'.
Enter authentication ('<username> <password>' or 'TOKEN: <your_token>'): alfredo 123
```

## Blocking the TCP connection
If you want to block the TCP connection at the client's local port, you can run the following command on a Linux terminal
(replacing 12606 with the client's local port number):
```shell
sudo iptables -A OUTPUT -p tcp --sport 12606 -j REJECT --reject-with tcp-reset
```
To unblock the connection:
```shell
sudo iptables -D OUTPUT -p tcp --sport 12606 -j REJECT --reject-with tcp-reset
```

If you want to block the TCP connection at the server's port, run this command instead (replacing 12345 with the
server's port):
```shell
sudo iptables -A OUTPUT -p tcp --dport 12345 -j REJECT --reject-with tcp-reset
```

To unblock the connection:
```shell
sudo iptables -D OUTPUT -p tcp --dport 12345 -j REJECT --reject-with tcp-reset
```

When either the client's local port or the server's port is blocked, the client will attempt to reconnect after
the user tries sending a message or entering a room, for instance. In the former case, the reconnection should work
at the first try, as it is only required for the OS to change the client's local port. In the latter case, however,
the reconnection will only work once the server's port is unblocked.

## Interrupting the Client
When the user logs in with credentials (username and password), they get a token. If the client is interrupted
(with Ctrl+C, for example), they can login again with the token from the previous session and they will automatically
rejoin the room they were in.