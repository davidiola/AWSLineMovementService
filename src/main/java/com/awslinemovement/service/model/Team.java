package com.awslinemovement.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Data;

@Data
@DynamoDBDocument
public class Team {
    private String name;
    private LineTuple spread;
    private LineTuple total;
    private String ml;

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDBAttribute(attributeName = "Spread")
    public LineTuple getSpread() { return spread; }
    public void setSpread(LineTuple spread) { this.spread = spread; }

    @DynamoDBAttribute(attributeName = "Total")
    public LineTuple getTotal() { return total; }
    public void setTotal(LineTuple total) { this.total = total; }

    @DynamoDBAttribute(attributeName = "ML")
    public String getML() { return ml; }
    public void setML(String ml) { this.ml = ml; }
}
