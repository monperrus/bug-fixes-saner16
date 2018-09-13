/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hama.examples;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Testcase for {@link GradientDescentExample}
 */
public class GradientDescentTest {
  @Test
  public void testCorrectGDExecution() throws Exception {
    GradientDescentExample.main(new String[]{"src/test/resources/gd_file_sample.txt"});
  }

  @Test
  public void testWrongGDExecutionWithEmptyArgs() {
    try {
        GradientDescentExample.main(new String[0]);
        fail("GradientDescentExample should fail if the argument list has size 0");
    } catch (Exception e) {
        // everything ok
    }
  }

}
