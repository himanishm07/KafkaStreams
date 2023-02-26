package model;

import lombok.*;

@Builder
@Value
@Data
@AllArgsConstructor
public class BettingPOJO {
    String carName;
    Long betPrice;
}
