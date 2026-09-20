package com.insurance.platform.config;

import com.insurance.platform.model.entity.AuditLog;
import com.insurance.platform.model.entity.CustomerProfile;
import com.insurance.platform.model.entity.InsuranceCompany;
import com.insurance.platform.model.entity.InsurancePolicy;
import com.insurance.platform.model.entity.Notification;
import com.insurance.platform.model.entity.PolicyDocument;
import com.insurance.platform.model.entity.PolicyRenewal;
import com.insurance.platform.model.entity.PolicyType;
import com.insurance.platform.model.entity.PremiumPayment;
import com.insurance.platform.model.entity.User;
import com.insurance.platform.model.enums.DocumentType;
import com.insurance.platform.model.enums.NotificationType;
import com.insurance.platform.model.enums.PaymentMethod;
import com.insurance.platform.model.enums.PaymentStatus;
import com.insurance.platform.model.enums.PolicyCategory;
import com.insurance.platform.model.enums.PolicyStatus;
import com.insurance.platform.model.enums.PremiumFrequency;
import com.insurance.platform.model.enums.RenewalStatus;
import com.insurance.platform.model.enums.Role;
import com.insurance.platform.repository.AuditLogRepository;
import com.insurance.platform.repository.CustomerProfileRepository;
import com.insurance.platform.repository.InsuranceCompanyRepository;
import com.insurance.platform.repository.InsurancePolicyRepository;
import com.insurance.platform.repository.NotificationRepository;
import com.insurance.platform.repository.PolicyDocumentRepository;
import com.insurance.platform.repository.PolicyRenewalRepository;
import com.insurance.platform.repository.PolicyTypeRepository;
import com.insurance.platform.repository.PremiumPaymentRepository;
import com.insurance.platform.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Seeds realistic demonstration data (customers, policies, payments, renewals,
 * notifications, documents, audit entries) so every UI page has content.
 *
 * <p>Runs only when {@code app.demo.seed} (or {@code DEMO_SEED}) is true.
 * Never logs passwords. Skips entirely if demo customers already exist,
 * so restarts against a persistent database do not duplicate data.
 */
@Component
@Order(2)
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String DEMO_PASSWORD = "Customer@123";

    private final UserRepository userRepository;
    private final CustomerProfileRepository profileRepository;
    private final InsuranceCompanyRepository companyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final InsurancePolicyRepository policyRepository;
    private final PremiumPaymentRepository paymentRepository;
    private final PolicyRenewalRepository renewalRepository;
    private final PolicyDocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.seed:${DEMO_SEED:false}}")
    private boolean seedDemo;

    @Value("${app.file.storage-path:${FILE_STORAGE_PATH:./uploads}}")
    private String storagePath;

    public DemoDataSeeder(UserRepository userRepository,
                          CustomerProfileRepository profileRepository,
                          InsuranceCompanyRepository companyRepository,
                          PolicyTypeRepository policyTypeRepository,
                          InsurancePolicyRepository policyRepository,
                          PremiumPaymentRepository paymentRepository,
                          PolicyRenewalRepository renewalRepository,
                          PolicyDocumentRepository documentRepository,
                          NotificationRepository notificationRepository,
                          AuditLogRepository auditLogRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
        this.policyTypeRepository = policyTypeRepository;
        this.policyRepository = policyRepository;
        this.paymentRepository = paymentRepository;
        this.renewalRepository = renewalRepository;
        this.documentRepository = documentRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemo) {
            log.info("Demo data seeding skipped (app.demo.seed=false)");
            return;
        }
        if (userRepository.existsByEmail("priya.sharma@example.com")) {
            log.info("Demo data already present; skipping");
            return;
        }
        seedReferenceData();

        User priya = saveCustomer("priya.sharma@example.com", "Priya", "Sharma", "9876501111",
                LocalDate.of(1990, 4, 12), "Female", "14 Sea Breeze, Bandra West",
                "Mumbai", "Maharashtra", "400050", "Software Engineer",
                new BigDecimal("1800000"));
        User rahul = saveCustomer("rahul.verma@example.com", "Rahul", "Verma", "9876502222",
                LocalDate.of(1985, 9, 23), "Male", "C-42 Greater Kailash II",
                "New Delhi", "Delhi", "110048", "Business Owner",
                new BigDecimal("2400000"));
        User anita = saveCustomer("anita.iyer@example.com", "Anita", "Iyer", "9876503333",
                LocalDate.of(1992, 1, 30), "Female", "77 100 Feet Road, Indiranagar",
                "Bengaluru", "Karnataka", "560038", "Doctor",
                new BigDecimal("2000000"));

        LocalDate today = LocalDate.now();

        // ---- Priya's policies ----
        InsurancePolicy p1 = savePolicy(priya, "POL-2025-1001", "Family Health Cover",
                "ACME", "HEALTH_PLUS", PolicyCategory.HEALTH,
                today.minusDays(165), today.plusDays(200),
                new BigDecimal("2500"), PremiumFrequency.MONTHLY, new BigDecimal("500000"),
                PolicyStatus.ACTIVE, "Rahul Sharma", "9876501112",
                "Family floater including parents");
        InsurancePolicy p2 = savePolicy(priya, "POL-2025-1002", "SecureLife Term Plan",
                "GLOBEX", "TERM_LIFE", PolicyCategory.LIFE,
                today.minusDays(353), today.plusDays(12),
                new BigDecimal("15000"), PremiumFrequency.YEARLY, new BigDecimal("5000000"),
                PolicyStatus.EXPIRING_SOON, "Rahul Sharma", "9876501112",
                "20-year term plan");
        InsurancePolicy p3 = savePolicy(priya, "POL-2024-1003", "Swift Car Comprehensive",
                "ACME", "MOTOR_COMP", PolicyCategory.AUTO,
                today.minusDays(390), today.minusDays(25),
                new BigDecimal("8500"), PremiumFrequency.YEARLY, new BigDecimal("800000"),
                PolicyStatus.EXPIRED, "Priya Sharma", "9876501111",
                "Maruti Swift KA-05-MN-4321");

        // ---- Rahul's policies ----
        InsurancePolicy p4 = savePolicy(rahul, "POL-2025-1004", "Home Shield Plus",
                "SECURELIFE", "HOME_SECURE", PolicyCategory.HOME,
                today.minusDays(45), today.plusDays(320),
                new BigDecimal("12000"), PremiumFrequency.YEARLY, new BigDecimal("7500000"),
                PolicyStatus.ACTIVE, "Meera Verma", "9876502223",
                "2BHK in Greater Kailash with contents cover");
        InsurancePolicy p5 = savePolicy(rahul, "POL-2025-1005", "Europe Trip Cover",
                "HEALTHGUARD", "TRAVEL_SECURE", PolicyCategory.TRAVEL,
                today.minusDays(24), today.plusDays(6),
                new BigDecimal("3500"), PremiumFrequency.ONE_TIME, new BigDecimal("1000000"),
                PolicyStatus.EXPIRING_SOON, "Meera Verma", "9876502223",
                "Schengen trip 12 Oct - 26 Oct");
        InsurancePolicy p6 = savePolicy(rahul, "POL-2024-1006", "Health Plus Individual",
                "HEALTHGUARD", "HEALTH_PLUS", PolicyCategory.HEALTH,
                today.minusDays(425), today.minusDays(60),
                new BigDecimal("1800"), PremiumFrequency.MONTHLY, new BigDecimal("300000"),
                PolicyStatus.EXPIRED, "Rahul Verma", "9876502222",
                "Lapsed; renewal missed");

        // ---- Anita's policies ----
        InsurancePolicy p7 = savePolicy(anita, "POL-2025-1007", "Wealth Protect Term",
                "GLOBEX", "TERM_LIFE", PolicyCategory.LIFE,
                today.minusDays(100), today.plusDays(400),
                new BigDecimal("22000"), PremiumFrequency.YEARLY, new BigDecimal("10000000"),
                PolicyStatus.ACTIVE, "Karthik Iyer", "9876503334",
                "25-year term with critical illness rider");
        InsurancePolicy p8 = savePolicy(anita, "POL-2025-1008", "City Hatchback Cover",
                "SECURELIFE", "MOTOR_COMP", PolicyCategory.AUTO,
                today.minusDays(337), today.plusDays(28),
                new BigDecimal("7200"), PremiumFrequency.YEARLY, new BigDecimal("600000"),
                PolicyStatus.EXPIRING_SOON, "Anita Iyer", "9876503333",
                "Hyundai i20 KA-03-PQ-7788");

        // ---- Payments ----
        savePayment(p1, new BigDecimal("2500"), today.minusDays(90), today.minusDays(90),
                PaymentMethod.UPI, "UPI-88230111", PaymentStatus.COMPLETED, "July premium");
        savePayment(p1, new BigDecimal("2500"), today.minusDays(60), today.minusDays(60),
                PaymentMethod.CREDIT_CARD, "CC-77120983", PaymentStatus.COMPLETED, "August premium");
        savePayment(p1, new BigDecimal("2500"), today.minusDays(30), today.minusDays(30),
                PaymentMethod.UPI, "UPI-88230455", PaymentStatus.COMPLETED, "September premium");
        savePayment(p1, new BigDecimal("2500"), null, today.plusDays(5),
                PaymentMethod.UPI, null, PaymentStatus.PENDING, "October premium due soon");

        savePayment(p2, new BigDecimal("15000"), today.minusDays(353), today.minusDays(353),
                PaymentMethod.NET_BANKING, "NB-55019273", PaymentStatus.COMPLETED, "Annual premium");
        savePayment(p2, new BigDecimal("16200"), null, today.plusDays(12),
                PaymentMethod.NET_BANKING, null, PaymentStatus.PENDING, "Renewal premium due");

        savePayment(p3, new BigDecimal("8500"), today.minusDays(390), today.minusDays(390),
                PaymentMethod.DEBIT_CARD, "DC-33098812", PaymentStatus.COMPLETED, "Last year premium");
        savePayment(p3, new BigDecimal("9100"), null, today.minusDays(10),
                PaymentMethod.DEBIT_CARD, null, PaymentStatus.OVERDUE, "Renewal premium overdue");

        savePayment(p4, new BigDecimal("12000"), today.minusDays(45), today.minusDays(45),
                PaymentMethod.BANK_TRANSFER, "NEFT-20918834", PaymentStatus.COMPLETED, "Annual premium");
        savePayment(p5, new BigDecimal("3500"), today.minusDays(24), today.minusDays(24),
                PaymentMethod.CREDIT_CARD, "CC-77125501", PaymentStatus.COMPLETED, "Single trip premium");
        savePayment(p6, new BigDecimal("1800"), today.minusDays(120), today.minusDays(120),
                PaymentMethod.UPI, "UPI-88229910", PaymentStatus.COMPLETED, "Last paid premium");
        savePayment(p6, new BigDecimal("1800"), null, today.minusDays(60),
                PaymentMethod.UPI, null, PaymentStatus.FAILED, "Auto-debit failed; policy lapsed");

        savePayment(p7, new BigDecimal("22000"), today.minusDays(465), today.minusDays(465),
                PaymentMethod.BANK_TRANSFER, "NEFT-20910011", PaymentStatus.COMPLETED, "Previous year");
        savePayment(p7, new BigDecimal("22000"), today.minusDays(100), today.minusDays(100),
                PaymentMethod.BANK_TRANSFER, "NEFT-20917762", PaymentStatus.COMPLETED, "Current year");
        savePayment(p8, new BigDecimal("6800"), today.minusDays(337), today.minusDays(337),
                PaymentMethod.CASH, "BRN-10293", PaymentStatus.COMPLETED, "Paid at branch");
        savePayment(p8, new BigDecimal("7200"), null, today.plusDays(28),
                PaymentMethod.CASH, null, PaymentStatus.PENDING, "Renewal due with expiry");

        // ---- Renewals ----
        saveRenewal(p2, p2.getExpiryDate(), p2.getExpiryDate().plusYears(1),
                new BigDecimal("16200"), RenewalStatus.PENDING,
                today.minusDays(3).atStartOfDay(), null, "Customer requested renewal");
        saveRenewal(p5, p5.getExpiryDate(), p5.getExpiryDate().plusDays(15),
                new BigDecimal("1800"), RenewalStatus.PENDING,
                today.minusDays(1).atStartOfDay(), null, "Trip extension requested");
        saveRenewal(p8, p8.getExpiryDate(), p8.getExpiryDate().plusYears(1),
                new BigDecimal("7200"), RenewalStatus.PENDING,
                today.minusDays(2).atStartOfDay(), null, "Annual rollover");
        saveRenewal(p1, p1.getStartDate().minusDays(1), p1.getExpiryDate(),
                new BigDecimal("2400"), RenewalStatus.COMPLETED,
                today.minusDays(165).atStartOfDay(), today.minusDays(164).atStartOfDay(),
                "Renewed for current term");
        saveRenewal(p3, p3.getExpiryDate(), p3.getExpiryDate().plusYears(1),
                new BigDecimal("9100"), RenewalStatus.CANCELLED,
                today.minusDays(20).atStartOfDay(), today.minusDays(15).atStartOfDay(),
                "Renewal window missed; policy expired");

        // ---- Notifications ----
        notify(priya, NotificationType.POLICY_EXPIRY, "Policy expiring soon: SecureLife Term Plan",
                "Your policy POL-2025-1002 expires in 12 days. Raise a renewal request to stay covered.",
                p2.getId(), false, null);
        notify(priya, NotificationType.PAYMENT_DUE, "Premium due: Family Health Cover",
                "Premium of 2500 is due in 5 days for policy POL-2025-1001.", p1.getId(), false, null);
        notify(priya, NotificationType.SYSTEM, "Welcome to InsureTrack",
                "Your account is ready. Add policies to track premiums and renewals in one place.",
                null, true, today.minusDays(160).atStartOfDay());
        notify(rahul, NotificationType.POLICY_EXPIRY, "Policy expiring soon: Europe Trip Cover",
                "Your policy POL-2025-1005 expires in 6 days. Extend it before you travel.",
                p5.getId(), false, null);
        notify(rahul, NotificationType.PAYMENT_FAILED, "Payment failed: Health Plus Individual",
                "Auto-debit of 1800 failed for policy POL-2024-1006. The policy has lapsed.",
                p6.getId(), false, null);
        notify(rahul, NotificationType.RENEWAL, "Renewal request received",
                "Your renewal request for policy POL-2025-1005 is under process.", p5.getId(), false, null);
        notify(anita, NotificationType.POLICY_EXPIRY, "Policy expiring soon: City Hatchback Cover",
                "Your policy POL-2025-1008 expires in 28 days. Renew to keep NCB intact.",
                p8.getId(), false, null);
        notify(anita, NotificationType.PAYMENT_SUCCESS, "Premium received: Wealth Protect Term",
                "We have received 22000 towards policy POL-2025-1007. Receipt is available under Documents.",
                p7.getId(), true, today.minusDays(100).atStartOfDay());

        // ---- Documents (real downloadable PDFs) ----
        saveDocument(p1, priya, DocumentType.POLICY_DOCUMENT, "Family Health Cover",
                "POL-2025-1001", p1.getExpiryDate());
        saveDocument(p2, priya, DocumentType.POLICY_DOCUMENT, "SecureLife Term Plan",
                "POL-2025-1002", p2.getExpiryDate());
        saveDocument(p7, anita, DocumentType.ID_PROOF, "Wealth Protect Term",
                "POL-2025-1007", p7.getExpiryDate());

        // ---- Audit trail ----
        audit(priya.getId(), "USER_LOGIN", "User", String.valueOf(priya.getId()),
                "Demo customer signed in", "127.0.0.1");
        audit(priya.getId(), "POLICY_CREATED", "InsurancePolicy", String.valueOf(p2.getId()),
                "Policy created: POL-2025-1002", "127.0.0.1");
        audit(rahul.getId(), "PAYMENT_RECORDED", "PremiumPayment", String.valueOf(p5.getId()),
                "Premium recorded for POL-2025-1005", "127.0.0.1");

        log.info("Seeded demo data: 3 customers, 8 policies, payments, renewals, notifications, documents");
    }

    // ---------- helpers ----------

    private void seedReferenceData() {
        saveCompanyIfMissing("SecureLife Insurance", "SECURELIFE", "care@securelife.example.com");
        saveCompanyIfMissing("HealthGuard Insurance", "HEALTHGUARD", "support@healthguard.example.com");
        saveTypeIfMissing("Travel Secure", "TRAVEL_SECURE", PolicyCategory.TRAVEL, "Overseas travel cover");
    }

    private void saveCompanyIfMissing(String name, String code, String email) {
        boolean exists = companyRepository.findAll().stream()
                .anyMatch(c -> code.equalsIgnoreCase(c.getCode()));
        if (exists) {
            return;
        }
        InsuranceCompany company = new InsuranceCompany();
        company.setName(name);
        company.setCode(code);
        company.setContactEmail(email);
        company.setActive(true);
        companyRepository.save(company);
    }

    private void saveTypeIfMissing(String name, String code, PolicyCategory category, String description) {
        boolean exists = policyTypeRepository.findAll().stream()
                .anyMatch(t -> code.equalsIgnoreCase(t.getCode()));
        if (exists) {
            return;
        }
        PolicyType type = new PolicyType();
        type.setName(name);
        type.setCode(code);
        type.setCategory(category);
        type.setDescription(description);
        type.setActive(true);
        policyTypeRepository.save(type);
    }

    private User saveCustomer(String email, String firstName, String lastName, String phone,
                              LocalDate dob, String gender, String address, String city,
                              String state, String postalCode, String occupation, BigDecimal income) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setRole(Role.ROLE_CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now().minusDays(170));
        user.setUpdatedAt(LocalDateTime.now().minusDays(170));
        user = userRepository.save(user);

        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        profile.setDateOfBirth(dob);
        profile.setGender(gender);
        profile.setAddress(address);
        profile.setCity(city);
        profile.setState(state);
        profile.setPostalCode(postalCode);
        profile.setCountry("India");
        profile.setOccupation(occupation);
        profile.setAnnualIncome(income);
        profile.setIdProofType("AADHAAR");
        profile.setIdProofNumber("XXXX-XXXX-" + Math.abs(email.hashCode() % 9000 + 1000));
        profileRepository.save(profile);
        return user;
    }

    private InsuranceCompany requireCompany(String code) {
        return companyRepository.findAll().stream()
                .filter(c -> code.equalsIgnoreCase(c.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing company: " + code));
    }

    private PolicyType requireType(String code) {
        return policyTypeRepository.findAll().stream()
                .filter(t -> code.equalsIgnoreCase(t.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing policy type: " + code));
    }

    private InsurancePolicy savePolicy(User customer, String policyNumber, String policyName,
                                       String companyCode, String typeCode, PolicyCategory category,
                                       LocalDate start, LocalDate expiry, BigDecimal premium,
                                       PremiumFrequency frequency, BigDecimal coverage,
                                       PolicyStatus status, String nominee, String nomineeContact,
                                       String notes) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setPolicyNumber(policyNumber);
        policy.setCustomer(customer);
        policy.setInsuranceCompany(requireCompany(companyCode));
        policy.setPolicyType(requireType(typeCode));
        policy.setPolicyName(policyName);
        policy.setCategory(category);
        policy.setStartDate(start);
        policy.setExpiryDate(expiry);
        policy.setPremiumAmount(premium);
        policy.setPremiumFrequency(frequency);
        policy.setCoverageAmount(coverage);
        policy.setStatus(status);
        policy.setNomineeName(nominee);
        policy.setNomineeContact(nomineeContact);
        policy.setNotes(notes);
        return policyRepository.save(policy);
    }

    private void savePayment(InsurancePolicy policy, BigDecimal amount, LocalDate paidOn,
                             LocalDate due, PaymentMethod method, String txnRef,
                             PaymentStatus status, String notes) {
        PremiumPayment payment = new PremiumPayment();
        payment.setPolicy(policy);
        payment.setAmount(amount);
        payment.setPaymentDate(paidOn);
        payment.setDueDate(due);
        payment.setPaymentMethod(method);
        payment.setTransactionReference(txnRef);
        payment.setStatus(status);
        payment.setNotes(notes);
        paymentRepository.save(payment);
    }

    private void saveRenewal(InsurancePolicy policy, LocalDate prevExpiry, LocalDate newExpiry,
                             BigDecimal premium, RenewalStatus status,
                             LocalDateTime requestedAt, LocalDateTime processedAt, String remarks) {
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setPolicy(policy);
        renewal.setPreviousExpiryDate(prevExpiry);
        renewal.setNewExpiryDate(newExpiry);
        renewal.setRenewalPremium(premium);
        renewal.setStatus(status);
        renewal.setRequestedAt(requestedAt);
        renewal.setProcessedAt(processedAt);
        renewal.setRemarks(remarks);
        renewalRepository.save(renewal);
    }

    private void notify(User user, NotificationType type, String title, String message,
                        Long policyId, boolean read, LocalDateTime createdAt) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedPolicyId(policyId);
        notification.setRead(read);
        if (read) {
            notification.setReadAt(LocalDateTime.now().minusDays(30));
        }
        if (createdAt != null) {
            notification.setCreatedAt(createdAt);
        }
        notificationRepository.save(notification);
    }

    private void saveDocument(InsurancePolicy policy, User uploadedBy, DocumentType type,
                              String policyName, String policyNumber, LocalDate expiry) {
        try {
            Path dir = Path.of(storagePath);
            Files.createDirectories(dir);
            String storedName = "demo-" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            Path target = dir.resolve(storedName);
            List<String> lines = List.of(
                    "INSURANCE POLICY SCHEDULE (SAMPLE)",
                    "Policy Number: " + policyNumber,
                    "Policy Name: " + policyName,
                    "Holder: " + policy.getCustomer().getFirstName()
                            + " " + policy.getCustomer().getLastName(),
                    "Insurer: " + policy.getInsuranceCompany().getName(),
                    "Valid Till: " + expiry,
                    "Premium: " + policy.getPremiumAmount(),
                    "This is a seeded demonstration document.");
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                    stream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    stream.beginText();
                    stream.newLineAtOffset(50, 750);
                    stream.showText(lines.get(0));
                    stream.setFont(PDType1Font.HELVETICA, 11);
                    for (int i = 1; i < lines.size(); i++) {
                        stream.newLineAtOffset(0, -22);
                        stream.showText(lines.get(i));
                    }
                    stream.endText();
                }
                doc.save(target.toFile());
            }
            PolicyDocument document = new PolicyDocument();
            document.setPolicy(policy);
            document.setUploadedBy(uploadedBy);
            document.setDocumentType(type);
            document.setFileName(storedName);
            document.setOriginalFileName(policyNumber + "-schedule.pdf");
            document.setFilePath(target.toAbsolutePath().toString());
            document.setContentType("application/pdf");
            document.setFileSize(Files.size(target));
            documentRepository.save(document);
        } catch (IOException e) {
            log.warn("Could not seed document for {}: {}", policyNumber, e.getMessage());
        }
    }

    private void audit(Long userId, String action, String entityType, String entityId,
                       String details, String ip) {
        AuditLog entry = new AuditLog();
        entry.setUserId(userId);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        entry.setIpAddress(ip);
        auditLogRepository.save(entry);
    }
}
