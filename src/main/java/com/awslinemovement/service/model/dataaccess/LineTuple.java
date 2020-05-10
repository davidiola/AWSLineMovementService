package com.awslinemovement.service.model.dataaccess;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class LineTuple {
    @DynamoDBAttribute(attributeName = "LineAmount")
    private String lineAmount;
    @DynamoDBAttribute(attributeName = "LineOdds")
    private String lineOdds;
}
