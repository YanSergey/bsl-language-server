/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2020
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.context.DocumentContext;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticInfo;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticParameter;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.Trees;
import com.github._1c_syntax.bsl.parser.BSLParser;
import com.github._1c_syntax.bsl.parser.BSLParserRuleContext;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Russian;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.INFO,
  minutesToFix = 1,
  tags = {
    DiagnosticTag.BADPRACTICE
  }
)

@Slf4j
public class TypoDiagnostic extends AbstractDiagnostic {

  private static final Russian ruLang = new Russian();
  private static final AmericanEnglish enLang = new AmericanEnglish();
  private static final JLanguageTool ruLangTool = new JLanguageTool(ruLang);
  private static final JLanguageTool enLangTool = new JLanguageTool(enLang);
  private static final Map<String, JLanguageTool> languageToolMap = Map.of(
    "en", enLangTool,
    "ru", ruLangTool
  );

  static {
    languageToolMap.forEach((lang, languageTool) ->
      languageTool.getAllRules().stream()
        .filter(rule -> !rule.isDictionaryBasedSpellingRule())
        .map(Rule::getId)
        .forEach(languageTool::disableRule)
    );
  }

  private static final Pattern SPACES_PATTERN = Pattern.compile("\\s+");
  private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");

  private static final Integer[] rulesToFind = new Integer[]{
    BSLParser.RULE_string,
    BSLParser.RULE_lValue,
    BSLParser.RULE_var_name,
    BSLParser.RULE_subName
  };
  private static final Set<Integer> tokenTypes = Set.of(
    BSLParser.STRING,
    BSLParser.IDENTIFIER
  );

  private static final int DEFAULT_MIN_WORD_LENGTH = 3;
  private static final String DEFAULT_USER_WORDS_TO_IGNORE = "";

  @DiagnosticParameter(
    type = Integer.class,
    defaultValue = "" + DEFAULT_MIN_WORD_LENGTH
  )
  private int minWordLength = DEFAULT_MIN_WORD_LENGTH;

  @DiagnosticParameter(
    type = String.class
  )
  private String userWordsToIgnore = DEFAULT_USER_WORDS_TO_IGNORE;

  public TypoDiagnostic(DiagnosticInfo info) {
    super(info);
  }

  @Override
  public void configure(Map<String, Object> configuration) {
    if (configuration == null) {
      return;
    }
    minWordLength = Math.max((int) configuration.getOrDefault("minWordLength", minWordLength), 2);
    userWordsToIgnore = (String) configuration.getOrDefault("userWordsToIgnore", userWordsToIgnore);
  }

  private ArrayList<String> getWordsToIgnore() {
    String exceptions = info.getResourceString("diagnosticExceptions").replaceAll("\n", "");
    return new ArrayList<>(Arrays.asList(exceptions.split(",")));
  }

  private ArrayList<String> getUserWordsToIgnore() {
    String exceptions = userWordsToIgnore.replaceAll("\n", "");
    return new ArrayList<>(Arrays.asList(exceptions.split(",")));
  }

  private void languageToolPreparation(String lang) {
    ArrayList<String> wordsToIgnore = getWordsToIgnore();

    languageToolMap.get(lang).getAllActiveRules()
      .forEach(rule -> ((SpellingCheckRule) rule).addIgnoreTokens(wordsToIgnore));

    if (!userWordsToIgnore.equals("")) {
      ArrayList<String> usersWordsToIgnore = getUserWordsToIgnore();
      languageToolMap.get(lang).getAllActiveRules()
        .forEach(rule -> ((SpellingCheckRule) rule).addIgnoreTokens(usersWordsToIgnore));
    }
  }

  private String getTokenizedStringFromTokens(DocumentContext documentContext, Map<String, List<Token>> tokensMap) {
    StringBuilder text = new StringBuilder();

    Trees.findAllRuleNodes(documentContext.getAst(), rulesToFind).stream()
      .map(ruleContext -> (BSLParserRuleContext) ruleContext)
      .flatMap(ruleContext -> ruleContext.getTokens().stream())
      .filter(token -> tokenTypes.contains(token.getType()))
      .forEach((Token token) -> {
          String curText = QUOTE_PATTERN.matcher(token.getText()).replaceAll("");
          var splitList = Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(curText));
          splitList.stream()
            .filter(element -> element.length() >= minWordLength)
            .forEach(element -> tokensMap.computeIfAbsent(element, newElement -> new ArrayList<>()).add(token));

          text.append(" ");
          text.append(String.join(" ", splitList));

        }
      );

    return Arrays.stream(SPACES_PATTERN.split(text.toString().trim()))
      .distinct()
      .collect(Collectors.joining(" "));
  }

  @Override
  protected void check(DocumentContext documentContext) {

    String lang = info.getResourceString("diagnosticLanguage");
    Map<String, List<Token>> tokensMap = new HashMap<>();

    languageToolPreparation(lang);
    String result = getTokenizedStringFromTokens(documentContext, tokensMap);

    try {
      List<RuleMatch> matches;

      synchronized (languageToolMap.get(lang)) {
        matches = languageToolMap.get(lang).check(result, true, JLanguageTool.ParagraphHandling.ONLYNONPARA);
      }

      if (!matches.isEmpty()) {

        Set<Token> uniqueValues = new HashSet<>();
        matches
          .stream()
          .filter(ruleMatch -> !ruleMatch.getSuggestedReplacements().isEmpty())
          .map(ruleMatch -> result.substring(ruleMatch.getFromPos(), ruleMatch.getToPos()))
          .forEach(substring -> {
            List<Token> tokens = tokensMap.get(substring);
            if (tokens != null) {
              tokens.stream()
                .filter(uniqueValues::add)
                .forEach(token -> diagnosticStorage.addDiagnostic(token, info.getMessage(substring)));
            }
          });
      }
    } catch(IOException e){
      LOGGER.error(e.getMessage(), e);
    }
  }

}