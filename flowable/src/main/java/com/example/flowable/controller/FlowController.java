package com.example.flowable.controller;

import liquibase.repackaged.org.apache.commons.collections4.MapUtils;
import org.flowable.engine.runtime.ProcessInstance;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = "flow")
public class FlowController {


    /**
     * 流程处理服务
     */
    @Autowired
    private IFlowService flowService;

    @RequestMapping("/create")
    @ResponseBody
    public Map<String, String> createFlow() {
        Map<String, String> res = new HashMap<>();

        String flowPath = "F:\\fangzhuowork\\individual-project\\demos\\flowable\\src\\main\\resources\\a.bpmn20.xml";

        if (null == flowService.createFlow(flowPath)) {
            res.put("msg", "创建流程失败");
            res.put("res", "0");
            return res;
        }
        res.put("msg", "创建流程成功");
        res.put("res", "1");
        return res;
    }

    @RequestMapping("/start")
    @ResponseBody
    public Map<String,Object> startFlow(@RequestBody @RequestParam(required = false) Map<String,String> paras){
        Map<String,Object> res =new HashMap<>();
        Map<String,String> data = new HashMap<>();

        if (MapUtils.isEmpty(paras)){
            res.put("msg","启动流程失败");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        String processKey = paras.get("processKey");
        if (StringUtils.isEmpty(processKey)){
            res.put("msg","启动流程失败");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        Map<String,Object> flowParas=new HashMap<>();
        flowParas.putAll(paras);
        ProcessInstance processInstance = flowService.strartFlow(processKey,flowParas);
        if (null == processInstance){
            res.put("msg","启动流程失败");
            res.put("res","0");
            res.put("data",data);
            return res;
        }
        data.put("processId",processInstance.getId());
        res.put("msg","启动流程成功");
        res.put("res","1");
        res.put("data",data);
        return res;
    }

    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        flowService.genProcessDiagram(httpServletResponse,processId);
    }

    @RequestMapping("/complete")
    @ResponseBody
    public Map<String,Object> completeTask(@RequestBody @RequestParam(required = false) Map<String,String> paras){
        Map<String,Object> res =new HashMap<>();
        Map<String,String> data = new HashMap<>();

        if (MapUtils.isEmpty(paras)){
            res.put("msg","请输入任务参数");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        String taskId = paras.get("taskId");
        if (StringUtils.isEmpty(taskId)){
            res.put("msg","请输入任务ID");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        Map<String,Object> flowParas=new HashMap<>();
        flowParas.putAll(paras);
        boolean bok = flowService.completeTask(taskId,flowParas);

        if (bok){
            data.put("taskId",taskId);
            res.put("msg","启动流程成功");
            res.put("res","1");
        }
        else {
            data.put("taskId",taskId);
            res.put("msg","启动流程失败");
            res.put("res","0");
        }

        res.put("data",data);
        return res;
    }

    @RequestMapping("/create2")
    @ResponseBody
    public Map<String, String> create2Flow() {
        Map<String, String> res = new HashMap<>();

        if (null == flowService.create2Flow()) {
            res.put("msg", "创建流程失败");
            res.put("res", "0");
            return res;
        }
        res.put("msg", "创建流程成功");
        res.put("res", "1");
        return res;
    }


    @RequestMapping("/accept")
    @ResponseBody
    public Map<String,Object> acceptTask(@RequestBody @RequestParam(required = false) Map<String,String> paras){
        Map<String,Object> res =new HashMap<>();
        Map<String,String> data = new HashMap<>();

        if (MapUtils.isEmpty(paras)){
            res.put("msg","请输入任务参数");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        String taskId = paras.get("taskId");
        if (StringUtils.isEmpty(taskId)){
            res.put("msg","请输入任务ID");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        Map<String,Object> flowParas=new HashMap<>();
        flowParas.putAll(paras);
        flowParas.put("outcome","通过");
        boolean bok = flowService.completeTask(taskId,flowParas);

        if (bok){
            data.put("taskId",taskId);
            res.put("msg","通过任务成功");
            res.put("res","1");
        }
        else {
            data.put("taskId",taskId);
            res.put("msg","通过任务失败");
            res.put("res","0");
        }

        res.put("data",data);
        return res;
    }


    @RequestMapping("/reject")
    @ResponseBody
    public Map<String,Object> rejectTask(@RequestBody @RequestParam(required = false) Map<String,String> paras){
        Map<String,Object> res =new HashMap<>();
        Map<String,String> data = new HashMap<>();

        if (MapUtils.isEmpty(paras)){
            res.put("msg","请输入任务参数");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        String taskId = paras.get("taskId");
        if (StringUtils.isEmpty(taskId)){
            res.put("msg","请输入任务ID");
            res.put("res","0");
            res.put("data",data);
            return res;
        }

        Map<String,Object> flowParas=new HashMap<>();
        flowParas.putAll(paras);
        flowParas.put("outcome","拒绝");
        boolean bok = flowService.completeTask(taskId,flowParas);

        if (bok){
            data.put("taskId",taskId);
            res.put("msg","拒绝任务成功");
            res.put("res","1");
        }
        else {
            data.put("taskId",taskId);
            res.put("msg","拒绝任务失败");
            res.put("res","0");
        }

        res.put("data",data);
        return res;
    }





}


