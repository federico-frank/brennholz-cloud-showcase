variable "name" {
  type = string
}

variable "instance_class" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_ids" {
  type = list(string)
}

variable "password" {
  type      = string
  sensitive = true
}
