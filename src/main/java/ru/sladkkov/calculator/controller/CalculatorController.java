package ru.sladkkov.calculator.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sladkkov.calculator.service.CalculatorService;

import java.math.BigDecimal;

@RestController
@RequestMapping("calculator/v1")
@Tag(name = "Calculation controller", description = "Позволяет выполнять простые операции с числами")
public class    CalculatorController {

    @Parameter(description = "Сервис для калькулятора, инжектится спрингом автоматически. " +
            "Содержит всю логику для операций: сложения, вычитания, деления, умножения.")
    CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @Operation(
            summary = "Сложение чисел",
            description = "Позволяет сложить два любых числа"
    )
    @GetMapping("/add")
    public ResponseEntity<BigDecimal> add(@RequestParam @Parameter(description = "Первое слагаемое") BigDecimal a, @RequestParam @Parameter(description = "Второе слагаемое") BigDecimal b) {
        return ResponseEntity.ok(calculatorService.add(a, b));
    }

    @Operation(
            summary = "Вычитание чисел",
            description = "Позволяет вычесть два любых числа"
    )
    @GetMapping("/subtract")
    public BigDecimal subtract(@RequestParam @Parameter(description = "Уменьшаемое") BigDecimal a, @RequestParam @Parameter(description = "Вычитаемое") BigDecimal b) {
        return calculatorService.subtract(a, b);
    }

    @Operation(
            summary = "Умножение чисел",
            description = "Позволяет умножить два любых числа"
    )
    @GetMapping("/multiply")
    public BigDecimal multiply(@RequestParam @Parameter(description = "Первый множитель") BigDecimal a, @RequestParam @Parameter(description = "Второй множитель") BigDecimal b) {
        return calculatorService.multiply(a, b);
    }

    @Operation(
            summary = "Деление чисел",
            description = "Позволяет делить два любых числа. Не допускает деление на 0"
    )
    @GetMapping("/division")
    public BigDecimal division(@RequestParam @Parameter(description = "Делимое") BigDecimal a, @RequestParam @Parameter(description = "Делитель") BigDecimal b) {
        return calculatorService.division(a, b);
    }
}
