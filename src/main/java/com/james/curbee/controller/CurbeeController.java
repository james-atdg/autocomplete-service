package com.james.curbee.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.james.curbee.service.CurbeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@Validated
@RequestMapping(value = "/api/curbee")
@Tag(name = "Curbee API", description = "Provides autocomplete against predefined word list.")
public class CurbeeController {
	
	@Autowired
	CurbeeService curbeeService;
    
	@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Word count retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @Operation(summary = "Get dictionary word count.")
    @GetMapping("/hello")
    public String hello() {
        return "Hello! Word count is " + curbeeService.countWords();
    }
    
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Suggestions retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
        @ApiResponse(responseCode = "418", description = "No suggestions", content = @Content)
    })
    @Operation(summary = "Get autocomplete suggestions for a search string.")
    @GetMapping("/autocomplete/{letters}")
    public List<String> autocomplete(@PathVariable @NotBlank @Size(min = 2, max=50) String letters) {
        return curbeeService.searchWords(letters.toUpperCase());
    }
}
