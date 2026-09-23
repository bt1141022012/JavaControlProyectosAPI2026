# ==========================================
# ETAPA 1: Compilación de la aplicación
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar el archivo de configuración de dependencias para aprovechar el caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar omitiendo pruebas para agilizar la construcción
COPY src ./src
RUN mvn package -DskipTests

# ==========================================
# ETAPA 2: Imagen liviana de ejecución (JRE)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el ejecutable .jar generado en la etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Variables de entorno para la base de datos MySQL en Aiven
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver" \
    SPRING_DATASOURCE_URL="jdbc:mysql://mysql-3f038c52-esfe-d7fd.j.aivencloud.com:14102/practica20260916?sslMode=REQUIRED" \
    SPRING_DATASOURCE_USERNAME="avnadmin" \
    SPRING_DATASOURCE_PASSWORD="AVNS_B03YxoTLw_FI1FoTB9Z" \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Puerto en el que se ejecuta Spring Boot por defecto
EXPOSE 8080

# Comando para iniciar la aplicación Java
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]