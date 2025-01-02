import json
import boto3
import csv
import psycopg2
import os
import logging

def lambda_handler(event, context):
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    
    try:
        # S3 details
        s3 = boto3.client('s3')
        # bucket_name = 'inventory-csv-bucket'  # Static bucket name
        bucket_name = event['Records'][0]['s3']['bucket']['name']
        object_key = event['Records'][0]['s3']['object']['key']
        file_name = object_key.split('/')[-1]
        file_path = f'/tmp/{file_name}'
        
        logger.info(f"Downloading file: {object_key} from bucket: {bucket_name}")
        s3.download_file(bucket_name, object_key, file_path)
        
        # RDS Database connection
        rds_host = os.getenv('RDS_HOST')
        rds_user = os.getenv('RDS_USER')
        rds_password = os.getenv('RDS_PASSWORD')
        rds_database = os.getenv('RDS_DATABASE')
        
        conn = psycopg2.connect(
            host=rds_host,
            user=rds_user,
            password=rds_password,
            database=rds_database
        )
        
        cursor = conn.cursor()
        logger.info("Connected to RDS successfully")
        
        # Process the CSV file and insert into the database
        with open(file_path, 'r') as csv_file:
            reader = csv.reader(csv_file)
            header = next(reader)
            columns = ', '.join(header)  # Ensure CSV headers match DB column names
            placeholders = ', '.join(['%s'] * len(header))
            query = f"INSERT INTO your_table_name ({columns}) VALUES ({placeholders})"
            
            for row in reader:
                cursor.execute(query, row)
        
        conn.commit()
        cursor.close()
        conn.close()
        logger.info("File processed and data inserted successfully")
        
        return {
            'statusCode': 200,
            'body': json.dumps('File processed and data inserted successfully')
        }
    
    except Exception as e:
        logger.error(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps(f"Error: {str(e)}")
        }
