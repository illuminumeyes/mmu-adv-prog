package uk.ac.mmu.advprog.assessment.browser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
    
    private static final String BASE_SQL = """
        SELECT DISTINCT Wine.id, Wine.name, Wine.type, 
               Winery.name AS winery_name, Region.country, Wine.abv
        FROM Wine
        LEFT JOIN Winery ON Wine.winery_id = Winery.id
        LEFT JOIN Region ON Winery.region_id = Region.id
        LEFT JOIN Wine_Grape ON Wine.id = Wine_Grape.wine_id
        LEFT JOIN Grape ON Wine_Grape.grape_id = Grape.id
        WHERE 1=1
    """;
    
    private final StringBuilder sql;
    private final List<Object> params;

    public QueryBuilder() {
        this.sql = new StringBuilder(BASE_SQL);
        this.params = new ArrayList<>();
    }

    public QueryBuilder addFilter(String field, String value) {
        switch (field) {
            case "abvMin" -> addNumericFilter("Wine.abv >= ?", value);
            case "abvMax" -> addNumericFilter("Wine.abv <= ?", value);
            case "grape" -> addGrapeFilter(value);
            case "blend_type" -> addLikeFilter("Wine.blend_type", value);
            default -> addColumnFilter(field, value);
        }
        return this;
    }

    private void addNumericFilter(String condition, String value) {
        try {
            sql.append(" AND ").append(condition);
            params.add(Double.valueOf(value));
        } catch (NumberFormatException ignored) {
        }
    }

    private void addGrapeFilter(String value) {
        sql.append("""
             AND EXISTS (
                SELECT 1 FROM Wine_Grape
                JOIN Grape ON Wine_Grape.grape_id = Grape.id
                WHERE Wine_Grape.wine_id = Wine.id
                AND LOWER(Grape.name) LIKE ?
            )
        """);
        params.add("%" + value.toLowerCase() + "%");
    }

    private void addLikeFilter(String column, String value) {
        sql.append(" AND LOWER(").append(column).append(") LIKE ? ");
        params.add("%" + value.toLowerCase() + "%");
    }

    private void addColumnFilter(String field, String value) {
        String column = mapFieldToColumn(field);
        if (column != null) {
            addLikeFilter(column, value);
        }
    }

    private String mapFieldToColumn(String field) {
        return switch (field) {
            case "wineName" -> "Wine.name";
            case "type" -> "Wine.type";
            case "country" -> "Region.country";
            case "regionName" -> "Region.name";
            case "wineryName" -> "Winery.name";
            case "body" -> "Wine.body";
            case "acidity" -> "Wine.acidity";
            default -> null;
        };
    }

    public String build() {
        return sql.append(" ORDER BY Wine.name ASC").toString();
    }

    public void setParameters(PreparedStatement stmt) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof Double d) {
                stmt.setDouble(i + 1, d);
            } else {
                stmt.setString(i + 1, param.toString());
            }
        }
    }
}