package site.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import site.persistent.entity.Diet;
import site.persistent.repository.DietRepository;

import java.util.Optional;

@RestController
@RequestMapping("/diets")
public class DietController {
    private final DietRepository dietRepository;

    public DietController(DietRepository dietRepository) {
        this.dietRepository = dietRepository;
    }

    @GetMapping
    public ModelAndView showDietsPage(@RequestParam(defaultValue = "0") int page) {
        ModelAndView model = new ModelAndView("diets");

        model.addObject("diets", dietRepository.findAll(PageRequest.of(page, 8)));

        return model;
    }

    @GetMapping("/{slug}")
    public ModelAndView showDietPage(@PathVariable("slug") String slug) {
        ModelAndView model = new ModelAndView("diet");

        Optional<Diet> dietOpt = dietRepository.findBySlug(slug);
        if (dietOpt.isEmpty()) {
            return new ModelAndView("redirect:/");
        }
        model.addObject("diet", dietOpt.get());

        return model;
    }
}
