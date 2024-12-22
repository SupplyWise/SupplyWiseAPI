package com.supplywise.supplywise.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class FileService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    public FileService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFileToS3(MultipartFile file) throws IOException {
        String keyName = "inventory/" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.addUserMetadata("uploaded-by", "SpringBootApp");
        metadata.addUserMetadata("timestamp", String.valueOf(System.currentTimeMillis()));

        logger.info("Uploading file to S3 with key: {}", keyName);
        s3Client.putObject(bucketName, keyName, file.getInputStream(), metadata);

        String s3Url = s3Client.getUrl(bucketName, keyName).toString();
        logger.info("File uploaded successfully to S3. URL: {}", s3Url);

        return s3Url;
    }
}
