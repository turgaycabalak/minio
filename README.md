# Spring Boot MinIO Application

This project provides a simple object storage solution using **Spring Boot** and **MinIO**. The application supports
operations such as file upload, download, copy, move, delete, and bucket management.

## Features

- **Bucket Management**: Create, list, delete, and view details of buckets.
- **File Management**: Upload, download, delete, copy, and move files.
- **Versioning**: Supports versioning for files within buckets.
- **Integration**: Seamless integration with Spring Boot and MinIO Client.

## Technologies Used

- **Java 21** or higher
- **Maven** (or another build tool)
- **MinIO Server** (or another S3-compatible storage service)

1. **Clone the project:**

    ```bash
    git clone https://github.com/turgaycabalak/minio.git
    cd minio

2. **To install dependencies and run the application:**

    ```bash
    mvn clean install
    mvn spring-boot:run

3. **Configuration**
    ```bash
   minio.url=http://localhost:9000
   minio.access-key=minioadmin
   minio.secret-key=minioadmin

4. **MinIO Docker Run**
   ```bash
   docker run --name minio \
    --publish 9000:9000 \
    --publish 9001:9001 \
    --volume /path/to/minio-persistence:/bitnami/minio/data \
    --user root \
    --env MINIO_ROOT_USER=myaccesskey \
    --env MINIO_ROOT_PASSWORD=mysecretkey \
    -d bitnami/minio:latest

The application will start and be accessible at <b>`http://localhost:4080`.<b>

## API Endpoints

### Bucket APIs

- **List Buckets**: To list all buckets:
    - `GET /api/v1/bucket`


- **List Bucket Details**: To list details of all buckets:
    - `GET /api/v1/bucket/detail`


- **Get Bucket Details**: To get details of a specific bucket:
    - `GET /api/v1/bucket/detail/{bucketName}`


- **Create Bucket**: To create a new bucket:
    - `POST /api/v1/bucket/{bucketName}`


- **Delete Bucket**: To delete a bucket:
    - `DELETE /api/v1/bucket/{bucketName}`

### File APIs

- **Upload File**: To upload a file to a bucket:
    - `POST /api/v1/document/{bucketName}/file`


- **Download File**: To download a file from a bucket:
    - `GET /api/v1/document/{bucketName}/file/{fileName}`


- **List All Files**: To list all files in a bucket:
    - `GET /api/v1/document/{bucketName}/documents`


- **Get File Metadata**: To get metadata of a specific file:
    - `GET /api/v1/document/{bucketName}/document/{fileName}/metadata`


- **Move File**: To move a file from one bucket to another:
    - `PUT /api/v1/document/file/move/{fileName}/{fromBucket}/{toBucket}`


- **Copy File**: To copy a file from one bucket to another:
    - `POST /api/v1/document/file/copy/{fileName}/{fromBucket}/{toBucket}`


- **Delete File**: To delete a file from a bucket:
    - `DELETE /api/v1/document/{bucketName}/file/{fileName}`


- **Delete All Files**: To delete all files in a bucket:
    - `DELETE /api/v1/document/{bucketName}`

## API Documentation

You can access the API documentation and test the endpoints directly using **Swagger**. After starting the application,
navigate to:

**Swagger UI**
[http://localhost:4070/swagger-ui/index.html#/](http://localhost:4070/swagger-ui/index.html#/)

## Contributing

1. Fork the repository
2. Create a new branch (`git checkout -b feature-branch`)
3. Make your changes
4. Commit your changes (`git commit -am 'Add new feature'`)
5. Push to the branch (`git push origin feature-branch`)
6. Create a new Pull Request

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Acknowledgements

- Thanks to the Spring Boot and Redis communities for their valuable resources and support.
