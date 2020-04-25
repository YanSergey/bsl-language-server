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

import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticCompatibilityMode;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticInfo;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticScope;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.Trees;
import com.github._1c_syntax.bsl.parser.BSLParser;
import com.github._1c_syntax.bsl.parser.BSLParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.INFO,
  scope = DiagnosticScope.BSL,
  compatibilityMode = DiagnosticCompatibilityMode.COMPATIBILITY_MODE_8_3_12,
  minutesToFix = 1,
  tags = {
    DiagnosticTag.DEPRECATED
  }

)
public class DeprecatedAttributes8312Diagnostic extends AbstractVisitorDiagnostic {

  private static final String CHART_PLOT_AREA_RU = "ОбластьПостроенияДиаграммы";
  private static final String CHART_PLOT_AREA_EN = "ChartPlotArea";
  // ChartPlotArea deprecated attributes
  private static final String SHOW_SCALE_RU = "ОтображатьШкалу";
  private static final String SHOW_SCALE_EN = "ShowScale";
  private static final String SCALE_LINES_RU = "ЛинииШкалы";
  private static final String SCALE_COLOR_RU = "ЦветШкалы";
  private static final String SHOW_SERIES_SCALE_LABELS_RU = "ОтображатьПодписиШкалыСерий";
  private static final String SHOW_SERIES_SCALE_LABELS_EN = "ShowSeriesScaleLabels";
  private static final String SHOW_POINTS_SCALE_LABELS_RU = "ОтображатьПодписиШкалыТочек";
  private static final String SHOW_POINTS_SCALE_LABELS_EN = "ShowPointsScaleLabels";
  private static final String SHOW_VALUES_SCALE_LABELS_RU = "ОтображатьПодписиШкалыЗначений";
  private static final String SHOW_VALUES_SCALE_LABELS_EN = "ShowValuesScaleLabels";
  private static final String SHOW_SCALE_VALUE_LINES_RU = "ОтображатьЛинииЗначенийШкалы";
  private static final String SHOW_SCALE_VALUE_LINES_EN = "ShowScaleValueLines";
  private static final String VALUE_SCALE_FORMAT_RU = "ФорматШкалыЗначений";
  private static final String VALUE_SCALE_FORMAT_EN = "ValueScaleFormat";
  private static final String LABELS_ORIENTATION_RU = "ОриентацияМеток";
  private static final String LABELS_ORIENTATION_EN = "LabelsOrientation";

  private static final String CHART_RU = "Диаграмма";
  private static final String CHART_EN = "Chart";
  private static final String GANTT_CHART_EN = "GanttChart";
  private static final String GANTT_CHART_RU = "ДиаграммаГанта";
  private static final String PIVOT_CHART_EN = "PivotChart";
  private static final String PIVOT_CHART_RU = "СводнаяДиаграмма";
  // General deprecated attributes
  private static final String SHOW_LEGEND_EN = "ShowLegend";
  private static final String SHOW_LEGEND_RU = "ОтображатьЛегенду";
  private static final String SHOW_TITLE_EN = "ShowTitle";
  private static final String SHOW_TITLE_RU = "ОтображатьЗаголовок";
  // Chart deprecated attributes
  private static final String COLOR_PALETTE_EN = "ColorPalette";
  private static final String COLOR_PALETTE_RU = "ПалитраЦветов";
  private static final String GRADIENT_PALETTE_START_COLOR_EN = "GradientPaletteStartColor";
  private static final String GRADIENT_PALETTE_START_COLOR_RU = "ЦветНачалаГрадиентнойПалитры";
  private static final String GRADIENT_PALETTE_END_COLOR_EN = "GradientPaletteEndColor";
  private static final String GRADIENT_PALETTE_END_COLOR_RU = "ЦветКонцаГрадиентнойПалитры";
  private static final String GRADIENT_PALETTE_MAX_COLORS_EN = "GradientPaletteMaxColors";
  private static final String GRADIENT_PALETTE_MAX_COLORS_RU = "МаксимальноеКоличествоЦветовГрадиентнойПалитры";
  // Chart deprecated methods
  private static final String GET_PALETTE_EN = "GetPalette";
  private static final String GET_PALETTE_RU = "ПолучитьПалитру";
  private static final String SET_PALETTE_EN = "SetPalette";
  private static final String SET_PALETTE_RU = "УстановитьПалитру";

  // Global context enum
  private static final String CHART_LABELS_ORIENTATION_RU = "ОриентацияМетокДиаграммы";

  // Global context enum attribute
  private static final String CHILD_FORM_ITEMS_GROUP_EN = "ChildFormItemsGroup";
  private static final String CHILD_FORM_ITEMS_GROUP_RU = "ГруппировкаПодчиненныхЭлементовФормы";
  private static final String CHILD_FORM_ITEMS_GROUP_HORIZONTAL_EN = "Horizontal";
  private static final String CHILD_FORM_ITEMS_GROUP_HORIZONTAL_RU = "Горизонтальная";

  // Global context methods
  private static final String CLEAR_EVENT_LOG_EN = "ClearEventLog";
  private static final String CLEAR_EVENT_LOG_RU = "ОчиститьЖурналРегистрации";

  private static final HashMap<String, String> NEW_ATTRIBUTES_AND_METHODS = new HashMap<>();
  static {
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SCALE_RU.toLowerCase(), "ОтображатьШкалы");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SCALE_EN.toLowerCase(), "ShowScales");
    NEW_ATTRIBUTES_AND_METHODS.put(SCALE_LINES_RU.toLowerCase(), "ЛинииШкал");
    NEW_ATTRIBUTES_AND_METHODS.put(SCALE_COLOR_RU.toLowerCase(), "ЦветШкал");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SERIES_SCALE_LABELS_RU.toLowerCase(),
      "ШкалаСерий.ПоложениеПодписейШкалы");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SERIES_SCALE_LABELS_EN.toLowerCase(),
      "SeriesScale.ScaleLabelLocation");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_POINTS_SCALE_LABELS_RU.toLowerCase(),
      "ШкалаТочек.ПоложениеПодписейШкалы");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_POINTS_SCALE_LABELS_EN.toLowerCase(),
      "PointsScale.ScaleLabelLocation");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_VALUES_SCALE_LABELS_RU.toLowerCase(),
      "ШкалаЗначений.ПоложениеПодписейШкалы");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_VALUES_SCALE_LABELS_EN.toLowerCase(),
      "ValuesScale.ScaleLabelLocation");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SCALE_VALUE_LINES_RU.toLowerCase(),
      "ШкалаЗначений.ОтображениеЛинийСетки");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_SCALE_VALUE_LINES_EN.toLowerCase(),
      "ValuesScale.GridLinesShowMode");
    NEW_ATTRIBUTES_AND_METHODS.put(VALUE_SCALE_FORMAT_RU.toLowerCase(),
      "ШкалаЗначений.ФорматПодписей");
    NEW_ATTRIBUTES_AND_METHODS.put(VALUE_SCALE_FORMAT_EN.toLowerCase(),
      "ValuesScale.LabelFormat");
    NEW_ATTRIBUTES_AND_METHODS.put(LABELS_ORIENTATION_RU.toLowerCase(),
      "ШкалаТочек.ОриентацияПодписей");
    NEW_ATTRIBUTES_AND_METHODS.put(LABELS_ORIENTATION_EN.toLowerCase(),
      "PointsScale.LabelOrientation");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_LEGEND_RU.toLowerCase(),
      "одно из свойств ОбластьЛегендыДиаграммы, " +
      "ОбластьЛегендыДиаграммыГанта или ОбластьЛегендыСводнойДиаграммы");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_LEGEND_EN.toLowerCase(),
      "one of the properties of ChartLegendArea, " +
      "GanttChartLegendArea or PivotChartLegendArea");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_TITLE_EN.toLowerCase(),
      "one of the properties of ChartTitleArea, " +
      "GanttChartTitleArea or PivotChartTitleArea");
    NEW_ATTRIBUTES_AND_METHODS.put(SHOW_TITLE_RU.toLowerCase(),
      "одно из свойств ОбластьЗаголовкаДиаграммы, " +
      "ОбластьЗаголовкаДиаграммыГанта или ОбластьЗаголовкаСводнойДиаграммы");
    NEW_ATTRIBUTES_AND_METHODS.put(COLOR_PALETTE_EN.toLowerCase(),
      "ColorPaletteDescription.ColorPalette");
    NEW_ATTRIBUTES_AND_METHODS.put(COLOR_PALETTE_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.ПалитраЦветов");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_START_COLOR_EN.toLowerCase(),
      "ColorPaletteDescription.GradientPaletteStartColor");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_START_COLOR_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.ЦветНачалаГрадиентнойПалитры");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_END_COLOR_EN.toLowerCase(),
      "ColorPaletteDescription.GradientPaletteEndColor");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_END_COLOR_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.ЦветКонцаГрадиентнойПалитры");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_MAX_COLORS_EN.toLowerCase(),
      "ColorPaletteDescription.GradientPaletteMaxColors");
    NEW_ATTRIBUTES_AND_METHODS.put(GRADIENT_PALETTE_MAX_COLORS_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.МаксимальноеКоличествоЦветовГрадиентнойПалитры");
    NEW_ATTRIBUTES_AND_METHODS.put(GET_PALETTE_EN.toLowerCase(),
      "ColorPaletteDescription.GetPalette");
    NEW_ATTRIBUTES_AND_METHODS.put(GET_PALETTE_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.ПолучитьПалитру");
    NEW_ATTRIBUTES_AND_METHODS.put(SET_PALETTE_EN.toLowerCase(),
      "ColorPaletteDescription.SetPalette");
    NEW_ATTRIBUTES_AND_METHODS.put(SET_PALETTE_RU.toLowerCase(),
      "ОписаниеПалитрыЦветов.УстановитьПалитру");
    NEW_ATTRIBUTES_AND_METHODS.put(CHART_LABELS_ORIENTATION_RU.toLowerCase(),
      "ОриентацияПодписейДиаграммы");
    NEW_ATTRIBUTES_AND_METHODS.put(CHILD_FORM_ITEMS_GROUP_HORIZONTAL_EN.toLowerCase(),
      "AlwaysHorizontal");
    NEW_ATTRIBUTES_AND_METHODS.put(CHILD_FORM_ITEMS_GROUP_HORIZONTAL_RU.toLowerCase(),
      "ГоризонтальнаяВсегда");
  }

  private static String getDeprecatedAttributesRegex(Metaobject metaobject) {
    HashMap<String, String> attributesPair = new HashMap<>();
    if (metaobject.equals(Metaobject.CHART_PLOT_AREA)) {
      attributesPair.put(SHOW_SCALE_RU, SHOW_SCALE_EN);
      attributesPair.put(SCALE_LINES_RU, "");
      attributesPair.put(SCALE_COLOR_RU, "");
      attributesPair.put(SHOW_SERIES_SCALE_LABELS_RU, SHOW_SERIES_SCALE_LABELS_EN);
      attributesPair.put(SHOW_POINTS_SCALE_LABELS_RU, SHOW_POINTS_SCALE_LABELS_EN);
      attributesPair.put(SHOW_VALUES_SCALE_LABELS_RU, SHOW_VALUES_SCALE_LABELS_EN);
      attributesPair.put(SHOW_SCALE_VALUE_LINES_RU, SHOW_SCALE_VALUE_LINES_EN);
      attributesPair.put(VALUE_SCALE_FORMAT_RU, VALUE_SCALE_FORMAT_EN);
      attributesPair.put(LABELS_ORIENTATION_RU, LABELS_ORIENTATION_EN);
    } else if (metaobject.equals(Metaobject.CHART)) {
      attributesPair.put(SHOW_LEGEND_EN, SHOW_LEGEND_RU);
      attributesPair.put(SHOW_TITLE_EN, SHOW_TITLE_RU);
      attributesPair.put(COLOR_PALETTE_EN, COLOR_PALETTE_RU);
      attributesPair.put(GRADIENT_PALETTE_START_COLOR_EN, GRADIENT_PALETTE_START_COLOR_RU);
      attributesPair.put(GRADIENT_PALETTE_END_COLOR_EN, GRADIENT_PALETTE_END_COLOR_RU);
      attributesPair.put(GRADIENT_PALETTE_MAX_COLORS_EN, GRADIENT_PALETTE_MAX_COLORS_RU);
    } else if (metaobject.equals(Metaobject.ENUM_ITEMS_GROUP)) {
      attributesPair.put(CHILD_FORM_ITEMS_GROUP_HORIZONTAL_EN, CHILD_FORM_ITEMS_GROUP_HORIZONTAL_RU);
    }
    StringJoiner regex = new StringJoiner("|");

    attributesPair.forEach((k, v) -> {
      regex.add(k);
      regex.add(v);
    });

    return regex.toString();
  }

  private static String getDeprecatedMethodsRegex(Metaobject metaobject) {
    HashMap<String, String> attributesPair = new HashMap<>();

    if (metaobject.equals(Metaobject.CHART)) {
      attributesPair.put(GET_PALETTE_EN, GET_PALETTE_RU);
      attributesPair.put(SET_PALETTE_EN, SET_PALETTE_RU);
    }

    StringJoiner regex = new StringJoiner("|");

    attributesPair.forEach((k, v) -> {
      regex.add(k);
      regex.add(v);
    });

    return regex.toString();
  }

  private static String getMetaobjectNameRegex(Metaobject metaobject) {
    HashMap<String, String> namePair = new HashMap<>();
    if (metaobject.equals(Metaobject.CHART_PLOT_AREA)) {
      namePair.put(CHART_PLOT_AREA_RU, CHART_PLOT_AREA_EN);
    } else if (metaobject.equals(Metaobject.CHART)) {
      namePair.put(CHART_RU, CHART_EN);
      namePair.put(GANTT_CHART_EN, GANTT_CHART_RU);
      namePair.put(PIVOT_CHART_EN, PIVOT_CHART_RU);
    } else if (metaobject.equals(Metaobject.ENUM_ITEMS_GROUP)) {
      namePair.put(CHILD_FORM_ITEMS_GROUP_EN, CHILD_FORM_ITEMS_GROUP_RU);
    }
    StringJoiner regex = new StringJoiner("|");

    namePair.forEach((k, v) -> {
      regex.add(k);
      regex.add(v);
    });

    return regex.toString();
  }

  private static final Pattern CHART_PLOT_AREA_ATTRIBUTES_PATTERN = Pattern.compile(
    getDeprecatedAttributesRegex(Metaobject.CHART_PLOT_AREA),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHART_ATTRIBUTES_PATTERN = Pattern.compile(
    getDeprecatedAttributesRegex(Metaobject.CHART),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHART_METHODS_PATTERN = Pattern.compile(
    getDeprecatedMethodsRegex(Metaobject.CHART),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHART_PLOT_AREA_NAME_PATTERN = Pattern.compile(
    getMetaobjectNameRegex(Metaobject.CHART_PLOT_AREA),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHART_NAME_PATTERN = Pattern.compile(
    getMetaobjectNameRegex(Metaobject.CHART),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHART_LABELS_ORIENTATION_PATTERN = Pattern.compile(
    CHART_LABELS_ORIENTATION_RU,
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHILD_FORM_ITEMS_GROUP_NAME_PATTERN = Pattern.compile(
    getMetaobjectNameRegex(Metaobject.ENUM_ITEMS_GROUP),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CHILD_FORM_ITEMS_GROUP_ATTRIBUTE_PATTERN = Pattern.compile(
    getDeprecatedAttributesRegex(Metaobject.ENUM_ITEMS_GROUP),
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern CLEAR_EVENT_LOG_PATTERN = Pattern.compile(
    CLEAR_EVENT_LOG_EN + "|" + CLEAR_EVENT_LOG_RU,
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);


  public DeprecatedAttributes8312Diagnostic(DiagnosticInfo info) {
    super(info);
  }

  @Override
  public ParseTree visitMethodCall(BSLParser.MethodCallContext ctx) {

    Matcher matcher = CHART_METHODS_PATTERN.matcher(ctx.methodName().getText());
    if (matcher.matches()) {
      String deprecatedMethod = matcher.group();
      String message = String.format(info.getResourceString("deprecatedMethodsMessage"),
        deprecatedMethod,
        Objects.requireNonNullElseGet(NEW_ATTRIBUTES_AND_METHODS.get(deprecatedMethod.toLowerCase()),
          String::new));
      diagnosticStorage.addDiagnostic(ctx, info.getMessage(message));
    }

    return super.visitMethodCall(ctx);
  }

  @Override
  public ParseTree visitGlobalMethodCall(BSLParser.GlobalMethodCallContext ctx) {
    Matcher matcher = CLEAR_EVENT_LOG_PATTERN.matcher(ctx.methodName().getText());
    if (matcher.matches()) {
      String deprecatedMethod = ctx.getStart().getText();
      String message = String.format(info.getResourceString("deprecatedGlobalMethodsMessage"),
        deprecatedMethod);
      diagnosticStorage.addDiagnostic(ctx, info.getMessage(message));
    }
    return super.visitGlobalMethodCall(ctx);
  }

  @Override
  public ParseTree visitAccessProperty(BSLParser.AccessPropertyContext ctx) {

    HashMap<Pattern, Pattern> patternsToCheck = new HashMap<>();
    patternsToCheck.put(CHART_PLOT_AREA_NAME_PATTERN, CHART_PLOT_AREA_ATTRIBUTES_PATTERN);
    patternsToCheck.put(CHART_NAME_PATTERN, CHART_ATTRIBUTES_PATTERN);
    patternsToCheck.put(CHILD_FORM_ITEMS_GROUP_NAME_PATTERN, CHILD_FORM_ITEMS_GROUP_ATTRIBUTE_PATTERN);

    patternsToCheck.forEach((k, v) -> checkDeprecatedAttributes(ctx, k, v));

    return super.visitAccessProperty(ctx);
  }

  @Override
  public ParseTree visitComplexIdentifier(BSLParser.ComplexIdentifierContext ctx) {
    Matcher matcher = CHART_LABELS_ORIENTATION_PATTERN.matcher(ctx.getStart().getText());
    if (matcher.matches()) {
      String deprecatedEnum = ctx.getStart().getText();
      String message = String.format(info.getResourceString("deprecatedEnumNameMessage"),
        deprecatedEnum,
        Objects.requireNonNullElseGet(NEW_ATTRIBUTES_AND_METHODS.get(deprecatedEnum.toLowerCase()),
          String::new));
      diagnosticStorage.addDiagnostic(ctx, info.getMessage(message));
    }

    return super.visitComplexIdentifier(ctx);
  }

  private void checkDeprecatedAttributes(BSLParserRuleContext ctx,
                                            Pattern objectNamePattern,
                                            Pattern deprecatedAttributesPattern) {

    Matcher deprecatedAttributesMatcher = deprecatedAttributesPattern.matcher(ctx.getText().substring(1));

    if (deprecatedAttributesMatcher.matches()) {
      var complexCtx = Trees.getAncestorByRuleIndex(ctx, BSLParser.RULE_complexIdentifier);
      if (complexCtx == null) {
        complexCtx = Trees.getAncestorByRuleIndex(ctx, BSLParser.RULE_lValue);
        if (complexCtx == null) {
          return;
        }
      }
      if (objectNamePattern.matcher(complexCtx.getStart().getText()).matches()) {
        String deprecatedAttribute = deprecatedAttributesMatcher.group();
        String message = String.format(info.getResourceString("deprecatedAttributeMessage"),
          deprecatedAttribute,
          Objects.requireNonNullElseGet(NEW_ATTRIBUTES_AND_METHODS.get(deprecatedAttribute.toLowerCase()),
            String::new));

        diagnosticStorage.addDiagnostic(ctx, info.getMessage(message));
      }

    }
  }

  private enum Metaobject{
    CHART,
    CHART_PLOT_AREA,
    ENUM_ITEMS_GROUP
  }
}


