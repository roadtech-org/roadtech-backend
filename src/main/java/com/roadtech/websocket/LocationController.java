package com.roadtech.websocket;

import com.roadtech.dto.mechanic.LocationUpdateDto;
import com.roadtech.security.CustomUserDetails;
import com.roadtech.service.MechanicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final MechanicService mechanicService;

    @MessageMapping("/location")
    public void handleLocationUpdate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Payload LocationUpdateDto locationDto
    ) {
        if (userDetails != null) {
            log.debug("Received location update from mechanic {}: {}, {}",
                    userDetails.getUserId(),
                    locationDto.getLatitude(),
                    locationDto.getLongitude()
            );

            mechanicService.updateLocation(userDetails.getUserId(), locationDto);
        }
    }
}
