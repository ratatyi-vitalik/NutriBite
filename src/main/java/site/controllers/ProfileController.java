package site.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import site.persistent.entity.Subscriber;
import site.persistent.entity.User;
import site.persistent.repository.SubscriberRepository;
import site.persistent.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import site.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ProfileController {
    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;
    private final UserService userService;

    public ProfileController(UserRepository userRepository,
                             SubscriberRepository subscriberRepository,
                             UserService userService) {
        this.userRepository = userRepository;
        this.subscriberRepository = subscriberRepository;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView showProfilePage(@AuthenticationPrincipal User user) {
        ModelAndView model = new ModelAndView("profile");
        model.addObject("user", user);
        return model;
    }

    @GetMapping("/profile/favourites")
    public ModelAndView favourites(@AuthenticationPrincipal User user) {
        ModelAndView model = new ModelAndView("favourites");
        model.addObject("user", user);
        model.addObject("recipes", user.getSaved());
        return model;
    }

    @PostMapping("/profile-subscribe")
    public RedirectView subscribe(@AuthenticationPrincipal User user) {
        user.setSubscribed(true);
        userRepository.save(user);
        userService.reloadUserInSecurityContext(user);
        if (!subscriberRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            subscriberRepository.save(new Subscriber(user.getEmail().trim().toLowerCase()));
        }
        return new RedirectView("/profile");
    }

    @PostMapping("/profile-unsubscribe")
    public RedirectView unsubscribe(@AuthenticationPrincipal User user) {
        user.setSubscribed(false);
        userRepository.save(user);
        userService.reloadUserInSecurityContext(user);
        if (subscriberRepository.existsByEmail(user.getEmail().trim().toLowerCase())) {
            subscriberRepository.delete(new Subscriber(user.getEmail().trim().toLowerCase()));
        }
        return new RedirectView("/profile");
    }

    @PostMapping("/deletePFP")
    public RedirectView deletePFP(@AuthenticationPrincipal User user) {
        user.setAvatarPath(null);
        userRepository.save(user);
        userService.reloadUserInSecurityContext(user);
        return new RedirectView("/profile");
    }

    @PostMapping("/changePFP")
    public RedirectView changePFP(@RequestParam MultipartFile file,
                                  @AuthenticationPrincipal User user,
                                  RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Empty file!");
            return new RedirectView("/profile");
        }

        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/png") || contentType.equals("image/jpeg"))) {
            redirectAttributes.addFlashAttribute("error", "Invalid format!");
            return new RedirectView("/profile");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFileName = "user" + user.getId() + "pfp" + extension;
            Path physicalPath = Paths.get("uploads/pfps/" + newFileName);
            Files.write(physicalPath, file.getBytes());

            String webPath = "/uploads/pfps/" + newFileName;
            user.setAvatarPath(webPath);

            userRepository.save(user);
            userService.reloadUserInSecurityContext(user);

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Upload failed!");
            return new RedirectView("/profile");
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/profile/change-password")
    public RedirectView changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       @AuthenticationPrincipal User user,
                                       RedirectAttributes redirectAttributes) {
        String changePass = userService.changeUserPassword(user, oldPassword, newPassword, confirmPassword);
        if (changePass == null) {
            redirectAttributes.addFlashAttribute("Success", "Password successfully changed!");
        } else {
            redirectAttributes.addFlashAttribute("ChangePassError", changePass);
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/saveName")
    public RedirectView saveName(@RequestParam String name,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes redirectAttributes) {
        user.setFullName(name);
        redirectAttributes.addFlashAttribute("Success", "Name changed");
        userRepository.save(user);
        userService.reloadUserInSecurityContext(user);
        return new RedirectView("/profile");
    }

    @PostMapping("/saveUsername")
    public RedirectView saveUsername(@RequestParam String username,
                                     @AuthenticationPrincipal User user,
                                     RedirectAttributes redirectAttributes) {
        if (userRepository.findByName(username).isPresent()) {
            redirectAttributes.addFlashAttribute("Error", "Username already taken");
        } else {
            user.setName(username);
            redirectAttributes.addFlashAttribute("Success", "Username changed");
            userRepository.save(user);
            userService.reloadUserInSecurityContext(user);
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/profile/delete")
    public RedirectView deleteProfile(@AuthenticationPrincipal User user, HttpServletRequest request) {
        userRepository.delete(user);
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return new RedirectView("/");
    }
}
