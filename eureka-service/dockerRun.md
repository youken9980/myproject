### maven build
```
mvn clean package dockerfile:build -Dmaven.test.skip=true -U
```

### create network
```
docker network create -d mynet
```

### single
```
docker rm $(docker stop $(docker ps -q -f name=eureka-node*))

docker run -it -d -p 8761:8761 \
  -e hostName="eureka-node1" \
  -e serverPort="8761" \
  --network mynet --name eureka-node1 \
  local/eureka-service --spring.profiles.active=single
docker logs -f eureka-node1
```

### multiple
```
docker rm $(docker stop $(docker ps -q -f name=eureka-node*))

docker run -it -d --expose 8761 \
  -e hostName="eureka-node3" \
  -e serverPort="8761" \
  -e eurekaServerUrl="http://eureka-node1:8761/eureka/,http://eureka-node2:8761/eureka/" \
  --network mynet --name eureka-node3 \
  local/eureka-service --spring.profiles.active=multiple
docker run -it -d --expose 8761 \
  -e hostName="eureka-node2" \
  -e serverPort="8761" \
  -e eurekaServerUrl="http://eureka-node1:8761/eureka/,http://eureka-node3:8761/eureka/" \
  --network mynet --name eureka-node2 \
  local/eureka-service --spring.profiles.active=multiple
docker run -it -d -p 8761:8761 \
  -e hostName="eureka-node1" \
  -e serverPort="8761" \
  -e eurekaServerUrl="http://eureka-node2:8761/eureka/,http://eureka-node3:8761/eureka/" \
  --network mynet --name eureka-node1 \
  local/eureka-service --spring.profiles.active=multiple

docker logs -f eureka-node1
```
