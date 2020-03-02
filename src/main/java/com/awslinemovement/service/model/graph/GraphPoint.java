package com.awslinemovement.service.model.graph;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphPoint {
    private String x;
    private String y;
}
