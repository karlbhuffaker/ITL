# Script Name: CheckForServices
# Description: 
#     This script is used to test if a VM is started or not.  If a ping isn't returned,
#		besides not being turned on it could mean that the VM has not been created.
# References:
#     https://www.itprotoday.com/devops-and-software-development/tricks-test-connection
#
#     A log will be written to the value passed in the $vmLoggingFolderPath $vmLogName variables.

[CmdletBinding()]

Param (
        [Parameter(Mandatory=$True, HelpMessage='Name of remote VM where installed services are being checked for')]
        [Alias('NodeName')]
        [string]$vmName,
        
        [Parameter(Mandatory=$True, HelpMessage='Admin User')]
        [Alias('UserName')]
        [string]$adminUserName,
        
        [Parameter(Mandatory=$True, HelpMessage='Admin User Password')]
        [Alias('Password')]
        [string]$adminPassword,

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
$vmInfo = "PoweredOff"

# Logging Defined
$Logfile = $vmLoggingFolderPath + $vmLogName
LogWrite ""
LogWrite "[Event] VM Test Connection for $vmName Started"

## Check to make sure the vm is online 
$timeOut = 0
$maxTimeout = 30
$isVMConnected = $false

do {
    try {
        $timeOut = $timeOut + 1
        if (Test-Connection -computername $vmName -Count 1 -ErrorAction stop) {
            $isVMConnected = $true
            $vmInfo = Get-VM -Name $vmName
			LogWrite "Connected to $vmName."
        }

    } catch [System.Net.NetworkInformation.PingException] {
        LogWrite "Timeout Count - catch: $timeOut; $vmName is not responding to ping."
        Start-Sleep -s 30
    }

} While (($timeOut -lt $maxTimeout) -and ($isVMConnected -eq $false)) 

if (($vmInfo.PowerState -eq "PoweredOn") -and ($isVMConnected -eq $True)) {
    Write-Host "Connection to $vmName was successful and is powered on."
    LogWrite "Connection to $vmName was successful and is powered on."

} elseif (($vmInfo.PowerState -eq "PoweredOff") -and ($isVMConnected -eq $False)) {
    Write-Host "Failed - $vmName shows no connection and is powered off."
    LogWrite "Failed - $vmName shows no connection and is powered off."
}

LogWrite "[Event] VM Test Connection for $vmName Completed Successfully"
