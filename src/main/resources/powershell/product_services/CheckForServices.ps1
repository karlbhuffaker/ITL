# Script Name: CheckForServices
# Description: 
#     This script is used to find out if a Service is being ran on another VM.
#     This will assist to determine if software is installed such as Oracle, 
#     MSSQL, or ICP.
#
# References:
#     https://www.itprotoday.com/devops-and-software-development/tricks-test-connection
#
#     A log will be written to the valued passed in the $vmLoggingFolderPath $vmLogName variables.

[CmdletBinding()]

Param (
        [Parameter(Mandatory=$True, HelpMessage='Name of remote VM where installed services are being checked for')]
        [Alias('NodeName')]
        [string]$vmName,

        [Parameter(Mandatory=$True, HelpMessage='Name of services to check for')]
        [Alias('ServiceName')]
        [string]$vmService,

        
        [Parameter(Mandatory=$True, HelpMessage='User of the VM')]
        [Alias('UserName')]
        [string]$vmUserName,
        
        [Parameter(Mandatory=$True, HelpMessage='User Password for the VM')]
        [Alias('Password')]
        [string]$vmPassword,

        [Parameter(Mandatory=$True, HelpMessage="Path where a log should be created if it doesn't already exist")]
        [Alias('LoggingFolderPath')]
        [string]$vmLoggingFolderPath,

        [Parameter(Mandatory=$True, HelpMessage='Name of the log file')]
        [Alias('LogName')]
        [string]$vmLogName
    )

# --------------------------------------------------------------------------
# Log a Message
Function LogWrite
{
   
   Param ([string]$Message)

   $Stamp = (Get-Date).toString("yyyy/MM/dd HH:mm:ss")
   $Line = "$Stamp $Message"

   #Add-content $Logfile -value $Line

   If($Logfile) {
        Add-Content $Logfile -Value $Line
    }
    Else {
        Write-Output $Line
    }
}

# --------------------------------------------------------------------------
# Begin Coding

## Variables
$isInstalled = $False

# Loging Defined
$Logfile = $vmLoggingFolderPath + $vmLogName
LogWrite ""
LogWrite "[Event] $vmLogName Started"

## What version of Powershell is running
$psVersion = 5
if ($PSVersionTable.PSVersion.Major -eq '7') {
    $psVersion = 7
}

# Credentials Defined
$password = ConvertTo-SecureString $vmPassword -AsPlainText -Force
$cred = New-Object System.Management.Automation.PSCredential ($vmUserName, $password )

$separator = "",","
$option = [System.StringSplitOptions]::None
$FQDNNodeName = $vmName.Split($separator, $option)
$Cred = Get-Credential -Credential $cred

# Insert an * on the end of the service to make it find "like" values.
$vmService = $vmService+'*'

## Check to make sure the vm is online 
$timeOut = 0
$maxTimeout = 30
$isVMConnected = $false

do {
    try {
        $timeOut = $timeOut +1

        if (Test-Connection -computername $vmName -Count 1 -ErrorAction stop) {
            $isVMConnected = $true
            $ServiceStatus = Invoke-Command -ComputerName $FQDNNodeName -Credential $Cred -ScriptBlock {Get-Service -Name $args[0]} -ArgumentList $vmService
        }

    } catch [System.Net.NetworkInformation.PingException] {
        Write-Host "Timeout Count: $timeOut; $vmName is not responding to ping."
        #LogWrite "Timeout Count: $timeOut; $vmName is not responding to ping."

        #Wait 30 seconds
	    Start-Sleep -s 30
    }

} While (($timeOut -lt $maxTimeout) -and ($isVMConnected -eq $false)) 

if ($isVMConnected -eq $false) {
    Write-Host "$vmName is not responding.  Check to see if it is turned off."
    LogWrite "$vmName is not responding.  Check to see if it is turned off."
} else {

    if ($ServiceStatus.Name -like $vmService) {
        $isInstalled = $True
        LogWrite "[Event] $vmService is installed on $vmName"
        Write-Host "[Event] $vmService is installed on $vmName"

    } else {
        LogWrite "[Event] $vmService  is not installed on $vmName"
        Write-Host "[Event] $vmService  is not installed on $vmName"
    }
}

LogWrite "[Event] $vmLogName Ended"

return $isInstalled