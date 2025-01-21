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
		
		[Parameter(Mandatory=$False, HelpMessage="Path to the CU Install file in the repository")]
        [Alias('RepoCUInstallFilePathName')]
        [string]$RepoCUInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the CU Install file at runtime")]
        [Alias('CUInstallFilePathName')]
        [string]$CUInstallFilePath,

		[Parameter(Mandatory=$False, HelpMessage="Path to the CU Install install.properties file at runtime")]
        [Alias('CUInstallPropertiesFilePathName')]
        [string]$CUInstallPropertiesFilePath,
		
		[Parameter(Mandatory=$False, HelpMessage="CU Install file name")]
        [Alias('CUInstallFileNameName')]
        [string]$CUInstallFileName,

		[Parameter(Mandatory=$False, HelpMessage="CU Install batch job")]
        [Alias('CUInstallBatchJobName')]
        [string]$CUInstallBatchJob,
		
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

# Test to see if the ICP folder can be renamed.  If it can then the CU Install
# process can continue.  Otherwise it will fail.
Function Rename_ICP_RemoteFolder {
    Param (
        $session
    )
    $renameWorked = $False
    $currentName = 'C:\Optum\ICP'
    $newName     = 'C:\Optum\ICPX'


    invoke-command -Session $session -ScriptBlock{
        $currentName = $args[0]
        $newName     = $args[1]

        if (Test-Path $currentName) {
            Rename-Item -Path $currentName -NewName $newName
        }

    } -ArgumentList $currentName, $newName



    # If the ICP folder was renamed to ICPX then rename it back to ICP
    if (!(Test-Path $currentName)) {
        $renameWorked = $True
        invoke-command -Session $session -ScriptBlock{
            $currentName = $args[0]
            $newName     = $args[1]

            if (Test-Path $newName) {
                Rename-Item -Path $newName -NewName $currentName
            }

        } -ArgumentList $currentName, $newName
    }

    return $renameWorked

}

## ----- Main Code

## The full path and full name where the log file is to be created.
$Logfile = $vmLoggingFolderPath + $vmLogName

LogWrite "[Event] Starting a CU Install"

## Check to make sure the correct DB Service is installed.  
## Note: The return value is passed back as a $true or $false
$isInstalled = D:\app\Tomcat9\webapps\ITL\WEB-INF\classes\powershell\product_services\CheckForServices.ps1 -vmName $vmName -vmService $vmService -vmUserName $adminUserName -vmPassword $adminPassword -vmLoggingFolderPath $vmLoggingFolderPath -vmLogName $vmLogName
LogWrite "After the call to CheckForServices.ps1 isInstalled = $isInstalled"

if ($isInstalled -eq $true) {
	# Create Session with remote VM
	LogWrite "Starting a session"
	$pw = $adminPassword
	$password = ConvertTo-SecureString $pw -AsPlainText -Force
	$cred = New-Object System.Management.Automation.PSCredential ($adminUserName, $password )

	$session = New-PSSession -ComputerName $vmName -Credential $cred
	LogWrite "Session has started $computerName"


	#Make directory on remote VM
	LogWrite "Creating a remote directory"

    $script = {

        $pathToCreate = $args[0]

        if (-not (Test-Path -LiteralPath $args[0])) {
    
            try {
                New-Item -Path $args[0] -ItemType Directory -ErrorAction Stop | Out-Null #-Force
            }
            catch {
                Write-Error -Message "Unable to create directory '$args[0]'. Error was: $_" -ErrorAction Stop
            }
            $output = "Successfully created directory $pathToCreate."
        } else {
            $output = "Directory $pathToCreate already existed"
        }

        # Save lastexitcode right after call to exe completes
        $exitCode = $LASTEXITCODE

        # Return the output and the exitcode using a hashtable
        New-Object -TypeName PSCustomObject -Property @{Host=$env:computername; Output=$output; ExitCode=$exitCode}
    } 

    # Capture the results from the remote computers
    $results = Invoke-Command -Session $session -ScriptBlock $script -ArgumentList $CUInstallFilePath
    $results | select Host, Output, ExitCode | Format-List
    $finalResults = $results.Host + ": " + $results.Output
    LogWrite $finalResults

	#LogWrite "Remote directory created $CUInstallFilePath"
	
	LogWrite "Starting a file copy"
    $sourcePath = $CUInstallFilePath + $CUInstallFileName
    Copy-Item -Recurse -Force -Verbose -Path $sourcePath -Destination $CUInstallFilePath -ToSession $session -ErrorVariable ProcessError
	LogWrite "Copy files complete $sourcePath"
	

	# Unzip product zip file on remote VM
	LogWrite "Unzipping the product code"
    $CUInstallPathFile = $CUInstallFilePath + $CUInstallFileName
        
    invoke-command -Session $session -ScriptBlock {
        Expand-Archive -LiteralPath $args[0] -DestinationPath $args[1] -Force
    }  -ArgumentList ($CUInstallPathFile, $CUInstallFilePath)

	LogWrite "Product code unzipped $CUInstallPathFile"

	# Stop ICP Services
	LogWrite "Stoping the ICP Services"

    invoke-command -Session $session -ScriptBlock {
		PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\Optum\ICP\bin\icp_svc.bat' 'icp' 'stop'"
    }
	LogWrite "ICP Services Stopped"

	# Wait 30 seconds
	Start-Sleep -s 30

	# Check to see if the Optum\ICP folder can be renamed
    LogWrite "Checking to see if the ICP folder can be renamed"
    $rtnVal = Rename_ICP_RemoteFolder $session
	LogWrite "Could the ICP remote folder be renamed: $rtnVal"	
	
    if ($rtnVal -eq $True) {
        LogWrite "Renaming the ICP folder was successful"

        # Run CU Installer
        LogWrite "Starting the CU Install"
        $CUInstallCmd = $CUInstallPropertiesFilePath + '/installcu.bat'## Works
        #$CUInstallCmd = $CUInstallPropertiesFilePath + '/installcu.bat -f  ' + $CUInstallPropertiesFilePath + '/install.properties'  ## 933 - Does not work
        LogWrite "Starting the CU Install - $CUInstallCmd"

        invoke-command -Session $session -ScriptBlock {            
            Powershell -command Start-Process $args[0] -Verb runas -Wait
        } -ArgumentList $CUInstallCmd	
        	
	    LogWrite "CU Install Completed"

	    # Wait 30 seconds
	    Start-Sleep -s 30

    }    
    else {
        LogWrite "Renaming the ICP folder failed"
    }

	# Start ICP Services
	LogWrite "Starting the ICP Services"
	invoke-command -Session $session -ScriptBlock {
		PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& 'C:\Optum\ICP\bin\icp_svc.bat' 'icp' 'start'"
    } 
    
    # Wait 30 seconds for the ICP Services to start
	Start-Sleep -s 30

	LogWrite "ICP Services Started"

	# Remove Session
	LogWrite "Removing session"
	$getSession = Get-PSSession
	Remove-PSSession -Session $getSession
	LogWrite "Session removed"

	LogWrite "[Event] CU Install was Completed Successfully"
} else {
	LogWrite "[Event] CU Install Failed - ICP Services were not found"
}