output "images_bucket_name" {
  value = aws_s3_bucket.this["images"].bucket
}

output "images_bucket_arn" {
  value = aws_s3_bucket.this["images"].arn
}

output "deploy_bucket_name" {
  value = aws_s3_bucket.this["deploy"].bucket
}

output "deploy_script_etag" {
  value = aws_s3_object.deploy["deploy.sh"].etag
}

output "deploy_bucket_arn" {
  value = aws_s3_bucket.this["deploy"].arn
}
