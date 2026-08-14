package com.backend.couriersyncfeat4.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PackageCountResponse {
    private UUID userId;
    private Integer packageCount;
}
