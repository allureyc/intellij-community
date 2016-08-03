/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.jetbrains.intellij.build.impl

import org.jetbrains.intellij.build.BuildMessageLogger

import java.util.function.Function

/**
 * todo[nik] this is replacement for BuildInfoPrinter. BuildInfoPrinter should be deleted after we move its remaining methods to this class.
 *
 * @author nik
 */
class TeamCityBuildMessageLogger extends BuildMessageLogger {
  public static final Function<String, BuildMessageLogger> FACTORY = { new TeamCityBuildMessageLogger(it) }
  private static final PrintStream out = System.out //we need to store real System.out because AntBuilder replaces it during task execution
  private final String parallelTaskId

  TeamCityBuildMessageLogger(String parallelTaskId) {
    this.parallelTaskId = parallelTaskId
  }

  @Override
  void logMessage(String message, Level level) {
    String status = level == Level.WARNING ? " status='WARNING'" : ""
    if (parallelTaskId != null) {
      out.println "##teamcity[message flowId='${escape(parallelTaskId)}' text='${escape(message)}'$status]"
    }
    else if (!status.isEmpty()) {
      out.println "##teamcity[message text='${escape(message)}'$status]"
    }
    else {
      out.println message
    }
  }

  @Override
  void logProgressMessage(String message) {
    out.println "##teamcity[progressMessage '${escape(message)}']"
  }

  @Override
  void startBlock(String blockName) {
    out.println "##teamcity[blockOpened name='${escape(blockName)}']"
  }

  @Override
  void finishBlock(String blockName) {
    out.println "##teamcity[blockClosed name='${escape(blockName)}']"
  }


  private static char escapeChar(char c) {
    switch (c) {
      case '\n': return 'n' as char
      case '\r': return 'r' as char
      case '\u0085': return 'x' as char // next-line character
      case '\u2028': return 'l' as char // line-separator character
      case '\u2029': return 'p' as char // paragraph-separator character
      case '|': return '|' as char
      case '\'': return '\'' as char
      case '[': return '[' as char
      case ']': return ']' as char
    }

    return 0;
  }

  private static String escape(String text) {
    StringBuilder escaped = new StringBuilder();
    for (char c : text.toCharArray()) {
      char escChar = escapeChar(c);
      if (escChar == 0 as char) {
        escaped.append(c);
      }
      else {
        escaped.append('|').append(escChar);
      }
    }

    return escaped.toString();
  }
}
