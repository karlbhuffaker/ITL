Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
Stop-VM -VM ${vmName} -Confirm:$false
