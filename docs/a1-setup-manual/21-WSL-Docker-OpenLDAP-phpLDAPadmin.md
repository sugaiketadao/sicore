## Creating OpenLDAP and phpLDAPadmin Containers in Docker on WSL

1. Create a Docker configuration file on Windows.
    - Save the following text as `docker-compose.yml`. (Encoding: UTF-8 without BOM, Line endings: LF)
    - You can specify any value for the organization name `Example Inc` after `LDAP_ORGANISATION:`.
    - You can specify any value for the domain `example.com` after `LDAP_DOMAIN:`. (This corresponds to `dc=example,dc=com`.)
    - The password `dcldappass` after `LDAP_ADMIN_PASSWORD:` is the administrator password for OpenLDAP. You can use any value. (It is used in the ldapadd and ldapsearch commands and for logging into phpLDAPadmin within this procedure.)
    - `PHPLDAPADMIN_HTTPS: "false"` disables HTTPS for phpLDAPadmin. Since HTTPS is enabled by default, this setting is required to access phpLDAPadmin on port `80`.

```docker-compose
services:
  openldap:
    image: osixia/openldap:1.5.0
    container_name: openldap
    environment:
      LDAP_ORGANISATION: "Example Inc"
      LDAP_DOMAIN: "example.com"
      LDAP_ADMIN_PASSWORD: "dcldappass"
    ports:
      - "389:389"
    volumes:
      - ldapdata:/var/lib/ldap
      - ldapconfig:/etc/ldap/slapd.d
  phpldapadmin:
    image: osixia/phpldapadmin:0.9.0
    container_name: phpldapadmin
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "openldap"
      PHPLDAPADMIN_HTTPS: "false"
    ports:
      - "8080:80"
    depends_on:
      - openldap
volumes:
  ldapdata:
  ldapconfig:
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

5. Verify that the containers are running.
```Command
$ docker compose ps
The containers are ready when the STATUS of both the openldap and phpldapadmin containers shows Up or running.
```

6. Log in to Ubuntu in Docker (the openldap container).
```Command
The name after `docker exec -it` is the container name specified in `container_name` in `docker-compose.yml`.
$ docker exec -it openldap bash
```

7. Create an OU (Organizational Unit). (The OU name `users` can be any value.)
```Command
Create the following content as ou.ldif.

# cat << 'EOF' > /tmp/ou.ldif
dn: ou=users,dc=example,dc=com
objectClass: organizationalUnit
ou: users
EOF

Add the OU using the ldapadd command.
# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/ou.ldif
```

8. Create a test user. (The user ID `U001` and password `P001` can be any values.)
```Command
Generate the password hash.
# slappasswd -s P001
{SSHA}xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Copy the generated hash value.

Create the following content as U001.ldif. Replace the {SSHA}... part with the hash value generated above.
# cat << 'EOF' > /tmp/U001.ldif
dn: uid=U001,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: U001
cn: Mike Davis
sn: Davis
userPassword: {SSHA}xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
EOF

Add the user using the ldapadd command.
# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/U001.ldif

To add U002, create U002.ldif in the same way and run ldapadd.
# slappasswd -s P002
{SSHA}yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy

# cat << 'EOF' > /tmp/U002.ldif
dn: uid=U002,ou=users,dc=example,dc=com
objectClass: inetOrgPerson
uid: U002
cn: IKEDA Ken
sn: IKEDA
userPassword: {SSHA}yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
EOF

# ldapadd -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -f /tmp/U002.ldif
```

9. Verify that the users were created.
```Command
# ldapsearch -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -b "ou=users,dc=example,dc=com"

The creation is successful if the user information is displayed.
If cn or sn contains non-ASCII characters, ldapsearch output shows `cn::` with two colons, and the value is displayed as Base64-encoded alphanumeric characters. This is not garbled text but the expected behavior for non-ASCII characters.
```

10. Log out from Docker Ubuntu.
```Command
# exit
```

11. Access phpLDAPadmin in a browser on Windows.
    - Click `login` in the tree panel on the left side of the screen to open the login page.
        - URL: `http://localhost:8080`
        - Login DN: `cn=admin,dc=example,dc=com`
        - Password: `dcldappass`
    - Verify the created users under `dc=example,dc=com` in the tree panel on the left side of the screen.

12. The LDAP connection settings in `web.properties` are as follows.
```properties
ldap.url=ldap://localhost:389
ldap.user.dn.fmt=uid=%s,ou=users,dc=example,dc=com
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

### Display User List on Ubuntu in Docker (openldap container)
```Command
The name after `docker exec -it` is the container name specified in `container_name` in `docker-compose.yml`.
$ docker exec -it openldap bash

Display the user list using LDAP search.
# ldapsearch -x -H ldap://localhost -D "cn=admin,dc=example,dc=com" -w dcldappass -b "ou=users,dc=example,dc=com"
# exit
```

### Recreate Docker Image

***This step is not normally required.***

```Command
Stop Docker.
$ docker compose down

Check the image name and delete it.
$ docker images
$ docker rmi osixia/openldap:1.5.0
$ docker rmi osixia/phpldapadmin:0.9.0

Check the volume and delete it.
$ docker volume ls
$ docker volume rm user01_ldapdata
$ docker volume rm user01_ldapconfig

Restart Docker.
$ docker compose up -d
```

