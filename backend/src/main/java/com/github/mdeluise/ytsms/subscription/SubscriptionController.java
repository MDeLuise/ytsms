package com.github.mdeluise.ytsms.subscription;

import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/subscription")
@Tag(name = "Subscription", description = "Endpoints for operations on notes.")
public class SubscriptionController {
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionDTOConverter subscriptionDtoConverter;


    @Autowired
    public SubscriptionController(SubscriptionDTOConverter subscriptionDTOConverter,
                                  SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionDtoConverter = subscriptionDTOConverter;
        this.userService = userService;
    }


    @Operation(summary = "Get all the Subscriptions", description = "Get all the Subscriptions.")
    @GetMapping
    public ResponseEntity<Collection<SubscriptionDTO>> findAll() {
        Set<SubscriptionDTO> allSubscriptions =
            subscriptionService.getAll().stream().map(subscriptionDtoConverter::convertToDTO)
                               .collect(Collectors.toSet());
        return ResponseEntity.ok(allSubscriptions);
    }


    @Operation(
        summary = "Delete a single Subscription",
        description = "Delete the given Subscription, according to the `id` parameter."
    )
    @DeleteMapping("/{id}")
    public void remove(
        @Parameter(description = "The ID of the Subscription on which to perform the operation") @PathVariable
        Long id) {
        subscriptionService.remove(id);
    }


    @Operation(
        summary = "Create a new Subscription",
        description = "Create a new Subscription."
    )
    @PostMapping
    public ResponseEntity<SubscriptionDTO> saveWithId(@RequestBody SubscriptionDTO entityToSave) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = userService.get(authentication.getName());
        entityToSave.setUserId(authenticatedUser.getId());
        SubscriptionDTO result = subscriptionDtoConverter.convertToDTO(
            subscriptionService.save(subscriptionDtoConverter.convertFromDTO(entityToSave)));
        return ResponseEntity.ok(result);
    }
}
