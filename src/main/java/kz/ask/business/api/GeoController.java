package kz.ask.business.api;

import java.util.Map;
import kz.ask.shared.util.TwoGisLinkParser;
import kz.ask.shared.util.TwoGisLinkParser.ParsedCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/geo")
public class GeoController {

    @PostMapping("/parse-2gis-link")
    public ResponseEntity<Map<String, Object>> parseTwoGisLink(@RequestBody Map<String, String> body) {
        String link = body.get("link");
        if (link == null || link.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "link is required"));
        }
        ParsedCoordinates coords = TwoGisLinkParser.parse(link);
        if (coords == null) {
            return ResponseEntity.ok(Map.of("found", false));
        }
        return ResponseEntity.ok(Map.of(
                "found", true,
                "latitude", coords.latitude(),
                "longitude", coords.longitude()
        ));
    }
}
