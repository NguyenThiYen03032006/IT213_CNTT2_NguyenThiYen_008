package vn.rikkei.exam.clinicappointment.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import vn.rikkei.exam.clinicappointment.repository.AppUserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final AppUserRepository appUserRepository;

    @Override
    public void run(String... args) {
        try {
            if (appUserRepository.count() == 0) {
                log.info("CSDL chua co du lieu app_users. Dang nap data ");
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                        new ClassPathResource("seed_data.sql")
                );
                populator.setContinueOnError(true);
                populator.execute(dataSource);
                log.info("Da nap data thanh cong! So user hien tai: {}", appUserRepository.count());
            } else {
                log.info("CSDL da co san {} app_users. Bo qua khoi tao seed data.", appUserRepository.count());
            }
        } catch (Exception e) {
            log.warn("Loi trong qua trinh nap seed data (co the do bang chua tao hoac quyen CSDL): {}", e.getMessage());
        }
    }
}
