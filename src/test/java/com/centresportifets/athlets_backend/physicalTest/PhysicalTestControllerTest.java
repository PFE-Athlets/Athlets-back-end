package com.centresportifets.athlets_backend.physicalTest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.centresportifets.athlets_backend.physicalTest.dto.PhysicalTestCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

@WebMvcTest(PhysicalTestController.class)
public class PhysicalTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PhysicalTestService physicalTestService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }

    @Test
    void getAllRoute() throws Exception {
        // Arrange
        PhysicalTest mockTest = new PhysicalTest();
        mockTest.setName("VMA Test");
        mockTest.setUnit("km/h");
        
        when(physicalTestService.getPhysicalTests()).thenReturn(List.of(mockTest));

        // Act & Assert
        mockMvc.perform(get("/api/physicalTest")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("VMA Test"))
                .andExpect(jsonPath("$[0].unit").value("km/h"));
    }

    @Test
    void createTestRoute() throws Exception {
        // Arrange
        PhysicalTestCreateRequest request = new PhysicalTestCreateRequest();
        request.setTestName("Cooper Test");
        request.setUnit("Meters");

        doNothing().when(physicalTestService).createPhysicalTest(any(PhysicalTestCreateRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/physicalTest/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}