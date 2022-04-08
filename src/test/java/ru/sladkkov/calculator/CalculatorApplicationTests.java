package ru.sladkkov.calculator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sladkkov.calculator.service.CalculatorService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CalculatorApplicationTests {
    @Autowired
    CalculatorService calculatorService;

    @Test
    void testAdd() {
        BigDecimal actualResult = calculatorService.add(BigDecimal.ONE, BigDecimal.TEN);
        BigDecimal expectedResult = BigDecimal.valueOf(11);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @Test
    void testSubtract() {
        BigDecimal actualArea = calculatorService.subtract(BigDecimal.ONE, BigDecimal.TEN);
        BigDecimal expectedArea = BigDecimal.valueOf(-9);
        Assertions.assertEquals(expectedArea, actualArea);
    }

    @Test
    void testMultiply() {
        BigDecimal actualArea = calculatorService.multiply(BigDecimal.ONE, BigDecimal.TEN);
        BigDecimal expectedArea = BigDecimal.valueOf(10);
        Assertions.assertEquals(expectedArea, actualArea);
    }

    @Test
    void testDivisionByZero() {
        Throwable thrown = assertThrows(ArithmeticException.class, () -> {
            calculatorService.division(BigDecimal.ONE, BigDecimal.ZERO);
        });
        assertNotNull(thrown.getMessage());
    }

    @Test
    void testDivision() {
        BigDecimal actualArea = calculatorService.division(BigDecimal.ONE, BigDecimal.valueOf(3));
        BigDecimal expectedArea = BigDecimal.valueOf(0.33333);
        Assertions.assertEquals(expectedArea, actualArea);
    }
}
