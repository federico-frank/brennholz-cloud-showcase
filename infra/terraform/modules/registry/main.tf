resource "aws_ecr_repository" "app" {
  name                 = "${var.name}-app"
  image_tag_mutability = "IMMUTABLE" # ein Tag = genau ein Build (Git-Commit)
  force_delete         = true        # Demo: destroy auch mit Images

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }
}

# Speicher kostet: nur die letzten 5 Images behalten
resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Nur die letzten 5 Images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 5
      }
      action = { type = "expire" }
    }]
  })
}
