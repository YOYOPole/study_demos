package com.example.flowable.form;

import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 流程服务类
 */
public interface IFlowFormService {


    Deployment createFlow();

    /**
     * 启动工作流
     */
    ProcessInstance strartFlow(String processKey, Map<String,Object> paras);



    void genProcessDiagram(HttpServletResponse httpServletResponse, String processId);

    boolean isFinished(String processInstanceId);

    boolean completeTask(String taskId,Map<String, Object> paras);

    //    获取任务表单数据
    void getTaskFormData(String ACT_RU_TASK_ID);
}

