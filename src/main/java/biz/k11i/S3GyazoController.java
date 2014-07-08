/*
 * Copyright (c) 2014 KOMIYA Atsushi
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package biz.k11i;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gyazo クライアントからの画像アップロードを受け付けるコントローラです。
 * <p>
 * 各種設定は {@code application.yml} ファイルに記述します。
 * </p>
 *
 * @author KOMIYA Atsushi
 */
@Controller
@EnableAutoConfiguration
@Component
public class S3GyazoController {
    private static final Logger logger = LoggerFactory.getLogger(S3GyazoController.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("yyyy.MM.dd-HH.mm.ss.SSS");

    @Value("${s3.bucket}")
    String bucket;
    @Value("${app.url.prefix}")
    String urlPrefix;
    @Autowired
    AmazonS3Client amazonS3Client;

    @RequestMapping(value = "/upload.cgi", method = RequestMethod.POST)
    @ResponseBody
    String upload(@RequestParam("imagedata") MultipartFile imagedata) throws IOException {
        if (imagedata.isEmpty()) {
            String message = "画像のアップロードがされていません。";
            logger.warn(message);
            throw new BadRequestException(message);
        }

        byte[] bytes = imagedata.getBytes();
        String hash = generateHash(bytes);
        String filename = String.format("%s.png", hash);

        try (InputStream input = imagedata.getInputStream()) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("image/png");
            objectMetadata.setContentLength(bytes.length);

            PutObjectRequest req = new PutObjectRequest(
                    bucket,
                    filename,
                    input,
                    objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3Client.putObject(req);
        }

        String result = urlPrefix + filename;
        logger.info("New image uploaded {}", result);
        return result;
    }

    static String generateHash(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        md.update(bytes);
        byte[] hashedBytes = md.digest();

        return String.format("%s-%s",
                DATETIME_FORMATTER.print(System.currentTimeMillis()),
                DatatypeConverter.printHexBinary(hashedBytes).substring(0, 16));
    }

    public static void main(String[] args) {
        SpringApplication.run(S3GyazoController.class, args);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends RuntimeException {
        BadRequestException(String message) {
            super(message);
        }
    }

    @Configuration
    static class AppConfig {
        @Value("${iam.accessKey}")
        String accessKey;
        @Value("${iam.secretKey}")
        String secretKey;

        @Bean
        AWSCredentials awsCredentials() {
            return new BasicAWSCredentials(accessKey, secretKey);
        }

        @Bean
        AmazonS3Client amazonS3Client() {
            return new AmazonS3Client(awsCredentials());
        }
    }
}
