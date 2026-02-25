## Creating a PostgreSQL Container in Docker on WSL

1. Create a Docker configuration file on Windows.
    - Save the following text as `docker-compose.yml`. (Encoding: UTF-8 without BOM, Line ending: LF)
    - You can specify any version for `17.2` after `image: postgres:`.
    - You can specify any value for the password `dcpgpass` after `POSTGRES_PASSWORD:`, which is the password for the postgres user on Docker.
    - `/tmp/share_docker:/tmp/share_host` links `/tmp/share_docker` on WSL and `/tmp/share_host` on Docker, creating a shared directory between Ubuntu on WSL and Ubuntu on Docker.

```docker-compose
services:
  postgres:
    image: postgres:17.2
    environment:
      POSTGRES_PASSWORD: dcpgpass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - /tmp/share_docker:/tmp/share_host
volumes:
  pgdata:
```

2. Copy the Docker configuration file to the Ubuntu directory on WSL.
    - Copy docker-compose.yml to the HOME directory `\\wsl.localhost\Ubuntu\home\user01` of user `user01` created during installation using Explorer.

3. Start WSL. If WSL is already running, restart it.
    - To stop WSL: Run Command Prompt from the Start menu and execute the `wsl --shutdown` command.
    - To start WSL: Run Ubuntu from the Start menu.

***All the following procedures will be performed on WSL***

4. Start Docker.
```Command
$ docker compose up -d
The first execution will take some time as the Docker image will be downloaded.
```

5. Log in to Ubuntu on Docker.
```Command
$ docker ps

Copy the container name (e.g., user01-postgres-1) from the result and log in with the following command.
$ docker exec -it container_name bash
(Example: docker exec -it user01-postgres-1 bash)
```

6. Log in to PostgreSQL on Docker-Ubuntu.
```Command
Switch to postgres OS user
# su - postgres
$ psql -h localhost -p 5432 -U postgres
```

7. Create a role (user) and database. (You can specify any values for role name `dbuser01`, password `dbpass01`, and database name `db01`)
```SQL
# CREATE ROLE dbuser01 WITH LOGIN PASSWORD 'dbpass01';
# CREATE DATABASE db01 OWNER dbuser01;
# GRANT ALL PRIVILEGES ON DATABASE db01 TO dbuser01;
# quit
```

8. Log in to the created database.
```Command
$ psql -h localhost -p 5432 -d db01 -U postgres
```

9. Create a schema. (You can specify any value for schema name `schema01`)　***Skip this step if using only the public schema***
```SQL
# CREATE SCHEMA schema01 AUTHORIZATION dbuser01;
# GRANT ALL PRIVILEGES ON SCHEMA schema01 TO dbuser01;

Change the schema priority of the created database. (Specify the created schema before `public`)
# ALTER DATABASE db01 SET search_path TO schema01, public;
# quit

If an error occurs with CREATE SCHEMA, log in with the created user and create the schema (execute the above SQL).
```

10. Verify the current schema.　***Skip this step if using only the public schema***
```Command
Log in to the database with the created user.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

```SQL
=> SELECT current_schema();
`schema01` will be displayed.
=> \q
```

11. Log out from Docker-Ubuntu.
```Command
$ exit
Exited from "su - postgres".
# exit
Log out from Docker-Ubuntu.
```

12. Connect to the database from the host Windows with the following information. (Connect using A5 or psqledit)
    - HOST: localhost
    - PORT: 5432
    - DB: db01
    - USER: dbuser01
    - PASS: dbpass01

---
- Docker stop command
```Command
$ docker compose down
```

- Docker start command
```Command
$ docker compose up -d
```


- Log in to the database on Ubuntu on Docker.
```Command
$ docker ps

Copy the container name (e.g., user01-postgres-1) from the result and log in to Ubuntu on Docker with the following command.
$ docker exec -it container_name bash
(Example: docker exec -it user01-postgres-1 bash)

Switch to postgres OS user.
# su - postgres

Log in to the database.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

---
## Procedure to Execute SQL Files with psql on Docker

1. Grant write permission to the shared directory on WSL.

```Command
$ sudo chmod 777 /tmp/share_docker/
```

2. Copy the target SQL file to the Ubuntu directory on WSL.
    - Copy the SQL file to `\\wsl.localhost\Ubuntu\tmp\share_docker` using Explorer.
    - `/tmp/share_docker/` on WSL is mounted as `/tmp/share_host/` on Docker. (See docker-compose above)

3. Log in to the DB on Ubuntu on Docker and execute the SQL file.

```SQL
[Example] Change the client encoding, move to the directory, and execute the file
=> \encoding UTF8
=> show client_encoding;
=> \cd /tmp/share_host
=> \i example_data_create.sql
```

---
## Docker Image Recreate Commands

***This procedure is usually not required.***

```Command
Stop Docker.
$ docker compose down

Verify and remove the image.
$ docker images
$ docker rmi postgres:17.2

Verify and remove the volume.
$ docker volume ls
$ docker volume rm user01_pgdata

Restart Docker.
$ docker compose up -d
```

