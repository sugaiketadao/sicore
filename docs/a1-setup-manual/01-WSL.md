## Installing WSL (Windows Subsystem for Linux)
1. Launch Windows PowerShell from the Start menu with administrator privileges (right-click menu) and execute the `wsl --install` command.
    - If `Create a default Unix user account:` is displayed after installation, skip OS restart and proceed to step 4.

2. Restart the OS after installation.

3. After OS restart, launch Command Prompt from the Start menu and execute the `wsl` command.
    - If nothing is displayed, refer to [If WSL Cannot Be Executed](#if-wsl-cannot-be-executed) below.

4. Create a user on WSL. (Username and password can be any values you specify)
    - Enter the username after `Create a default Unix user account:`.
    - Enter the password after `New password:`.
    - Enter the password again after `Retype new password:`.

5. Move to the home directory.
    - Enter `cd ~`.

---
- WSL stop method
    - Launch Command Prompt from the Start menu and execute the `wsl --shutdown` command.

- WSL start method
    - Launch Ubuntu from the Start menu.

---
## If WSL Cannot Be Executed
1. Open Control Panel from the Start menu and select "Programs" > "Turn Windows features on or off".

2. If the following are unchecked, change them to checked.
    - "Windows Subsystem for Linux"
    - "Virtual Machine Platform"

3. Restart the OS.

4. Launch Command Prompt from the Start menu and execute the `wsl` command.

5. If "No distributions installed on Windows Subsystem for Linux" is displayed, launch Windows PowerShell from the Start menu with administrator privileges (right-click menu) and execute the `wsl --install Ubuntu` command.

6. Return to the user creation steps.
