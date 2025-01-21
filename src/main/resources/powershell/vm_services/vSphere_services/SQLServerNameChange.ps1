$serviceName = 'MSSQLSERVER'
$vmName = $env:computername
$maxRepeat = 20
$status = "Stopped"
$userName = 'sa'
$password = 'Optum123'


If (Get-Service $serviceName -ErrorAction SilentlyContinue) {
    do {
        $count = (Get-Service $serviceName | ? ($_.status -eq $status)).count
        $maxRepeat--
        sleep -Milliseconds 6000
    } until ($count -eq 0 -or $maxRepeat -eq 0)
    if ((Get-Service $serviceName).Status -eq 'Running') {
        ## Get the current server name according to @@servername
        $sqlStmt = "select @@servername;"
        $currentServerName = Invoke-Sqlcmd -ServerInstance $vmName -Query $sqlStmt  -username $userName -password 'Optum123'
        $mCurrentName = $currentServerName.Column1
        Write-Host "Current @@servername is: $mCurrentName"
        
        ## Drop the hostname in SQL Server
        Write-Host "Drop the SQL Server hostname $mCurrentName in $vmName"
        $sqlStmt = "sp_dropserver '" + $mCurrentName +"';"
        Invoke-Sqlcmd -Query $sqlStmt -ServerInstance $vmName -username $userName -password 'Optum123'

        ## Add the new hostname in SQL Server from variable
        Write-Host "Adding the SQL Server hostname $vmName in $vmName"
        $sqlStmt = "sp_addserver '" + $vmName +"', 'local';"
        Invoke-Sqlcmd -Query $sqlStmt -ServerInstance $vmName -username $userName -password 'Optum123'
    } 
    Else {
    Write-Host "Service is stopped"
    }
}

