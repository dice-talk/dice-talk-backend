package com.example.dice_talk.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
//                .acl(ObjectCannedACL.PUBLIC_READ)
                .contentType(file.getContentType())
                .build();

        // 실제 업로드
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 업로드된 파일 URL 반환
        return getFileUrl(fileName);
    }

    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/" + fileName;
    }

    public void moveToDeletedFolder(String imageUrl, String deletedPrefix) {
        // 1. 버킷명 (보통 필드로 주입)
        String bucket = this.bucketName;

        // 2. imageUrl → S3 Key로 변환
        String decodedUrl = URLDecoder.decode(imageUrl, StandardCharsets.UTF_8);
        String s3Key = decodedUrl.replace("https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/", "");

        // 3. 파일명만 추출
        String fileName = s3Key.substring(s3Key.lastIndexOf("/") + 1);

        // 4. 삭제용 Key 생성
        String deletedKey = deletedPrefix + "/" + fileName;

        System.out.println("🚩 bucket : " + bucket);
        System.out.println("🚩 s3Key : " + s3Key);
        System.out.println("🚩 fileName : " + fileName);
        System.out.println("🚩 deletedKey : " + deletedKey);

        // 5. 복사
        try {
            String encodedCopySource = URLEncoder.encode(bucket + "/" + s3Key, StandardCharsets.UTF_8);
            s3Client.copyObject(builder -> builder
                    .copySource(encodedCopySource)
                    .destinationBucket(bucket)
                    .destinationKey(deletedKey)
            );
        } catch (S3Exception e) {
            System.err.println("S3 이동 실패 : " + e.awsErrorDetails().errorMessage());
            throw e;
        }

        // 6. 원본 삭제
        s3Client.deleteObject(builder -> builder
                .bucket(bucket)
                .key(s3Key)
        );
    }


}
