<#
    Program: RemoteVMCleanup.ps1
    Author: Karl Huffaker
    Description: 
        This script will clean up the remote VM after a provision request is completed.
        This script is designed to delete either a complete folder or the contents inside the foler minus
        any log files.  The log files are kept in case they need to be reviewed.

    Parameters:
        $deleteRequestFolder: 
            Set to True if you want the folder and all its contentes deleted.  
            Set to False it you only want the files minues the *.log files deleted.
#>
Param (
        [Parameter(Mandatory=$True, HelpMessage='Name of remote VM where installed services are being checked for')]
        [Alias('NodeName')]
        [string]$vmName,
        
        [Parameter(Mandatory=$True, HelpMessage='Name of user group')]
        [Alias('userGroupName')]
        [string]$userGroup,

        [Parameter(Mandatory=$True, HelpMessage='User of the VM')]
        [Alias('UserName')]
        [string]$adminUserName,
        
        [Parameter(Mandatory=$True, HelpMessage='User Password for the VM')]
        [Alias('Password')]
        [string]$adminPassword,
		
		[Parameter(Mandatory=$True, HelpMessage="Path to the provision folder at runtime")]
        [Alias('provisionFilePathName')]
        [string]$provisionFilePath,
		
		[Parameter(Mandatory=$False, HelpMessage="Delete the current request folder.  If false then all files except the .log file will be deleted in the current request folder.")]
        [Alias('requestFolderDelete')]
        [string]$deleteRequestFolder,
		
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

## ----- Main Code

LogWrite "[Event] Starting a Remote VM Cleanup on $vmName"

# Create Session with remote VM
LogWrite "Starting a session"
$pw = $adminPassword
$password = ConvertTo-SecureString $pw -AsPlainText -Force
$cred = New-Object System.Management.Automation.PSCredential ($adminUserName, $password )

$session = New-PSSession -ComputerName $vmName -Credential $cred
LogWrite "Session has started $vmName"

## The full path and full name where the log file is to be created.
$Logfile = $vmLoggingFolderPath + $vmLogName



<#
    If deleteRequestFolder is False then don't delete the folder but only delete 
    all files inside the folder except for the .log file.  Otherwise, delete
    the entire folder and all it's contents.
#>
if ($deleteRequestFolder -eq 'False') {
	LogWrite "Deleting all files inside $provisionFilePath except for the log files"

    $ServiceStatus = Invoke-Command -Session $session -ScriptBlock {       
        
        # Test to see if the folder exists
        if (Test-Path $args[0]) {
            $folders = Get-ChildItem -Path $args[0] -Recurse

             foreach($folder in $folders.fullname){
                Write-Host "Clearing in $folder"
                Get-ChildItem -Path $folder -Filter '*.*' -Exclude *.log | Remove-Item -ErrorVariable removalErrors
            }
        }
        $removalErrors.Count

    } -ArgumentList $provisionFilePath

} else {

    $ServiceStatus = Invoke-Command -Session $session -ScriptBlock {  

        # Test to see if the folder exists
        if (Test-Path $args[0]) {
            Remove-Item -Recurse -Force $args[0] -ErrorVariable removalErrors -ErrorAction Stop
        }
        $removalErrors.Count

    } -ArgumentList $provisionFilePath
}

if ($removalErrors.Count -gt 0) {
    LogWrite "There were errors when trying to delete the files from $provisionFilePath on $vmName"
} else {
    LogWrite "The files on $vmName in $provisionFilePath except the log files were deleted."
}

LogWrite "[Event] $vmName Remote VM Cleanup is complete"
