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
		
		[Parameter(Mandatory=$False, HelpMessage="Path to the LCD file in the repository")]
        [Alias('RepoLCDFilePathName')]
        [string]$RepoLCDFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the LCD file at runtime")]
        [Alias('LCDFilePathName')]
        [string]$LCDFilePath,
		
		[Parameter(Mandatory=$False, HelpMessage="LCD file name")]
        [Alias('LCDFileNameName')]
        [string]$LCDFileName,

		[Parameter(Mandatory=$False, HelpMessage="LCD Loader batch job")]
        [Alias('LCDLoaderBatchJobName')]
        [string]$LCDLoaderBatchJob,
		
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

LogWrite "[Event] Starting a LCD Load" + $Logfile

## Check to make sure the correct DB Service is installed.  
## Note: The return value is passed back as a $true or $false
$isInstalled = D:\app\Tomcat9\webapps\ITL\WEB-INF\classes\powershell\product_services\CheckForServices.ps1 -vmName $vmName -vmService $vmService -vmUserName $adminUserName -vmPassword $adminPassword -vmLoggingFolderPath $vmLoggingFolderPath -vmLogName $vmLogName

if ($isInstalled -eq $true) {
	#Create Session with remote VM
	LogWrite "Starting a session"
	$pw = $adminPassword
	$password = ConvertTo-SecureString $pw -AsPlainText -Force
	$cred = New-Object System.Management.Automation.PSCredential ($adminUserName, $password )

	$session = New-PSSession -ComputerName $vmname -Credential $cred
	LogWrite "Session has started"
		
    # Check to make sure the LCD_Extracts folder exists.  Create it if not.
    GenerateRemoteFolder $session $LCDFilePath

	#Copy files from ITL to dest folder on remote VM
	LogWrite "Starting a file copy"
    $repoPath = $RepoLCDFilePath + $LCDFileName
    Copy-Item -Recurse -Force -Verbose -Path $repoPath -Destination $LCDFilePath -ToSession $session -ErrorVariable ProcessError
	LogWrite "Copy files complete"

	#Run LCD Loader Batch Job
	LogWrite "Starting the LCD Load"
    invoke-command -computername $vmname -credential $cred -ScriptBlock {cmd /c "C:\Optum\ICP\bin\LCDBulkLoader.bat"}

	LogWrite "LCD Load Completed"

	#Remove Session
	LogWrite "Removing session"
	$getSession = Get-PSSession
	Remove-PSSession -Session $getSession
	LogWrite "Session removed"

	LogWrite "[Event] LCD Load was Completed Successfully"
} else {
	LogWrite "[Event] LCD Load failed - ICP Services were not found"
}