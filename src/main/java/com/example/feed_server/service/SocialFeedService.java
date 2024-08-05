package com.example.feed_server.service;

import com.example.feed_server.repository.SocialFeedRepository;
import org.springframework.stereotype.Service;

@Service
public class SocialFeedService {

    private SocialFeedRepository feedRepository;

    public SocialFeedService(SocialFeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }
}
