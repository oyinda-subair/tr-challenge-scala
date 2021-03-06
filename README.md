# Trade Republic Coding Challenge

This challenge has been rewritten in Scala and uses mongodb as datastore

# Setup

## Framework Requirements
- JVM running on your local machine
- SBT
- Akka Http
- Docker v3
- An IDE of your choice


### Running the app
The app requires mongoDB as database and partner service which has been added to the docker-compose file
```shell
docker-compose up -d
```
To run the app you can use the following gradle commands
```
sbt compile
sbt test
sbt run
```

Once the server is running you can check the results at
```
http://localhost:9000/candlesticks?isin={ISIN}
```