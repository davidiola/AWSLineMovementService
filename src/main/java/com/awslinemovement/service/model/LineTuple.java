package com.awslinemovement.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class LineTuple {
    private String lineAmount;
    private String lineOdds;

    @DynamoDBAttribute(attributeName = "LineAmount")
    public String getLineAmount() { return lineAmount; }
    public void setLineAmount(String lineAmount) { this.lineAmount = lineAmount; }

    @DynamoDBAttribute(attributeName = "LineOdds")
    public String getLineOdds() { return lineOdds; }
    public void setLineOdds(String lineOdds) { this.lineOdds = lineOdds; }
}
