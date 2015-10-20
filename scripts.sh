#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0NTI4MzM3MTE5MDowMDc5MmM1M2NhODg4YWI4ODM1Njp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUwMUB2cmEubGFiZXhwaXJhdGlvbjoxNDQ1MzY5NzcxMTkwOmJkYTFkMjk4ZmE5MzY5NmMzNzIxZTI5YWRlYjk4ZTU2YzViZmNjMWUwYmFkZWFmYmFmZTAxMWUxODY1ODE1YmE1NjU1NWQxNzcyYmMyN2QxZjJjMGNmYWFkYzFhMGNkZjYzMTFmZjk5ZDE5NDQxZTI3ZjgwYTBmN2ZmOWRjMGFl

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests/9ca10dee-730e-486a-9138-d8aade4913e2/resourceViews?withExtendedData=true&withOperations=true&limit=100

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?limit=100

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/ba73b71f-e65a-4c22-8b85-33ddcd4fd551?withExtendedData=true&withOperations=true&limit=100


curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

                                                                                                            https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests