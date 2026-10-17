package com.example.medpassbackend;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class MedPassController {

    private final MedPassRepo repo;

    public MedPassController(MedPassRepo repo) {
        this.repo = repo;
    }

    // ===================== VERIFY =====================
    @GetMapping("/verify")
    public VerifyResponse verify(@RequestParam String doc,
                                 @RequestParam String guard) {

        String participantHash = HashUtil.sha256Hex(doc);
        String guardHash = HashUtil.sha256Hex(guard);

        String disease = repo.findDiseaseByIdentifierHash(participantHash);

        repo.logAccess(guardHash, "VERIFY", participantHash);

        if (disease == null) {
            return new VerifyResponse(false, null, List.of());
        }

        List<String> items = repo.findAllowedItems(disease);
        return new VerifyResponse(true, disease, items);
    }

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {

        if (req == null || req.username == null || req.password == null) {
            return new LoginResponse(false, "NONE", "Missing username or password");
        }

        String username = req.username.trim();
        String passHash = HashUtil.sha256Hex(req.password);

        String role = repo.findRoleByLogin(username, passHash);
        if (role == null) {
            return new LoginResponse(false, "NONE", "Invalid login");
        }

        String guardHash = HashUtil.sha256Hex(username);
        repo.logAccess(guardHash, "LOGIN", null);

        return new LoginResponse(true, role, "OK");
    }

    // ===================== ADMIN =====================
    @GetMapping("/admin/logs")
    public List<AccessLogDto> logs(@RequestParam String guard) {
        String guardHash = HashUtil.sha256Hex(guard);
        return repo.getLogsForGuard(guardHash);
    }

    // ===================== UTILS =====================
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}