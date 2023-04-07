package com.example.flowable.controller;

import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 流程服务类
 */
public interface IFlowService {

    /**
     * 部署工作流
     */
    Deployment createFlow(String filePath);


    /**
     * 启动工作流
     */
    ProcessInstance strartFlow(String processKey, Map<String,Object> paras);



    void genProcessDiagram(HttpServletResponse httpServletResponse, String processId);

    boolean isFinished(String processInstanceId);

    boolean completeTask(String taskId, Map<String, Object> paras);

    Deployment create2Flow();
}

