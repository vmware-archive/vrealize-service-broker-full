#!/bin/bash

#curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude1@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens

host=vra-cafe.vra.pcflab.net
resourceId=c2f31ef4-dd53-4c7f-b522-9fe2de174533
requestId=561c7d9c-a97a-40cb-8487-7c03636e841c
token=MTQ2MDEyMTk3NzIwNzo1NTViZjBhNzMyY2I4MzU4N2I3Yjp0ZW5hbnQ6dnJhbGFidXNlcm5hbWU6YWRtaW5pc3RyYXRvckB2cmEucGNmbGFiLm5ldGV4cGlyYXRpb246MTQ2MDE1MDc3NzAwMDo2ZmRlMzU5OWI4NDYyNDI1NDYxOWU0NGU5YjU1Mzc4NzUzZWM4ZjM3M2RjZWQyYmM5YjgyNDNmNmEzOTBjODVjNTdlMjNiNjljNjU4NDE5NWZmYjU4NjlkNWNkNjQ2ZDRkZTJjMTQ0MGZiMTcwNzJhNGM5ZTZjYTZmNDRkY2E1OA==

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItemViews

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems/e06ff060-dc7a-4f46-a7a7-c32c031fa31e/requests/template

# resource details for a request
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/requests/${requestId}/resourceViews

# a delete template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" \
#https://vra.vra.lab/catalog-service/api/consumer/resources/cedfe0cd-acc4-4de8-8c57-880e9c0c27d5/actions/051a18db-6bf5-4468-97e0-942330528c92/requests/template


#post a delete template
#curl --insecure -X POST -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" \
#-d '{"type":"com.vmware.vcac.catalog.domain.request.CatalogResourceRequest","resourceId":"cedfe0cd-acc4-4de8-8c57-880e9c0c27d5","actionId":"051a18db-6bf5-4468-97e0-942330528c92","description":"huh"}' \
#https://vra.vra.lab/catalog-service/api/consumer/resources/cedfe0cd-acc4-4de8-8c57-880e9c0c27d5/actions/051a18db-6bf5-4468-97e0-942330528c92/requests

#all requests
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/requests&limit=500

#all resources
curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resourceViews?limit=100

#details of a resource
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resourceViews/${resourceId}?withExtendedData=true&withOperations=true&limit=100

#details for a request
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/requests/a42ecb0f-75cb-4967-b108-3b2fb8beb100
#426e4549-84e6-4c03-a6bf-1e7582ebc963

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
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resources/09d21b21-3dc8-44f6-a7e7-6b47a40b7da8/actions/25e17ec5-e2fd-4bba-bb3b-25b69dd18bd7/requests/template

#post the delete template
#curl --insecure -X POST -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" -d '{"type":"com.vmware.vcac.catalog.domain.request.CatalogResourceRequest","resourceId":"06852d93-466d-4d73-80bc-78764b3d768a","actionId":"051a18db-6bf5-4468-97e0-942330528c92","description":"delete from script","data":{"description":"in data delete from script","reasons":"to try it out"}}' https://vra.vra.lab/catalog-service/api/consumer/resources/06852d93-466d-4d73-80bc-78764b3d768a/actions/051a18db-6bf5-4468-97e0-942330528c92/requests


