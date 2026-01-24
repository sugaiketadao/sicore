## Docker Installation on WSL

***All operations are performed on WSL***

1. Retrieve and execute the installation shell.
```Command
$ curl -fsSL https://get.docker.com -o get-docker.sh
$ sudo sh get-docker.sh
Enter the password set during user creation.
```

2. Verify that systemd is enabled.
```Command
$ sudo vi /etc/wsl.conf

If the following content is present, the configuration is complete.
--------------------
[boot]
systemd=true
--------------------

If the file does not exist or the above content is not present:
  Press the i key to enter edit mode and input the above content.
  Press the ESC key to exit edit mode, type :wq and press Enter to save.

If the above content is already present:
  Type :q and press Enter to exit vi.
```

3. Configure permissions to allow Docker to be used by users other than root.
```Command
$ sudo usermod -aG docker ${USER}
```

4. Restart WSL.
    - How to stop WSL: Execute Command Prompt from the Start menu and run the `wsl --shutdown` command.
    - How to start WSL: Execute Ubuntu from the Start menu.

5. Start the Docker service.
```Command
$ sudo service docker start
Enter the password set during user creation.
```

6. Test Docker execution.
```Command
$ docker run hello-world

A message containing the following will be displayed:
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

---
- Docker stop command
```Command
$ docker compose down
```

- Docker start command
```Command
$ docker compose up -d
```
