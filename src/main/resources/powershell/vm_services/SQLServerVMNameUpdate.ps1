# Script Name: Update-ServerName.ps1
# Description: 
#     This script is used to rename the service name object on a VM where MSSQL is installed.
#
#     A log will be written to the valued passed in the $vmLoggingFolderPath $vmLogName variables.
#     If no values are passed for $vmLoggingFolderPath and $vmLogName then the output is simply written to the output window if available.

[CmdletBinding()]

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

        [Parameter(Mandatory=$False, HelpMessage="Path where a log should be created if it doesn't already exist")]
        [Alias('LoggingFolderPath')]
        [string]$vmLoggingFolderPath,

        [Parameter(Mandatory=$False, HelpMessage='Name of the log file')]
        [Alias('LogName')]
        [string]$vmLogName
    )


## --------------------------------------------------------------------------
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

## Make-SqlConnection will connect to the requested database and if the connection is successful 
## it will return the connection object.

function Make-SqlConnection {
    param(
        [Parameter(Mandatory)]
        [string]$DatabaseName,

        [Parameter(Mandatory)]
        [pscredential]$Credential
    )

    $RtnVal = $false
    $userName = 'sa'
    $password = 'Optum123'

    $ErrorActionPreference = 'Stop'    

    try {
        $userName = $Credential.UserName
        $password = $Credential.GetNetworkCredential().Password
        #$connectionString = 'Data Source={0};database={1};User ID={2};Password={3}' -f $vmName,$DatabaseName,$userName,$password
        $connectionString = 'Data Source={0};User ID={1};Password={2}' -f $vmName,$userName,$password
        $sqlConnection = New-Object System.Data.SqlClient.SqlConnection $ConnectionString
        $sqlConnection.Open()

        ## This will run if the Open() method does not throw an exception
        $true
    } catch {
        ## Only return $false if the exception was thrown because it can't connect for some reason.  Otherwise throw the general exception.
        if ($_.Exception.Message -match 'cannot open server') {
            $false
        } else {
            throw $_
        }
    } finally {
        ## The connection will be closed further down the process.
        #$sqlConnection.Close()
    }

    return $sqlConnection
}

<#
    When runnig the Get-Service, Stop-Service, Start and Refresh methods on a remote VM 
    they have to be wrapped in a ScriptBlock or they will fail.
#>
function ReplaceMSService {
    param(
        [Parameter(Mandatory)]
        [object]$session, 

        [Parameter(Mandatory=$True, HelpMessage='Name of services to check for')]
        [Alias('ServiceName')]
        [string]$vmService
    )

    $pw_version = $PSVersionTable.PSVersion.Major
    Write-Host "Using PowerShell Version: $pw_version "

    $ServiceStatus = Invoke-Command -ScriptBlock {       
        
        $service = Get-Service -Name $args[0]
        Stop-Service -InputObject $service -Verbose -Force
        $service.Start()
        $service.Refresh()

        # Save lastexitcode right after call to exe completes
        $exitCode = $service

        # Return the output and the exitcode using a hashtable
        New-Object -TypeName PSCustomObject -Property @{Host=$env:computername; Output=$output; ExitCode=$exitCode}
        
        return $service
    } -Session $session -ArgumentList $vmService
    
    return $ServiceStatus
    
}

##---------------------------------------------------------

## Call the Make-SqlConnection function 
## Begin Coding
Clear-Host

## Loging Defined
$Logfile = $vmLoggingFolderPath + $vmLogName
LogWrite " "
LogWrite "[Event] Update Server Name Started on $vmName"

$DbName = "ICP"
$username = "sa"

## Create a remote session
$adminUserName = 'icpsupport@icp.lab' 
$adminPassword = 'Optum098'
$password = ConvertTo-SecureString $adminPassword -AsPlainText -Force
$cred = New-Object System.Management.Automation.PSCredential ($adminUserName, $password )
$session = New-PSSession -ComputerName $vmName -Credential $cred

## Check to make sure the vm is online 
$timeOut = 0
## Note that is is the number of loop times before aborting.  The Test-Connection command will get ran maxTriesBeforeTimeout 
## times so in essance the time is doubled before the code times out.  
## Example: 45 loops will result in approximantly 1.5 minutes.
$maxTriesBeforeTimeout = 75

## dns Names
$dbServerNameUpdateFailed = $False


try {
    do {
        try {
            $isVMConnected = $false
            $timeOut = $timeOut +1

            if (Test-Connection -computername $vmName -Count 1 -ErrorAction stop) {
                $isVMConnected = $true

                $password = ConvertTo-SecureString "Optum123" -AsPlainText -Force
                $psCred = New-Object System.Management.Automation.PSCredential -ArgumentList ($username, $password)
                [Object] $sqlConnection = Make-SqlConnection -DatabaseName $DbName -Credential $psCred

                # We don't need to wait any longer.
                $timeOut = $maxTriesBeforeTimeout

            }
        } catch [System.Net.NetworkInformation.PingException] {
            Write-Host "Timeout Count: $timeOut; $vmName is not responding to ping."
            LogWrite "Timeout Count: $timeOut; $vmName is not responding to ping."

            Start-Sleep -s 30
        }

    } While ($timeOut -lt $maxTriesBeforeTimeout) 


    if ($isVMConnected -eq $true) {

        if ($sqlConnection[1].State -eq 'Open') {
            Write-Host "Connection was made to $vmName."
            LogWrite "Connection was made to $vmName"

            ## Get the current server name according to @@servername
            $sqlStmt = "select @@servername;"
            $currentServerName = Invoke-Sqlcmd -ServerInstance $vmName -Query $sqlStmt  -username $userName -password 'Optum123'
            $mCurrentName = $currentServerName.Column1
            Write-Host "Current @@servername is: $mCurrentName"
            LogWrite "Current @@servername is: $mCurrentName"

            ## Drop the hostname in SQL Server
            Write-Host "Drop the SQL Server hostname $mCurrentName in $vmName"
            LogWrite "Drop the SQL Server hostname $mCurrentName in $vmName"
            $sqlStmt = "sp_dropserver '" + $mCurrentName +"';"
            Invoke-Sqlcmd -Query $sqlStmt -ServerInstance $vmName -username $userName -password 'Optum123'

            ## Add the new hostname in SQL Server from variable
            Write-Host "Adding the SQL Server hostname $vmName in $vmName"
            LogWrite "Adding the SQL Server hostname $vmName in $vmName"
            $sqlStmt = "sp_addserver '" + $vmName +"', 'local';"
            Invoke-Sqlcmd -Query $sqlStmt -ServerInstance $vmName -username $userName -password 'Optum123'

            ## Stop MSSQLServer Service.  Because of dependencies -Force must be used in the command.
            Write-Host "Stop MSSQLServer Service on $vmName"
            LogWrite "Stop MSSQLServer Service on $vmName"
            
            ## The Powershell version must be checked in order to run certain commands.
            ## The session will be passed for consistency. 
            #$service = StopStartMSService $session $vmService
            $service = ReplaceMSService $session $vmService
            LogWrite "Refreshed MSSQLServer Service on $vmName"
            #Run_Get-Service $session $vmService
            #LogWrite "Refreshed MSSQLServer Service on $vmName"
            
        }

    } else {
        Write-Host "Connection failed!"
        LogWrite "Connection failed!"
        throw $_
    }
}
catch {
    $false
    $dbServerNameUpdateFailed = $True
    Write-Host "[Event] Server update Failed.  Unable to connect to the $DbName database!"
    LogWrite "[Event] Server update Failed.  Unable to connect to the $DbName database!"
}
finally {
    ## Close the connection when we're done
    ## First check to make sure the connection isn't null
    if ($null -ne $sqlConnection) {
        $sqlConnection[1].Close()
    }

    if ($dbServerNameUpdateFailed -eq $False) {
        Write-Host "[Event] Server update Completed Successfully!"
        LogWrite "[Event] Server update Completed Successfully!"
    }
}
