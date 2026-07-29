package kz.ask.search.basic.infrastructure.meilisearch;

import java.util.ArrayList;
import java.util.List;
import kz.ask.search.basic.application.processor.SearchPlan;
import org.springframework.stereotype.Component;

@Component
public class MeilisearchFilterCompiler {

    public String compile(SearchPlan plan) {
        List<String> filters = new ArrayList<>();
        if (plan.getItemType() != null) {
            filters.add("documentType = " + plan.getItemType().name());
        }
        if (plan.getMinPrice() != null) {
            filters.add("price >= " + plan.getMinPrice().toPlainString());
        }
        if (plan.getMaxPrice() != null) {
            filters.add("price <= " + plan.getMaxPrice().toPlainString());
        }
        addTextFilter(filters, "categoryLabel", plan.getUserSelectedCategory());
        if (plan.getRadiusMeters() != null
                && plan.getUserLatitude() != null
                && plan.getUserLongitude() != null) {
            filters.add("_geoRadius(" + plan.getUserLatitude() + ", "
                    + plan.getUserLongitude() + ", " + plan.getRadiusMeters() + ")");
        }
        return String.join(" AND ", filters);
    }

    private void addTextFilter(List<String> filters, String field, String value) {
        if (value != null && !value.isBlank()) {
            filters.add(field + " = '" + value.replace("'", "\\'") + "'");
        }
    }
}
