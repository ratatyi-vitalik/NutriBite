package site.controllers;

import jakarta.servlet.http.HttpSession;
import site.persistent.entity.comments.Comment;
import site.persistent.entity.comments.Reply;
import site.persistent.entity.recipes.Rating;
import site.persistent.entity.recipes.Recipe;
import site.persistent.entity.User;
import site.persistent.repository.CommentRepository;
import site.persistent.repository.RatingRepository;
import site.persistent.repository.RecipeRepository;
import site.persistent.repository.UserRepository;
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
    private final RatingRepository ratingRepository;

    public RecipeController(RecipeRepository recipeRepository, UserRepository userRepository,
                            CommentRepository commentRepository, RatingRepository ratingRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
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

    @PostMapping("/save-recipe")
    public ResponseEntity<?> saveRecipe(@RequestParam Long recipeId, HttpSession session) {
        User user = (User)session.getAttribute("user");
        if (user != null) {
            user = userRepository.findById(user.getId()).get();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in");
        }
        Optional<Recipe> optRecipe = recipeRepository.findById(recipeId);
        if (optRecipe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found!");
        }

        Recipe recipe = optRecipe.get();
        boolean saved;

        Set<User> savedByUsers = recipe.getSaved();
        if (savedByUsers.contains(user)) {
            savedByUsers.remove(user);
            saved = false;
        } else {
            savedByUsers.add(user);
            saved = true;
        }

        recipe.setSaved(savedByUsers);
        recipeRepository.save(recipe);

        return ResponseEntity.ok(Map.of("saved", saved));
    }

    @PostMapping("/rate-recipe")
    public ResponseEntity<?> rateRecipe(@RequestParam Long recipeId, @RequestParam int score, HttpSession session) {
        User user = (User)session.getAttribute("user");
        if (user != null) {
            Optional<User> userOpt = userRepository.findById(user.getId());
            if (userOpt.isEmpty()) {
                session.removeAttribute("user");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in");
            }
            user = userOpt.get();
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in");
        }

        Optional<Recipe> optRecipe = recipeRepository.findById(recipeId);
        if (optRecipe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found!");
        }
        Recipe recipe = optRecipe.get();
        if (1 > score || score > 5) {
            return ResponseEntity.status(400).body("Score must be between 1 and 5!");
        }

        Optional<Rating> ratingOpt = ratingRepository.findByRecipeIdAndUserId(recipe.getId(), user.getId());
        if (ratingOpt.isPresent()) {
            Rating rating = ratingOpt.get();
            rating.setScore(score);
            ratingRepository.save(rating);
        } else {
            recipe.getRatings().add(new Rating(score, recipe, user));
            recipeRepository.save(recipe);
        }

        recipe = recipeRepository.findById(recipeId).get();

        return ResponseEntity.ok(Map.of("average", recipe.getAverageRating(),
                "totalRatings", recipe.getRatings().size(),
                "userScore", score));
    }
}
