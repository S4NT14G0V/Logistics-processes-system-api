package com.backend.couriersyncfeat4.dto.output;

public interface InventorySummaryResponse {

    String getRegion();

    Long getInTransit();

    Long getDelivered();

    Long getPending();
}
