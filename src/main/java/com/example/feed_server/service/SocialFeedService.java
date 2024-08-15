package com.example.feed_server.service;

import com.example.feed_server.dto.FeedInfo;
import com.example.feed_server.dto.FeedRequest;
import com.example.feed_server.dto.UserInfo;
import com.example.feed_server.entity.SocialFeed;
import com.example.feed_server.repository.SocialFeedRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class SocialFeedService {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private SocialFeedRepository feedRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;

    @Value("${sns.user-server}")
    private String userServiceUrl;
    private RestClient restClient = RestClient.create();

    public SocialFeedService(SocialFeedRepository feedRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.feedRepository = feedRepository;
        this.kafkaTemplate = kafkaTemplate;
        // Spring 에서 제공하는 data 기능이 포함된 objectMapper 를 제공받아 사용
        this.objectMapper = objectMapper;
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
        SocialFeed savedFeed = feedRepository.save(new SocialFeed(feed));

        // 사용자 조회
        UserInfo uploader = getUserInfo(savedFeed.getUploaderId());
        FeedInfo feedInfo = new FeedInfo(savedFeed, uploader.getUsername());

        try {
            kafkaTemplate.send("feed.created", objectMapper.writeValueAsString(feedInfo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return savedFeed;
    }

    // feed 갱신
    public void refreshAllFeeds() {
        List<SocialFeed> feeds = getAllFeeds();

        for(SocialFeed feed : feeds) {
            UserInfo uploader = getUserInfo(feed.getUploaderId());
            FeedInfo feedInfo = new FeedInfo(feed, uploader.getUsername());

            try {
                kafkaTemplate.send("feed.created", objectMapper.writeValueAsString(feedInfo));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
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
