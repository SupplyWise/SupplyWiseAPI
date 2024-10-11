# SupplyWise - API

## How to run

- Make sure you navigate to the correct directory

### Run locally
- Useful in development

1. Ensure you have both maven and open-jdk-17 installed.
2. Run the following command to start
    ```bash
    mvn clean spring-boot:run
    ```

### Run with Docker 
- Useful when connecting components and later stages of development

1. Ensure you have docker installed.

2. Build the Docker image:
    ```bash
    docker compose up --build
    ```
- Note: On the first time it will take longer to pull all the necessary images
