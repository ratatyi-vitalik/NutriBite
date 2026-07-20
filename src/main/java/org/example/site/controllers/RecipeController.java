package org.example.site.controllers;

import jakarta.servlet.http.HttpSession;
import org.example.site.persistent.entity.comments.Comment;
import org.example.site.persistent.entity.comments.Reply;
import org.example.site.persistent.entity.recipes.Recipe;
import org.example.site.persistent.entity.User;
import org.example.site.persistent.repository.CommentRepository;
import org.example.site.persistent.repository.RecipeRepository;
import org.example.site.persistent.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public RecipeController(RecipeRepository recipeRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/recipe")
    public ModelAndView recipe(Long recipeId, HttpSession session) {
        ModelAndView model = new ModelAndView("recipe");

        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (recipe.isPresent()) {
            model.addObject("recipe", recipe.get());
        }
        else {
            return new ModelAndView("redirects:/");
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addObject("user", user);
        }
        return model;
    }

    @PostMapping("/comment-post")
    public ModelAndView postComment(@RequestParam String text, @RequestParam Long recipeId, @RequestParam(required = false) Long commentId, HttpSession session) {
        User user = (User)session.getAttribute("user");
        if (user != null) {
            user = userRepository.findById(user.getId()).get();
        }
        else {
            return new ModelAndView("redirect:/recipe?recipeId=" + recipeId);
        }

        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return new ModelAndView("redirect:/");
        }

        if (commentId == null) {
            Comment comment = new Comment();
            comment.setText(text);
            comment.setAuthor(user);
            comment.setDate(LocalDateTime.now());

            recipe.getComments().add(comment);
        }
        else {
            Reply reply = new Reply();
            reply.setAuthor(user);
            reply.setText(text);
            reply.setDate(LocalDateTime.now());

            List<Comment> comments = recipe.getComments();
            Comment comment = null;
            for (Comment com: recipe.getComments()) {
                if (com.getId().equals(commentId)) {
                    comment = com;
                    break;
                }
            }

            if (comment != null) {
                comment.getReplies().add(reply);
            } else {
                return new ModelAndView("redirect:/recipe?recipeId=" + recipeId);
            }
        }

        recipeRepository.save(recipe);

        return new ModelAndView("redirect:/recipe?recipeId=" + recipeId);
    }

    @PostMapping("/like-comment")
    public ResponseEntity<?> likeComment(@RequestParam Long commentId, HttpSession session) {
        User user = (User)session.getAttribute("user");
        if (user != null) {
            user = userRepository.findById(user.getId()).get();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Войдите в аккаунт");
        }

        Optional<Comment> com = commentRepository.findById(commentId);
        Comment comment;
        if (com.isPresent()) {
            comment = com.get();
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Комментарий не найден");
        }

        Set<User> likes = comment.getLikes();
        if (likes.contains(user)) {
            likes.remove(user);
            comment.setLikes(likes);
            commentRepository.save(comment);
            return ResponseEntity.ok(Map.of("likes", likes.size(), "liked", false));
        }
        else {
            likes.add(user);
            comment.setLikes(likes);
            commentRepository.save(comment);
            return ResponseEntity.ok((Map.of("likes", likes.size(), "liked", true)));
        }
    }
}
