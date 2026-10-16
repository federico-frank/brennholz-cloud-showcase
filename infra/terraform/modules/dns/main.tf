# DNS-Zone für die eigene Domain: A-Einträge für die Domain und www auf die Elastic IP.
# Die Nameserver der Zone werden beim Registrar (NameHero) eingetragen.
resource "aws_route53_zone" "main" {
  name    = var.zone_name
  comment = "Live-Demo der Brennholz-Plattform"
}

resource "aws_route53_record" "apex" {
  zone_id = aws_route53_zone.main.zone_id
  name    = var.zone_name
  type    = "A"
  ttl     = 300
  records = [var.public_ip]
}

resource "aws_route53_record" "www" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "www.${var.zone_name}"
  type    = "A"
  ttl     = 300
  records = [var.public_ip]
}
