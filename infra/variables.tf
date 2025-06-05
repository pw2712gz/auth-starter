variable "aws_region" {
  description = "AWS region to deploy to"
  type        = string
  default     = "us-east-1"
}

variable "service_name" {
  description = "App Runner service name"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository in the format owner/repo"
  type        = string
}

variable "github_branch" {
  description = "GitHub branch to deploy"
  type        = string
  default     = "main"
}

variable "github_connection_arn" {
  description = "GitHub connection ARN from AWS App Runner"
  type        = string
}
