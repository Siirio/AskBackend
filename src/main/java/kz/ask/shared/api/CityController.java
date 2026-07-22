package kz.ask.shared.api;

import java.util.List;
import kz.ask.shared.domain.CityService;
import kz.ask.shared.domain.dto.CityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/resolve")
    public ResponseEntity<CityDto> resolve(@RequestParam String name) {
        return ResponseEntity.ok(cityService.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<CityDto>> listAll() {
        return ResponseEntity.ok(cityService.listAll());
    }
}
