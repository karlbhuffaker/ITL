Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
Start-VM -VM ${vmName} -Confirm:$false
