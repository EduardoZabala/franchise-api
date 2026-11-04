output "dynamodb_table_name" {
  value = aws_dynamodb_table.franchises.name
}

output "ecr_repository_url" {
  description = "Where CI pushes the Docker image."
  value       = aws_ecr_repository.api.repository_url
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  value = aws_ecs_service.api.name
}

output "github_deploy_role_arn" {
  description = "Set it as the AWS_ROLE_ARN variable of the GitHub repository."
  value       = aws_iam_role.github_deploy.arn
}

output "github_actions_variables" {
  description = "Repository variables to configure in GitHub (Settings > Secrets and variables > Actions > Variables)."
  value = {
    AWS_REGION     = var.aws_region
    AWS_ROLE_ARN   = aws_iam_role.github_deploy.arn
    ECR_REPOSITORY = aws_ecr_repository.api.name
    ECS_CLUSTER    = aws_ecs_cluster.main.name
    ECS_SERVICE    = aws_ecs_service.api.name
  }
}
