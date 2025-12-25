# OrderFlow

OrderFlow is a two-service Spring Boot system with an API and a worker. The API manages products and orders, publishes `OrderCreated` events to SQS, and the worker consumes events to reserve inventory and simulate payments.

## Architecture overview
- **api-service**: REST API for products and orders, writes to Postgres, publishes `OrderCreated` events to SQS.
- **worker-service**: Polls SQS, processes orders (inventory + payment), updates order status, and logs notifications.

## Run locally
```bash
docker compose up --build
```

API is available at `http://localhost:8080`.

## Get a JWT
```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user"}'
```

## Create a product and inventory
```bash
TOKEN=<paste token>
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","description":"Small widget","price":25.00,"inventoryQuantity":50}'
```

## Create an order
```bash
TOKEN=<paste token>
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'
```

## Check order status
```bash
TOKEN=<paste token>
curl -s http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

## List SQS queues in LocalStack
```bash
docker exec -it $(docker ps -qf "name=localstack") awslocal sqs list-queues
```

## Troubleshooting
- **401 Unauthorized**: ensure you include `Authorization: Bearer <token>` from `/auth/login`.
- **Order stuck in CREATED**: verify worker-service is running and can reach LocalStack/Postgres.
- **LocalStack queues missing**: check `docker/localstack/init-queues.sh` mounted to `/etc/localstack/init/ready.d`.
- **Payment failures**: orders with `totalAmount > 1000` will transition to `FAILED`.
