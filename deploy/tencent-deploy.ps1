param(
    [Parameter(Mandatory = $true)]
    [string]$HostName,

    [string]$User = "ubuntu",
    [string]$SshKey = "",
    [string]$RemoteDir = "/opt/aihelp",
    [string]$ServiceName = "aihelp"
)

$ErrorActionPreference = "Stop"

function Invoke-Remote {
    param([string]$Command)
    $target = "$User@$HostName"
    if ($SshKey) {
        ssh -i $SshKey $target $Command
    } else {
        ssh $target $Command
    }
}

function Copy-Remote {
    param([string]$Source, [string]$Destination)
    $target = "$User@$HostName`:$Destination"
    if ($SshKey) {
        scp -i $SshKey $Source $target
    } else {
        scp $Source $target
    }
}

Write-Host "Building project..."
mvn -s settings-local.xml clean package

Write-Host "Preparing remote directory..."
Invoke-Remote "sudo mkdir -p $RemoteDir && sudo chown -R $User:$User $RemoteDir"

Write-Host "Uploading jar and service file..."
Copy-Remote "target/aihelp-1.0.0.jar" "$RemoteDir/aihelp.jar"
Copy-Remote "deploy/aihelp.service" "/tmp/$ServiceName.service"

Write-Host "Installing systemd service..."
Invoke-Remote "sudo mv /tmp/$ServiceName.service /etc/systemd/system/$ServiceName.service && sudo systemctl daemon-reload && sudo systemctl enable --now $ServiceName && sudo systemctl restart $ServiceName"

Write-Host "Deployment status:"
Invoke-Remote "sudo systemctl --no-pager status $ServiceName"

