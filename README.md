### Photobox Coding exercise

## Description
The context of this coding exercise is to implement a small application that will handle events
related to the usage of some photos.
 
I found the exercise's description a bit ambiguous, particularly for the Deletion operation.
Thus, I consider deleting all photos related to a given Event.

## Technical Stack
I choose to use Scala and Akka HTTP, backed with Akka Actor to develop the application.

For the Json parsing part, I choose spray-json.

## Usage
You will find a fat jar build with ```sbt assembly```, so a simple 
```
java -jar monapplication.jar
```
will run the application.

Two endpoints are available:
 - one for the Creation/Update/Deletion of an Event (replace the eventType's value with one from 
 ```"CREATE", "UPDATE", "DELETE"```):
 ```
  curl --location --request POST 'http://localhost:8080/notifUsage' \
    --header 'Content-Type: application/json' \
    --data-raw '{
    "eventId" : "f7c80f1e-1d08-11eb-9e2e-f74124011ce1",
    "source" : "editor",
    "sourceId" : "5b59848c-1d12-11eb-9951-6fcd478d7426",
    "eventType": "CREATE",
    "date" : "2020-11-02T12:44:36.685Z",
    "photoIds" : [ 2136515453, 2136515454]
  }'
 ```
 - one for the Retrieval of some sources used for some given photos' id:
  ```
  curl --location --request GET 'http://localhost:8080/getPhotoUsage?photoIds=2136515454,2136515453' \
  --header 'Content-Type: application/json'
  ```

You will also find some unit tests for different cases, from the Json serialization to the endpoints testing, and the data consistency.