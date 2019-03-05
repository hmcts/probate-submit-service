provider "azurerm" {
  version = "1.22.1"
}

locals {
  aseName = "core-compute-${var.env}"
  //java_proxy_variables: "-Dhttp.proxyHost=${var.proxy_host} -Dhttp.proxyPort=${var.proxy_port} -Dhttps.proxyHost=${var.proxy_host} -Dhttps.proxyPort=${var.proxy_port}"

  //probate_frontend_hostname = "probate-frontend-aat.service.core-compute-aat.internal"
  previewVaultName = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
  localenv = "${(var.env == "preview" || var.env == "spreview") ? "aat": "${var.env}"}"
}

data "azurerm_key_vault" "probate_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "s2s_key" {
  name      = "microservicekey-probate-backend"
  vault_uri = "https://s2s-${local.localenv}.vault.azure.net/"
}

data "azurerm_key_vault_secret" "probate_mail_host" {
  name = "probate-mail-host"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "probate_mail_username" {
  name = "probate-mail-username"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "probate_mail_password" {
  name = "probate-mail-password"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "probate_mail_port" {
  name = "probate-mail-port"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "probate_mail_sender" {
  name = "probate-mail-sender"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "probate_mail_recipient" {
  name = "probate-mail-recipient"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "spring_application_json_submit_service" {
  name = "spring-application-json-submit-service-azure"
  vault_uri = "${data.azurerm_key_vault.probate_key_vault.vault_uri}"
}

module "probate-submit-service" {
  source = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  is_frontend  = false
  subscription = "${var.subscription}"
  asp_name     = "${var.asp_name}"
  capacity     = "${var.capacity}"
  common_tags  = "${var.common_tags}"
  asp_rg       = "${var.asp_rg}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"

  app_settings = {

	  // Logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"
  

    DEPLOYMENT_ENV= "${var.deployment_env}"

    MAIL_USERNAME = "${data.azurerm_key_vault_secret.probate_mail_username.value}"
    MAIL_PASSWORD = "${data.azurerm_key_vault_secret.probate_mail_password.value}"
    MAIL_HOST = "${data.azurerm_key_vault_secret.probate_mail_host.value}"
    MAIL_PORT = "${data.azurerm_key_vault_secret.probate_mail_port.value}"
    MAIL_JAVAMAILPROPERTIES_SENDER = "${data.azurerm_key_vault_secret.probate_mail_sender.value}"
    MAIL_JAVAMAILPROPERTIES_RECIPIENT = "${data.azurerm_key_vault_secret.probate_mail_recipient.value}"

    AUTH_PROVIDER_SERVICE_CLIENT_KEY = "${data.azurerm_key_vault_secret.s2s_key.value}"
    SPRING_APPLICATION_JSON = "${data.azurerm_key_vault_secret.spring_application_json_submit_service.value}"
   
    MAIL_JAVAMAILPROPERTIES_SUBJECT = "${var.probate_mail_subject}"
    MAIL_JAVAMAILPROPERTIES_MAIL_SMTP_AUTH = "${var.probate_mail_use_auth}"
    MAIL_JAVAMAILPROPERTIES_MAIL_SMTP_SSL_ENABLE = "${var.probate_mail_use_ssl}"
    SERVICES_PERSISTENCE_BASEURL = "${var.services_persistence_baseUrl}" 
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL = "${var.idam_service_api}"
    SERVICES_CORECASEDATA_BASEURL = "${var.ccd_baseUrl}"
    SERVICES_CORECASEDATA_ENABLED = "${var.ccd_enabled}"
   
    java_app_name = "${var.microservice}"
    LOG_LEVEL = "${var.log_level}"

  }
}
