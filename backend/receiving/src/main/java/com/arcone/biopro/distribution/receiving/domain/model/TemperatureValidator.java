package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemperatureValidator {

    public static ValidationResult validateTemperature(ValidateTemperatureCommand validateTemperatureCommand , List<ProductConsequence> productConsequenceList){
        if(validateTemperatureCommand == null){
            throw new IllegalArgumentException("Temperature Information is required");
        }

        if(productConsequenceList == null || productConsequenceList.isEmpty()){
            throw new IllegalArgumentException("ProductConsequenceList is required");
        }

        var productConsequence = getTemperatureConsequence(validateTemperatureCommand.getTemperature(), productConsequenceList);

        if (productConsequence == null){

            throw new IllegalArgumentException("Product Consequence not found.");
        }

        if(productConsequence.isAcceptable()){
            return ValidationResult.builder()
                .valid(true)
                .build();
        }else{
            return ValidationResult.builder()
                .valid(false)
                .message("Temperature does not meet thresholds all products will be quarantined")
                .build();
        }
    }

    private static ProductConsequence getTemperatureConsequence(BigDecimal temperature , List<ProductConsequence> productConsequenceList ){

        return productConsequenceList.stream()
            .filter(consequence -> consequence.getResultValue() != null)
            .filter(consequence -> {
                try {
                    ExpressionParser expressionParser = new SpelExpressionParser();
                    Expression expression = expressionParser.parseExpression(consequence.getResultValue());
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("TEMPERATURE", temperature);
                    StandardEvaluationContext context = new StandardEvaluationContext(parameters);
                    context.addPropertyAccessor(new MapAccessor());
                    return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
                } catch (Exception e) {
                    return false;
                }
            }).findFirst()
            .orElse(null);

    }
}
