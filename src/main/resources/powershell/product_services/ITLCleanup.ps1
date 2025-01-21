<#
    Program: ITLCleanup.ps1
    Author: Karl Huffaker
    Description: 
        This script is designed to delete eirther a complete folder or the contents inside the foler minus
        any log files.  The log files are kept in case they need to be reviewed.

    Parameters:
        $deleteRequestFolder: 
            Set to True if you want the folder and all its contentes deleted.  
            Set to False it you only want the files minues the *.log files deleted.
#>
Param (
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

## The full path and full name where the log file is to be created.
$Logfile = $vmLoggingFolderPath + $vmLogName

LogWrite "[Event] Starting a ITL Cleanup"

<#
    If deleteRequestFolder is False then don't delete the folder but only delete 
    all files inside the folder except for the .log file.  Otherwise, delete
    the entire folder and all it's contents.
#>
if ($deleteRequestFolder -eq 'False') {
	LogWrite "Deleting all files inside $provisionFilePath except for the log files"

    # Test to see if the folder exists
    if (Test-Path $provisionFilePath) {

        $folders = Get-ChildItem -Path $provisionFilePath -Recurse #-Directory
        foreach($folder in $folders.fullname){
            Write-Host "Clearing in $folder"
            Get-ChildItem -Path $folder -Filter '*.*' -Exclude *.log, *.ps1 | Remove-Item -ErrorVariable removalErrors
        }
        
        if ($removalErrors.Count -gt 0) {
            LogWrite "There were errors when trying to delete the files from $provisionFilePath"
        } else {
            LogWrite "The files in $provisionFilePath except the log files were deleted."
        }
	}

} else {
    LogWrite "Deleting the $provisionFilePath with all it's content."
    Remove-Item -Recurse -Force $provisionFilePath -ErrorVariable removalErrors

    if ($removalErrors.Count -gt 0) {
        LogWrite "There were errors when trying to delete $provisionFilePath"
    } else {
        LogWrite "The $provisionFilePath with all it's content was deleted."
    }
}

LogWrite "[Event] ITL Cleanup is complete"