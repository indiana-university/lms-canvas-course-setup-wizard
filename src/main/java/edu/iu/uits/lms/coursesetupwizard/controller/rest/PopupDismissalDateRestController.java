package edu.iu.uits.lms.coursesetupwizard.controller.rest;

import edu.iu.uits.lms.coursesetupwizard.model.PopupDismissalDate;
import edu.iu.uits.lms.coursesetupwizard.repository.PopupDismissalDateRepository;
import edu.iu.uits.lms.coursesetupwizard.service.PopupDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/popupDates")
@Tag(name = "PopupDismissalDateRestController", description = "Interact with the PopupDismissalDate repository with CRUD operations")
@Slf4j
public class PopupDismissalDateRestController {

   @Autowired
   private PopupDismissalDateRepository popupDismissalDateRepository = null;

   @GetMapping("/{id}")
   @Operation(summary = "Get a PopupDismissalDate by id")
   public PopupDismissalDate get(@PathVariable Long id) {
      return popupDismissalDateRepository.findById(id).orElse(null);
   }

   @GetMapping("/all")
   @Operation(summary = "Get all PopupDismissalDate objects")
   public List<PopupDismissalDate> getAll() {
      return (List<PopupDismissalDate>)popupDismissalDateRepository.findAll();
   }

   @PutMapping("/{id}")
   @Operation(summary = "Update a PopupDismissalDate by id")
   public ResponseEntity<?> update(@PathVariable Long id, @RequestParam(name = "popupDismissalDate") String popupDismissalDate) {
      PopupDismissalDate updatedPopupDismissalDate = popupDismissalDateRepository.findById(id).orElse(null);

      if (updatedPopupDismissalDate != null) {
         try {
            updatedPopupDismissalDate.setDismissUntil(PopupDateUtil.validateDate(popupDismissalDate));

            return ResponseEntity.ok(popupDismissalDateRepository.save(updatedPopupDismissalDate));
         } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
         }
      } else {
         return ResponseEntity.badRequest().body("Existing record not found. Check the id provided and try again.");
      }
   }

   @PostMapping("/")
   @Operation(summary = "Create a new PopupDismissalDate")
   public ResponseEntity<?> create(@RequestParam(name = "popupDismissalDate") String popupDismissalDate) {
      try {
         PopupDismissalDate newPopupDismissalDate = PopupDismissalDate.builder()
               .dismissUntil(PopupDateUtil.validateDate(popupDismissalDate))
               .build();
         return ResponseEntity.ok(popupDismissalDateRepository.save(newPopupDismissalDate));
      } catch (IllegalArgumentException e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @DeleteMapping("/{id}")
   @Operation(summary = "Delete a PopupDismissalDate by id")
   public String delete(@PathVariable Long id) {
      popupDismissalDateRepository.deleteById(id);
      return "Delete success.";
   }

   @DeleteMapping("/old")
   @Operation(summary = "Delete any PopupDismissalDate records that are in the past")
   public ResponseEntity<String> deleteOld() {
      int recs = popupDismissalDateRepository.removePastDates();
      return ResponseEntity.ok("Deleted " + recs + " records.");
   }
}
