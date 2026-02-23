#!/usr/bin/env pwsh
# Git Push to GitHub - Automated Setup Script
# Usage: .\push-to-github.ps1 -GitHubUsername "your-username" -RepositoryName "recordbook"

param(
    [string]$GitHubUsername = "",
    [string]$RepositoryName = "recordbook",
    [string]$BranchName = "main",
    [string]$CommitMessage = "feat: implement daily expense record feature with API"
)

# Colors for output
$Green = "`e[32m"
$Red = "`e[31m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

function Write-Success {
    Write-Host "$Green✓ $args$Reset"
}

function Write-Error-Custom {
    Write-Host "$Red✗ $args$Reset"
}

function Write-Info {
    Write-Host "$Blue→ $args$Reset"
}

function Write-Warning-Custom {
    Write-Host "$Yellow⚠ $args$Reset"
}

# Main script
Write-Host "`n$Blue========================================$Reset"
Write-Host "$Blue  GitHub Push Setup Script$Reset"
Write-Host "$Blue========================================$Reset`n"

# Check if username is provided
if ([string]::IsNullOrEmpty($GitHubUsername)) {
    Write-Error-Custom "GitHub username not provided"
    Write-Info "Usage: .\push-to-github.ps1 -GitHubUsername 'your-username' -RepositoryName 'recordbook'"
    exit 1
}

# Change to project directory
$ProjectPath = "C:\sanchay\recordbook"
if (!(Test-Path $ProjectPath)) {
    Write-Error-Custom "Project path not found: $ProjectPath"
    exit 1
}

Set-Location $ProjectPath
Write-Success "Changed to project directory: $ProjectPath"

# Step 1: Check if Git is installed
Write-Info "Checking if Git is installed..."
try {
    $GitVersion = git --version
    Write-Success "Git is installed: $GitVersion"
} catch {
    Write-Error-Custom "Git is not installed. Please install Git first from https://git-scm.com"
    exit 1
}

# Step 2: Initialize Git repository
Write-Info "Initializing Git repository..."
if (!(Test-Path ".git")) {
    git init
    Write-Success "Git repository initialized"
} else {
    Write-Warning-Custom "Git repository already initialized"
}

# Step 3: Configure Git user
Write-Info "Configuring Git user (local)..."
git config user.name "Sanchay"
git config user.email "sanchay@urviclean.com"
Write-Success "Git user configured"

# Step 4: Add all files
Write-Info "Adding all files to Git..."
git add .
Write-Success "All files staged"

# Step 5: Create commit
Write-Info "Creating initial commit..."
git commit -m "$CommitMessage"
Write-Success "Commit created"

# Step 6: Rename branch to main if needed
Write-Info "Setting up main branch..."
$CurrentBranch = git rev-parse --abbrev-ref HEAD
if ($CurrentBranch -ne $BranchName) {
    git branch -M $BranchName
    Write-Success "Branch renamed to $BranchName"
} else {
    Write-Success "Already on $BranchName branch"
}

# Step 7: Add remote
Write-Info "Adding remote repository..."
$RemoteURL = "https://github.com/$GitHubUsername/$RepositoryName.git"
$ExistingRemote = git remote get-url origin 2>$null
if ($ExistingRemote) {
    Write-Warning-Custom "Remote 'origin' already exists: $ExistingRemote"
    Write-Info "To change it, run: git remote set-url origin $RemoteURL"
} else {
    git remote add origin $RemoteURL
    Write-Success "Remote repository added: $RemoteURL"
}

# Step 8: Display next steps
Write-Host "`n$Blue========================================$Reset"
Write-Host "$Green  Setup Complete!$Reset"
Write-Host "$Blue========================================$Reset`n"

Write-Info "Repository Configuration:"
Write-Host "  Username: $GitHubUsername"
Write-Host "  Repository: $RepositoryName"
Write-Host "  Branch: $BranchName"
Write-Host "  Remote URL: $RemoteURL"

Write-Info "`nGit Status:"
git status

Write-Host "`n$Yellow  IMPORTANT - Next Steps:$Reset"
Write-Host "  1. Create a repository on GitHub:"
Write-Host "     https://github.com/new"
Write-Host ""
Write-Host "  2. Push your code to GitHub:"
Write-Host "     git push -u origin main"
Write-Host ""
Write-Host "  3. You may be prompted for credentials:"
Write-Host "     - Use your GitHub username"
Write-Host "     - Use a Personal Access Token as password"
Write-Host "       (Create at: https://github.com/settings/tokens)"
Write-Host ""
Write-Host "  4. Verify the push:"
Write-Host "     git log --oneline -5"
Write-Host ""

Write-Host "$Green  Documentation:$Reset"
Write-Host "  See GITHUB_PUSH_GUIDE.md for detailed instructions"
Write-Host ""

