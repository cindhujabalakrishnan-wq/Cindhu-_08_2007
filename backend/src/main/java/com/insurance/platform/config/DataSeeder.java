package com.insurance.platform.config;

import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.InsuranceCompanyRepository;
import com.insurance.platform.repository.PolicyTypeRepository;
import com.insurance.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the admin user plus sample companies and policy types on startup.
 * Never logs passwords.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_ADMIN:false}")
    private boolean seedAdmin;

    @Value("${ADMIN_EMAIL:admin@insurance.local}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository,
                      InsuranceCompanyRepository companyRepository,
                      PolicyTypeRepository policyTypeRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.policyTypeRepository = policyTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCompanies();
        seedPolicyTypes();
        if (seedAdmin) {
            seedAdminUser();
        } else {
            log.info("Admin seeding skipped (SEED_ADMIN=false)");
        }
    }

    private void seedAdminUser() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("SEED_ADMIN=true but ADMIN_PASSWORD is empty; skipping admin seed");
            return;
        }
        String email = adminEmail.toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            log.info("Admin user already exists: {}", email);
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);
        log.info("Seeded admin user: {}", email);
    }

    private void seedCompanies() {
        if (companyRepository.count() > 0) {
            return;
        }
        saveCompany("Acme General Insurance", "ACME", "contact@acme.example.com");
        saveCompany("Globex Life Insurance", "GLOBEX", "hello@globex.example.com");
        log.info("Seeded sample insurance companies");
    }

    private void saveCompany(String name, String code, String email) {
        InsuranceCompany company = new InsuranceCompany();
        company.setName(name);
        company.setCode(code);
        company.setContactEmail(email);
        company.setActive(true);
        companyRepository.save(company);
    }

    private void seedPolicyTypes() {
        if (policyTypeRepository.count() > 0) {
            return;
        }
        saveType("Term Life", "TERM_LIFE", PolicyCategory.LIFE, "Term life cover");
        saveType("Health Plus", "HEALTH_PLUS", PolicyCategory.HEALTH, "Comprehensive health cover");
        saveType("Motor Comprehensive", "MOTOR_COMP", PolicyCategory.AUTO, "Car insurance");
        saveType("Home Secure", "HOME_SECURE", PolicyCategory.HOME, "Home insurance");
        log.info("Seeded sample policy types");
    }

    private void saveType(String name, String code, PolicyCategory category, String description) {
        PolicyType type = new PolicyType();
        type.setName(name);
        type.setCode(code);
        type.setCategory(category);
        type.setDescription(description);
        type.setActive(true);
        policyTypeRepository.save(type);
    }
}
