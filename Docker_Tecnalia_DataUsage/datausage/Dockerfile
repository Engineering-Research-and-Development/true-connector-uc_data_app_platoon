#FROM openjdk:11-jdk-slim
FROM openjdk:11.0.12-jre
WORKDIR tmp
COPY *.jar datausage.jar
#COPY etc/platoon_datausage_cape.properties /etc/platoon_datausage_cape.properties
ENTRYPOINT ["java","-jar","datausage.jar"]
