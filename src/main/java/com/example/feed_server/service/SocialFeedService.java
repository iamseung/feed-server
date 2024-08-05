package com.example.feed_server.service;

import com.example.feed_server.dto.FeedRequest;
import com.example.feed_server.entity.SocialFeed;
import com.example.feed_server.repository.SocialFeedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SocialFeedService {

    private SocialFeedRepository feedRepository;

    public SocialFeedService(SocialFeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public List<SocialFeed> getAllFeeds() {
        return feedRepository.findAll();
    }

    public List<SocialFeed> getAllFeedsByUploaderId(int uploaderId) {
        return feedRepository.findByUploaderId(uploaderId);
    }

    public SocialFeed getFeedById(int feedId) {
        // 없을 경우 null, Controller 에서 처리
        return feedRepository.findById(feedId).orElse(null);
    }

    public void deleteFeed(int feedId) {
        feedRepository.deleteById(feedId);
    }

    @Transactional
    public SocialFeed createFeed(FeedRequest feed) {
        return feedRepository.save(new SocialFeed(feed));
    }
}
