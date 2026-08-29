package site.controllers;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import site.persistent.dto.RecipeForm;
import site.persistent.entity.*;
import site.persistent.entity.comments.Comment;
import site.persistent.entity.comments.Reply;
import site.persistent.entity.recipes.*;
import site.persistent.repository.*;
import site.service.SendMailService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class AdminController {
    private final RecipeRepository recipeRepository;
    private final ProposedRecipeRepository proposedRecipeRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriberRepository subscriberRepository;
    private final SendMailService  sendMailService;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public AdminController(RecipeRepository recipeRepository,
                           ProposedRecipeRepository proposedRecipeRepository,
                           CategoryRepository categoryRepository,
                           SubscriberRepository subscriberRepository,
                           SendMailService sendMailService,
                           CommentRepository commentRepository,
                           ReplyRepository replyRepository,
                           ReportRepository reportRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository) {
        this.recipeRepository = recipeRepository;
        this.proposedRecipeRepository = proposedRecipeRepository;
        this.categoryRepository = categoryRepository;
        this.subscriberRepository = subscriberRepository;
        this.sendMailService = sendMailService;
        this.commentRepository = commentRepository;
        this.replyRepository = replyRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/admin")
    public ModelAndView showAdminDashboard() {
        ModelAndView model = new ModelAndView("Admin/admin");
        model.addObject("totalRecipesCount", recipeRepository.count());
        model.addObject("proposedRecipesCount", proposedRecipeRepository.count());
        model.addObject("categoriesCount", categoryRepository.count());
        model.addObject("usersCount", userRepository.count());
        return model;
    }

    @GetMapping("/admin/categories")
    public ModelAndView showManageCategoriesPage() {
        ModelAndView model = new ModelAndView("Admin/adminCategories");
        List<Category> categories = categoryRepository.findAll();

        List<List<Category>> categoryPages = new ArrayList<>();
        for (int i = 0; i < categories.size(); i += 8) {
            categoryPages.add(categories.subList(i, Math.min(i + 8, categories.size())));
        }
        model.addObject("categoryPages", categoryPages);
        model.addObject("totalCategories", categories.size());
        model.addObject("newCategory", new Category());
        return model;
    }

    @PostMapping("/admin/categories/add")
    public RedirectView addCategory(@RequestParam String name, @RequestParam MultipartFile file, RedirectAttributes redirectAttributes) {
        Category category = new Category();
        category.setName(name);
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Empty file!");
            return new RedirectView("/admin/categories");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            redirectAttributes.addFlashAttribute("error", "Invalid format!");
            return new RedirectView("/admin/categories");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = "category" + UUID.randomUUID() + extension;
            Path physicalPath = Paths.get("uploads/categories/" + newFileName);
            Files.write(physicalPath, file.getBytes());

            String webPath = "/uploads/categories/" + newFileName;
            category.setImagePath(webPath);

            categoryRepository.save(category);
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Upload failed!");
            return new RedirectView("/admin/categories");
        }
        redirectAttributes.addFlashAttribute("message", "Upload success!");
        return new RedirectView("/admin/categories");
    }

    @PostMapping("/admin/categories/delete")
    public RedirectView deleteCategory(@RequestParam long categoryId, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            categoryRepository.delete(categoryOpt.get());
            redirectAttributes.addFlashAttribute("message", "Upload success!");
            return new RedirectView("/admin/categories");
        }
        redirectAttributes.addFlashAttribute("error", "This category don't exists!");
        return new RedirectView("/admin/categories");
    }

    @GetMapping("/admin/recipes/add")
    public ModelAndView showAddRecipePage() {
        return new ModelAndView("Admin/adminAddRecipe");
    }

    @PostMapping("/admin/recipes/add")
    public RedirectView showRecipePage(@RequestParam("title") String title,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam(value = "prepTime", required = false) String prepTime,
                                       @RequestParam(value = "cookTime", required = false) String cookTime,
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
        Recipe recipe = new Recipe(title, description, prepTime, cookTime,
                servings, user, LocalDateTime.now(), new Nutrition(calories, carbs, fat, protein));

        if (imageFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Empty file!");
            return new RedirectView("/admin/recipes/add");
        }

        recipe = recipeRepository.save(recipe);

        String contentType = imageFile.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            redirectAttributes.addFlashAttribute("error", "Invalid format!");
            recipeRepository.delete(recipe);
            return new RedirectView("/admin/recipes/add");
        }

        try {
            String originalFilename = imageFile.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = "recipe" + recipe.getId() + extension;
            Path physicalPath = Paths.get("uploads/recipes/" + newFileName);
            Files.write(physicalPath, imageFile.getBytes());

            String webPath = "/uploads/recipes/" + newFileName;
            recipe.setImagePath(webPath);


        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Upload failed!");
            recipeRepository.delete(recipe);
            return new RedirectView("/admin/recipes/add");
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            contentType = videoFile.getContentType();
            if (contentType == null || !(contentType.equals("video/mp4") || contentType.equals("video/webm"))) {
                redirectAttributes.addFlashAttribute("error", "Invalid format!");
                String webPath = recipe.getImagePath();
                Path path = Paths.get(webPath.startsWith("/") ? webPath.substring(1) : webPath);
                Files.deleteIfExists(path);
                recipeRepository.delete(recipe);
                return new RedirectView("/admin/recipes/add");
            }

            try {
                String originalFilename = videoFile.getOriginalFilename();
                String extension = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newFileName = "recipe" + recipe.getId() + "video" + extension;
                Path physicalPath = Paths.get("uploads/recipes/" + newFileName);
                Files.write(physicalPath, videoFile.getBytes());

                String webPath = "/uploads/recipes/" + newFileName;
                recipe.setVideoPath(webPath);


            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Upload failed!");
                String webPath = recipe.getImagePath();
                Path path = Paths.get(webPath.startsWith("/") ? webPath.substring(1) : webPath);
                Files.deleteIfExists(path);
                recipeRepository.delete(recipe);
                return new RedirectView("/admin/recipes/add");
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
        recipe.setIngredientSections(ingredientSections);

        List<InstructionStep> steps = new ArrayList<>();
        if (stepDescriptions != null) {
            for (String text : stepDescriptions) {
                if (text != null && !text.isBlank()) {
                    steps.add(new InstructionStep(text));
                }
            }
        }
        recipe.setInstructions(steps);
        recipeRepository.save(recipe);

        redirectAttributes.addFlashAttribute("message", "Recipe added successfully");
        return new RedirectView("/admin/recipes/add");
    }

    @GetMapping("/admin/recipes")
    public ModelAndView showRecipesListPage(@RequestParam(defaultValue = "0") int page) {
        ModelAndView model = new ModelAndView("Admin/adminRecipes");
        model.addObject("recipePage", recipeRepository.findAll(PageRequest.of(page, 12)));
        model.addObject("totalRecipes", recipeRepository.count());
        return model;
    }

    @PostMapping("/admin/recipes/delete")
    public RedirectView deleteRecipe(@RequestParam long recipeId, RedirectAttributes redirectAttributes) {
        if (recipeRepository.existsById(recipeId)) {
            recipeRepository.deleteById(recipeId);
            redirectAttributes.addFlashAttribute("message", "Recipe deleted successfully!");
            return new RedirectView("/admin/recipes");
        }
        redirectAttributes.addFlashAttribute("error", "This recipe doesn't exists!");
        return new RedirectView("/admin/recipes");
    }

    @GetMapping("/admin/proposed")
    public ModelAndView showProposedPage(@RequestParam(defaultValue = "0") int page) {
        ModelAndView model = new ModelAndView("Admin/adminProposed");
        model.addObject("proposedRecipes", proposedRecipeRepository.findAll(PageRequest.of(page, 12)));
        model.addObject("totalProposedRecipes", proposedRecipeRepository.count());
        return model;
    }

    @GetMapping("/admin/proposedRecipe")
    public ModelAndView showProposedRecipePage(@RequestParam long proposedRecipeId, RedirectAttributes redirectAttributes) {
        ModelAndView model = new ModelAndView("Admin/adminProposedRecipe");
        Optional<ProposedRecipe> proposedRecipeOpt = proposedRecipeRepository.findById(proposedRecipeId);
        if (proposedRecipeOpt.isPresent()) {
            model.addObject("recipe", proposedRecipeOpt.get());
            return model;
        }
        redirectAttributes.addFlashAttribute("error", "This proposed recipe doesn't exists!");
        return new ModelAndView("redirect:/admin/proposed");
    }

    @PostMapping("/admin/proposed/decline")
    public RedirectView declineRecipe(@RequestParam(defaultValue = "0") long proposedRecipeId, RedirectAttributes redirectAttributes) {
        Optional<ProposedRecipe> proposedRecipeOpt = proposedRecipeRepository.findById(proposedRecipeId);
        if (proposedRecipeOpt.isPresent()) {
            proposedRecipeRepository.deleteById(proposedRecipeId);
            redirectAttributes.addFlashAttribute("message", "Recipe was declined");
            return new RedirectView("/admin/proposed");
        }
        redirectAttributes.addFlashAttribute("error", "This recipe wasn't exists");
        return new RedirectView("/admin/proposed");
    }

    @PostMapping("/admin/proposed/accept")
    @Transactional
    public RedirectView acceptRecipe(@RequestParam long proposedRecipeId, RedirectAttributes redirectAttributes) {
        Optional<ProposedRecipe> proposedRecipeOpt = proposedRecipeRepository.findById(proposedRecipeId);
        if (proposedRecipeOpt.isPresent()) {
            Recipe recipe = new  Recipe(proposedRecipeOpt.get());
            recipeRepository.save(recipe);
            proposedRecipeRepository.deleteById(proposedRecipeId);

            notificationRepository.save(new Notification(recipe.getAuthor(),
                    "Your recipe was approved!",
                    "Recipe " + recipe.getTitle() + " was accepted and appeared on website",
                    "/recipe?recipeId=" + recipe.getId()));
            redirectAttributes.addFlashAttribute("message", "Recipe was accepted");
            return new RedirectView("/admin/proposed");
        }
        redirectAttributes.addFlashAttribute("error", "This recipe doesn't exists!");
        return new RedirectView("/admin/proposed");
    }

    @GetMapping("/admin/mail")
    public ModelAndView showSendMailPage() {
        return new ModelAndView("Admin/adminSendMail").addObject("subscribersCount", subscriberRepository.count());
    }

    @PostMapping("/admin/mail/send")
    public RedirectView sendNewsletter(@RequestParam String title, @RequestParam String subject,
                                       @RequestParam String content, @RequestParam(required = false) String ctaText,
                                       @RequestParam(required = false) String ctaUrl) {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        if (!subscribers.isEmpty()) {
            sendMailService.SendNewsMails(subscribers, title, subject, content, ctaText, ctaUrl);
        }

        return new RedirectView("/admin/mail");
    }

    @GetMapping("/admin/api/subscribers")
    public ResponseEntity<List<String>> getSubscribers(@RequestParam(required = false) String query, @RequestParam(defaultValue = "0") int page) {
        List<String> subscribers;
        if (query != null && !query.isEmpty()) {
            subscribers = subscriberRepository.findEmailsByQuery(query, PageRequest.of(page, 20));
        } else {
            subscribers = subscriberRepository.findAllEmails(PageRequest.of(page, 20));
        }
        return ResponseEntity.ok(subscribers);
    }

    @DeleteMapping("/admin/api/subscribers")
    public ResponseEntity<Void> deleteSubscriber(@RequestParam String email) {
        if (subscriberRepository.existsByEmail(email)) {
            subscriberRepository.deleteByEmail(email);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/delete-comment")
    public RedirectView deleteComment(@RequestParam(required = false, defaultValue = "0") long commentId,
                                      @RequestParam(required = false, defaultValue = "0") long replyId,
                                      @RequestParam long recipeId) {
        if (commentId != 0 && commentRepository.existsById(commentId)) {
            commentRepository.deleteById(commentId);
        }
        else if (replyId != 0 && replyRepository.existsById(replyId)) {
            replyRepository.deleteById(replyId);
        }
        return new RedirectView("/recipe?recipeId=" + recipeId);
    }

    @GetMapping("/admin/reports")
    public ModelAndView showReportsPage(@RequestParam(defaultValue = "PENDING") Report.ReportStatus status,
                                        @RequestParam(defaultValue = "0") int page) {
        ModelAndView model = new ModelAndView("Admin/adminReports");
        model.addObject("currentStatus", status.name());
        model.addObject("totalReports", reportRepository.count());
        model.addObject("totalPages", (int)reportRepository.count() / 10);
        model.addObject("pendingCount", reportRepository.countByStatus(Report.ReportStatus.PENDING));
        model.addObject("resolvedCount", reportRepository.countByStatus(Report.ReportStatus.RESOLVED));
        model.addObject("reports", reportRepository.findByStatus(status, PageRequest.of(page, 10)));
        return model;
    }

    @PostMapping("/admin/reports/resolve")
    public ResponseEntity<?> resolveReport(@RequestParam long reportId,
                                                @RequestParam String actionType,
                                                @RequestParam(required = false) Integer days,
                                                @RequestParam(required = false) Integer hours,
                                                @RequestParam(required = false) Integer minutes,
                                                @RequestParam(required = false) String adminNote) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            report.setStatus(Report.ReportStatus.RESOLVED);

            if ("DELETE_COMMENT".equals(actionType)) {
                if (report.getComment() != null) {
                    report.getComment().setHidden(true);
                } else if (report.getReply() != null) {
                    report.getReply().setHidden(true);
                }
            }
            else if ("BAN_USER".equals(actionType)) {
                Ban ban = new Ban();

                if (adminNote != null && !adminNote.isEmpty()) {
                    ban.setReason(adminNote);
                }

                ban.setPeriod(LocalDateTime.now().plusDays(days).plusHours(hours).plusMinutes(minutes));

                User commentAuthor = null;
                if (report.getComment() != null) {
                    commentAuthor = report.getComment().getAuthor();
                } else if (report.getReply() != null) {
                    commentAuthor = report.getReply().getAuthor();
                }
                assert commentAuthor != null;
                commentAuthor.setBanned(ban);
                userRepository.save(commentAuthor);
            }
            reportRepository.save(report);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found!");
        }
    }

    @PostMapping("/admin/reports/reject")
    public ResponseEntity<?> dismissReport(@RequestParam long reportId) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            report.setStatus(Report.ReportStatus.REJECTED);
            reportRepository.save(report);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Report not found!");
        }
    }

    @GetMapping("/admin/users")
    public ModelAndView showUsersPage(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "0") int bannedPage,
                                      @RequestParam(defaultValue = "all") String tab) {
        ModelAndView model = new ModelAndView("Admin/adminUsers");
        model.addObject("usersPage", userRepository.findAll(PageRequest.of(page, 10)));
        Page<User> bannedUsers = userRepository.findByBanNotNull(PageRequest.of(bannedPage, 10));
        if (bannedUsers == null) {
            bannedUsers = Page.empty(PageRequest.of(bannedPage, 10));
        }
        model.addObject("bannedUsers", bannedUsers);
        model.addObject("activeTab", tab);
        return model;
    }

    @PostMapping("/admin/users/ban")
    public ResponseEntity<?> banUser(@RequestParam long userId,
                                     @RequestParam(required = false, defaultValue = "") String reason,
                                     @RequestParam int durationHours) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.isBanned()) {
                user.getBan().setPeriod(user.getBan().getPeriod().plusHours(durationHours));
                notificationRepository.save(new Notification(user,
                        "Your ban was extended",
                        "Your ban was extended by " + durationHours + " hours"));
                if (user.getBan().getReason() == null || user.getBan().getReason().isBlank()) {
                    user.getBan().setReason(reason);
                }
            } else {
                user.setBanned(new Ban(reason, LocalDateTime.now().plusHours(durationHours)));
                notificationRepository.save(new Notification(user,
                        "You are banned",
                        "You are banned for " + durationHours + " hours due to: " + reason));
            }

            userRepository.save(user);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
    }

    @PostMapping("/admin/users/unban")
    @Transactional
    public ResponseEntity<?> unbanUser(@RequestParam long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getBan() != null) {
                user.setBanned(null);
                userRepository.save(user);
                notificationRepository.save(new Notification(user,
                        "You are unbanned",
                        "You are unbanned by administrator"));
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/admin/users")
    @Transactional
    public ResponseEntity<?> deleteUser(@RequestParam long  userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
