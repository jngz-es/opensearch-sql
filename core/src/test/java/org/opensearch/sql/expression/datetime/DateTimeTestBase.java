/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.datetime;

import static org.opensearch.sql.data.type.ExprCoreType.DOUBLE;
import static org.opensearch.sql.expression.function.BuiltinFunctionRepository.DEFAULT_NAMESPACE;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.sql.data.model.ExprDateValue;
import org.opensearch.sql.data.model.ExprDatetimeValue;
import org.opensearch.sql.data.model.ExprTimestampValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.expression.DSL;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.ExpressionTestBase;
import org.opensearch.sql.expression.FunctionExpression;
import org.opensearch.sql.expression.env.Environment;
import org.opensearch.sql.expression.function.BuiltinFunctionRepository;
import org.opensearch.sql.expression.function.FunctionName;
import org.opensearch.sql.expression.function.FunctionSignature;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class DateTimeTestBase extends ExpressionTestBase {

  @Mock
  protected Environment<Expression, ExprValue> env;

  @Mock
  protected Expression nullRef;

  @Mock
  protected Expression missingRef;

  @Autowired
  protected BuiltinFunctionRepository functionRepository;

  protected FunctionExpression maketime(Expression hour, Expression minute, Expression second) {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("maketime"),
        List.of(DOUBLE, DOUBLE, DOUBLE)));
    return (FunctionExpression)func.apply(List.of(hour, minute, second));
  }

  protected LocalTime maketime(Double hour, Double minute, Double second) {
    return maketime(DSL.literal(hour), DSL.literal(minute), DSL.literal(second))
        .valueOf(null).timeValue();
  }

  protected FunctionExpression makedate(Expression year, Expression dayOfYear) {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("makedate"),
        List.of(DOUBLE, DOUBLE)));
    return (FunctionExpression)func.apply(List.of(year, dayOfYear));
  }

  protected LocalDate makedate(Double year, Double dayOfYear) {
    return makedate(DSL.literal(year), DSL.literal(dayOfYear)).valueOf(null).dateValue();
  }

  protected FunctionExpression unixTimeStampExpr() {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("unix_timestamp"), List.of()));
    return (FunctionExpression)func.apply(List.of());
  }

  protected Long unixTimeStamp() {
    return unixTimeStampExpr().valueOf(null).longValue();
  }

  protected FunctionExpression unixTimeStampOf(Expression value) {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("unix_timestamp"),
        List.of(value.type())));
    return (FunctionExpression)func.apply(List.of(value));
  }

  protected Double unixTimeStampOf(Double value) {
    return unixTimeStampOf(DSL.literal(value)).valueOf(null).doubleValue();
  }

  protected Double unixTimeStampOf(LocalDate value) {
    return unixTimeStampOf(DSL.literal(new ExprDateValue(value))).valueOf(null).doubleValue();
  }

  protected Double unixTimeStampOf(LocalDateTime value) {
    return unixTimeStampOf(DSL.literal(new ExprDatetimeValue(value))).valueOf(null).doubleValue();
  }

  protected Double unixTimeStampOf(Instant value) {
    return unixTimeStampOf(DSL.literal(new ExprTimestampValue(value))).valueOf(null).doubleValue();
  }

  protected FunctionExpression fromUnixTime(Expression value) {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("from_unixtime"),
        List.of(value.type())));
    return (FunctionExpression)func.apply(List.of(value));
  }

  protected FunctionExpression fromUnixTime(Expression value, Expression format) {
    var func = functionRepository.resolve(Collections.singletonList(DEFAULT_NAMESPACE),
        new FunctionSignature(new FunctionName("from_unixtime"),
        List.of(value.type(), format.type())));
    return (FunctionExpression)func.apply(List.of(value, format));
  }

  protected LocalDateTime fromUnixTime(Long value) {
    return fromUnixTime(DSL.literal(value)).valueOf(null).datetimeValue();
  }

  protected LocalDateTime fromUnixTime(Double value) {
    return fromUnixTime(DSL.literal(value)).valueOf(null).datetimeValue();
  }

  protected String fromUnixTime(Long value, String format) {
    return fromUnixTime(DSL.literal(value), DSL.literal(format)).valueOf(null).stringValue();
  }

  protected String fromUnixTime(Double value, String format) {
    return fromUnixTime(DSL.literal(value), DSL.literal(format)).valueOf(null).stringValue();
  }

  protected ExprValue eval(Expression expression) {
    return expression.valueOf(env);
  }
}
