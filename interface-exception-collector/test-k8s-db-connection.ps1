# Test Database Connection in Kubernetes
Write-Host "=== Testing Database Connection ===" -ForegroundColor Green

Write-Host "1. Testing connection to postgres service..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgres -U postgres -c "SELECT version();"

Write-Host "
2. Testing connection to postgresql-service in db namespace..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgresql-service.db.svc.cluster.local -U postgres -c "SELECT version();"

Write-Host "
3. Checking if exception_collector_db database exists..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgres -U postgres -c "SELECT datname FROM pg_database WHERE datname='exception_collector_db';"
