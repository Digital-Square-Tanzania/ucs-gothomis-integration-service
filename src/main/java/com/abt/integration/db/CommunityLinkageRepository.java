package com.abt.integration.db;

import com.abt.integration.model.ChwMetadata;
import com.abt.integration.model.ClientLookupResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CommunityLinkageRepository {
    private final String schema;

    public CommunityLinkageRepository(String schema) {
        this.schema = schema;
    }

    public Optional<ClientLookupResult> findClientByUniqueId(Connection connection, String uniqueId) throws SQLException {
        String sql = "SELECT base_entity_id, unique_id FROM " + schema + ".client WHERE unique_id = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uniqueId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ClientLookupResult(
                        resultSet.getString("base_entity_id"),
                        resultSet.getString("unique_id")
                ));
            }
        }
    }

    public Optional<ChwMetadata> findChwMetadata(Connection connection, String chwUsername) throws SQLException {
        String sql = "SELECT tm.identifier, tm.team_name, tm.uuid, tm.location_uuid,tl.village " +
                "FROM " + schema + ".team_members tm " +
                "LEFT JOIN " + schema + ".tanzania_locations tl ON tl.location_uuid = tm.location_uuid " +
                "WHERE tm.identifier = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chwUsername);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ChwMetadata(
                        resultSet.getString("identifier"),
                        resultSet.getString("team_name"),
                        resultSet.getString("uuid"),
                        resultSet.getString("location_uuid"),
                        resultSet.getString("village")
                ));
            }
        }
    }
}
