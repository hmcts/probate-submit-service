provider "vault" {
  //  # It is strongly recommended to configure this provider through the
  //  # environment variables described above, so that each user can have
  //  # separate credentials set in the environment.
  //  #
  //  # This will default to using $VAULT_ADDR
  //  # But can be set explicitly
  address = "https://vault.reform.hmcts.net:6200"
}


data "vault_generic_secret" "probate_mail_host" {
  path = "secret/${var.vault_section}/probate/probate_mail_host"
}

data "vault_generic_secret" "probate_mail_username" {
  path = "secret/${var.vault_section}/probate/probate_mail_username"
}

data "vault_generic_secret" "probate_mail_password" {
  path = "secret/${var.vault_section}/probate/probate_mail_password"
}

data "vault_generic_secret" "probate_mail_port" {
  path = "secret/${var.vault_section}/probate/probate_mail_port"
}

data "vault_generic_secret" "probate_mail_sender" {
  path = "secret/${var.vault_section}/probate/probate_mail_sender"
}

data "vault_generic_secret" "probate_mail_recipient" {
  path = "secret/${var.vault_section}/probate/probate_mail_recipient"
}

data "vault_generic_secret" "idam_backend_service_key" {
  path = "secret/${var.vault_section}/ccidam/service-auth-provider/api/microservice-keys/probate-backend"
}

data "vault_generic_secret" "spring_application_json_submit_service" {
  path = "secret/${var.vault_section}/probate/spring_application_json_submit_service_azure"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  //java_proxy_variables: "-Dhttp.proxyHost=${var.proxy_host} -Dhttp.proxyPort=${var.proxy_port} -Dhttps.proxyHost=${var.proxy_host} -Dhttps.proxyPort=${var.proxy_port}"

  //probate_frontend_hostname = "probate-frontend-aat.service.core-compute-aat.internal"
  previewVaultName = "pro-submit-ser"
  nonPreviewVaultName = "pro-submit-ser-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"

  nonPreviewVaultUri = "${module.probate-submit-service-vault.key_vault_uri}"
  previewVaultUri = "https://pro-submit-ser-aat.vault.azure.net/"
  vaultUri = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultUri : local.nonPreviewVaultUri}"
}

module "probate-submit-service" {
  source = "git@github.com:hmcts/moj-module-webapp.git?ref=cnp707b"
  product = "${var.product}-${var.microservice}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  is_frontend  = false
  subscription = "${var.subscription}"
  asp_name     = "${var.product}-${var.env}-asp-${var.env}-asp"
  capacity     = "${var.capacity}"
  common_tags  = "${var.common_tags}"
  asp_rg       = "mgmt-asp-${var.env}"
  app_settings = {

	  // Logging vars
    REFORM_TEAM = "${var.product}"
    REFORM_SERVICE_NAME = "${var.microservice}"
    REFORM_ENVIRONMENT = "${var.env}"
  

    DEPLOYMENT_ENV= "${var.deployment_env}"
    //JAVA_OPTS = "${local.java_proxy_variables}"

    MAIL_USERNAME = "${data.vault_generic_secret.probate_mail_username.data["value"]}"
    MAIL_PASSWORD = "${data.vault_generic_secret.probate_mail_password.data["value"]}"
    MAIL_HOST = "${data.vault_generic_secret.probate_mail_host.data["value"]}"
    MAIL_PORT = "${data.vault_generic_secret.probate_mail_port.data["value"]}"
    MAIL_JAVAMAILPROPERTIES_SENDER = "${data.vault_generic_secret.probate_mail_sender.data["value"]}"
    MAIL_JAVAMAILPROPERTIES_RECIPIENT = "${data.vault_generic_secret.probate_mail_recipient.data["value"]}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY = "${data.vault_generic_secret.idam_backend_service_key.data["value"]}"
    SPRING_APPLICATION_JSON = "${data.vault_generic_secret.spring_application_json_submit_service.data["value"]}"
   
    MAIL_JAVAMAILPROPERTIES_SUBJECT = "${var.probate_mail_subject}"
    MAIL_JAVAMAILPROPERTIES_MAIL_SMTP_AUTH = "${var.probate_mail_use_auth}"
    MAIL_JAVAMAILPROPERTIES_MAIL_SMTP_SSL_ENABLE = "${var.probate_mail_use_ssl}"
    SERVICES_PERSISTENCE_BASEURL = "${var.services_persistence_baseUrl}" 
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL = "${var.idam_service_api}"
    SERVICES_CORECASEDATA_BASEURL = "${var.ccd_baseUrl}"
    SERVICES_CORECASEDATA_ENABLED = "${var.ccd_enabled}"
   
    java_app_name = "${var.microservice}"
    LOG_LEVEL = "${var.log_level}"
    //ROOT_APPENDER = "JSON_CONSOLE" //Remove json logging

  }
}

module "probate-submit-service-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.probate-submit-service.resource_group_name}"
  product_group_object_id = "33ed3c5a-bd38-4083-84e3-2ba17841e31e"
}
