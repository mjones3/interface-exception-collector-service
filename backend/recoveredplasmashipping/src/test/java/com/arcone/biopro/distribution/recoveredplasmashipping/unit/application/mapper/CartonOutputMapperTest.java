package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonItemOutputMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapperImpl;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = {CartonOutputMapperImpl.class, CartonItemOutputMapperImpl.class})
class CartonOutputMapperTest {


    @Autowired
    private CartonOutputMapper cartonOutputMapper;



    @Test
    void toOutput_ShouldMapCartonToCartonOutput() {
        // Arrange
        Carton carton = Mockito.mock(Carton.class);
        Mockito.when(carton.canClose()).thenReturn(true);
        Mockito.when(carton.canVerify()).thenReturn(true);

        CartonItem cartonItem = Mockito.mock(CartonItem.class);
        Mockito.when(cartonItem.getStatus()).thenReturn("VERIFIED");
        Mockito.when(carton.getVerifiedProducts()).thenReturn(List.of(cartonItem));

        // Act
        CartonOutput result = cartonOutputMapper.toOutput(carton);

        // Assert
        assertNotNull(result);
        assertTrue(result.canClose());
        assertTrue(result.canVerify());
        assertNotNull(result.verifiedProducts());
        assertTrue(result.verifiedProducts().size() > 0);
        assertEquals("VERIFIED",result.verifiedProducts().get(0).status());

    }
}
