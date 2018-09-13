/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ExecutionTree;


/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessInstanceModificationMultiInstanceTest extends PluggableProcessEngineTestCase {

  public static final String PARALLEL_MULTI_INSTANCE_TASK_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.parallelTasks.bpmn20.xml";
  public static final String PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.parallelSubprocess.bpmn20.xml";

  public static final String SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.sequentialTasks.bpmn20.xml";
  public static final String SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.sequentialSubprocess.bpmn20.xml";

  public static final String PARALLEL_MULTI_INSTANCE_TASK_COMPLETION_CONDITION_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.parallelTasksCompletionCondition.bpmn20.xml";
  public static final String PARALLEL_MULTI_INSTANCE_SUBPROCESS_COMPLETION_CONDITION_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.parallelSubprocessCompletionCondition.bpmn20.xml";

  public static final String NESTED_PARALLEL_MULTI_INSTANCE_TASK_PROCESS =
      "org/camunda/bpm/engine/test/api/runtime/ProcessInstanceModificationMultiInstanceTest.nestedParallelTasks.bpmn20.xml";

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testStartBeforeMultiInstanceBodyParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miTasks#multiInstanceBody")
      .execute();

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miTasks")
          .activity("miTasks")
          .activity("miTasks")
          .activity("miTasks")
        .endScope()
        .beginMiBody("miTasks")
          .activity("miTasks")
          .activity("miTasks")
          .activity("miTasks")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("miTasks").concurrent().noScope().up()
            .child("miTasks").concurrent().noScope().up()
            .child("miTasks").concurrent().noScope().up().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("miTasks").concurrent().noScope().up()
            .child("miTasks").concurrent().noScope().up()
            .child("miTasks").concurrent().noScope().up()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("miTasks", "miTasks", "miTasks", "miTasks", "miTasks", "miTasks", "afterTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeMultiInstanceBodyParallelSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miSubProcess#multiInstanceBody")
      .execute();

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
        .endScope()
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope().up().up().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope().up().up()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("subProcessTask", "subProcessTask", "subProcessTask",
        "subProcessTask", "subProcessTask", "afterTask", "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_COMPLETION_CONDITION_PROCESS)
  public void testStartInnerActivityParallelTasksWithCompletionCondition() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasksCompletionCondition");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miTasks")
      .startBeforeActivity("miTasks")
      .execute();

    // then the process is able to complete successfully and respects the completion condition
    completeTasksInOrder("miTasks", "miTasks", "miTasks", "miTasks");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_COMPLETION_CONDITION_PROCESS)
  public void testStartInnerActivityParallelSubprocessWithCompletionCondition() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocessCompletionCondition");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miSubProcess")
      .startBeforeActivity("miSubProcess")
      .execute();

    // then the process is able to complete successfully and respects the completion condition
    completeTasksInOrder("subProcessTask", "subProcessTask", "subProcessTask",
        "subProcessTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS)
  public void testStartBeforeMultiInstanceBodySequentialTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miTasks#multiInstanceBody")
      .execute();

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miTasks")
          .activity("miTasks")
        .endScope()
        .beginMiBody("miTasks")
          .activity("miTasks")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child("miTasks").scope().up().up()
        .child(null).concurrent().noScope()
          .child("miTasks").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("miTasks", "miTasks", "miTasks", "miTasks", "miTasks", "miTasks", "afterTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeMultiInstanceBodySequentialSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miSubProcess#multiInstanceBody")
      .execute();

    // then
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
        .endScope()
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").scope().up().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("subProcessTask", "subProcessTask", "subProcessTask",
        "subProcessTask", "subProcessTask", "subProcessTask", "afterTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testStartBeforeInnerActivityParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miTasks", tree.getActivityInstances("miTasks#multiInstanceBody")[0].getId())
      .execute();

    // then the mi variables should be correct
    List<Execution> leafExecutions = runtimeService.createExecutionQuery().activityId("miTasks").list();
    assertEquals(4, leafExecutions.size());
    assertVariableSet(leafExecutions, "loopCounter", Arrays.asList(0, 1, 2, 3));
    for (Execution leafExecution : leafExecutions) {
      assertVariable(leafExecution, "nrOfInstances", 4);
      assertVariable(leafExecution, "nrOfCompletedInstances", 0);
      assertVariable(leafExecution, "nrOfActiveInstances", 4);
    }

    // and the trees should be correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miTasks")
          .activity("miTasks")
          .activity("miTasks")
          .activity("miTasks")
          .activity("miTasks")
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).scope()
          .child("miTasks").concurrent().noScope().up()
          .child("miTasks").concurrent().noScope().up()
          .child("miTasks").concurrent().noScope().up()
          .child("miTasks").concurrent().noScope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("miTasks", "miTasks", "miTasks", "miTasks", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivityParallelSubprocess() {
    // given the mi body is already instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miSubProcess", tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId())
      .execute();

    // then the mi variables should be correct
    List<Execution> leafExecutions = runtimeService.createExecutionQuery().activityId("subProcessTask").list();
    assertEquals(4, leafExecutions.size());
    assertVariableSet(leafExecutions, "loopCounter", Arrays.asList(0, 1, 2, 3));
    for (Execution leafExecution : leafExecutions) {
      assertVariable(leafExecution, "nrOfInstances", 4);
      assertVariable(leafExecution, "nrOfCompletedInstances", 0);
      assertVariable(leafExecution, "nrOfActiveInstances", 4);
    }

    // and the trees are correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).scope()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope().up().up()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope().up().up()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope().up().up()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("subProcessTask", "subProcessTask", "subProcessTask",
        "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivityWithMiBodyParallelSubprocess() {
    // given the mi body is not yet instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcessTask")
      .execute();

    // then the mi variables should be correct
    Execution leafExecution = runtimeService.createExecutionQuery().activityId("subProcessTask").singleResult();
    assertNotNull(leafExecution);
    assertVariable(leafExecution, "loopCounter", 0);
    assertVariable(leafExecution, "nrOfInstances", 1);
    assertVariable(leafExecution, "nrOfCompletedInstances", 0);
    assertVariable(leafExecution, "nrOfActiveInstances", 1);

    // and the tree should be correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child("beforeTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder(
        "subProcessTask", "afterTask", "beforeTask", "subProcessTask",
        "subProcessTask", "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivityWithMiBodySetNrOfInstancesParallelSubprocess() {
    // given the mi body is not yet instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcessTask")
      .setVariable("nrOfInstances", 3)
      .execute();

    // then the mi variables should be correct
    Execution leafExecution = runtimeService.createExecutionQuery().activityId("subProcessTask").singleResult();
    assertNotNull(leafExecution);
    assertVariable(leafExecution, "loopCounter", 0);
    assertVariable(leafExecution, "nrOfInstances", 3);
    assertVariable(leafExecution, "nrOfCompletedInstances", 0);
    assertVariable(leafExecution, "nrOfActiveInstances", 1);

    // and the trees should be correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child("beforeTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child(null).concurrent().noScope()
              .child("subProcessTask").scope()
      .done()
      );

    // and completing the single active instance completes the mi body (even though nrOfInstances is 3;
    // joining is performed on the number of concurrent executions)
    completeTasksInOrder("subProcessTask");
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .activity("afterTask")
      .done());

    // and the remainder of the process completes successfully
    completeTasksInOrder("beforeTask", "subProcessTask", "afterTask",
        "subProcessTask", "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS)
  public void testStartBeforeInnerActivitySequentialTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks");
    completeTasksInOrder("beforeTask");

    // then creating a second inner instance is not possible
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    try {
      runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("miTasks", tree.getActivityInstances("miTasks#multiInstanceBody")[0].getId())
      .execute();
    } catch (ProcessEngineException e) {
      assertTextPresent(e.getMessage(), "Concurrent instantiation not possible for activities "
          + "in scope miTasks#multiInstanceBody");
    }

  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivitySequentialSubprocess() {
    // given the mi body is already instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    try {
      runtimeService
        .createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("miSubProcess", tree.getActivityInstances("miSubProcess#multiInstanceBody")[0].getId())
        .execute();
    } catch (ProcessEngineException e) {
      assertTextPresent(e.getMessage(), "Concurrent instantiation not possible for activities "
          + "in scope miSubProcess#multiInstanceBody");
    }
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivityWithMiBodySequentialSubprocess() {
    // given the mi body is not yet instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcessTask")
      .execute();

    // then the mi variables should be correct
    Execution leafExecution = runtimeService.createExecutionQuery().activityId("subProcessTask").singleResult();
    assertNotNull(leafExecution);
    assertVariable(leafExecution, "loopCounter", 0);
    assertVariable(leafExecution, "nrOfInstances", 1);
    assertVariable(leafExecution, "nrOfCompletedInstances", 0);
    assertVariable(leafExecution, "nrOfActiveInstances", 1);

    // and the trees should be correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child("beforeTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").scope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("subProcessTask", "afterTask",
        "beforeTask", "subProcessTask", "subProcessTask", "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testStartBeforeInnerActivityWithMiBodySetNrOfInstancesSequentialSubprocess() {
    // given the mi body is not yet instantiated
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("subProcessTask")
      .setVariable("nrOfInstances", 3)
      .execute();

    // then the mi variables should be correct
    Execution leafExecution = runtimeService.createExecutionQuery().activityId("subProcessTask").singleResult();
    assertNotNull(leafExecution);
    assertVariable(leafExecution, "loopCounter", 0);
    assertVariable(leafExecution, "nrOfInstances", 3);
    assertVariable(leafExecution, "nrOfCompletedInstances", 0);
    assertVariable(leafExecution, "nrOfActiveInstances", 1);

    // and the trees should be correct
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child("beforeTask").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("subProcessTask").scope()
      .done()
      );

    // and two following sequential instances should be created
    completeTasksInOrder("subProcessTask");

    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .activity("beforeTask")
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
      .done());

    leafExecution = runtimeService.createExecutionQuery().activityId("subProcessTask").singleResult();
    assertNotNull(leafExecution);
    assertVariable(leafExecution, "loopCounter", 1);
    assertVariable(leafExecution, "nrOfInstances", 3);
    assertVariable(leafExecution, "nrOfCompletedInstances", 1);
    assertVariable(leafExecution, "nrOfActiveInstances", 1);

    completeTasksInOrder("subProcessTask");

    // and the remainder of the process completes successfully
    completeTasksInOrder("subProcessTask", "beforeTask", "subProcessTask",
        "subProcessTask", "subProcessTask", "afterTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelMultiInstanceBodyParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miTasks#multiInstanceBody")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testCancelMultiInstanceBodyParallelSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miSubProcess#multiInstanceBody")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelMultiInstanceBodySequentialTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miTasks#multiInstanceBody")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testCancelMultiInstanceBodySequentialSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miSubProcess#multiInstanceBody")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelInnerActivityParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(tree.getActivityInstances("miTasks")[0].getId())
      .execute();

    // then
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miTasks")
          .activity("miTasks")
          .activity("miTasks")
        .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).scope()
          .child("miTasks").concurrent().noScope().up()
          .child("miTasks").concurrent().noScope().up()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("miTasks", "miTasks", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelAllInnerActivityParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miTasks")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = NESTED_PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelAllInnerActivityNestedParallelTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedMiParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miTasks")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  /**
   * Ensures that the modification cmd does not prune the last concurrent execution
   * because parallel MI requires this
   */
  @Deployment(resources = PARALLEL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelInnerActivityParallelTasksAllButOne() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(tree.getActivityInstances("miTasks")[0].getId())
      .cancelActivityInstance(tree.getActivityInstances("miTasks")[1].getId())
      .execute();

    // then
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miTasks")
          .activity("miTasks")
        .endScope()
      .done());

    // the execution tree should still be in the expected shape
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).scope()
          .child("miTasks").concurrent().noScope()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("miTasks", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = PARALLEL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testCancelInnerActivityParallelSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(tree.getActivityInstances("miSubProcess")[0].getId())
      .execute();

    // then
    tree = runtimeService.getActivityInstance(processInstance.getId());
    assertThat(tree).hasStructure(
      describeActivityInstanceTree(processInstance.getProcessDefinitionId())
        .beginMiBody("miSubProcess")
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
          .beginScope("miSubProcess")
            .activity("subProcessTask")
          .endScope()
      .done());

    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
    assertThat(executionTree).matches(
      describeExecutionTree(null).scope()
        .child(null).scope()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope().up().up()
          .child(null).concurrent().noScope()
            .child("subProcessTask").scope().up().up()
      .done()
      );

    // and the process is able to complete successfully
    completeTasksInOrder("subProcessTask", "subProcessTask", "afterTask");
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelInnerActivitySequentialTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(tree.getActivityInstances("miTasks")[0].getId())
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_TASK_PROCESS)
  public void testCancelAllInnerActivitySequentialTasks() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialUserTasks");
    completeTasksInOrder("beforeTask");

    // when
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelAllForActivity("miTasks")
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = SEQUENTIAL_MULTI_INSTANCE_SUBPROCESS_PROCESS)
  public void testCancelInnerActivitySequentialSubprocess() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    completeTasksInOrder("beforeTask");

    // when
    ActivityInstance tree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .cancelActivityInstance(tree.getActivityInstances("miSubProcess")[0].getId())
      .execute();

    // then
    assertProcessEnded(processInstance.getId());
  }

  protected void completeTasksInOrder(String... taskNames) {
    for (String taskName : taskNames) {
      // complete any task with that name
      List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey(taskName).listPage(0, 1);
      assertTrue("task for activity " + taskName + " does not exist", !tasks.isEmpty());
      taskService.complete(tasks.get(0).getId());
    }
  }


  protected void assertVariable(Execution execution, String variableName, Object expectedValue) {
    Object variableValue = runtimeService.getVariable(execution.getId(), variableName);
    assertEquals("Value for variable '" + variableName + "' and " + execution + " "
        + "does not match.", expectedValue, variableValue);
  }

  protected void assertVariableSet(List<Execution> executions, String variableName, List<?> expectedValues) {
    List<Object> actualValues = new ArrayList<Object>();
    for (Execution execution : executions) {
      actualValues.add(runtimeService.getVariable(execution.getId(), variableName));
    }

    for (Object expectedValue : expectedValues) {
      boolean valueFound = actualValues.remove(expectedValue);
      assertTrue("Expected variable value '" + expectedValue + "' not contained in the list of actual values. "
          + "Unmatched actual values: " + actualValues,
          valueFound);
    }
    assertTrue("There are more actual than expected values.", actualValues.isEmpty());
  }

}
