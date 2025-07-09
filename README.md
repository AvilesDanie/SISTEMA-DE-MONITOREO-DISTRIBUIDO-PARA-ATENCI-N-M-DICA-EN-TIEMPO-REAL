# Proyecto - Instrucciones de instalación y ejecución

## 🐳 Levantar el entorno con Docker Compose

1. Inicia los contenedores:

   docker-compose up -d

2. Crear las bases de datos en CockroachDB:

   - Nodo 1:

     docker exec -it crdb-node1 ./cockroach sql --insecure

     Dentro de la consola SQL, ejecuta:

     CREATE DATABASE patientdatacollector_db;

   - Nodo 2:

     docker exec -it crdb-node2 ./cockroach sql --insecure

     Dentro de la consola SQL, ejecuta:

     CREATE DATABASE healthanalyzer_db;

   - Nodo 3:

     docker exec -it crdb-node3 ./cockroach sql --insecure

     Dentro de la consola SQL, ejecuta:

     CREATE DATABASE carenotifier_db;

---

## 🚀 Iniciar los microservicios

Inicia cada microservicio en el siguiente orden:

1. Eureka Server
2. Care Notifier
3. Health Analyzer
4. Patient Data Collector
5. API Gateway

*Asegúrate de que cada servicio se inicia correctamente antes de continuar con el siguiente.*

---

## 🧪 Pruebas con LotusC

Para ejecutar pruebas, inicia el archivo de configuración de LotusC con el siguiente comando:

   lotusc run locustfile.py

Reemplaza <nombre-del-archivo> por el archivo de definición de pruebas que vayas a utilizar.

---

