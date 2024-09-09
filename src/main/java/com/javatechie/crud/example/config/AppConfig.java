package com.javatechie.crud.example.config;

import com.google.gson.Gson;
import com.javatechie.crud.example.model.AwsSecret;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    public DataSource dataSource() {
        AwsSecret awsSecret = getSecret();
        return DataSourceBuilder
                .create()
                //.driverClassName()
                .url("jdbc" + awsSecret.getEngine() + "://" +  awsSecret.getHost() + ":" + awsSecret.getPort() + "/database-1")
                .username(awsSecret.getUsername())
                .password(awsSecret.getPassword())
                .build();
    }

    private AwsSecret getSecret() {

        String secretName = "db-credential";
        Region region = Region.of("us-east-1");

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            // For a list of exceptions thrown, see
            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            throw e;
        }

        AwsSecret awsSecret = null;
        if(getSecretValueResponse.secretString() != null){
            String secret = getSecretValueResponse.secretString();
            awsSecret = new Gson().fromJson(secret, AwsSecret.class);
        }

        return awsSecret;
    }
}
