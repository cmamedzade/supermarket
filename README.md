# supermarket

The Akka gRPC and Akka actor models are used in this project. The intention was to show how Akka HTTP and Actor model works together.
The Actor system spawns ten actors then actors register themselves with the receptionist. Every command and query contains a retail id that corresponds actor name, query&command with this id finds the right actor and sends a message then a specific actor handles that command or query. 
Alpakka cassandra is used for persistanse. Cassandra is used in the docker image. You have to change the docker container name or add an extra line to take the docker name from OS.
