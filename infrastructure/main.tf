provider "azurerm" {
  version = "1.22.1"
}

locals {
  vaultName = "${var.product}-${var.env}"
}
