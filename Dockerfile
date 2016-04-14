FROM maven:3-jdk-8

RUN apt-get update && apt-get install -y \
    gradle \
    git

WORKDIR /sccsb
RUN git clone https://github.com/spring-cloud/spring-cloud-cloudfoundry-service-broker.git
WORKDIR /sccsb/spring-cloud-cloudfoundry-service-broker
CMD ["./gradlew", "install"]

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ONBUILD ADD . /usr/src/app

ONBUILD RUN mvn install

RUN ls /usr/src/app

#RUN mkdir -p /usr/src/app
#ADD . /usr/src/app

#WORKDIR /usr/src/app
#RUN cd /usr/src/app
#RUN pwd
#RUN ls
#RUN mvn install