/*
 * Copyright 2000-2013 JetBrains s.r.o.
 * Copyright 2014-2015 AS3Boyan
 * Copyright 2014-2015 Elias Ku
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.plugins.haxe.tests.filters;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.RegexpFilter;
import com.intellij.openapi.project.Project;
import com.intellij.plugins.haxe.tests.runner.filters.ErrorFilter;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorFilterTest {

  @Test
  public void testLink() {
    String expression = "ERR: CoordinatesDetectionTest.hx:22(by.rovar.iso.model.CoordinatesDetectionTest.test2x2) - expected true but was false";
    ErrorFilter filter = createFilter();
    String fileName = "by/rovar/iso/model/CoordinatesDetectionTest.hx";
    Filter.Result result = filter.applyFilter(expression, expression.length());
    assertEquals("ERR: ".length(), result.highlightStartOffset);
    assertEquals("ERR: CoordinatesDetectionTest.hx:22".length(), result.highlightEndOffset);
    HLInfo info = (HLInfo) result.hyperlinkInfo;
    //idea count lines starting from 0
    info.checkInfo(fileName, 22 - 1);


  }

  private ErrorFilter createFilter() {
    return new ErrorFilter(null){
      @Override
      protected HyperlinkInfo createOpenFileHyperlink(String filePath, int line) {
        return createOpenFile(filePath, line);
      }
    };
  }

  private static HyperlinkInfo createOpenFile(String fileName, int line) {
    return new HLInfo(fileName, line);
  }

  private static class HLInfo implements HyperlinkInfo {
    public String myFileName;
    public int myLine;

    public HLInfo(String fileName, int line) {
      myFileName = fileName;
      myLine = line;
    }

    @Override
    public void navigate(Project project) {
    }

    public void checkInfo(String fileName, int line) {
      assertEquals(fileName, myFileName);
      assertEquals(line, myLine);
    }
  }
}
