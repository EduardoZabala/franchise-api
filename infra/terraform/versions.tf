terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }

  # State is local for simplicity. For team use, move it to a remote backend, e.g.:
  # backend "s3" { bucket = "<state-bucket>" key = "franchise-api/terraform.tfstate" region = "us-east-1" }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = var.project_name
      ManagedBy = "terraform"
    }
  }
}
