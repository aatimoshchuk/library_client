set container-name=postgres-with-logs-library
set image-name=library_database-postgres

docker stop %container-name%
docker rm %container-name%
docker rmi %image-name%
docker compose up -d
timeout /t 5