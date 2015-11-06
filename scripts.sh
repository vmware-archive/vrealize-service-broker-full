#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0NjgyMTY2ODcyMTphNDIwYjU2YWM2NWIyZDEyNjE5Yzp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUxQHZzcGhlcmUubG9jYWxleHBpcmF0aW9uOjE0NDY4NTA0NjgwMDA6MzgyYTQ3NjJhMzNjOTY5OGJmOWZiYWEwNzNlZjNjMGJjZWQ0MmZiMzhlMmRiY2I5NzU0ZjlmNmFlMDkxNWNiNWExNjdmZTM4MjdhMTM2NWI5YzE5NGY2MzNlYzc0MzZjNzVjNGU1ODc5N2U1MDZiMzAwNjFkOTExYzA4YjhjY2I=

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

# details for a request
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests/5fd7c0ec-83d4-4522-b50c-d2b0caeb7e1b/resourceViews?withExtendedData=true&withOperations=true&limit=100

# a delete template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/7a693d2b-c6fc-4e87-af1f-c80a1fcdc780/actions/2aea3bf0-d193-4149-906f-c6274a73314d/requests/template

# post a delete template
#curl --insecure -X POST -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" -d '{"type":"com.vmware.vcac.catalog.domain.request.CatalogResourceRequest","resourceId":"7a693d2b-c6fc-4e87-af1f-c80a1fcdc780","actionId":"2aea3bf0-d193-4149-906f-c6274a73314d","description":null,"data":{"description":null,"reasons":null}}' https://vra.vra.lab/catalog-service/api/consumer/resources/7a693d2b-c6fc-4e87-af1f-c80a1fcdc780/actions/2aea3bf0-d193-4149-906f-c6274a73314d/requests


#all requests
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests&limit=100

#all resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?limit=100

#details of a resource
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/a2e0f8ee-0902-4b4e-98ae-3af852a4654d?withExtendedData=true&withOperations=true&limit=100


curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog- service/api/consumer/requests/5fd7c0ec-83d4-4522-b50c-d2b0caeb7e1b/resourceViews


#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

#child resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%2776c13d79-d292-4d38-8449-f6c72ecb97ce%27

#parent resource of vm
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/df13aba9-278b-4fb9-beec-1e14f29a2337