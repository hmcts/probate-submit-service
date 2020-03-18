provider "azurerm" {
  version = "1.22.1"
}

locals {
  vaultName = "${var.raw_product}-${var.env}"
}
