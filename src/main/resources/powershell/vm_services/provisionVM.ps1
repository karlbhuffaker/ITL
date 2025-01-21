Connect-VIServer -Server ${vCenterName} -Protocol https -User ${vServiceUser} -Password ${vServicePassword}
New-VM -Name ${vmName} -Template ${template} -VMHost ${vmHost} -ResourcePool ${resourcePool} -Datastore ${dataStore} -OSCustomizationSpec ${osSpec} -Location ${vmLocation} -DiskStorageFormat Thin -NetworkName ${networkName}
Start-VM -VM ${vmName} -Confirm:$false -RunAsync