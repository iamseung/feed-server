package com.example.feed_server.service;

import com.example.feed_server.dto.FeedRequest;
import com.example.feed_server.dto.UserInfo;
import com.example.feed_server.entity.SocialFeed;
import com.example.feed_server.repository.SocialFeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class SocialFeedService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private SocialFeedRepository feedRepository;

    @Value("${sns.user-server}")
    private String userServiceUrl;
    private RestClient restClient = RestClient.create();

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

    // user-server 로 부터 회원 정보 조회, RestClient
    public UserInfo getUserInfo(int userId) {
        log.info("userId : " + String.valueOf(userId));

        return restClient.get()
                    .uri(userServiceUrl + "/api/users/" + userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new RuntimeException("invalid server response " + response.getStatusText());
                })
                .body(UserInfo.class);
    }
}
