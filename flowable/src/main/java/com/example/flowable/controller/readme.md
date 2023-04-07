SELECT * FROM ACT_RE_DEPLOYMENT;
SELECT * FROM ACT_RE_PROCDEF;

DELETE FROM act_ru_task;
DELETE FROM act_ru_variable;
DELETE FROM act_ru_identitylink;
DELETE FROM act_ru_execution;
DELETE FROM ACT_RE_PROCDEF;

DELETE FROM act_ge_bytearray;
DELETE FROM ACT_RE_DEPLOYMENT;

====================

http://localhost:8080/flow/create

#启动 需要processKey
http://localhost:8080/flow/start?processKey=myProcess_bpmn

msg	"启动流程成功"
res	"1"
data
processId	"8897445e-d445-" 

#监控任务
xxxtListener

#查看流程图 需要processId
http://localhost:8080/flow/processDiagram?processId=8897445e-d445-11ed-bbe6-988fe06ead7f
#
#流传到下个节点 需要taskId
http://localhost:8080/flow/complete?taskId=1c03af6e-d427-11ed-b836-988fe06ead7f

#有网关的案例
http://localhost:8080/flow/create2

复现来源
[Flowable_书山登峰人的博客-CSDN博客](https://blog.csdn.net/houyj1986/category_8556408.html)


宏观了解
https://xie.infoq.cn/article/446fbaeb20d269b544aedb847




##########
表单任务
魏:
localhost:8080/flowForm/start?processKey=leave_key&days=4&reason=我要去打怪兽维护世界和平

魏:
http://localhost:8080/flowForm/create

魏:
http://localhost:8080/flowForm/getForm?taskId=

魏:
http://localhost:8080/flowForm/complete?taskId=
