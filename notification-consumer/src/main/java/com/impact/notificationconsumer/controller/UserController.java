package com.impact.notificationconsumer.controller;

import com.impact.notificationconsumer.payload.request.PaginationRequest;
import com.impact.notificationconsumer.payload.response.GlobalResponse;
import com.impact.notificationconsumer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<GlobalResponse> getAllUsers(@RequestParam int pageNo, @RequestParam int pageSize,
                                                      @RequestParam String sortBy, @RequestParam String sortDirection) {
        PaginationRequest paginationRequest = PaginationRequest.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        GlobalResponse userResponse = userService.getAllUsers(paginationRequest);
        return ResponseEntity.ok().body(userResponse);

    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse> getUserById(@PathVariable(name = "id") Long userId) {
        GlobalResponse userResponse = userService.getUserById(userId);
        return ResponseEntity.ok().body(userResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse> searchUser(@RequestParam String query) {
        GlobalResponse userResponse = userService.searchUser(query);
        return ResponseEntity.ok().body(userResponse);
    }
}
