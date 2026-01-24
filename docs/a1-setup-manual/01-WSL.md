## WSL (Windows Subsystem for Linux) Installation
1. Execute Windows PowerShell from the Start menu with administrator privileges via the right-click menu, and run the `wsl --install` command.
    - If `Create a default Unix user account:` is displayed after installation, skip the OS restart and proceed to step 4.

2. Restart the OS after installation.

3. After OS restart, execute Command Prompt from the Start menu and run the `wsl` command.
    - If nothing is displayed, refer to [When WSL Cannot Be Executed](#when-wsl-cannot-be-executed) below.

4. Create a user on WSL. (You can specify any username and password)
    - Enter the username after `Create a default Unix user account:`.
    - Enter the password after `New password:`.
    - Enter the password again after `Retype new password:`.

5. Navigate to the home directory.
    - Enter `cd ~`.

---
- How to stop WSL
    - Execute Command Prompt from the Start menu and run the `wsl --shutdown` command.

- How to start WSL
    - Execute Ubuntu from the Start menu.

---
## When WSL Cannot Be Executed
1. Open Control Panel from the Start menu and select "Programs" > "Turn Windows features on or off".

2. If the following are unchecked, change them to checked.
    - "Windows Subsystem for Linux"
    - "Virtual Machine Platform"

3. Restart the OS.

4. Execute Command Prompt from the Start menu and run the `wsl` command.

5. If "No distributions installed for Windows Subsystem for Linux." is displayed, execute Windows PowerShell from the Start menu with administrator privileges via the right-click menu and run the `wsl --install Ubuntu` command.

6. Return to the user creation procedure.
