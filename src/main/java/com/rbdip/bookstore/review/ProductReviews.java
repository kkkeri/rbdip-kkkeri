package com.rbdip.bookstore.review;

import java.util.List;

public interface ProductReviews {

    Review addReview(Long productId, String authorName, Integer rating, String comment);

    List<Review> listReviews(Long productId);
}