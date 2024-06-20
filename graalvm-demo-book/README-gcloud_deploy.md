# Google Cloud

## Connect with gcloud
### 0. Prerequisites
- Google Cloud SDK installed locally

### 1. Authentication
```bash
gcloud auth login
```
Opens Browser and prompts you to log in with your Google Cloud Account.

### 2. Configure Project
```bash
gcloud config set project [PROJECT_ID]
```
Replace `[PROJECT_ID]` with the ID of your Google Cloud project.

```bash
gcloud config set compute/region [REGION]
```
Replace `[REGION]` with the closest region to your location (europe-west3).

## Upload Images to Google Artifacts
### 0. Prerequisites
- Docker installed locally

### 1. Set Up Artifact
```bash
gcloud services enable artifactregistry.googleapis.com
```
This enables the Artifact Registry API for your project `[PROJECT_ID]`.
```bash
gcloud artifacts repositories create [YOUR_REPO] --repository-format=docker --location=[REGION] --description="Artifact Repository"
```
This creates an Artifact Repository called `[YOUR_REPO]` for your project with the location `[REGION]` which should be the same as above (europe-west3).

### 2. Push to Artifact Repository
This is presuming that the image you want to push to the Artifact Repository has been built locally.
```bash
docker tag [YOUR_IMAGE] [REGION]-docker.pkg.dev/[PROJECT_ID]/[YOUR_REPO]/[YOUR_IMAGE]:[TAG]
```
`[YOUR_IMAGE]` is your local image <br>
`[REGION]` is the same region as picked above (europe-west3) <br>
`[PROJECT_ID]` is the same ID as above, ID of your Google Cloud project <br>
`[YOUR_REPO]` is the name of the Artifact Repository you created above <br>
`[TAG]` is a tag for the image

This creates a copy of `[YOUR_IMAGE]` with the name and tag (NAME:TAG) specified.

```bash
docker push [REGION]-docker.pkg.dev/[PROJECT_ID]/[YOUR_REPO]/[YOUR_IMAGE]:[TAG]
```
This pushes the image to the Artifact Repository you created. Now you should see the image in your Google Cloud Artifact Repository.

## Configure Secret with Secret Manager
[Official Documentation](https://cloud.google.com/run/docs/configuring/services/secrets)

Go to the Secret Manager in your GCP Project. This is where you set up a secret (bearer token) that can be used in Google Cloud Run container deployments.
Verify that the container and the account linked to this secret have the necessary rights to access the secret. It is recommended to use the
"latest" version of this secret when configuring the deployment. This way, you can change the secret using the Secret Manager, and the
container will always use the latest version.

### Get Bearer Token
In an [authenticated](#1-authentication) terminal use
```bash
gcloud auth print-identity-token
```
to print the bearer token.

## Virtual Private Cloud (VPC) in Google Cloud
Create a VPC with the following command:
```shell
gcloud compute networks create [NETWORK_NAME] `
    --subnet-mode=[MODE] `
    --bgp-routing-mode=[ROUTING_MODE] `
    --mtu=[MTU]
```
`[NETWORK_NAME]` the name for the VPC <br>
`[MODE]` two options: `auto` or `custom` <br>
`[ROUTING_MDOE]` two options: `regional` or `global` <br>
`[MTU]` Maximum Transfer Unit, defines the maximum size of IP-packets in byte (default=1460) [docu](https://cloud.google.com/vpc/docs/mtu?hl=de) <br>

Here we use:
```shell
gcloud compute networks create fra-uas-lecture `
    --subnet-mode=auto `
    --bgp-routing-mode=regional `
    --mtu=1460
```

Check if the VPC was created successfully:
```bash
gcloud compute networks list
# Further details about a single network:
gcloud compute networks describe [NETWORK_NAME]
```

### Subnet Modes: `auto` and `custom`
| Mode     | Description                                                                                                                                                |
|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `auto`   | This creates a subnet for every single region automatically. This will also create a new subnet for new regions after the VPC creation.                    |
| `custom` | With this mode you have to create the subnets for all regions yourself. But you can elect to only create subnets for regions you want services running in. |

[Official Documentation](https://cloud.google.com/vpc/docs/create-modify-vpc-networks?hl=de#creating_networks)

### Dynamic Routing Modes: `regional` and `global`
| Mode       | Description                                                                                                                                                                                                                                                                                                                                                                                              |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `regional` | In regional dynamic routing mode, Cloud Router creates a dynamic route for the destination and next hop in the same region as the Cloud Router. Cloud Router sets the priority for that dynamic route to the base priority, which Cloud Router derives from the multi-exit discriminator (MED) advertised by your on-premises router.                                                                    |
| `global`   | In global dynamic routing mode, Cloud Router creates a dynamic route for the destination and next hop in each Google Cloud region. In the region that contains the Cloud Router that learned that route, Cloud Router sets the priority for the dynamic route to the base priority. In all other regions, Cloud Router sets the priority to the base priority plus an appropriate region-to-region cost. |

[Official Documentation](https://cloud.google.com/network-connectivity/docs/router/concepts/overview?hl=de#priority-and-dynamic-routing)

## Troubleshooting
### Container Creation: Error "mvnw" not found
Full Error: `exec ./mvnw: no such file or directory`

Make sure the mvnw script doesn't have windows EOL (end of line) characters (`\r`).

Check from the project dir (unix shell):
```bash
file mvnw 

# Output if there are Windows EOL characters:
mvnw: POSIX shell script, ASCII text executable, with CRLF line terminators
```
To delete these characters run (replaces `\r` with nothing):
```bash
sed -i 's/\r//' mvnw

file mvnw
# Running the `file` command again should now return:
mvnw: POSIX shell script, ASCII text executable
```

You can Future-proof for EOL characters with Git:
```bash
git config --global core.autocrlf true
```
This configures Git to ensure line endings in files you checkout are correct for Windows.
For compatibility, line endings are converted to Unix style when you commit files.