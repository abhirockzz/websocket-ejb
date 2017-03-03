Bunch of tests for scenario where WebSocket endpoints are decorated with EJBs. This is might be helpful when reading through the [EJB integration section of the Java WebSocket API Handbook
](https://abhirockzz.gitbooks.io/java-websocket-api-handbook/content/part-1-tying_in_with_the_java_ee_platform.html#decorating-websocket-endpoints-with-ejb). This helps validate the behavior which might be a source of confusion since there are two specifications at play here. This is not something which is supported by the WebSocket specification but it is supported by [Tyrus](https://tyrus.java.net/) (hence you can try this on [GlassFish](https://glassfish.java.net/) or [Payara](http://www.payara.fish/))

## Scenarios

- [case 1](https://github.com/abhirockzz/websocket-ejb/blob/master/tests/src/test/java/io/gitbooks/abhirockzz/jwah/websocketejb/WebSocketEJBTest.java#L70): A new stateless EJB (from the pool) is allocated for a WebSocket client
- [case 2](https://github.com/abhirockzz/websocket-ejb/blob/master/tests/src/test/java/io/gitbooks/abhirockzz/jwah/websocketejb/WebSocketEJBTest.java#L109): A stateless EJB (once allocated) remains attached to a WebSocket client throughout it's lifecycle
- [case 3](https://github.com/abhirockzz/websocket-ejb/blob/master/tests/src/test/java/io/gitbooks/abhirockzz/jwah/websocketejb/WebSocketEJBTest.java#L146): The same Singleton bean instance is used for WebSocket clients (`Session`s)
- [case 4](https://github.com/abhirockzz/websocket-ejb/blob/master/tests/src/test/java/io/gitbooks/abhirockzz/jwah/websocketejb/WebSocketEJBTest.java#L181): A unique Stateful session bean instance is allocated to each unique client

## Build & run

There are two Maven projects

- [First one](https://github.com/abhirockzz/websocket-ejb/tree/master/application) is a simple Java EE (WAR) application with EJB-WebSocket endpoints
- The [second one](https://github.com/abhirockzz/websocket-ejb/tree/master/tests) contains the tests

Download the project to your machine. To try things out, first build and deploy the application on on GlassFish or Payara

- cd application
- `mvn clean install`
- deploy.... 

Then, run the tests

- cd tests
- `mvn test`

Note: if required, change the host port [here](https://github.com/abhirockzz/websocket-ejb/blob/master/tests/src/test/java/io/gitbooks/abhirockzz/jwah/websocketejb/WebSocketEJBTest.java#L55) before running the tests