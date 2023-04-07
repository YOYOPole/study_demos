package com.example.flowable.form;


import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.util.CollectionUtils;

import java.util.Set;


@Slf4j
public class CommonUserStartListener implements TaskListener {

    /**
     * 日志类
     */
//    private static final Logger log = LoggerFactory.getLogger(CommonUserStartListener.class);


    @Override
    public void notify(DelegateTask delegateTask) {
        log.warn("进入通用用户任务启动监听器");

        //查询信息
        log.info("任务执行人："+delegateTask.getAssignee());
        log.info("任务配置ID: " +delegateTask.getTaskDefinitionKey());
        log.info("任务id: " +delegateTask.getId());


        //查询变量
        Set<String> setNames= delegateTask.getVariableNames();
        if (!CollectionUtils.isEmpty(setNames)){
            log.info("任务变量:"+setNames.toString());
        }

        for (String varName :setNames){
            Object varValue= delegateTask.getVariable(varName);
            log.info("变量名:"+varName+" 变量值:"+ varValue);
        }

        //修改变量
        delegateTask.setVariable("Test_Var","测试变量");

        log.warn("退出通用用户任务启动监听器");
    }
}

