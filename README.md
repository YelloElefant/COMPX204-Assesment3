# COMPX204-Assesment 3

## TftpServer
The TftpServer runs on a specified port or defaults to 69, the server accepts any [RRQ](#rrq) packet, then makes a [TftpClient](#tftpclient) to handle
the packet. Server prints the workers name, clients adress, clients port, workers port, and the file it is serving. Then loops back to accept the next packet.

### To start the server
`java TftpServer <port>` will run the server once the `TftpServer.java` file has been compiled.

### Args for server 
the server accepts 1 arguemnt `port number` this must be a valid port number, and the server must have access to bind to that port
else the server will throw and exit.

---
## TftpWorker
The TftpWorker is made from the [TftpServer](#tftpserver) each worker can handle 1 Client, AKA 1 [RRQ](#rrq) packet. The worker takes the [RRQ](#rrq) packet, parses the file name from the data secontion of the packet, reads the file, splits the file contents into blocks each of size 512 bytes. then sends each block 1 by 1 to the client with a leading block number, waiting after each send for a [ACK](#ack) packet to be sent back with a matching block number 
if the worker doesnt receive a [ACK](#ack) packet after 5 seconds the worker will re send the data packet, if the worker resends the data packet 5 times the server will acknoledge that the client is no longer conected and will close the conection. 
to signal that the last packet has been sent to the client the worker does 1 of 2 things.
* sends a block containing less than 512 bytes of data, this is a packet of size less than 514 bytes (see [Packet Types](#packet-types)) 
* sends a block containing 0 bytes of data, this is a packet of size 2. 
the server then prints `"<worker name>: Last block sent"` then dies
---
## TftpClient 
this is a custom made client to request a file from the [TftpServer](#tftpserver). The client first sends a [RRQ](#rrq) packet to the server, then waits for the data packets to be returned, after each data packet the client pareses the data, and the block number out, and then writes the block to the destination file specified. if a duplicate block is recived this packet is ignored but a [ACK](#ack) packet is sent in response to this packet with its block number.

### To start the client
`java TftpClient <server>:port/<filePath> <saveLocation>` will run the client once the `TftpClient.java` file has been compiled.
### Args for server
The client accepts 2 arguments:
* `<server>:<port>/<filePath*>` -> this specifys the servers locaiton and what port to go in on and what file to request.
    * `<server>` -> ip adress or domain name of the server (not required), will default to `localhost`
    * `<port>` -> the port number the server listening on (not required), will default to `69`
    * `<filePath>` -> the file path of the requested file (required)
    * Acceptible forms:
        * `<server>/<filePath>` -> result = server specified, file specified, using default port
        * `/<filePath>` -> result = file Specified, default server and port
        * `<file Path>` -> result = file Specified, default server and port
        * `:<port>/<file Path>` -> result = file specified, port specified, default server
    * Not acceptible forms: if specifing the server or port file path MUST be seperated with a '/' server and port MUST seperated with a ':'
        * `:<port><file Path>`, `<server><filePath>`
        * `<server><port>/<filePath*>`
* `<saveLocation>` -> this specifys the save location to save the incoming file to.
---
## Packet Types
### RRQ
This is a file request packet ushally sent from the [client](#tftpclient), taking form, `[TYPE, FILENAME]`, the type for this packet is `1`
### DATA
This is DATA packet ushally sent from the [server](#tftpserver), taking the form `[TYPE, BLOCKNUMBER, DATA]`, the type for this packet is `2`, and the block number is the current blocks index in the list of blocks being sent
### ACK
This is an acknoledgement packet ushally sent by the [client](#tftpclient), taking the form `[TYPE, BLOCKNUMBER]`, the type for this packet is `3`, and the block number is the block number of the data packet just received
### ERROR
This is a error packet sent when an error has occored and one side needs to tell the other about it, taking the form `[TYPE, ERRORCODE]`, type for this packet is `4`, and the error code is the error thrown see [Error Codes](#error-codes)
## Error Codes
List of error codes are:
1. `File not found`
1. `Access violation`
1. `Disk full or allocation exceeded`
1. `Illegal TFTP operation`
1. `Unknown transfer ID`
1. `File already exists`
1. `No such user`
1. `ACK violation`
