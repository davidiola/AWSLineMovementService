package com.awslinemovement.service.model.graph;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphLine {
    private String id;
    private String color;
    private List<GraphPoint> data;
}
