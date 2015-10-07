#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0NDE2NTA0MjQxODpiODAyODhkYzMyZjVlYTgwZmFiYzp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUwMUB2cmEubGFiZXhwaXJhdGlvbjoxNDQ0MjUxNDQyNDE4OjNmODQ4MDE0NWUzOTdlMjEwMjY1ZWMxNzk5NzI4OTEzMDdjZTY3NTY1YzUzZGNjMzAwZmFhZTZhNjcyY2Q0NTAxOTJkYWY1Njk1NzA2NTZjN2FjZjRkZTM3NjRlZjczZDYxNDFmMDhkNGQ0ZjdkYzcxOWQzYjk2ZTMxZjUyYjQ0

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template