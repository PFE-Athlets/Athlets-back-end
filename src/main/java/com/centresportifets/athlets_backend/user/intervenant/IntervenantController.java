package com.centresportifets.athlets_backend.user.intervenant;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.centresportifets.athlets_backend.user.intervenant.dto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intervenants")
public class IntervenantController {
    private final IntervenantService service;

    @GetMapping
    public List<IntervenantData> list() { return service.list(); }

    @GetMapping("/{id}")
    public IntervenantData get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IntervenantData create(@RequestBody IntervenantCreateRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public IntervenantData update(@PathVariable Long id, @RequestBody IntervenantUpdateRequest request) { return service.update(id, request); }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id, Authentication auth) { service.deactivate(id, auth); }

    @PutMapping("/{id}/reactivate")
    public IntervenantData reactivate(@PathVariable Long id) { return service.reactivate(id); }

    @PostMapping("/{id}/resend-activation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendActivation(@PathVariable Long id) { service.resendActivation(id); }
}
