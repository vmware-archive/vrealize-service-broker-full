#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0NTM3MTY1MDMxNjpkZjVhMmQ1ZGQ5MjEyNWQ4ZjJhNzp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUwMUB2cmEubGFiZXhwaXJhdGlvbjoxNDQ1NDU4MDUwMzE2OmU0ZWU4OWNmMjk1MDA5YWNkNzJhMWIyMTRkNjMzZTcwMGVhYzQ4Y2I1NDJiY2QzZGRmN2E3NWU0YzQ5MmYxNzg5MTBjOGM0ZjAyYjA5ODA2YmExNDhjYmJhZDA1NDVkZTY3YTU5ODdmZDhiZDUwYzZmODMzMDI1OGFiZjk3Yzll

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

# details for a request
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests/9a24dbaf-22b6-47dd-bf1b-259970bf5c67/resourceViews?withExtendedData=true&withOperations=true&limit=100

# a delete template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/c15e1f40-a67d-4b46-bc99-d94ec9c8117a/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

#all requests
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests&limit=100

#all resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?limit=100

#details of a resource
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/a2e0f8ee-0902-4b4e-98ae-3af852a4654d?withExtendedData=true&withOperations=true&limit=100


#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

#child resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%2776c13d79-d292-4d38-8449-f6c72ecb97ce%27

#parent resource of vm
curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/df13aba9-278b-4fb9-beec-1e14f29a2337