package com.workflow.common.object;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class FunctionObject {
    String className;
    String methodName;
    List<ParameterObject> inputParameterList;
    JSONObject outputParameter;
}
