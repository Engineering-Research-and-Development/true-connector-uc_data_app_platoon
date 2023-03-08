FROM eclipse-temurin:11-jre-alpine

# Add Maintainer Info
LABEL maintainer="gabriele.deluca@eng.it"

RUN mkdir -p /home/nobody/app
RUN mkdir -p /home/nobody/data
RUN mkdir /var/log/ucapp

WORKDIR /home/nobody

# The application's jar file
COPY target/dependency-jars /home/nobody/app/dependency-jars

# Add the application's jar to the container
ADD target/dataUsage.jar /home/nobody/app/dataUsage.jar

RUN chown -R nobody:nogroup /home/nobody
RUN chown -R nobody:nogroup /var/log/ucapp

USER 65534

ENTRYPOINT java -jar /home/nobody/app/dataUsage.jar
