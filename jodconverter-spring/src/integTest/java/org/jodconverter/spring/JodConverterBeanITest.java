/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.jodconverter.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_EXECUTION_TIMEOUT;
import static org.jodconverter.core.office.AbstractOfficeManagerPool.DEFAULT_TASK_QUEUE_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_AFTER_START_PROCESS_DELAY;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_EXISTING_PROCESS_ACTION;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_KEEP_ALIVE_ON_SHUTDOWN;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_MAX_TASKS_PER_PROCESS;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_RETRY_INTERVAL;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_PROCESS_TIMEOUT;
import static org.jodconverter.local.office.LocalOfficeManager.DEFAULT_START_FAIL_FAST;

import java.io.File;
import java.util.Collections;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.local.office.LocalOfficeUtils;

/** Contains tests for the {@link JodConverterBean} class with default values. */
class JodConverterBeanITest {

  @Test
  void build_WithDefaultValues_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .satisfies(
              manager -> {
                assertThat(manager).isInstanceOf(LocalOfficeManager.class);
                assertThat(manager)
                    .extracting("tempDir")
                    .satisfies(
                        o ->
                            assertThat(o)
                                .asInstanceOf(InstanceOfAssertFactories.FILE)
                                .hasParent(OfficeUtils.getDefaultWorkingDir()));
                assertThat(manager)
                    .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
                assertThat(manager)
                    .extracting("entries")
                    .asList()
                    .hasSize(1)
                    .element(0)
                    .satisfies(
                        o ->
                            assertThat(o)
                                .extracting(
                                    "taskExecutionTimeout",
                                    "maxTasksPerProcess",
                                    "officeProcessManager.officeUrl.connectString",
                                    "officeProcessManager.officeHome",
                                    "officeProcessManager.processManager",
                                    "officeProcessManager.runAsArgs",
                                    "officeProcessManager.templateProfileDir",
                                    "officeProcessManager.processTimeout",
                                    "officeProcessManager.processRetryInterval",
                                    "officeProcessManager.afterStartProcessDelay",
                                    "officeProcessManager.existingProcessAction",
                                    "officeProcessManager.startFailFast",
                                    "officeProcessManager.keepAliveOnShutdown",
                                    "officeProcessManager.connection.officeUrl.connectString")
                                .containsExactly(
                                    DEFAULT_TASK_EXECUTION_TIMEOUT,
                                    DEFAULT_MAX_TASKS_PER_PROCESS,
                                    "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                                    LocalOfficeUtils.getDefaultOfficeHome(),
                                    LocalOfficeUtils.findBestProcessManager(),
                                    Collections.EMPTY_LIST,
                                    null,
                                    DEFAULT_PROCESS_TIMEOUT,
                                    DEFAULT_PROCESS_RETRY_INTERVAL,
                                    DEFAULT_AFTER_START_PROCESS_DELAY,
                                    DEFAULT_EXISTING_PROCESS_ACTION,
                                    DEFAULT_START_FAIL_FAST,
                                    DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                                    "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
              });

    } finally {
      bean.destroy();
    }
  }

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  void build_WithCustomValues_ShouldInitializedOfficeManagerWithCustomValues(
      final @TempDir File testFolder) throws OfficeException {

    final File workingDir = new File(testFolder, "temp");
    workingDir.mkdirs();

    final File templateProfileDir = new File(testFolder, "template");
    new File(templateProfileDir, "user").mkdirs();

    final JodConverterBean bean = new JodConverterBean();
    bean.setWorkingDir(workingDir.getPath());
    bean.setTaskExecutionTimeout(500L);
    bean.setTaskQueueTimeout(501L);
    bean.setHostName("localhost");
    bean.setPortNumbers("2006");
    bean.setOfficeHome(LocalOfficeUtils.getDefaultOfficeHome().getPath());
    bean.setTemplateProfileDir(templateProfileDir.getPath());
    bean.setTemplateProfileDirOrDefault(templateProfileDir.getPath());
    bean.setProcessTimeout(120_001L);
    bean.setProcessRetryInterval(255L);
    bean.setAfterStartProcessDelay(3000L); // Avoid failure on FreeBSD
    bean.setExistingProcessAction(ExistingProcessAction.KILL);
    bean.setStartFailFast(true);
    bean.setKeepAliveOnShutdown(false);
    bean.setMaxTasksPerProcess(99);

    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .satisfies(
              manager -> {
                assertThat(manager).isInstanceOf(LocalOfficeManager.class);
                assertThat(manager)
                    .extracting("tempDir")
                    .satisfies(
                        o ->
                            assertThat(o)
                                .asInstanceOf(InstanceOfAssertFactories.FILE)
                                .hasParent(workingDir));
                assertThat(manager).hasFieldOrPropertyWithValue("taskQueueTimeout", 501L);
                assertThat(manager)
                    .extracting("entries")
                    .asList()
                    .hasSize(1)
                    .element(0)
                    .satisfies(
                        o ->
                            assertThat(o)
                                .extracting(
                                    "taskExecutionTimeout",
                                    "maxTasksPerProcess",
                                    "officeProcessManager.officeUrl.connectString",
                                    "officeProcessManager.officeHome",
                                    "officeProcessManager.processManager",
                                    "officeProcessManager.runAsArgs",
                                    "officeProcessManager.templateProfileDir",
                                    "officeProcessManager.processTimeout",
                                    "officeProcessManager.processRetryInterval",
                                    "officeProcessManager.afterStartProcessDelay",
                                    "officeProcessManager.existingProcessAction",
                                    "officeProcessManager.startFailFast",
                                    "officeProcessManager.keepAliveOnShutdown",
                                    "officeProcessManager.connection.officeUrl.connectString")
                                .containsExactly(
                                    500L,
                                    99,
                                    "socket,host=localhost,port=2006,tcpNoDelay=1",
                                    LocalOfficeUtils.getDefaultOfficeHome(),
                                    LocalOfficeUtils.findBestProcessManager(),
                                    Collections.EMPTY_LIST,
                                    templateProfileDir,
                                    120_001L,
                                    255L,
                                    3000L,
                                    ExistingProcessAction.KILL,
                                    true,
                                    false,
                                    "socket,host=localhost,port=2006,tcpNoDelay=1"));
              });

    } finally {
      bean.destroy();
    }
  }

  @Test
  void build_WithBadPortNumber_ShouldInitializedOfficeManagerWithDefaultValues()
      throws OfficeException {

    final JodConverterBean bean = new JodConverterBean();
    bean.setPortNumbers("potato");
    try {
      bean.afterPropertiesSet();

      assertThat(bean)
          .extracting("officeManager")
          .isInstanceOf(LocalOfficeManager.class)
          .satisfies(
              manager -> {
                assertThat(manager).isInstanceOf(LocalOfficeManager.class);
                assertThat(manager)
                    .extracting("tempDir")
                    .satisfies(
                        o ->
                            assertThat(o)
                                .asInstanceOf(InstanceOfAssertFactories.FILE)
                                .hasParent(OfficeUtils.getDefaultWorkingDir()));
                assertThat(manager)
                    .hasFieldOrPropertyWithValue("taskQueueTimeout", DEFAULT_TASK_QUEUE_TIMEOUT);
                assertThat(manager)
                    .extracting("entries")
                    .asList()
                    .hasSize(1)
                    .element(0)
                    .satisfies(
                        o ->
                            assertThat(o)
                                .extracting(
                                    "taskExecutionTimeout",
                                    "maxTasksPerProcess",
                                    "officeProcessManager.officeUrl.connectString",
                                    "officeProcessManager.officeHome",
                                    "officeProcessManager.processManager",
                                    "officeProcessManager.runAsArgs",
                                    "officeProcessManager.templateProfileDir",
                                    "officeProcessManager.processTimeout",
                                    "officeProcessManager.processRetryInterval",
                                    "officeProcessManager.afterStartProcessDelay",
                                    "officeProcessManager.existingProcessAction",
                                    "officeProcessManager.startFailFast",
                                    "officeProcessManager.keepAliveOnShutdown",
                                    "officeProcessManager.connection.officeUrl.connectString")
                                .containsExactly(
                                    DEFAULT_TASK_EXECUTION_TIMEOUT,
                                    DEFAULT_MAX_TASKS_PER_PROCESS,
                                    "socket,host=127.0.0.1,port=2002,tcpNoDelay=1",
                                    LocalOfficeUtils.getDefaultOfficeHome(),
                                    LocalOfficeUtils.findBestProcessManager(),
                                    Collections.EMPTY_LIST,
                                    null,
                                    DEFAULT_PROCESS_TIMEOUT,
                                    DEFAULT_PROCESS_RETRY_INTERVAL,
                                    DEFAULT_AFTER_START_PROCESS_DELAY,
                                    DEFAULT_EXISTING_PROCESS_ACTION,
                                    DEFAULT_START_FAIL_FAST,
                                    DEFAULT_KEEP_ALIVE_ON_SHUTDOWN,
                                    "socket,host=127.0.0.1,port=2002,tcpNoDelay=1"));
              });

    } finally {
      bean.destroy();
    }
  }
}
