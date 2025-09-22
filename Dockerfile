# Dockerfile para Plataforma de Trámites
FROM openjdk:21-jdk-slim

# Metadatos
LABEL maintainer="Plataforma de Trámites"
LABEL description="Sistema de gestión de trámites urbanísticos"

# Crear directorio de la aplicación
WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Hacer ejecutable el script de Maven
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Crear directorios necesarios
RUN mkdir -p /app/storage/documents
RUN mkdir -p /app/logs

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx1024m -Xms512m"

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/plataforma-tramites-0.0.1-SNAPSHOT.jar"]