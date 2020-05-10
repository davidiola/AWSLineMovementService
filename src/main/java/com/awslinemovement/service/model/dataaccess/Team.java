package com.awslinemovement.service.model.dataaccess;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class Team {

    @DynamoDBAttribute(attributeName = "Name")
    private String name;
    @DynamoDBAttribute(attributeName = "Spread")
    private LineTuple spread;
    @DynamoDBAttribute(attributeName = "Total")
    private LineTuple total;
    @DynamoDBAttribute(attributeName = "ML")
    private String ml;
}
