# Control Proyectos API

## Requisitos

- Docker con Docker Compose.
- Newman usa temporalmente la contraseña inicial del administrador escrita directamente en `compose.newman.yaml`.

## Arranque básico

Inicia MySQL y la API, sin insertar datos iniciales:

```bash
docker compose up --build -d
```

La base de datos se conserva en el volumen `mysql_data`. Para detener los servicios sin borrar los datos:

```bash
docker compose down
```

## Datos semilla

El servicio de seed crea los roles y el administrador inicial. Úsalo cuando quieras probar la API con ese usuario, por ejemplo, al iniciar una base nueva. No se ejecuta con el arranque básico.

```bash
docker compose -f compose.yaml -f compose.seed.yaml up --build -d
```

El script evita duplicar roles y el usuario `admin` si ya existen. La carga no borra ni reinicia la base de datos.

## Pruebas con Newman

La colección inicia sesión como `admin`. En una base nueva, primero levanta la API y ejecuta el seed en segundo plano:

```bash
docker compose \
  -f compose.yaml \
  -f compose.seed.yaml \
  up --build -d
```

Luego ejecuta Newman por separado:

```bash
docker compose \
  -f compose.yaml \
  -f compose.newman.yaml \
  up --build --abort-on-container-exit --exit-code-from newman
```

Si el administrador ya existe en la base de datos, basta con iniciar la base y la API usando `docker compose up --build -d`. Newman espera a que la API responda antes de ejecutar la colección. Al terminar, los reportes se guardan en `postman/newman.xml` y `postman/newman-report.html`.

Si cambias la contraseña inicial en `database/seed-admin.sql`, actualiza también el valor de `adminPassword` en `compose.newman.yaml`.

## Prueba con k6

El escenario inicia sesión con el administrador semilla y cada uno de los 50 usuarios virtuales crea 10 categorías con su token. En una base nueva, carga el administrador antes de la prueba:

```bash
docker compose -f compose.yaml -f compose.seed.yaml up --build -d
```

Ejecuta la prueba con:

```bash
docker compose \
  -f compose.yaml \
  -f compose.k6.yaml \
  up --build --abort-on-container-exit --exit-code-from k6
```

El dashboard en vivo queda disponible en `http://localhost:5665` mientras corre k6. Al finalizar, el reporte HTML se guarda en `k6/reports/k6-report.html`. La prueba requiere el seed porque inicia sesión como `admin`; cada ejecución agrega hasta 500 categorías. Para detener y eliminar los contenedores al finalizar:

```bash
docker compose -f compose.yaml -f compose.k6.yaml down
```

Para detener y eliminar los contenedores después de las pruebas, sin borrar el volumen de la base de datos:

```bash
docker compose down
```