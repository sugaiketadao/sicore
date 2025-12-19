## Installing WSL (Windows Subsystem for Linux)
1. Right-click Windows PowerShell from the Start menu, select "Run as administrator", and execute the `wsl --install` command.
    - If `Create a default Unix user account:` appears after installation, skip the OS restart and proceed to the "Creating User" step.

2. Restart the OS after installation.

3. After OS restart, launch Command Prompt from the Start menu and execute the `wsl` command.
    - If nothing appears, refer to [When WSL Cannot Be Executed](#when-wsl-cannot-be-executed) below.

4. Create a user on WSL. (You can specify any username and password)
    - Enter a username after `Create a default Unix user account:`.
    - Enter a password after `New password:`.
    - Enter the password again after `Retype new password:`.

5. Navigate to the home directory.
    - Enter `cd ~`.

- Stopping WSL
    - Launch Command Prompt from the Start menu and execute the `wsl --shutdown` command.

 - Starting WSL
    - Launch Ubuntu from the Start menu.

---
## When WSL Cannot Be Executed
1. Open Control Panel from the Start menu, and select "Programs and Features" > "Turn Windows features on or off".

2. If the following are unchecked, enable them:
    - "Windows Subsystem for Linux"
    - "Virtual Machine Platform"

3. Restart the OS.

4. Launch Command Prompt from the Start menu and execute the `wsl` command.

5. If the message "No installed distributions" appears, right-click Windows PowerShell from the Start menu, select "Run as administrator", and execute the `wsl --install Ubuntu` command.

6. Return to the user creation step.
