package com.ecommerce.onlinestore.controller;

import com.ecommerce.onlinestore.model.dto.AddToCartRequest;
import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.dto.UpdateCartItemRequest;
import com.ecommerce.onlinestore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "APIs for managing shopping carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @Operation(summary = "Create cart", description = "Create a new shopping cart")
    public ResponseEntity<CartDTO> createCart(@RequestParam(required = false) String sessionId) {
        String cartSessionId = sessionId != null ? sessionId : generateSessionId();
        CartDTO cart = cartService.createCart(cartSessionId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/{cartId}")
    @Operation(summary = "Get cart", description = "Retrieve cart by ID")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long cartId) {
        CartDTO cart = cartService.getCartById(cartId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get cart by session", description = "Retrieve cart by session ID")
    public ResponseEntity<CartDTO> getCartBySession(@PathVariable String sessionId) {
        CartDTO cart = cartService.getCartBySessionId(sessionId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{cartId}/items")
    @Operation(summary = "Add item to cart", description = "Add product to shopping cart")
    public ResponseEntity<CartDTO> addItemToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody AddToCartRequest request) {

        CartDTO cart = cartService.addItemToCart(cartId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update quantity of item in cart")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartDTO cart = cartService.updateCartItem(cartId, itemId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove item from shopping cart")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long itemId) {

        CartDTO cart = cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}/clear")
    @Operation(summary = "Clear cart", description = "Remove all items from cart")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cartId}")
    @Operation(summary = "Delete cart", description = "Delete shopping cart")
    public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
        cartService.deleteCart(cartId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge carts", description = "Merge two shopping carts")
    public ResponseEntity<CartDTO> mergeCarts(
            @RequestParam Long sourceCartId,
            @RequestParam Long targetCartId) {

        CartDTO mergedCart = cartService.mergeCarts(sourceCartId, targetCartId);
        return ResponseEntity.ok(mergedCart);
    }

    private String generateSessionId() {
        return "session-" + UUID.randomUUID().toString();
    }
}
