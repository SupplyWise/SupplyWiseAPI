import json
import boto3
import csv
import psycopg2
import os
import logging
from datetime import datetime

def lambda_handler(event, context):
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    
    try:
        # S3 details
        s3 = boto3.client('s3')
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
            
            for row in reader:
                item_name, barcode, category, expiration_date, quantity, minimum_stock, restaurant_id, inventory_id = row
                
                # Ensure required fields are filled
                if not all([item_name, category, expiration_date, quantity, minimum_stock, restaurant_id, inventory_id]):
                    logger.warning(f"Skipping row with missing required values: {row}")
                    continue
                
                # Handle nullable barcode
                barcode = barcode if barcode else None
                
                # Convert types
                expiration_date = datetime.strptime(expiration_date, '%Y-%m-%d').date()
                quantity = int(quantity)
                minimum_stock = int(minimum_stock)
                
                # Check if the inventory exists
                cursor.execute("""
                    SELECT id FROM inventory WHERE id = %s
                """, (inventory_id,))
                inventory_result = cursor.fetchone()
                
                if not inventory_result:
                    logger.warning(f"Skipping row: Inventory {inventory_id} does not exist")
                    continue
                
                # Check if item exists, considering null barcode
                if barcode:
                    cursor.execute("""
                        SELECT id FROM items WHERE name = %s AND barcode = %s
                    """, (item_name, barcode))
                else:
                    cursor.execute("""
                        SELECT id FROM items WHERE name = %s AND barcode IS NULL
                    """, (item_name,))
                
                item_result = cursor.fetchone()
                
                if not item_result:
                    # Insert item, setting barcode as NULL if it's missing
                    cursor.execute("""
                        INSERT INTO items (name, barcode, category) 
                        VALUES (%s, %s, %s) RETURNING id
                    """, (item_name, barcode, category))
                    item_id = cursor.fetchone()[0]
                else:
                    item_id = item_result[0]
                
                # Create item_properties linked to the inventory
                cursor.execute("""
                    INSERT INTO item_properties (item_id, expiration_date, quantity, minimum_stock_quantity, restaurant_id, inventory_id)
                    VALUES (%s, %s, %s, %s, %s, %s)
                """, (item_id, expiration_date, quantity, minimum_stock, restaurant_id, inventory_id))
                
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
