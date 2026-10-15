variable "name" {
  type = string
}

variable "limit_usd" {
  type = number
}

variable "alert_emails" {
  type = list(string)
}
