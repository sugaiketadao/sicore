## Creating a PostgreSQL Container in Docker on WSL

1. Create a Docker configuration file on Windows.
    - Save the following text with the filename `docker-compose.yml`. (Encoding: UTF-8 without BOM, Line ending: LF)
    - The version `17.2` after `image: postgres:` can be any version you specify.
    - The password `dcpgpass` after `POSTGRES_PASSWORD:` is the password for the postgres user on Docker and can be any value you specify.
    - `/tmp/share_docker:/tmp/share_host` links `/tmp/share_docker` on WSL to `/tmp/share_host` on Docker, creating a shared directory between Ubuntu on WSL and Ubuntu on Docker.

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
    - Use File Explorer to copy docker-compose.yml to `\\wsl.localhost\Ubuntu\home\user01`. (`user01` is the username you created during WSL installation)

3. Start WSL. If WSL is already running, restart it.
    - Stopping WSL: Launch Command Prompt from the Start menu and execute the `wsl --shutdown` command.
    - Starting WSL: Launch Ubuntu from the Start menu.

***All following operations are performed on WSL***

4. Start Docker.
```Command
$ docker compose up -d
The first execution will download the Docker image, which takes time.
```

5. Log in to Ubuntu on Docker.
```Command
$ docker ps

Copy the container name from the results (e.g., user01-postgres-1) and use the following command to log in.
$ docker exec -it container_name bash
(Example: docker exec -it user01-postgres-1 bash)
```

6. Log in to PostgreSQL on Docker-Ubuntu.
```Command
Switch to the postgres OS user
# su - postgres
$ psql -h localhost -p 5432 -U postgres
```

7. Create a role (user) and database. (Role name `dbuser01`, password `dbpass01`, and database name `db01` can be any values you specify)
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

9. Create a schema. (Schema name `schema01` can be any value you specify) ***Skip this step if you only use the public schema***
```SQL
# CREATE SCHEMA schema01 AUTHORIZATION dbuser01;
# GRANT ALL PRIVILEGES ON SCHEMA schema01 TO dbuser01;

Change the schema priority of the created database. (Specify the created schema before `public`)
# ALTER DATABASE db01 SET search_path TO schema01, public;
# quit
```

10. Verify the current schema. ***Skip this step if you only use the public schema***
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
You are now exited from "su - postgres".
# exit
You are now exited from Docker-Ubuntu.
```

12. Connect to the database from the host Windows using the following information. (Connect with tools like A5 or psqledit)
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


- Log in to the database on Ubuntu in Docker.
```Command
$ docker ps

Copy the container name from the results (e.g., user01-postgres-1) and use the following command to log in to Ubuntu on Docker.
$ docker exec -it container_name bash
(Example: docker exec -it user01-postgres-1 bash)

Switch to the postgres OS user.
# su - postgres

Log in to the database.
$ psql -h localhost -p 5432 -d db01 -U dbuser01
```

---
## Procedure to Execute SQL Files in psql on Docker

1. Grant write permissions to the shared directory on WSL.

```Command
$ sudo chmod 777 /tmp/share_docker/
```

2. Copy the target SQL file to the Ubuntu directory on WSL.
    - Use File Explorer to copy the SQL file to `\\wsl.localhost\Ubuntu\tmp\share_docker`.

3. Log in to the database on Ubuntu in Docker and execute the SQL file.

```SQL
[Example]
=> \i /tmp/share_host/example_data_create.sql
```

---
## Docker Image Recreation Commands

***This is normally an unnecessary procedure.***

```Command
Stop Docker.
$ docker compose down

Verify and delete the image name.
$ docker images
$ docker rmi postgres:17.2

Verify and delete the volume.
$ docker volume ls
$ docker volume rm user01_pgdata

Restart Docker.
$ docker compose up -d
```

