## Creating a PostgreSQL Container in Docker on WSL

1. Create a Docker configuration file on Windows.
    - Save the following text as `docker-compose.yml`. (Encoding: UTF-8 without BOM, Line endings: LF)
    - You can specify any version after `image: postgres:`. The version `17.7` is used here as an example.
    - The password `dcpgpass` after `POSTGRES_PASSWORD:` is the password for the postgres user in Docker. You can use any value.
    - `/tmp/share_docker:/tmp/share_host` links `/tmp/share_docker` on WSL with `/tmp/share_host` in Docker, creating a shared directory between Ubuntu on WSL and Ubuntu in Docker (the postgres container).

```docker-compose
services:
  postgres:
    image: postgres:17.7
    container_name: postgres
    environment:
      POSTGRES_PASSWORD: "dcpgpass"
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - /tmp/share_docker:/tmp/share_host
volumes:
  pgdata:
```

2. Copy the Docker configuration file to the Ubuntu directory on WSL.
    - Use File Explorer to copy `docker-compose.yml` to the HOME directory `\\wsl.localhost\Ubuntu\home\user01` of the user created during WSL installation (e.g., `user01`).
    - If `docker-compose.yml` already exists, append the contents under `services:` and `volumes:` above to the existing file respectively.

3. Start WSL. If WSL is already running, restart it.
    - To stop WSL: Open Command Prompt from the Start menu and run the `wsl --shutdown` command.
    - To start WSL: Run Ubuntu from the Start menu.

***All steps below are performed on WSL.***

4. Start Docker.
```Command
$ docker compose up -d
On the first run, Docker images will be downloaded, which may take some time.
```

5. Verify that the container is running.
```Command
$ docker compose ps
The container is ready when the STATUS of the postgres container shows Up or running.
```

6. Log in to Ubuntu in Docker (the postgres container).
```Command
The name after `docker exec -it` is the container name specified in `container_name` in `docker-compose.yml`.
$ docker exec -it postgres bash
```

7. Log in to PostgreSQL on Docker Ubuntu.
```Command
Switch to the postgres OS user.
# su - postgres

$ psql -h localhost -p 5432 -U postgres
```

8. Create a role (user) and a DB. (The role name `dbuser01`, password `dbpass01`, and DB name `db01` can be any values.)
```SQL
# CREATE ROLE dbuser01 WITH LOGIN PASSWORD 'dbpass01';
# CREATE DATABASE db01 OWNER dbuser01;
# GRANT ALL PRIVILEGES ON DATABASE db01 TO dbuser01;
# quit
```

9. Log in to the created DB.
```Command
$ psql -h localhost -p 5432 -d db01 -U postgres
```

10. Create a schema. (The schema name `schema01` can be any value.) ***Skip this step if using only the public schema.***
```SQL
# CREATE SCHEMA schema01 AUTHORIZATION dbuser01;
# GRANT ALL PRIVILEGES ON SCHEMA schema01 TO dbuser01;

Change the schema search path priority for the created DB. (Specify the created schema before `public`.)
# ALTER DATABASE db01 SET search_path TO schema01, public;
# quit

If a permission error occurs with CREATE SCHEMA, log in as the created user and create the schema (execute the SQL above).
```

11. Verify the current schema. ***Skip this step if using only the public schema.***
```Command
Log in to the DB as the created user.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
=> SELECT current_schema();
The output should display `schema01`.
=> \q
```

12. Log out from Docker Ubuntu.
```Command
$ exit
This exits from the `su - postgres` shell.
# exit
This logs out from Docker Ubuntu.
```

13. Connect to the DB from the host Windows using the following connection settings. (Use A5 or psqledit for the connection.)
    - HOST: localhost
    - PORT: 5432
    - DB: db01
    - USER: dbuser01
    - PASS: dbpass01


14. The DB connection settings in `db.properties` are as follows.
```properties
default.conn.url=jdbc:postgresql://localhost:5432/db01
default.conn.user=dbuser01
default.conn.pass=dbpass01
```

---
### Steps to Execute an SQL File Using psql in Docker

1. Grant write permissions to the shared directory on WSL.

```Command
$ sudo chmod 777 /tmp/share_docker/
```

2. Copy the target SQL file to the Ubuntu directory on WSL.
    - Use File Explorer to copy the SQL file to `\\wsl.localhost\Ubuntu\tmp\share_docker`.
    - /tmp/share_docker/ on WSL is mounted as /tmp/share_host/ in Docker. (Refer to the docker-compose settings above.)

3. Log in to the DB on Ubuntu in Docker (the postgres container) and execute the SQL file.
```Command
The name after `docker exec -it` is the container name specified in `container_name` in `docker-compose.yml`.
$ docker exec -it postgres bash

Switch to the postgres OS user.
# su - postgres

Log in to the DB.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
Example: Change the client character encoding, navigate to the directory, and execute the file.
=> \encoding UTF8
=> show client_encoding;
=> \cd /tmp/share_host
=> \i example_data_create.sql
```

---
## Command Reference

### Stop Docker
```Command
$ docker compose down
```

### Start Docker
```Command
$ docker compose up -d
```

### Log in to DB on Ubuntu in Docker (postgres container)
```Command
The name after `docker exec -it` is the container name specified in `container_name` in `docker-compose.yml`.
$ docker exec -it postgres bash

Switch to the postgres OS user.
# su - postgres

Log in to the DB.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

### Recreate Docker Image

***This step is not normally required.***

```Command
Stop Docker.
$ docker compose down

Check the image name and delete it.
$ docker images
$ docker rmi postgres:17.7

Check the volume and delete it.
$ docker volume ls
$ docker volume rm user01_pgdata

Restart Docker.
$ docker compose up -d
```

