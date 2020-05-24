package com.awslinemovement.service.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.awslinemovement.service.Main;

import java.util.Map;

public class LambdaEntryPoint implements RequestHandler<Map<String,Object>, String> {
    @Override
    public String handleRequest(Map<String,Object> event, Context context)
    {
        Main.main(null);
        return "success";
    }
}
