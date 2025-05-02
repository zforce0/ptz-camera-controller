# Keystore Setup Guide for GitHub Actions

This guide explains how to prepare your keystore file for GitHub Actions to enable automated APK signing and releases.

## Creating a Keystore

If you don't already have a keystore, you can create one using the `keytool` command:

```bash
keytool -genkey -v -keystore ptz_controller_release.keystore -alias ptz_controller -keyalg RSA -keysize 2048 -validity 10000
```

Follow the prompts to enter your details:
- Enter a secure password
- Fill in your name, organization, and location details
- Confirm the information is correct

## Encoding the Keystore for GitHub Secrets

GitHub Actions requires secrets to be text-based. Since a keystore is a binary file, you need to encode it as Base64:

### On Linux/macOS:
```bash
base64 ptz_controller_release.keystore | tr -d '\n' | xclip -selection clipboard
```

### On Windows (PowerShell):
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("ptz_controller_release.keystore")) | Set-Clipboard
```

This command converts the keystore to Base64 and copies it to your clipboard.

## Adding Secrets to GitHub

1. Go to your GitHub repository
2. Click "Settings" > "Secrets and variables" > "Actions"
3. Click "New repository secret"
4. Add the following secrets:
   - Name: `KEYSTORE_FILE` 
     Value: *Paste the Base64-encoded keystore*
   - Name: `KEYSTORE_PASSWORD` 
     Value: *Your keystore password*
   - Name: `KEY_PASSWORD` 
     Value: *Your key password (may be the same as keystore password)*
   - Name: `KEY_ALIAS` 
     Value: `ptz_controller` (or whatever alias you used)

## Verifying Setup

To verify everything is set up correctly:

1. Push a tag to your repository:
   ```bash
   git tag -a v0.1.0 -m "Initial release"
   git push origin v0.1.0
   ```

2. Go to the "Actions" tab in your GitHub repository
3. You should see a workflow running that builds the APK
4. Once complete, check the "Releases" page to see if a release was created with the signed APK

## Troubleshooting

If the build fails, check the workflow logs for errors:

1. **Invalid keystore format**: Make sure the Base64 encoding has no line breaks
2. **Wrong password**: Verify your keystore and key passwords
3. **Keystore not found**: Check that the `KEYSTORE_FILE` secret is properly set
4. **Wrong alias**: Ensure the `KEY_ALIAS` matches what you used when creating the keystore

Remember, once these secrets are set up correctly, all future releases will be automatically signed with your keystore, ensuring a consistent identity for your app.