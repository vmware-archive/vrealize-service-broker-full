#!/bin/bash

#curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude1@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens

token=MTQ0NzA4MTc3NTEyMDo1Y2YwZmZjNzRiMTE2OTlkYzMyYzp0ZW5hbnQ6bGFidXNlcm5hbWU6dmR1ZGUxQHZzcGhlcmUubG9jYWxleHBpcmF0aW9uOjE0NDcxMTA1NzUwMDA6Y2YzMzQ0ZDg3OTJiNTJhYTI5ZWZiMjczMTA1MTVhY2UxNmY1ZTc3YmVmM2IxNTNhNjJlNTZjYmZjMTFlNmZiZDVmYmZjYzAzZGZiNTEwMWZmMTlkY2FjYWU2ZmI1NGQyNDk3ZTNiMjgxYjgyYmQ4NjA1OGE3ZWNmM2I5ODg1Y2E=

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

# resource details for a request
curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests/8720ac04-9910-4426-b8b3-758f6e02e3bc/resourceViews

# a delete template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/87385b2f-a9c3-4a78-86dd-1202563c500f/actions/051a18db-6bf5-4468-97e0-942330528c92/requests/template

#post a delete template
#curl --insecure -X POST -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" -d '{"type":"com.vmware.vcac.catalog.domain.request.CatalogResourceRequest","resourceId":"87385b2f-a9c3-4a78-86dd-1202563c500f","actionId":"051a18db-6bf5-4468-97e0-942330528c92","description":null,"data":{"description":null,"reasons":null}}' https://vra.vra.lab/catalog-service/api/consumer/resources/87385b2f-a9c3-4a78-86dd-1202563c500f/actions/051a18db-6bf5-4468-97e0-942330528c92/requests 

#all requests
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests&limit=100

#all resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?limit=100

#details of a resource
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/a2e0f8ee-0902-4b4e-98ae-3af852a4654d?withExtendedData=true&withOperations=true&limit=100


#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

#child resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%2787385b2f-a9c3-4a78-86dd-1202563c500f%27

#parent resource of vm
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resourceViews/df13aba9-278b-4fb9-beec-1e14f29a2337

#try to figure out the IP....
#location=https://vra.vra.lab/catalog-service/api/consumer/requests/8720ac04-9910-4426-b8b3-758f6e02e3bc

#info from "location"
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" $location

#resource view
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" $location/resourceViews

#delete template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/06852d93-466d-4d73-80bc-78764b3d768a/actions/051a18db-6bf5-4468-97e0-942330528c92/requests/template

#post the delete template
#curl --insecure -X POST -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" -d '{"type":"com.vmware.vcac.catalog.domain.request.CatalogResourceRequest","resourceId":"06852d93-466d-4d73-80bc-78764b3d768a","actionId":"051a18db-6bf5-4468-97e0-942330528c92","description":"delete from script","data":{"description":"in data delete from script","reasons":"to try it out"}}' https://vra.vra.lab/catalog-service/api/consumer/resources/06852d93-466d-4d73-80bc-78764b3d768a/actions/051a18db-6bf5-4468-97e0-942330528c92/requests


