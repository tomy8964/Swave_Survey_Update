FROM openjdk:17

WORKDIR /usr/src/app

COPY ./build/libs/surveydocument-0.0.1-SNAPSHOT.jar ./build/libs/surveydocument-0.0.1-SNAPSHOT.jar

EXPOSE 8080

EXPOSE 8082

#server
CMD ["java","-jar","-Dspring.profiles.active=server","./build/libs/surveydocument-0.0.1-SNAPSHOT.jar"]
#local
#CMD ["java","-jar","./build/libs/surveydocument-0.0.1-SNAPSHOT.jar"]
