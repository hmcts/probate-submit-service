output "vaultUri" {
  value = "${module.probate-submit-service-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${module.probate-submit-service-vault.key_vault_name}"
}
