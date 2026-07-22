package kz.ask.offer.service.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessServiceListResponse {

    private List<BusinessServiceRowResponse> items;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
