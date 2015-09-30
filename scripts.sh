#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"LAB"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0MzYyNTQyMjgzMTo1ZWZhYmM3MWRlNmMxOWVmZjI4Mzp0ZW5hbnQ6TEFCdXNlcm5hbWU6dmR1ZGUwMUB2cmEubGFiZXhwaXJhdGlvbjoxNDQzNzExODIyODMxOjhhYzUwMzE2NjUxYmRmMDEwNGViNDRmMzg0MjlmZTIyZGMzZmI5MTAxMzAyZjhhZjZmYjc3ZDc1ODcxMzRhYTczNGMwNGE5ZDVlOWI2NjJmMjA5NmIxYWI4MWI2ZDZjYWI0NDIwNjM0YWI1NWQwMjM2MDU2MmEzNzNiYTg1ZmY2

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems

curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/tenants/lab