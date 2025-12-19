## Installing Docker on WSL

***All operations are performed on WSL***

1. Download and execute the installation shell.
```Command
$ curl -fsSL https://get.docker.com -o get-docker.sh
$ sudo sh get-docker.sh
Enter the password you set during user creation.
```

2. Verify that systemd is enabled.
```Command
$ sudo vi /etc/wsl.conf

If the content is as follows, the configuration is complete.
--------------------
[boot]
systemd=true
--------------------
Exit vi with :q
```

3. Configure permissions to allow Docker to be used by non-root users.
```Command
$ sudo usermod -aG docker ${USER}
```

4. Restart WSL.
    - Stopping WSL: Launch Command Prompt from the Start menu and execute the `wsl --shutdown` command.
    - Starting WSL: Launch Ubuntu from the Start menu.

5. Start the Docker service.
```Command
$ sudo service docker start
Enter the password you set during user creation.
```

6. Test Docker execution.
```Command
$ docker run hello-world

The following will be displayed (along with other messages):
Hello from Docker!
This message shows that your installation appears to be working correctly.
```
