package com.bifrost.demo.service.util;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dsql.DsqlUtilities;

public final class AWSUtil {
    public static String generateDSQLToken(String endpoint, Region region) {
        DsqlUtilities utilities = DsqlUtilities.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return utilities.generateDbConnectAdminAuthToken(builder -> {
            builder.hostname(endpoint)
                    .region(region);
        });
    }
}
