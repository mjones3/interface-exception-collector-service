package com.arcone.biopro.distribution.receiving.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Import {


    public static ValidationResult validateTemperature(ValidateTemperatureCommand validateTemperatureCommand , ProductConsequenceRepository productConsequenceRepository){
        if(validateTemperatureCommand == null){
            throw new IllegalArgumentException("Temperature Information is required");
        }

        if(productConsequenceRepository == null){
            throw new IllegalArgumentException("ProductConsequenceRepository is required");
        }

        var productConsequence = getTemperatureConsequence(validateTemperatureCommand.getTemperatureCategory(), validateTemperatureCommand.getTemperature(), productConsequenceRepository);

        if(productConsequence == null){
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

    private static ProductConsequence getTemperatureConsequence(String temperatureCategory , BigDecimal temperature , ProductConsequenceRepository productConsequenceRepository ){

        return productConsequenceRepository
            .findAllByProductCategoryAndResultProperty(
                temperatureCategory,
                "TEMPERATURE")
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
            })
            .blockFirst();

    }

}
