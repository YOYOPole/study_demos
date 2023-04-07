package com.example.flowable.form;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.form.model.FormField;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service
@Slf4j
public class FlowFormServiceImpl implements IFlowFormService {


    /**
     * Flowable运行时服务
     */
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private FormRepositoryService formRepositoryService;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private TaskService taskService;



    @Override
    public Deployment createFlow() {
        //创建一个ProcessEngine实例，用于与Flowable引擎进行交互。
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        //
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/表单.bpmn20.xml").deploy();


        //添加表单
        formRepositoryService.createDeployment()
                .addClasspathResource("processes/OA请假.form")
                .name("外置表单")
                .parentDeploymentId(deployment.getId())
                .deploy();
        return deployment;
    }

    @Override
    public ProcessInstance strartFlow(String processKey, Map<String, Object> paras) {
        //processKey  leave_key
        if (StringUtils.isEmpty(processKey)){
            return null;
        }

        if (null == paras){
            paras = new HashMap<>();
        }

        ProcessDefinition leaveKey = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave_key").singleResult();;
//        Map<String,Object> map=new HashMap<>();
//        map.put("days","4");
//        map.put("reason","我要去打怪兽维护世界和平");
        ProcessInstance processInstance = runtimeService.startProcessInstanceWithForm(leaveKey.getId(),
                "请假", paras,
                "OA请假表单");

        return processInstance;
    }

    @Override
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) {

        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";
        if (this.isFinished(processId)) {// 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();

            processDefinitionId=pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId=pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<String>();

        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList =  historyService.createHistoricActivityInstanceQuery().processInstanceId(processId).orderByHistoricActivityInstanceStartTime().asc().list();

        for(HistoricActivityInstance tempActivity : highLightedActivitList){
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, flows, engconf.getActivityFontName(),
                engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {
            log.error("操作异常",e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().finished()
                .processInstanceId(processInstanceId).count() > 0;
    }


    /**
     * 完成任务
     */
    @Override
    public boolean completeTask(String taskId,  Map<String, Object> paras) {
//        Map<String,Object> map=new HashMap<>();
////        map.put("days","6");
////        map.put("reason","我要去旅游了");
//        taskService.completeTaskWithForm(taskId, formDefinitionId, "xx", map);


        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return false;
        }

        if (null == paras){
            taskService.complete(taskId);
        }
        else {
            taskService.complete(taskId,paras);
        }

        return true;

    }

    //    获取任务表单数据
    @Override
    public void getTaskFormData(String taskId) {


        //71e05286-df35-11ec-abb7-666ee0fc370d 这个参数是 ACT_RU_TASK的ID_
        FormInfo taskFormModel = taskService.getTaskFormModel(taskId);
        SimpleFormModel formModel = (SimpleFormModel) taskFormModel.getFormModel();
        List<FormField> fields = formModel.getFields();
        fields.forEach(e -> {
            System.out.println("e.getId() = " + e.getId());
            System.out.println("e.getName() = " + e.getName());
            System.out.println("e.getType() = " + e.getType());
            System.out.println("e.getValue() = " + e.getValue());

        });
    }
}

