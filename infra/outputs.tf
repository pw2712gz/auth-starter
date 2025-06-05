output "auth_backend_url" {
  description = "Public URL of the App Runner backend service"
  value       = aws_apprunner_service.auth_backend.service_url
}
