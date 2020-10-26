# Meetup RSVP API Trending Topics

## How to run

To run the application, you will need to run these following commands in order
from the project root:
1. `docker build -t cdev .`
2. `docker run -p 127.0.0.1:9042:9042 cdev`
3. `sbt "runMain com.zhangmin.meetup.LoadProducer"`
4. `sbt "runMain com.zhangmin.meetup.RSVPStreamer"`
5. `sbt "runMain com.zhangmin.meetup.TrendingTopics"`

Then, simply `curl -X GET localhost:8081/topk` and you will see real-time trending topics.
The default GET request with no query parameters will display the top 10 trending topics 
across all locations in the past 5 mins in real-time. 

To change the range of data queried, you can use the query params `until` (default is
 `System.currentTimeMillis())` and `interval` (default is 5)

You can also change the number of displayed topics with query param `count` (default is 10).

Filtering can be done on the country or city by using query params `country` and `city`.
Note that the only countries/cities that will return any data are listed in 
`src/main/resources/countries.json` as the data does not actually come from the 
Meetup websocket.

Here is an example request:

`/topk?until=2020-10-25T22:00:00-04:00&interval=60&count=100&city=Montreal` 
returns the top 100 trending topics in Montreal between `2020-10-25T22:00:00` and 
`2020-10-25T21:00:00`.

## Design

#### Overall Design

The system works by consuming events from a websocket and streaming the into a Cassandra cluster
using Spark. A separate service which has access to the Cassandra cluster exposes a REST API 
allowing range queries and filtering, and will return the top K topics.

This design was chosen as it will satisfy the real-time and historical trend requirements, and 
will be horizontally scalable.

#### LoadProducer 

This exists because the Meetup RSVPs websocket is deprecated, and I did not have 
the credentials to access the new API. The locations list was manually created 
and the topic list was generated using this command.

`curl https://www.meetup.com/topics/ | egrep -oh '"/topics/[^"]*/"' | sed 's/"//g' | sed 's/topics//' | sed 's/\///g' | sort | uniq` 

The load producer exposes a websocket and will push 100 RSVPs per second to any connection 
(rate is configurable). RSVP events use the system clock millis as the timestamp, and 
this will be the basis of the range queries.

#### RSVPStreamer

This component streams data using spark-streaming from the websocket, transforms it into DTOs 
and inserts the data to Cassandra.

#### TrendingTopics

This component is the API exposed to the user allowing range queries on the dataset.

## Improvements

To improve performance of the system, one approach could be to add the country/city to the partition key
so that data is partitioned by location. From my understanding, trends are very region dependent, so
we can expect reads to target specific regions more frequently. Partitioning the data by region and time
will help us keep the partition sizes small, and range queries will be more performant. Similarly, we can
increase the granularity of the time component of the partition key (i.e. partition by hour). 

Another thing that can be done is to use a timeseries database (e.g. InfluxDB). I did not use one here 
as I am not very familiar with them, but it seems like the data would lend itself well to a time series db. 