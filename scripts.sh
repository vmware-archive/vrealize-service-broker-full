#!/bin/bash

#curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude1@vra.lab","password":"P1v0t4l!","tenant":"lab"}' https://vra.vra.lab/identity/api/tokens

host=vra-cafe.vra.pcflab.net
resourceId=199af6df-9702-44ab-acc8-7b7c41d06881
requestId=673c7552-815e-41b4-8be2-c4af85c38ec0
catalogId=a3d19350-c15e-4d81-878a-38f4868a4c95
token=MTQ2MDk5NTI0NjU3ODo4MDg2ODE2MDBkNzY1Y2M2NDYyMDp0ZW5hbnQ6dnJhbGFidXNlcm5hbWU6YWRtaW5pc3RyYXRvckB2cmEucGNmbGFiLm5ldGV4cGlyYXRpb246MTQ2MTAyNDA0NjAwMDo1M2Y2MGYyMDMyZjk5YmI4Y2I2OWM3NThiNzE2OTE1OGFhY2ZiNWM4MTA4NzFjZWYyNWMxZjYyOWZlOTc2NWYyNmRjN2RlMWE3NzJmZGI4YzZhNjE3ZmIzNjAxODM1ZWVkOTY1N2EyZDZkYWMzMWExYmQyZjNlOWExMDg1YjM5Zg==

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/catalogItems/a3d19350-c15e-4d81-878a-38f4868a4c95

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" https://${host}/api/consumer/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#all catalog items
#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/catalogItems?withExtendedData=true

#entitled catalog items
#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/entitledCatalogItems?withExtendedData=true

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab

#request a catalog item request template
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/entitledCatalogItems/${catalogId}/requests/template

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
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/requests&limit=10000

#all resources
curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resourceViews?limit=100

#details of a resource
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resourceViews/${resourceId}?withExtendedData=true&withOperations=true&limit=100

#details for a request
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/requests/${requestId}

#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/resources/d591e58d-b2cf-4061-aec1-7f41168b7a6d/actions/fe9af618-f21d-47a2-bebc-62d5914f6e6c/requests/template

#child resources
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://${host}/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%${resourceId}%27
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%27197d075a-8f92-430b-b954-60575af8f589%27
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%27931b796e-7ea1-4b2d-a2a5-41f3eea5ef3a%27
#curl --insecure -X GET -H "Content-Type: application/json" -D headers.out -H "Authorization: Bearer ${token}" https://vra-cafe.vra.pcflab.net/catalog-service/api/consumer/resourceViews?managedOnly=false&withExtendedData=true&withOperations=true&%24filter=parentResource%20eq%20%27199af6df-9702-44ab-acc8-7b7c41d06881%27


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


