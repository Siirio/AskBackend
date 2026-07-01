package kz.ask.search.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchLocationRequest {

    private Double lat;
    private Double lng;
}
