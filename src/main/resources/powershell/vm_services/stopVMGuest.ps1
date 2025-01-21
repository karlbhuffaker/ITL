Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
Get-VM ${vmName} | Stop-VMGuest -Confirm:$false
