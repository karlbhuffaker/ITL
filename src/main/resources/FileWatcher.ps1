# Define the source and destination folders
$sourceFolder = "\\nas00583pn\SCM\Builds\ITL"
$destinationFolder       = "\\otl-svm0.otl.lab\icp-wvc\DEVOPS\Karl\ITL_War_Build"
$test_destination_folder = "\\icplabs-app-02\d$\app\Tomcat9\webapps"
$dev_destination_folder  = "\\icplabs-app-03\D$\app\Tomcat9\webapps"

# Check if the ITL.war file exists in the source folder
$sourceFile = Join-Path -Path $sourceFolder -ChildPath "ITL.war"
if (Test-Path -Path $sourceFile) {
    Copy-Item -Path $sourceFile -Destination $test_destination_folder
    Copy-Item -Path $sourceFile -Destination $dev_destination_folder

    # Move the file to the destination folder
    Move-Item -Path $sourceFile -Destination $destinationFolder
    Write-Output "File moved to $destinationFolder"
} else {
    Write-Output "ITL.war file not found in $sourceFolder"
}