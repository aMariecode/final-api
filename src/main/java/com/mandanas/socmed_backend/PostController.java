package com.mandanas.socmed_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    private final String uploadDir = "uploads/";

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "media", required = false) MultipartFile media) throws IOException {

        String mediaUrl = null;

        // Save media file if provided
        if (media != null && !media.isEmpty()) {
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            String filePath = uploadDir + media.getOriginalFilename();
            media.transferTo(new File(filePath));
            mediaUrl = filePath;
        }

        // Create and save the post
        Post post = new Post();
        post.setContent(content);
        post.setMediaUrl(mediaUrl);
        post.setLikes(0);
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Post>> bulkUploadPosts(@RequestBody List<Post> posts) {
        // Save all posts in bulk
        List<Post> savedPosts = postRepository.saveAll(posts);
        return ResponseEntity.ok(savedPosts);
    }

    @PatchMapping("/{id}/react")
    public ResponseEntity<Post> reactToPost(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));

        String reaction = requestBody.get("reaction");

        // Update the reaction count
        Map<String, Integer> reactions = post.getReactions();
        reactions.put(reaction, reactions.getOrDefault(reaction, 0) + 1);
        post.setReactions(reactions);

        return ResponseEntity.ok(postRepository.save(post));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setContent(updatedPost.getContent());
        post.setMediaUrl(updatedPost.getMediaUrl());
        return ResponseEntity.ok(postRepository.save(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}