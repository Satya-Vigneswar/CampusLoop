package com.campusloop.campusloop.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public MarketplaceSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> columns = jdbcTemplate.queryForList(
                """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'MARKETPLACE_PRODUCTS'
                """,
                String.class);

        if (columns.contains("CONDITION") && columns.contains("ITEM_CONDITION")) {
            jdbcTemplate.execute("ALTER TABLE marketplace_products DROP COLUMN item_condition");
        }
    }
}
