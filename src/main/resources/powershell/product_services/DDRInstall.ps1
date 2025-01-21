Param (
        [Parameter(Mandatory=$True, HelpMessage='Name of remote VM where installed services are being checked for')]
        [Alias('NodeName')]
        [string]$vmName,

        [Parameter(Mandatory=$True, HelpMessage='Name of user group')]
        [Alias('userGroupName')]
        [string]$userGroup,
		
        [Parameter(Mandatory=$True, HelpMessage='Name of services to check for')]
        [Alias('ServiceName')]
        [string]$vmService,
        
        [Parameter(Mandatory=$True, HelpMessage='Admin User')]
        [Alias('UserName')]
        [string]$adminUserName,
        
        [Parameter(Mandatory=$True, HelpMessage='Admin User Password')]
        [Alias('Password')]
        [string]$adminPassword,

		[Parameter(Mandatory=$False, HelpMessage="Request type")]
        [Alias('RequestTypeName')]
        [string]$RequestType,

		[Parameter(Mandatory=$False, HelpMessage="Path to the DDR Install file in the repository")]
        [Alias('RepoDDRInstallFilePathName')]
        [string]$RepoDDRInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the DDR Install file at runtime")]
        [Alias('DDRInstallFilePathName')]
        [string]$DDRInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the DDR Install install.properties file at runtime")]
        [Alias('DDRInstallPropertiesFilePathName')]
        [string]$DDRInstallPropertiesFilePath,
		
		[Parameter(Mandatory=$False, HelpMessage="DDR Install file name")]
        [Alias('DDRInstallFileNameName')]
        [string]$DDRInstallFileName,

		[Parameter(Mandatory=$False, HelpMessage="Path to the KB file in the repository")]
        [Alias('RepoKBFilePathName')]
        [string]$RepoKBFilePath,

		[Parameter(Mandatory=$False, HelpMessage="KB file name")]
        [Alias('KBFileNameName')]
        [string]$KBFileName,

		[Parameter(Mandatory=$False, HelpMessage="DDR Install batch job")]
        [Alias('DDRInstallBatchJobName')]
        [string]$DDRInstallBatchJob,
		
		[Parameter(Mandatory=$False, HelpMessage="Path where a log should be created if it doesn't already exist")]
        [Alias('LoggingFolderPath')]
        [string]$vmLoggingFolderPath,

        [Parameter(Mandatory=$False, HelpMessage='Name of the log file')]
        [Alias('LogName')]
        [string]$vmLogName
    )
	
## ----- Functions
	
## Log a Message
Function LogWrite
{
   
   Param ([string]$Message)

   $Stamp = (Get-Date).toString("yyyy/MM/dd HH:mm:ss")
   $Line = "$Stamp $Message"

   If($Logfile) {
        Add-Content $Logfile -Value $Line
    }
    Else {
        Write-Output $Line
    }
}

# Create a folder if it's not there.
Function GenerateRemoteFolder {
    Param (
        $session,
        [string]$path
    )

    invoke-command -Session $session -ScriptBlock{
        $global:foldPath = $null
        $path = $args[0]

        foreach($foldername in $path.split("\")) {
            $global:foldPath += ($foldername+"\")
            if (!(Test-Path $global:foldPath)){
                # Create the path
                New-Item -ItemType Directory -Path $global:foldPath
            }
        }

    } -ArgumentList $path
}

## ----- Main Code

## The full path and full name where the log file is to be created.
$Logfile = $vmLoggingFolderPath + $vmLogName

LogWrite "[Event] Starting a DDR Install"

## Check to make sure the correct DB Service is installed.  
## Note: The return value is passed back as a $true or $false
$isInstalled = D:\app\Tomcat9\webapps\ITL\WEB-INF\classes\powershell\product_services\CheckForServices.ps1 -vmName $vmName -vmService $vmService -vmUserName $adminUserName -vmPassword $adminPassword -vmLoggingFolderPath $vmLoggingFolderPath -vmLogName $vmLogName
LogWrite "After the call to CheckForServices.ps1 isInstalled = $isInstalled"

if ($isInstalled -eq $true) {
	#Create Session with remote VM
	LogWrite "Starting a session"
	$pw = $adminPassword
	$password = ConvertTo-SecureString $pw -AsPlainText -Force
	$cred = New-Object System.Management.Automation.PSCredential ($adminUserName, $password )

	LogWrite "computerName = $vmName"
	$session = New-PSSession -ComputerName $vmName -Credential $cred
	LogWrite "Session = $session has started."

	#Make directory on remote VM
	LogWrite "Creating a remote directory"

	Invoke-Command -Session $session -ScriptBlock {
		mkdir -path $args[0]
	} -ArgumentList $DDRInstallFilePath
	
	LogWrite "Remote directory $DDRInstallFilePath created"

	LogWrite "Starting a DDR product file copy"
    $sourcePath = $DDRInstallFilePath + $DDRInstallFileName
	LogWrite "DDR Install file copy sourcePath -  $sourcePath"
    Copy-Item -Recurse -Force -Verbose -Path $sourcePath -Destination $DDRInstallFilePath -ToSession $session #-ErrorVariable ProcessError
	LogWrite "Copy product file complete"

	LogWrite "Starting a KB file copy"
    $kbSourcePath = $RepoKBFilePath + $KBFileName
	LogWrite "KB file copy sourcePath -  $kbSourcePath"
    Copy-Item -Recurse -Force -Verbose -Path $kbSourcePath -Destination $DDRInstallFilePath -ToSession $session #-ErrorVariable ProcessError
	LogWrite "Copy KB file complete"

	#Unzip product zip file on remote VM
	LogWrite "Unzipping the product file"
    $DDRInstallPathFile = $DDRInstallFilePath + $DDRInstallFileName

#	invoke-command -computername $vmName -ScriptBlock{
#		Expand-Archive -LiteralPath $args[0] -DestinationPath $args[1]
#	} -ArgumentList ($DDRInstallPathFile, $DDRInstallFilePath)

    Invoke-Command -Session $session -ScriptBlock {
        Expand-Archive -LiteralPath $args[0] -DestinationPath $args[1]
    } -ArgumentList ($DDRInstallPathFile, $DDRInstallFilePath)

	LogWrite "Product file unzipped"

	# Stop ICP Services if RequestType = ddrUpgradeInstall
	$ddrUpgradeInstall = 'ddrUpgradeInstall'
    if ($RequestType -eq $ddrUpgradeInstall) {
	    LogWrite "Stopping the ICP Services - ddrUpgradeInstall"
        invoke-command -Session $session -ScriptBlock {
            PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\Optum\ICP\bin\icp_svc.bat' 'icp' 'stop'"
        }
        LogWrite "ICP Services Stopped"

        # Wait 30 seconds
        Start-Sleep -s 30
    }

	#Stop DDR Services if RequestType = ddrKbInstall
	$ddrKbInstall = 'ddrKbInstall'
	if ($RequestType -eq $ddrKbInstall) {
        LogWrite "Stop the DDR Services - ddrKbInstall"
        $StartDDRServicesCommand = 'C:\Optum\DDR\icp_svc.bat stop'
        invoke-command -Session $session -ScriptBlock {
            cmd /c  $args[0]
        } -ArgumentList $StartDDRServicesCommand
        LogWrite "DDR Services Stopped"

        #Wait 30 seconds
        Start-Sleep -s 30
    }

	# Check to make sure the icpdata folder exists.  install.bat won't run unless it does.
    $fullpath = 'D:\icpdata'
    $existsOnRemote = Invoke-Command -Session $session {
	    If(!(Test-Path $args[0])) {
            # Do Nothing.  Just checking for a folder or not.
	    }
    } -argumentList $fullpath

    if(-not $existsOnRemote){
        LogWrite "$fullpath was not found on the Remote directory!"
    }

    LogWrite "Creating a remote $fullpath directory"
    $existsOnRemote = Invoke-Command -Session $session {
	    If(!(Test-Path $args[0])) {
		    mkdir -path $args[0]
	    }
    } -argumentList $fullpath

    $existsOnRemote = ''
    $existsOnRemote = Invoke-Command -Session $session {
	    If((Test-Path $args[0])) {
            # Do Nothing
	    }
    } -argumentList $fullpath

    if(-not $existsOnRemote){
        LogWrite "$fullpath was created on the Remote directory!"
	}

	#Run DDR Installer
	LogWrite "Starting the DDR Installer"
    $DDRInstallCommand = $DDRInstallPropertiesFilePath + '/install.bat'
    LogWrite "DDR Install command - $DDRInstallCommand"
    invoke-command -Session $session -ScriptBlock {
        Powershell -command Start-Process $args[0] -Verb runas -Wait
    } -ArgumentList $DDRInstallCommand

	LogWrite "DDR Installer Completed"

	#Wait 30 seconds
	Start-Sleep -s 30

	#Start DDR Services
	LogWrite "Starting the DDR Services"
	$StartDDRServicesCommand = 'C:\Optum\DDR\icp_svc.bat start'
    invoke-command -Session $session -ScriptBlock {
        cmd /c  $args[0]
    } -ArgumentList $StartDDRServicesCommand
	LogWrite "DDR Services Started"

    # Clean up remote vm
    $usersNamePath = Split-Path -Path $vmLoggingFolderPath
    $usersNamePath = "$usersNamePath\"
    D:\Optum\ITL\repository\provision\powershell\RemoteVMCleanup.ps1 `
        -vmName $vmName `
        -userGroup $userGroup `
        -adminUserName $adminUserName `
        -adminPassword $adminPassword `
        -provisionFilePath $DDRInstallFilePath `
        -deleteRequestFolder 'true' `
        -vmLoggingFolderPath $usersNamePath `
        -vmLogName 'RemoteVMCleanup.log'
            
	#Remove Session
	LogWrite "Removing session"
	$getSession = Get-PSSession
	Remove-PSSession -Session $getSession
	LogWrite "Session removed"

	LogWrite "[Event] DDR Install was Completed Successfully"
} else {
	LogWrite "[Event] DDR Install Failed - Required ICP or DDR or Database services (ICP/DDR/SQL/ORA/POST) were not found"
}
