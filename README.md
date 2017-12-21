# HTTP and WebDav proxy
### Concepts
* Handler does one thing
* Handler is responsible for removing itself from pipeline when needed
* Use only one thread model (Netty, beware of two channels)
* ConnectionHandlers (Upstream and Downstream) propagates messages to the other channel
* Dispose of channel in any case of trouble
* Hooks to peek or transform request of responses (functions returning promises)
### TODO:
* Channel ID ~ Session ID
* HTTP proxy
* WebDav proxy
    * Litmus test
* Reuses connections
* HTTP pipelining
* Flush on read complete events or on some size? 
* Fire events to correct part of channel pipeline ? Could speed up stuff
### Vocabulary:
```
 _ _ _ _ _        _ _ _ _       _ _ _ _ _ _ 
|          |     |       |     |            |
| UPSTREAM | --- | PROXY | --- | DOWNSTREAM |
|_ _ _ _ _ |     |_ _ _ _|     | _ _ _ _ _ _|
```
* Upstream == client, browser
* Dowstream == backend server
