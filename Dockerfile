FROM openjdk:11.0.15-jre

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

# The application's jar file
COPY target/dependency-jars /run/dependency-jars

# Add the application's jar to the container
ADD target/dataUsage.jar /run/dataUsage.jar

ENTRYPOINT java -jar run/dataUsage.jar

