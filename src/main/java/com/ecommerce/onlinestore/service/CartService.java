package com.ecommerce.onlinestore.service;

import com.ecommerce.onlinestore.model.dto.AddToCartRequest;
import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.dto.UpdateCartItemRequest;

public interface CartService {

    CartDTO createCart(String sessionId);

    CartDTO getCartById(Long cartId);

    CartDTO getCartBySessionId(String sessionId);

    CartDTO addItemToCart(Long cartId, AddToCartRequest request);

    CartDTO updateCartItem(Long cartId, Long itemId, UpdateCartItemRequest request);

    CartDTO removeItemFromCart(Long cartId, Long itemId);

    void clearCart(Long cartId);

    void deleteCart(Long cartId);

    CartDTO mergeCarts(Long sourceCartId, Long targetCartId);
}
