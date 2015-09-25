#!/bin/sh

token=MTQ0MzIwODcwMzM4MTpkZjgzMmNjYmZmMzhiNmQ3YzdlMTp0ZW5hbnQ6TEFCdXNlcm5hbWU6dmR1ZGUxQHZzcGhlcmUubG9jYWxleHBpcmF0aW9uOjE0NDMyOTUxMDMzODE6ODVmNWFlNWI5NTJiZDQ3MTAyZDVlNzk4ODQ5YTE3ZmQwNzA1MzZhZmU5NWI3ZTk2YjBjM2ZiN2U3MzlmZDE3MjYzY2NiMjQxYmZlYzNhODNkZDgzYWFiZGY2NjMwNzFmZDIzNzNjYzU4YjhjYTMyYWQxZTZiMjc5NTg5ZmMyMGM=
url=https://vra.vra.lab/catalog-service/api/consumer/entitledCatalogItems

#catalog-service/api/consumer/requests

date

curl --insecure -H "Content-Type: application/json" -H "Authorization: Bearer $token" $url

date
