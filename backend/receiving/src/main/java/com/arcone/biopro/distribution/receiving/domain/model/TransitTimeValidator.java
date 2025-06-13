package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class TransitTimeValidator {

    private static final String TIME_FORMAT = "%sh %sm";

    public static ValidationResult validateTransitTime(ValidateTransitTimeCommand validateTransitTimeCommand , List<ProductConsequence> productConsequenceList){
        if(validateTransitTimeCommand == null){
            throw new IllegalArgumentException("Transit Time Information is required");
        }

        if(productConsequenceList == null || productConsequenceList.isEmpty()){
            throw new IllegalArgumentException("ProductConsequenceList is required");
        }

        var start = validateTransitTimeCommand.getStartDateTime().atZone(validateTransitTimeCommand.getStartZoneId());
        var end  =  validateTransitTimeCommand.getEndDateTime().atZone(validateTransitTimeCommand.getEndZoneId());
        var utcTimeZone = TimeZone.getTimeZone("UTC");
        var duration  = Duration.ofMinutes(ChronoUnit.MINUTES.between(start.withZoneSameInstant(utcTimeZone.toZoneId()), end.withZoneSameInstant(utcTimeZone.toZoneId())));

        var productConsequence = getTransitTimeConsequence(duration.toMinutes(),productConsequenceList);

        if (productConsequence == null){
            throw new IllegalArgumentException("Product Consequence not found.");
        }

        if(productConsequence.isAcceptable()){
            return ValidationResult.builder()
                .valid(true)
                .result(duration.toString())
                .resultDescription(formatTime(duration))
                .build();
        }else{
            return ValidationResult.builder()
                .valid(false)
                .result(duration.toString())
                .resultDescription(formatTime(duration))
                .message("Total Transit Time does not meet thresholds. All products will be quarantined.")
                .build();
        }
    }

    private static ProductConsequence getTransitTimeConsequence(final long totalTransitTimeMinutes , List<ProductConsequence> productConsequenceList ){

        return productConsequenceList.stream()
            .filter(consequence -> consequence.getResultValue() != null)
            .filter(consequence -> {
                try {
                    ExpressionParser expressionParser = new SpelExpressionParser();
                    Expression expression = expressionParser.parseExpression(consequence.getResultValue());
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("TRANSIT_TIME", totalTransitTimeMinutes);
                    StandardEvaluationContext context = new StandardEvaluationContext(parameters);
                    context.addPropertyAccessor(new MapAccessor());
                    return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
                } catch (Exception e) {
                    return false;
                }
            }).findFirst()
            .orElse(null);

    }

    private static String formatTime(Duration duration){
        var hours = duration.toHours();
        var minutes = duration.minusHours(hours).toMinutes();
        return String.format(TIME_FORMAT, hours, minutes);
    }
}
