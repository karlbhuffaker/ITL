Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
Restart-VM -VM ${vmName} -Confirm:$false