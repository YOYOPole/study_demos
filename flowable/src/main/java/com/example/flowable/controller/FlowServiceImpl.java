package com.example.flowable.controller;

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
import org.flowable.engine.runtime.ProcessInstance;
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
public class FlowServiceImpl implements IFlowService {


    /**
     * Flowable运行时服务
     */
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private TaskService taskService;


    @Override
    public Deployment createFlow(String filePath) {

        //解析BPMN模型看是否成功
        XMLStreamReader reader = null;
        InputStream inputStream = null;
        try {
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            inputStream=new FileInputStream(new File(filePath));
            reader = factory.createXMLStreamReader(inputStream);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);
            List<Process> processes = model.getProcesses();
            Process curProcess = null;
            if (CollectionUtils.isEmpty(processes)) {
                log.error("BPMN模型没有配置流程");
                return null;
            }

            curProcess = processes.get(0);

            inputStream=new FileInputStream(new File(filePath));
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name("TEST_FLOW")
                   .addInputStream(curProcess.getName()+".bpmn20.xml",inputStream);

            Deployment deployment= deploymentBuilder.deploy();
            log.warn("部署流程 name:"+curProcess.getName() + " "+deployment);
            return deployment;
        }
        catch (Exception e){
            log.error("BPMN模型创建流程异常",e);
            return null;
        }
        finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                log.error("关闭异常",e);
            }
        }
    }

    @Override
    public ProcessInstance strartFlow(String processKey, Map<String, Object> paras) {

        if (StringUtils.isEmpty(processKey)){
            return null;
        }

        if (null == paras){
            paras = new HashMap<>();
        }

        Deployment deployment = repositoryService.createDeploymentQuery().processDefinitionKey(processKey).singleResult();

        if (deployment == null){
            log.error("没有该流程");
            return  null;
        }

        return runtimeService.startProcessInstanceByKey(processKey,paras);
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
    public boolean completeTask(String taskId, Map<String, Object> paras) {

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

    @Override
    public Deployment create2Flow() {
        //创建一个ProcessEngine实例，用于与Flowable引擎进行交互。
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        //
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("processes/myProcess.bpmn20.xml").deploy();
        return deployment;
    }

    //createProcessEngine()：创建一个ProcessEngine实例，用于与Flowable引擎进行交互。


}

