resource "aws_apprunner_service" "auth_backend" {
  service_name = var.service_name

  source_configuration {
    authentication_configuration {
      connection_arn = var.github_connection_arn
    }

    auto_deployments_enabled = true

    code_repository {
      repository_url = "https://github.com/${var.github_repo}"
      source_code_version {
        type  = "BRANCH"
        value = var.github_branch
      }

      code_configuration {
        configuration_source = "API"

        code_configuration_values {
          runtime     = "DOCKER"
          build_command = null
          port          = "8080"
          start_command = null
        }
      }
    }
  }

  instance_configuration {
    cpu    = "1024"  # 1 vCPU
    memory = "2048"  # 2 GB
  }
}
