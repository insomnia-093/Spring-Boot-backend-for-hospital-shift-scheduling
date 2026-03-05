package org.example.hospital.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.hospital.domain.Department;
import org.example.hospital.domain.DutyCalendarEntry;
import org.example.hospital.domain.Role;
import org.example.hospital.domain.RoleType;
import org.example.hospital.domain.Shift;
import org.example.hospital.domain.ShiftStatus;
import org.example.hospital.domain.UserAccount;
import org.example.hospital.repository.DepartmentRepository;
import org.example.hospital.repository.DutyCalendarEntryRepository;
import org.example.hospital.repository.RoleRepository;
import org.example.hospital.repository.ShiftRepository;
import org.example.hospital.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final ShiftRepository shiftRepository;
    private final DutyCalendarEntryRepository dutyCalendarEntryRepository;

    @Value("${app.init.admin.enabled:true}")
    private boolean adminSeedEnabled;

    @Value("${app.init.admin.email:admin@hospital.local}")
    private String adminEmail;

    @Value("${app.init.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.init.admin.full-name:System Admin}")
    private String adminFullName;

    @Value("${app.init.demo.enabled:true}")
    private boolean demoSeedEnabled;

    public DataInitializer(RoleRepository roleRepository,
                           UserAccountRepository userAccountRepository,
                           PasswordEncoder passwordEncoder,
                           DepartmentRepository departmentRepository,
                           ShiftRepository shiftRepository,
                           DutyCalendarEntryRepository dutyCalendarEntryRepository) {
        this.roleRepository = roleRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.shiftRepository = shiftRepository;
        this.dutyCalendarEntryRepository = dutyCalendarEntryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            ensureRoles();
            if (adminSeedEnabled) {
                ensureAdminUser();
            }
            if (demoSeedEnabled) {
                ensureDemoData();
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureRoles() {
        for (RoleType roleType : RoleType.values()) {
            roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType)));
        }
    }

    private void ensureAdminUser() {
        if (userAccountRepository.existsByEmail(adminEmail)) {
            return;
        }
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Admin seed requires app.init.admin.email and app.init.admin.password");
        }
        UserAccount admin = new UserAccount(adminEmail, passwordEncoder.encode(adminPassword), adminFullName);
        Set<Role> adminRoles = EnumSet.of(RoleType.ADMIN).stream()
                .map(roleType -> roleRepository.findByName(roleType).orElseGet(() -> roleRepository.save(new Role(roleType))))
                .collect(java.util.stream.Collectors.toSet());
        admin.setRoles(adminRoles);
        userAccountRepository.save(admin);
    }

    private void ensureDemoData() {
        Department respiratory = ensureDepartment("呼吸内科", "内科-呼吸系统");
        Department cardio = ensureDepartment("心血管内科", "内科-心血管");
        Department surgery = ensureDepartment("普通外科", "外科");
        Department emergency = ensureDepartment("急诊科", "急诊");

        Map<String, Department> departments = Map.of(
            "呼吸内科", respiratory,
            "心血管内科", cardio,
            "普通外科", surgery,
            "急诊科", emergency
        );

        Role doctorRole = roleRepository.findByName(RoleType.DOCTOR)
            .orElseGet(() -> roleRepository.save(new Role(RoleType.DOCTOR)));
        Role nurseRole = roleRepository.findByName(RoleType.NURSE)
            .orElseGet(() -> roleRepository.save(new Role(RoleType.NURSE)));

        UserAccount doc1 = ensureDemoUser("demo_doc1@hospital.local", "李医生", doctorRole, departments.get("呼吸内科"));
        UserAccount doc2 = ensureDemoUser("demo_doc2@hospital.local", "王医生", doctorRole, departments.get("呼吸内科"));
        UserAccount doc3 = ensureDemoUser("demo_doc3@hospital.local", "张医生", doctorRole, departments.get("心血管内科"));
        UserAccount doc4 = ensureDemoUser("demo_doc4@hospital.local", "赵医生", doctorRole, departments.get("普通外科"));
        UserAccount nurse1 = ensureDemoUser("demo_nurse1@hospital.local", "陈护士", nurseRole, departments.get("急诊科"));

        YearMonth ym = YearMonth.now();
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LocalDateTime monthStartTime = monthStart.atStartOfDay();
        LocalDateTime monthEndTime = monthEnd.atTime(LocalTime.of(23, 59, 59));

        log.info("[DEMO_INIT] month={}, range=[{} ~ {}]", ym, monthStartTime, monthEndTime);
        log.info("[DEMO_INIT] departments(resp={}, cardio={}, surgery={}, emergency={})",
            respiratory.getId(), cardio.getId(), surgery.getId(), emergency.getId());

        List<Shift> existingMonthShifts = shiftRepository.findByStartTimeBetween(monthStartTime, monthEndTime);
        log.info("[DEMO_INIT] existing month shifts count={}", existingMonthShifts.size());

        if (existingMonthShifts.isEmpty()) {
            LocalDate d1 = ym.atDay(Math.min(5, ym.lengthOfMonth()));
            LocalDate d2 = d1.plusDays(1);
            LocalDate d3 = d2.plusDays(1);
            LocalDate d4 = d3.plusDays(1);

            Shift s1 = new Shift(LocalDateTime.of(d1, LocalTime.of(8, 0)), LocalDateTime.of(d1, LocalTime.of(16, 0)), RoleType.DOCTOR, departments.get("呼吸内科"));
            s1.setStatus(ShiftStatus.ASSIGNED);
            s1.setAssignedUser(doc1);
            s1.setNotes("DEMO-呼吸白班");

            Shift s2 = new Shift(LocalDateTime.of(d1, LocalTime.of(16, 0)), LocalDateTime.of(d1, LocalTime.of(23, 59)), RoleType.DOCTOR, departments.get("呼吸内科"));
            s2.setStatus(ShiftStatus.ASSIGNED);
            s2.setAssignedUser(doc2);
            s2.setNotes("DEMO-呼吸夜班");

            Shift s3 = new Shift(LocalDateTime.of(d2, LocalTime.of(8, 0)), LocalDateTime.of(d2, LocalTime.of(16, 0)), RoleType.DOCTOR, departments.get("心血管内科"));
            s3.setStatus(ShiftStatus.ASSIGNED);
            s3.setAssignedUser(doc3);
            s3.setNotes("DEMO-心内白班");

            Shift s4 = new Shift(LocalDateTime.of(d2, LocalTime.of(16, 0)), LocalDateTime.of(d2, LocalTime.of(23, 59)), RoleType.DOCTOR, departments.get("心血管内科"));
            s4.setStatus(ShiftStatus.OPEN);
            s4.setNotes("DEMO-心内夜班待派");

            Shift s5 = new Shift(LocalDateTime.of(d3, LocalTime.of(8, 0)), LocalDateTime.of(d3, LocalTime.of(16, 0)), RoleType.DOCTOR, departments.get("普通外科"));
            s5.setStatus(ShiftStatus.ASSIGNED);
            s5.setAssignedUser(doc4);
            s5.setNotes("DEMO-外科白班");

            Shift s6 = new Shift(LocalDateTime.of(d3, LocalTime.of(16, 0)), LocalDateTime.of(d3, LocalTime.of(23, 59)), RoleType.DOCTOR, departments.get("普通外科"));
            s6.setStatus(ShiftStatus.ASSIGNED);
            s6.setAssignedUser(doc4);
            s6.setNotes("DEMO-外科夜班");

            Shift s7 = new Shift(LocalDateTime.of(d4, LocalTime.of(8, 0)), LocalDateTime.of(d4, LocalTime.of(16, 0)), RoleType.NURSE, departments.get("急诊科"));
            s7.setStatus(ShiftStatus.ASSIGNED);
            s7.setAssignedUser(nurse1);
            s7.setNotes("DEMO-急诊白班");

            Shift s8 = new Shift(LocalDateTime.of(d4, LocalTime.of(16, 0)), LocalDateTime.of(d4, LocalTime.of(23, 59)), RoleType.NURSE, departments.get("急诊科"));
            s8.setStatus(ShiftStatus.OPEN);
            s8.setNotes("DEMO-急诊夜班待派");

            shiftRepository.saveAll(List.of(s1, s2, s3, s4, s5, s6, s7, s8));
            log.info("[DEMO_INIT] seeded 8 demo shifts for month={}", ym);
        } else {
            log.info("[DEMO_INIT] skip shift seeding because month already has data");
        }

        List<DutyCalendarEntry> existingMonthCalendar = dutyCalendarEntryRepository.findByDateBetween(monthStart, monthEnd);
        log.info("[DEMO_INIT] existing month calendar entries count={}", existingMonthCalendar.size());

        if (existingMonthCalendar.isEmpty()) {
            LocalDate d1 = ym.atDay(Math.min(5, ym.lengthOfMonth()));
            LocalDate d2 = d1.plusDays(1);
            LocalDate d3 = d2.plusDays(1);
            LocalDate d4 = d3.plusDays(1);
            dutyCalendarEntryRepository.saveAll(List.of(
                new DutyCalendarEntry(d1, departments.get("呼吸内科"), "DEMO-呼吸内科值班", 2),
                new DutyCalendarEntry(d2, departments.get("心血管内科"), "DEMO-心内科值班", 1),
                new DutyCalendarEntry(d3, departments.get("普通外科"), "DEMO-外科值班", 2),
                new DutyCalendarEntry(d4, departments.get("急诊科"), "DEMO-急诊值班", 1)
            ));
            log.info("[DEMO_INIT] seeded 4 demo calendar entries for month={}", ym);
        } else {
            log.info("[DEMO_INIT] skip calendar seeding because month already has data");
        }
    }

    private Department ensureDepartment(String name, String description) {
        return departmentRepository.findByName(name)
            .orElseGet(() -> departmentRepository.save(new Department(name, description)));
    }

    private UserAccount ensureDemoUser(String email, String fullName, Role role, Department department) {
        return userAccountRepository.findByEmail(email).orElseGet(() -> {
            UserAccount user = new UserAccount(email, passwordEncoder.encode("Demo123!"), fullName);
            user.addRole(role);
            if (department != null) {
                user.setDepartment(department);
            }
            return userAccountRepository.save(user);
        });
    }
}
