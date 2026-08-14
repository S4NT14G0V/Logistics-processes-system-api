package com.backend.couriersyncfeat4.dto.input;

public record PlaceInput(
    String name,
    String address,
    String city,
    String department,
    Float latitude,
    Float longitude
) {

}
