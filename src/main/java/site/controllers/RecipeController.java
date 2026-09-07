package site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import site.persistent.dto.RecipeForm;
import site.persistent.entity.Notification;
import site.persistent.entity.ProposedRecipe;
import site.persistent.entity.Report;
import site.persistent.entity.comments.Comment;
import site.persistent.entity.comments.Reply;
import site.persistent.entity.recipes.*;
import site.persistent.entity.User;
import site.persistent.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import site.service.IpUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final ProposedRecipeRepository proposedRecipeRepository;
    private final ReportRepository reportRepository;
    private final ReplyRepository replyRepository;
    private final NotificationRepository notificationRepository;
    private final RecipeViewRepository recipeViewRepository;

    public RecipeController(RecipeRepository recipeRepository, UserRepository userRepository,
                            CommentRepository commentRepository, RatingRepository ratingRepository,
                            ProposedRecipeRepository proposedRecipeRepository,
                            ReportRepository reportRepository,
                            ReplyRepository replyRepository,
                            NotificationRepository notificationRepository,
                            RecipeViewRepository recipeViewRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.proposedRecipeRepository = proposedRecipeRepository;
        this.reportRepository = reportRepository;
        this.replyRepository = replyRepository;
        this.notificationRepository = notificationRepository;
        this.recipeViewRepository = recipeViewRepository;
    }

    @GetMapping("/recipe")
    public ModelAndView recipe(Long recipeId, @AuthenticationPrincipal User user) {
        ModelAndView model = new ModelAndView("recipe");

        Optional<Recipe> recipe = recipeRepository.findById(recipeId);
        if (recipe.isPresent()) {
            model.addObject("recipe", recipe.get());
        } else {
            return new ModelAndView("redirects:/");
        }
        model.addObject("user", user);
        return model;
    }

    @PostMapping("/comment-post")
    public RedirectView postComment(@RequestParam String text,
                                    @RequestParam Long recipeId,
                                    @RequestParam(required = false) Long commentId,
                                    @AuthenticationPrincipal User authUser) {
        Recipe recipe = recipeRepository.findById(recipeId).orElse(null);
        if (recipe == null) {
            return new RedirectView("/");
        }
        User user = userRepository.findById(authUser.getId()).orElse(null);
        if (user == null) {
            return new RedirectView("/");
        }

        if (commentId == null) {
            Comment comment = new Comment();
            comment.setText(text);
            comment.setAuthor(user);
            comment.setDate(LocalDateTime.now());
            comment.setRecipe(recipe);

            recipe.getComments().add(comment);
            if (!user.getId().equals(recipe.getAuthor().getId())) {
                notificationRepository.save(new Notification(
                        recipe.getAuthor(),
                        "New comment on your recipe!",
                        user.getName() + " wrote a comment: \"" + text + "\"",
                        "/recipe?recipeId=" + recipeId
                ));
            }
        } else {
            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            if (commentOpt.isPresent()) {
                Comment comment = commentOpt.get();

                Reply reply = new Reply();
                reply.setAuthor(user);
                reply.setText(text);
                reply.setDate(LocalDateTime.now());
                reply.setComment(comment);

                comment.getReplies().add(reply);
                if (!user.getId().equals(comment.getAuthor().getId())) {
                    notificationRepository.save(new Notification(
                            comment.getAuthor(),
                            "New reply to your comment!",
                            user.getName() + " replied: \"" + text + "\"",
                            "/recipe?recipeId=" + recipeId
                    ));
                }
            } else {
                return new RedirectView("/recipe?recipeId=" + recipeId);
            }
        }
        recipeRepository.save(recipe);

        return new RedirectView("/recipe?recipeId=" + recipeId);
    }

    @PostMapping("/like-comment")
    @Transactional
    public ResponseEntity<?> likeComment(@RequestParam(required = false, defaultValue = "0") Long commentId,
                                         @RequestParam(required = false, defaultValue = "0") Long replyId,
                                         @AuthenticationPrincipal User user) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        Optional<Reply> replyOpt = replyRepository.findById(replyId);
        Comment comment;
        if (commentOpt.isPresent()) {
            comment = commentOpt.get();
            Set<User> likes = comment.getLikes();
            if (likes.contains(user)) {
                likes.remove(user);
                comment.setLikes(likes);
                commentRepository.save(comment);
                if (!user.equals(comment.getAuthor())) {
                    notificationRepository.deleteByUserAndCommentId(comment.getAuthor(), commentId);
                }
                return ResponseEntity.ok(Map.of("likes", likes.size(), "liked", false));
            } else {
                likes.add(user);
                comment.setLikes(likes);
                commentRepository.save(comment);
                if (!user.equals(comment.getAuthor())) {
                    notificationRepository.save(new Notification(comment.getAuthor(), "New like!",
                            user.getName() + " liked your comment",
                            "/recipe?recipeId=" + comment.getRecipe().getId(), comment));
                }
                return ResponseEntity.ok((Map.of("likes", likes.size(), "liked", true)));
            }
        } else if (replyOpt.isPresent()) {
            Reply reply = replyOpt.get();

            Set<User> likes = reply.getLikes();
            if (likes.contains(user)) {
                likes.remove(user);
                reply.setLikes(likes);
                replyRepository.save(reply);
                if (!user.equals(reply.getAuthor())) {
                    notificationRepository.deleteByUserAndReplyId(reply.getAuthor(), replyId);
                }
                return ResponseEntity.ok(Map.of("likes", likes.size(), "liked", false));
            } else {
                likes.add(user);
                reply.setLikes(likes);
                replyRepository.save(reply);
                if (!user.equals(reply.getAuthor())) {
                    notificationRepository.save(new Notification(reply.getAuthor(), "New like!",
                            user.getName() + " liked your comment",
                            "/recipe?recipeId=" + reply.getComment().getRecipe().getId(), reply));
                }
                return ResponseEntity.ok((Map.of("likes", likes.size(), "liked", true)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Комментарий не найден");
        }
    }

    @PostMapping("/save-recipe")
    public ResponseEntity<?> saveRecipe(@RequestParam Long recipeId, @AuthenticationPrincipal User user) {
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
    public ResponseEntity<?> rateRecipe(@RequestParam Long recipeId,
                                        @RequestParam int score,
                                        @AuthenticationPrincipal User user) {
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

    @GetMapping("/recipe/propose")
    public ModelAndView proposeRecipe(@AuthenticationPrincipal User user) {
        ModelAndView model = new ModelAndView("proposeRecipe");
        long secondsToWait = 0;
        if (user != null) {
            model.addObject("user", user);
            if (user.getLastProposeRecipe() != null) {
                LocalDateTime allowedTime = user.getLastProposeRecipe().plusMinutes(5);
                if (allowedTime.isAfter(LocalDateTime.now())) {
                    secondsToWait = Duration.between(LocalDateTime.now(), allowedTime).getSeconds();
                }
            }
        }

        model.addObject("secondsToWait", secondsToWait);
        return model;
    }

    @PostMapping("/recipe/propose")
    public RedirectView saveProposedRecipe(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "prepTime", required = false) Integer prepTime,
            @RequestParam(value = "cookTime", required = false) Integer cookTime,
            @RequestParam(value = "servings", required = false) String servings,

            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,

            @RequestParam(value = "calories", required = false) String calories,
            @RequestParam(value = "carbs", required = false) String carbs,
            @RequestParam(value = "protein", required = false) String protein,
            @RequestParam(value = "fat", required = false) String fat,

            @ModelAttribute RecipeForm recipeForm,
            @RequestParam("stepDescriptions") List<String> stepDescriptions,

            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) throws IOException {
        LocalDateTime lastPropose = user.getLastProposeRecipe();
        if (lastPropose != null && lastPropose.plusMinutes(5).isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error",
                    "Please wait 5 minutes before submitting another recipe!");
            return new RedirectView("/recipe/propose");
        }

        ProposedRecipe proposedRecipe = new ProposedRecipe(title, description, prepTime, cookTime,
                servings, user, LocalDateTime.now(), new Nutrition(calories, carbs, fat, protein));

        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Empty file!");
            return new RedirectView("/recipe/propose");
        }

        proposedRecipe = proposedRecipeRepository.save(proposedRecipe);

        String contentType = imageFile.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            redirectAttributes.addFlashAttribute("error", "Invalid format!");
            proposedRecipeRepository.delete(proposedRecipe);
            return new RedirectView("/recipe/propose");
        }

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = "recipe" + proposedRecipe.getId() + extension;
            Path physicalPath = Paths.get("uploads/recipes/" + newFileName);
            Files.write(physicalPath, imageFile.getBytes());

            String webPath = "/uploads/recipes/" + newFileName;
            proposedRecipe.setImagePath(webPath);


        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Upload failed!");
            proposedRecipeRepository.delete(proposedRecipe);
            return new RedirectView("/recipe/propose");
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            contentType = videoFile.getContentType();
            if (contentType == null || !(contentType.equals("video/mp4") || contentType.equals("video/webm"))) {
                redirectAttributes.addFlashAttribute("error", "Invalid format!");
                String webPath = proposedRecipe.getImagePath();
                Path path = Paths.get(webPath.startsWith("/") ? webPath.substring(1) : webPath);
                Files.deleteIfExists(path);
                proposedRecipeRepository.delete(proposedRecipe);
                return new RedirectView("/recipe/propose");
            }

            try {
                String originalFilename = videoFile.getOriginalFilename();
                String extension = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newFileName = "recipe" + proposedRecipe.getId() + "video" + extension;
                Path physicalPath = Paths.get("uploads/recipes/" + newFileName);
                Files.write(physicalPath, videoFile.getBytes());

                String webPath = "/uploads/recipes/" + newFileName;
                proposedRecipe.setVideoPath(webPath);


            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Upload failed!");
                String webPath = proposedRecipe.getImagePath();
                Path path = Paths.get(webPath.startsWith("/") ? webPath.substring(1) : webPath);
                Files.deleteIfExists(path);
                proposedRecipeRepository.delete(proposedRecipe);
                return new RedirectView("/recipe/propose");
            }
        }

        List<IngredientSection> ingredientSections = new ArrayList<>();
        for (RecipeForm.IngredientSectionDto ingSecDto : recipeForm.getSections()) {

            if (ingSecDto == null || ingSecDto.getTitle() == null || ingSecDto.getTitle().isBlank()) {
                continue;
            }

            List<Ingredient> ingredients = new ArrayList<>();
            if (ingSecDto.getIngredients() != null) {
                for (RecipeForm.IngredientDto ingDto : ingSecDto.getIngredients()) {
                    if (ingDto != null && ingDto.getName() != null && !ingDto.getName().isBlank()) {
                        ingredients.add(new Ingredient(ingDto.getAmount(), ingDto.getName()));
                    }
                }
            }
            ingredientSections.add(new IngredientSection(ingSecDto.getTitle(), ingredients));
        }
        proposedRecipe.setIngredientSections(ingredientSections);

        List<InstructionStep> steps = new ArrayList<>();
        if (stepDescriptions != null) {
            for (String text : stepDescriptions) {
                if (text != null && !text.isBlank()) {
                    steps.add(new InstructionStep(text));
                }
            }
        }
        proposedRecipe.setInstructions(steps);
        proposedRecipeRepository.save(proposedRecipe);
        user.setLastProposeRecipe(LocalDateTime.now());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("message", "Recipe was suggested");
        return new RedirectView("/recipe/propose");
    }

    @PostMapping("/report-comment")
    public ResponseEntity<?> reportComment(@RequestParam(required = false, defaultValue = "0") long commentId,
                                           @RequestParam(required = false, defaultValue = "0") long replyId,
                                           @RequestParam String reason,
                                           @RequestParam String details,
                                           @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized!");
        }

        if (commentId != 0) {
            commentRepository.findById(commentId).ifPresent(comment ->
                    reportRepository.save(new Report(user, comment, reason, details)));
            return ResponseEntity.ok().build();
        }

        if (replyId != 0) {
            replyRepository.findById(replyId).ifPresent(reply ->
                    reportRepository.save(new Report(user, reply, reason, details)));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment or reply not found!");
    }

    @PostMapping("/recipe/view")
    public ResponseEntity<Void> countView(@RequestParam long recipeId, @AuthenticationPrincipal User user, HttpServletRequest request) {
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        if (recipeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Recipe recipe = recipeOpt.get();
        if (user != null) {
            if (!recipeViewRepository.existsByRecipeIdAndUserIdAndViewedAtAfter(recipeId, user.getId(), LocalDateTime.now().minusHours(2))) {
                recipeViewRepository.save(new RecipeView(recipe, user));
            }
        }
        else {
            if (!recipeViewRepository.existsByRecipeIdAndIpAddressAndViewedAtAfter(recipeId, IpUtils.getClientIp(request), LocalDateTime.now().minusHours(2))) {
                recipeViewRepository.save(new RecipeView(recipe, IpUtils.getClientIp(request)));
            }
        }
        return ResponseEntity.ok().build();
    }
}
