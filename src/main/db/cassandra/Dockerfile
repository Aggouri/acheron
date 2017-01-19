# Dockerfile is used to execute Cassandra DDLs
FROM cassandra:3.9

# Installs netcat
RUN apt-get update && apt-get install -y netcat

# Copies DDL file
COPY ddl.cql ddl.cql