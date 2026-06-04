package com.helphub.backend.config;

import com.helphub.backend.common.enums.DonationStatus;
import com.helphub.backend.common.enums.PaymentMethod;
import com.helphub.backend.common.enums.SupportNeedContributionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

// TODO: What the fuck is this ?

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSchemaConstraintInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgres()) {
            return;
        }

        ensureSupportNeedContributionStatusColumn();
        ensureEnumConstraint(
                "donations",
                "payment_method",
                "donations_payment_method_check",
                PaymentMethod.values());
        ensureEnumConstraint(
                "donations",
                "status",
                "donations_status_check",
                DonationStatus.values());
        ensureEnumConstraint(
                "support_need_contributions",
                "payment_method",
                "support_need_contributions_payment_method_check",
                PaymentMethod.values());
        ensureEnumConstraint(
                "support_need_contributions",
                "status",
                "support_need_contributions_status_check",
                SupportNeedContributionStatus.values());
    }

    private boolean isPostgres() {
        try {
            String version = jdbcTemplate.queryForObject("select version()", String.class);
            return version != null && version.contains("PostgreSQL");
        } catch (Exception ex) {
            log.debug("Skipping payment schema constraint repair because database type could not be detected", ex);
            return false;
        }
    }

    private void ensureEnumConstraint(String tableName, String columnName, String constraintName, Enum<?>[] values) {
        if (!columnExists(tableName, columnName)) {
            return;
        }

        String allowedValues = Arrays.stream(values)
                .map(Enum::name)
                .map(this::toSqlLiteral)
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("""
                ALTER TABLE %s
                DROP CONSTRAINT IF EXISTS %s
                """.formatted(tableName, constraintName));

        jdbcTemplate.execute("""
                ALTER TABLE %s
                ADD CONSTRAINT %s CHECK (%s IN (%s))
                """.formatted(tableName, constraintName, columnName, allowedValues));
    }

    private void ensureSupportNeedContributionStatusColumn() {
        if (!tableExists("support_need_contributions")) {
            return;
        }

        jdbcTemplate.execute("""
                ALTER TABLE support_need_contributions
                ADD COLUMN IF NOT EXISTS status varchar(30)
                """);
        jdbcTemplate.update(
                "UPDATE support_need_contributions SET status = ? WHERE status IS NULL",
                SupportNeedContributionStatus.SUCCESS.name());
        jdbcTemplate.execute("""
                ALTER TABLE support_need_contributions
                ALTER COLUMN status SET NOT NULL
                """);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND table_name = ?
                """, Integer.class, tableName);

        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, tableName, columnName);

        return count != null && count > 0;
    }

    private String toSqlLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
