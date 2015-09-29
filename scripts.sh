#!/bin/bash

#token=$(curl --insecure -H "Accept: application/json" -H 'Content-Type: application/json' --data '{"username":"vdude01@vra.lab","password":"P1v0t4l!","tenant":"LAB"}' https://vra.vra.lab/identity/api/tokens| jsawk 'return this.id')

token=MTQ0MzUzNjAyMzYyMToyZWVjNWJiN2UzNzM5MGM4YTE1ODp0ZW5hbnQ6TEFCdXNlcm5hbWU6dmR1ZGUxQHZzcGhlcmUubG9jYWxleHBpcmF0aW9uOjE0NDM2MjI0MjM2MjE6NmZmZDM4MzAyMWEzMTUwZjA3ZDg4ZWUxZjU3MjU5Yzk0MTBiODE4YjM5NjcwMDhhNTNjYzMyMWVmZDk4ZGI4OWY4ODZiN2EzMDhhYjMxMWJhYmQzYzY5YzYyNTIzMmJiMjJkMGQ5NTFjNjAxMDJjZWZiNGVlZmYzZThjY2IzZWQ=

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/identity/api/authorization/tenants/lab/principals/vdude01@vra.lab/roles

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/requests

#curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/catalogItems

curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems