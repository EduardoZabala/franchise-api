# Single-table design (same keys used by the application and by LocalStack):
#   pk = FRANCHISE#{franchiseId}
#   sk = METADATA | BRANCH#{branchId} | BRANCH#{branchId}#PRODUCT#{productId}
resource "aws_dynamodb_table" "franchises" {
  name         = var.dynamodb_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled = true
  }
}
