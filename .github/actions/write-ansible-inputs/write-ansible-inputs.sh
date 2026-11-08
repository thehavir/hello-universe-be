#!/usr/bin/env bash
set -euo pipefail

umask 077
: "${VPS_HOST:?Set the VPS_HOST variable}" "${PUBLIC_API_HOST:?Set the PUBLIC_API_HOST variable}"
: "${DEPLOY_SSH_PRIVATE_KEY:?Set the DEPLOY_SSH_PRIVATE_KEY secret}"
: "${VPS_SSH_KNOWN_HOSTS:?Set the VPS_SSH_KNOWN_HOSTS secret}"
printf '%s\n' "$DEPLOY_SSH_PRIVATE_KEY" > "$RUNNER_TEMP/deploy-ssh-key"
printf '%s\n' "$VPS_SSH_KNOWN_HOSTS" > "$RUNNER_TEMP/vps-known-hosts"
# The playbook targets the production group.
jq -n --arg host "$VPS_HOST" --arg user "$VPS_DEPLOY_USER" --argjson port "$VPS_SSH_PORT" \
  --arg key "$RUNNER_TEMP/deploy-ssh-key" \
  --arg ssh_args "-o StrictHostKeyChecking=yes -o UserKnownHostsFile=\"$RUNNER_TEMP/vps-known-hosts\" -o IdentitiesOnly=yes" \
  '{all: {children: {production: {hosts: {hello_universe_vps: {
    ansible_host: $host, ansible_user: $user, ansible_port: $port,
    ansible_ssh_private_key_file: $key, ansible_python_interpreter: "/usr/bin/python3",
    ansible_ssh_common_args: $ssh_args
  }}}}}}' > "$RUNNER_TEMP/ansible-inventory.json"
# The playbook reads these variable names; keep them unchanged.
jq -n --arg release_ref "$GITHUB_SHA" --arg deployment_id "$BUILD_ID" \
  --arg artifact_dir "$GITHUB_WORKSPACE/release-files" \
  --arg public_api_url "https://$PUBLIC_API_HOST/apods?start_date=2024-01-01&end_date=2024-01-01" \
  '{release_ref: $release_ref, deployment_id: $deployment_id,
    artifact_dir: $artifact_dir, public_api_url: $public_api_url}' > "$RUNNER_TEMP/ansible-extra-vars.json"
