package com.ecommerce.onlinestore.service.impl;

import com.ecommerce.onlinestore.mapper.CartMapper;
import com.ecommerce.onlinestore.model.dto.AddToCartRequest;
import com.ecommerce.onlinestore.model.dto.CartDTO;
import com.ecommerce.onlinestore.model.dto.UpdateCartItemRequest;
import com.ecommerce.onlinestore.model.entity.Cart;
import com.ecommerce.onlinestore.model.entity.CartItem;
import com.ecommerce.onlinestore.model.entity.Product;
import com.ecommerce.onlinestore.model.exception.CartNotFoundException;
import com.ecommerce.onlinestore.model.exception.InsufficientStockException;
import com.ecommerce.onlinestore.model.exception.ProductNotFoundException;
import com.ecommerce.onlinestore.repository.CartRepository;
import com.ecommerce.onlinestore.repository.ProductRepository;
import com.ecommerce.onlinestore.service.CartService;
import com.ecommerce.onlinestore.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Override
    public CartDTO createCart(String sessionId) {
        log.debug("Creating new cart for session: {}", sessionId);

        Cart cart = Cart.builder()
                .sessionId(sessionId)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Created new cart with id: {} for session: {}", savedCart.getId(), sessionId);

        return cartMapper.toDTO(savedCart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartById(Long cartId) {
        log.debug("Fetching cart by id: {}", cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartBySessionId(String sessionId) {
        log.debug("Fetching cart by session id: {}", sessionId);

        Cart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for session: " + sessionId));

        return cartMapper.toDTO(cart);
    }

    @Override
    public CartDTO addItemToCart(Long cartId, AddToCartRequest request) {
        log.debug("Adding item to cart: {}, product: {}, quantity: {}",
                cartId, request.getProductId(), request.getQuantity());

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        validateStockAvailability(product, request.getQuantity());

        // Check if item already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            validateMaxQuantity(newQuantity);
            existingItem.setQuantity(newQuantity);
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.addItem(newItem);
        }

        cart.calculateTotals();
        Cart savedCart = cartRepository.save(cart);
        log.info("Added product {} to cart {}", product.getId(), cartId);

        return cartMapper.toDTO(savedCart);
    }

    @Override
    public CartDTO updateCartItem(Long cartId, Long itemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item: {} in cart: {} with quantity: {}", itemId, cartId, request.getQuantity());

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        validateMaxQuantity(request.getQuantity());
        validateStockAvailability(cartItem.getProduct(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cart.calculateTotals();

        Cart savedCart = cartRepository.save(cart);
        log.info("Updated cart item {} in cart {} to quantity {}", itemId, cartId, request.getQuantity());

        return cartMapper.toDTO(savedCart);
    }

    @Override
    public CartDTO removeItemFromCart(Long cartId, Long itemId) {
        log.debug("Removing item: {} from cart: {}", itemId, cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        log.info("Removed cart item {} from cart {}", itemId, cartId);

        return cartMapper.toDTO(savedCart);
    }

    @Override
    public void clearCart(Long cartId) {
        log.debug("Clearing cart: {}", cartId);

        Cart cart = cartRepository.findByIdWithItems(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));

        cart.clear();
        cartRepository.save(cart);
        log.info("Cleared cart {}", cartId);
    }

    @Override
    public void deleteCart(Long cartId) {
        log.debug("Deleting cart: {}", cartId);

        if (!cartRepository.existsById(cartId)) {
            throw new CartNotFoundException(cartId);
        }

        cartRepository.deleteById(cartId);
        log.info("Deleted cart {}", cartId);
    }

    @Override
    public CartDTO mergeCarts(Long sourceCartId, Long targetCartId) {
        log.debug("Merging cart {} into cart {}", sourceCartId, targetCartId);

        Cart sourceCart = cartRepository.findByIdWithItems(sourceCartId)
                .orElseThrow(() -> new CartNotFoundException(sourceCartId));

        Cart targetCart = cartRepository.findByIdWithItems(targetCartId)
                .orElseThrow(() -> new CartNotFoundException(targetCartId));

        // Merge items from source cart to target cart
        for (CartItem sourceItem : sourceCart.getItems()) {
            CartItem existingItem = targetCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(sourceItem.getProduct().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + sourceItem.getQuantity();
                validateMaxQuantity(newQuantity);
                validateStockAvailability(sourceItem.getProduct(), newQuantity);
                existingItem.setQuantity(newQuantity);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(targetCart)
                        .product(sourceItem.getProduct())
                        .quantity(sourceItem.getQuantity())
                        .unitPrice(sourceItem.getUnitPrice())
                        .build();
                targetCart.addItem(newItem);
            }
        }

        targetCart.calculateTotals();
        Cart savedCart = cartRepository.save(targetCart);

        // Delete source cart after merge
        cartRepository.delete(sourceCart);

        log.info("Merged cart {} into cart {}", sourceCartId, targetCartId);
        return cartMapper.toDTO(savedCart);
    }

    private void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    product.getName(), requestedQuantity, product.getStockQuantity());
        }
    }

    private void validateMaxQuantity(Integer quantity) {
        if (quantity > Constants.MAX_CART_QUANTITY) {
            throw new RuntimeException("Maximum quantity per product is " + Constants.MAX_CART_QUANTITY);
        }
        if (quantity < Constants.MIN_CART_QUANTITY) {
            throw new RuntimeException("Minimum quantity per product is " + Constants.MIN_CART_QUANTITY);
        }
    }
}
