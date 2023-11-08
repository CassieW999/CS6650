# MusicAlbums Assignment 2

## Introduction

For this assignment, we are required to integrate with database and server load balancer.The database will be used to store the data of the music albums. 
The server load balancer will be used to distribute the load to the servers. For database, I chose RDS Postgres because it is easy to use and the data model
is simple and fixed. For server load balancer, I chose AWS Elastic Load Balancer because it is easy to use and it is free.

## Database Model
```
TABLE albums (
    id SERIAL PRIMARY KEY,
    image BYTEA,
    artist VARCHAR(255),
    title VARCHAR(255),
    year INT
);
```
As we can see, the id is auto-incremented, thus for creating new albums, we do not 
need to specify the id. The image is stored as byte array. The artist, title and year
are stored as string, string and integer respectively, which are fetched and transformed
from doPost() request.

## Load Testing

### 1. Load Testing without Load Balancer(single server)

#### 1.1. threadGroupSize = 10, numThreadGroups = 10, delay = 2
#### 1.2. threadGroupSize = 10, numThreadGroups = 20, delay = 2
#### 1.3. threadGroupSize = 10, numThreadGroups = 30, delay = 2

### 2. Load Testing with Load Balancer(2 servers)

#### 2.1. threadGroupSize = 10, numThreadGroups = 10, delay = 2
#### 2.2. threadGroupSize = 10, numThreadGroups = 20, delay = 2
#### 2.3. threadGroupSize = 10, numThreadGroups = 30, delay = 2

### 3. Load Testing with Load Balancer(2 servers) and Database Configuration Tuning

Database Configuration Tuning:
- I upgraded the database server to a higher tier, which has more memory and CPU.

#### 3.1. threadGroupSize = 10, numThreadGroups = 30, delay = 2


### Results Comparison Table for 30 numThreadGroups
                  single server   |   2 servers   |   2 servers + Tuning
    WallTime   |      1:00.2      |   0:30.5      |   0:30.5
    Throughput |      4.99        |   9.83        |   9.83
    Requests   |      299         |   590         |   590
    Get() Mean |      0.2         |   0.1         |   0.1
    Get() p99  |      0.3         |   0.2         |   0.2
    Get() Mid  |      0.4         |   0.3         |   0.3
    Get() Max  |      0.5         |   0.4         |   0.4
    Get() Min  |      0.5         |   0.4         |   0.4
    Post() Mean|      0.2         |   0.1         |   0.1
    Post() p99 |      0.3         |   0.2         |   0.2
    Post() Mid |      0.4         |   0.3         |   0.3
    Post() Max |      0.5         |   0.4         |   0.4
    Post() Min |      0.5         |   0.4         |   0.4

