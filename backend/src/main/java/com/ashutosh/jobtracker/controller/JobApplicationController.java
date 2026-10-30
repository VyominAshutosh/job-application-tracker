package com.ashutosh.jobtracker.controller;

import com.ashutosh.jobtracker.dto.ApplicationRequest;
import com.ashutosh.jobtracker.dto.ApplicationResponse;
import com.ashutosh.jobtracker.dto.StatusUpdateRequest;
import com.ashutosh.jobtracker.entity.ApplicationStatus;
import com.ashutosh.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HTTP in, HTTP out. No rules here on purpose: everything decides in the service.
 * @CrossOrigin lets the React page call this API from another origin: the dev server
 * on port 5173, and the deployed site on Vercel (any *.vercel.app address).
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(originPatterns = {"http://localhost:5173", "http://127.0.0.1:5173", "https://*.vercel.app"})
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    /** POST /api/applications */
    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/applications  or  GET /api/applications?status=INTERVIEW */
    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) ApplicationStatus status) {
        return service.findAll(status);
    }

    /** GET /api/applications/search?q=amazon */
    @GetMapping("/search")
    public List<ApplicationResponse> search(@RequestParam(name = "q", required = false) String q) {
        return service.search(q);
    }

    /** GET /api/applications/stats */
    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return service.stats();
    }

    /** GET /api/applications/7 */
    @GetMapping("/{id}")
    public ApplicationResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /** PUT /api/applications/7 */
    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable Long id,
                                      @Valid @RequestBody ApplicationRequest request) {
        return service.update(id, request);
    }

    /** PATCH /api/applications/7/status. PATCH, because only one field changes. */
    @PatchMapping("/{id}/status")
    public ApplicationResponse changeStatus(@PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest request) {
        return service.changeStatus(id, request.status());
    }

    /** DELETE /api/applications/7 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
