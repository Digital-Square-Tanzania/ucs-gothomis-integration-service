package com.abt.integration.db;

import com.abt.integration.model.ChwMetadata;
import com.abt.integration.model.ClientLookupResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityLinkageRepositoryTest {

    private Connection connection;
    private CommunityLinkageRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:community_linkage_repo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
        repository = new CommunityLinkageRepository("public");

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS public");
            statement.execute("CREATE TABLE IF NOT EXISTS public.client (base_entity_id VARCHAR(255) NOT NULL, client_id VARCHAR(255) NOT NULL, unique_id VARCHAR(255) NOT NULL, first_name VARCHAR(255), middle_name VARCHAR(255), last_name VARCHAR(255), sex VARCHAR(255), birth_date VARCHAR(255), marital_status VARCHAR(255), phone_number VARCHAR(255), entity_type VARCHAR(255), team VARCHAR(255), team_id VARCHAR(255), location_id VARCHAR(255), provider_id VARCHAR(255), event_date TIMESTAMP, family VARCHAR(255), leadership VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS public.team_members (uuid VARCHAR(255) NOT NULL, identifier VARCHAR(255), name VARCHAR(255), location_uuid VARCHAR(255) NOT NULL, location_name VARCHAR(255), team_name VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS public.tanzania_locations (location_uuid VARCHAR(255) NOT NULL, village VARCHAR(255), health_facility VARCHAR(255), hfr_code VARCHAR(255), ward VARCHAR(255), district_council VARCHAR(255), district VARCHAR(255), region VARCHAR(255), zone VARCHAR(255), country VARCHAR(255), is_pepfar_site BOOLEAN, village_code VARCHAR(50), ward_code VARCHAR(255), district_code VARCHAR(50), council_code VARCHAR(50), region_code VARCHAR(50))");
            statement.execute("DELETE FROM public.client");
            statement.execute("DELETE FROM public.team_members");
            statement.execute("DELETE FROM public.tanzania_locations");
            statement.execute("INSERT INTO public.client (base_entity_id, client_id, unique_id) VALUES ('base-123', 'client-123', '1223141')");
            statement.execute("INSERT INTO public.team_members (uuid, identifier, name, location_uuid, location_name, team_name) VALUES ('team-uuid-1', 'tintu', 'Tintu CHW', 'location-uuid-1', 'Makao', 'Team A')");
            statement.execute("INSERT INTO public.tanzania_locations (location_uuid, village, health_facility, hfr_code, ward, district_council, district, region, zone, country, is_pepfar_site, village_code, ward_code, district_code, council_code, region_code) VALUES ('location-uuid-1', 'Village', 'Facility', 'HFR-1', 'Ward', 'Council', 'District', 'Region', 'Zone', 'TZ', TRUE, 'V-1', 'W-1', 'D-1', 'C-1', 'R-1')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void findsClientByUniqueId() throws Exception {
        Optional<ClientLookupResult> result = repository.findClientByUniqueId(connection, "1223141");

        assertTrue(result.isPresent());
        assertEquals("base-123", result.get().baseEntityId());
        assertEquals("1223141", result.get().uniqueId());
    }

    @Test
    void findsChwMetadataByUsername() throws Exception {
        Optional<ChwMetadata> result = repository.findChwMetadata(connection, "tintu");

        assertTrue(result.isPresent());
        assertEquals("tintu", result.get().providerId());
        assertEquals("Team A", result.get().team());
        assertEquals("team-uuid-1", result.get().teamId());
        assertEquals("location-uuid-1", result.get().locationId());
    }
}
