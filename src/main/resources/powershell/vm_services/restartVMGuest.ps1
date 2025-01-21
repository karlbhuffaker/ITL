Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
Get-VM ${vmName} | Restart-VMGuest -Confirm:$false