package com.awslinemovement.service.model.graph;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphData {
    private List<GraphLine> lines;
}
