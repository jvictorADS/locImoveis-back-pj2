package com.locImoveis.pj2back.controller;

import com.locImoveis.pj2back.dto.dashboard.DashboardResponseDTO;
import com.locImoveis.pj2back.entity.Usuario;
import com.locImoveis.pj2back.entity.enums.CobrancaStatus;
import com.locImoveis.pj2back.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponseDTO buscarResumo(
            @AuthenticationPrincipal Usuario locador,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Integer locatarioId,
            @RequestParam(required = false) Integer imovelId,
            @RequestParam(required = false) CobrancaStatus status
    ) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à data final.");
        }
        return dashboardService.buscarResumo(locador, dataInicio, dataFim, locatarioId, imovelId, status);
    }
}