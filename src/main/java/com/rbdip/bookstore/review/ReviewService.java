package com.rbdip.bookstore.review;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewService implements ProductReviews {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        String name = authorName == null ? "anonymous" : authorName;
        Review review = new Review(productId, name, rating, comment);
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}