package org.example.site.controllers;

import jakarta.servlet.http.HttpSession;
import org.example.site.persistent.entity.User;
import org.example.site.persistent.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ProfileController {
    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ModelAndView showProfilePage(HttpSession session) {
        ModelAndView model = new ModelAndView("profile");

        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null) {
            return new ModelAndView("redirect:/");
        }

        model.addObject("user", currentUser);

        return model;
    }

    @PostMapping("/subscribe")
    public RedirectView subscribe(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setSubscribed(true);
            userRepository.save(user);
            session.setAttribute("user", user);
        }
        return new RedirectView("/profile");
    }

    @PostMapping("/unsubscribe")
    public RedirectView unsubscribe(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setSubscribed(false);
            userRepository.save(user);
            session.setAttribute("user", user);
        }
        return new RedirectView("/profile");
    }

    @PostMapping("/deletePFP")
    public RedirectView deletePFP(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setAvatarPath(null);
            userRepository.save(user);
            session.setAttribute("user", user);
        }
        return new RedirectView("/profile");
    }

    @PostMapping("/changePFP")
    public RedirectView changePFP(@RequestParam MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new RedirectView("/");
        }

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
            session.setAttribute("user", user);

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
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            if (BCrypt.checkpw(oldPassword, user.getPassword()) && newPassword.equals(confirmPassword) && !oldPassword.equals(newPassword)) {
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                redirectAttributes.addFlashAttribute("Success", "Password successfully changed!");
                userRepository.save(user);
            } else if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("ChangePassError", "Old password doesn't match");
            } else if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("ChangePassError", "New passwords don't match");
            } else if (oldPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("ChangePassError", "New and old passwords must not match!");
            }
        }
        else {
            return new RedirectView("/profile");
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/saveName")
    public RedirectView saveName(@RequestParam String name,  HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user.setFullName(name);
            redirectAttributes.addFlashAttribute("Success", "Name changed");
            userRepository.save(user);
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/saveUsername")
    public RedirectView saveUsername(@RequestParam String username,  HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user != null) {

            if (userRepository.findByName(username).isPresent()) {
                redirectAttributes.addFlashAttribute("Error", "Username already taken");
            }
            else {
                user.setName(username);
                redirectAttributes.addFlashAttribute("Success", "Username changed");
                userRepository.save(user);
            }
        }

        return new RedirectView("/profile");
    }

    @PostMapping("/profile/delete")
    public RedirectView deleteProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userRepository.delete(user);
        }
        return new RedirectView("/");
    }
}
