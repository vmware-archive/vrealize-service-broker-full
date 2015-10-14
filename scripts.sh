#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0NDgzMjU3MTU5NjpmODYxMDgzZWU4YTlhNzM3ZjY0Mzp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUwMUB2cmEubGFiZXhwaXJhdGlvbjoxNDQ0OTE4OTcxNTk2OjdkNjFmN2M2ODY2ZmRjNjQ2ZjFjNjA5YjExYjE0ZjBiYmViYWJlYjg4MDQwMmM3OTg0MmQzMDYzODI1NDQ5MDZhMGJjY2ZmMWJkYTJhOTFlYTMyMWU2NWNhM2JkMGI5MWJjZjA0ZTQ3OTI5MzczZjk1ODNjYjQ1NjVmNjhmZTY1

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests/5c09a0f6-a19f-4ce9-904a-8f3bf8242ddc
