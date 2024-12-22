import json
import boto3
import csv
import psycopg2
import os

def lambda_handler(event, context):
    # S3 event details
    s3 = boto3.client('s3')
    bucket_name = event['Records'][0]['s3']['bucket']['name']
    object_key = event['Records'][0]['s3']['object']['key']
    
    # Fetch the file from S3
    s3.download_file(bucket_name, object_key, '/tmp/' + object_key.split('/')[-1])
    file_path = '/tmp/' + object_key.split('/')[-1]
    
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
    
    try:
        cursor = conn.cursor()
        
        # Process the CSV file and insert into the database
        with open(file_path, 'r') as csv_file:
            reader = csv.reader(csv_file)
            header = next(reader)  # Skip header row
            for row in reader:
                cursor.execute(
                    "INSERT INTO your_table_name (column1, column2, column3) VALUES (%s, %s, %s)",
                    row
                )
        
        conn.commit()
        cursor.close()
        return {
            'statusCode': 200,
            'body': json.dumps('File processed and data inserted successfully')
        }
    except Exception as e:
        conn.rollback()
        return {
            'statusCode': 500,
            'body': json.dumps(f"Error: {str(e)}")
        }
    finally:
        conn.close()
