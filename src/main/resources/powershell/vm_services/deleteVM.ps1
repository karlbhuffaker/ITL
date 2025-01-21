$userName = ${userName}
$pw = ${pw}
$password = ConvertTo-SecureString ${pw} -AsPlainText -Force
$cred = New-Object System.Management.Automation.PSCredential ($userName, $password )
$vmname = ${vmName}
$vmFQDN = [System.Net.Dns]::GetHostByName($vmname).HostName
$adServer = ${server}
$vmNet = Get-NetIPAddress -CimSession $vmFQDN -AddressFamily 'IPv4' | where-object -FilterScript {$_.PrefixOrigin -eq 'DHCP'}
$ipAddress = $vmNet.IPAddress
#Remove VM from DHCP
Remove-DhcpServerv4Lease -ComputerName ${dhcpServer} -IPAddress $ipAddress -Confirm:$false
#Remove VM from DNS
Remove-DnsServerResourceRecord -ZoneName ${scopeId} -ComputerName ${dhcpServer} -RRType 'A' -Name $vmname -Force:$True  -Confirm:$false
#Remove VM from AD
Remove-ADComputer -Credential $cred -Server $adServer -Identity $vmname -Confirm:$false
#Remove VM from vCenter
Connect-VIServer -Server ${vCenterName} -Protocol 'https' -User ${vServiceUser} -Password ${pw}
Stop-VM -VM $vmname -Confirm:$false
Remove-VM -VM $vmname -Confirm:$false -DeletePermanently
#Wait 60 seconds so the network infrastructure is updated
Start-Sleep -s 60

