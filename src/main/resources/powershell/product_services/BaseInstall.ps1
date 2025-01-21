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
		
		[Parameter(Mandatory=$False, HelpMessage="Path to the Base Install file in the repository")]
        [Alias('RepoBaseInstallFilePathName')]
        [string]$RepoBaseInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the Base Install file at runtime")]
        [Alias('BaseInstallFilePathName')]
        [string]$BaseInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the Base Install install.properties file at runtime")]
        [Alias('BaseInstallPropertiesFilePathName')]
        [string]$BaseInstallPropertiesFilePath,
		
		[Parameter(Mandatory=$False, HelpMessage="Base Install file name")]
        [Alias('BaseInstallFileNameName')]
        [string]$BaseInstallFileName,

		[Parameter(Mandatory=$False, HelpMessage="Base Install batch job")]
        [Alias('BaseInstallBatchJobName')]
        [string]$BaseInstallBatchJob,
		
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

LogWrite "[Event] Starting a Base Install"

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
	} -ArgumentList $BaseInstallFilePath
	
	LogWrite "Remote directory $BaseInstallFilePath created"

	LogWrite "Starting a file copy"
    $sourcePath = $BaseInstallFilePath + $BaseInstallFileName
	LogWrite "Base Install file copy sourcePath -  $sourcePath"
    Copy-Item -Recurse -Force -Verbose -Path $sourcePath -Destination $BaseInstallFilePath -ToSession $session #-ErrorVariable ProcessError
	LogWrite "Copy files complete"

	#Unzip product zip file on remote VM
	LogWrite "Unzipping the product code"
    $BaseInstallPathFile = $BaseInstallFilePath + $BaseInstallFileName

    Invoke-Command -Session $session -ScriptBlock {
        Expand-Archive -LiteralPath $args[0] -DestinationPath $args[1]
    } -ArgumentList ($BaseInstallPathFile, $BaseInstallFilePath)

	LogWrite "Product code unzipped"

	#Run Base Installer
	LogWrite "Starting the Base Install"
    $BaseInstallCommand = $BaseInstallPropertiesFilePath + '/setupicp.bat -f ' +  $BaseInstallPropertiesFilePath + '/install.properties'
	LogWrite "Base Install command - $BaseInstallCommand"
    invoke-command -Session $session -ScriptBlock {
        cmd /c  $args[0]
    } -ArgumentList $BaseInstallCommand

	LogWrite "Base Install Completed"

	#Wait 30 seconds
	Start-Sleep -s 30

	#Start ICP Services
	LogWrite "Starting the ICP Services"
	$StartICPServicesCommand = 'C:\Optum\ICP\bin\icp_svc.bat icp start'
    invoke-command -Session $session -ScriptBlock {
        cmd /c  $args[0]
    } -ArgumentList $StartICPServicesCommand
	LogWrite "ICP Services Started"

    # Clean up remote vm
    $usersNamePath = Split-Path -Path $vmLoggingFolderPath
    $usersNamePath = "$usersNamePath\"
    D:\Optum\ITL\repository\provision\powershell\RemoteVMCleanup.ps1 `
        -vmName $vmName `
        -userGroup $userGroup `
        -adminUserName $adminUserName `
        -adminPassword $adminPassword `
        -provisionFilePath $BaseInstallFilePath `
        -deleteRequestFolder 'true' `
        -vmLoggingFolderPath $usersNamePath `
        -vmLogName 'RemoteVMCleanup.log'
            
	#Remove Session
	LogWrite "Removing session"
	$getSession = Get-PSSession
	Remove-PSSession -Session $getSession
	LogWrite "Session removed"

	LogWrite "[Event] Base Install was Completed Successfully"
} else {
	LogWrite "[Event] Base Install Failed - database services (SQL/ORA) were not found"
}
