package im.bzh.controller;

import im.bzh.dto.ContactDTO;
import im.bzh.entity.Contact;
import im.bzh.service.ContactService;
import im.bzh.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping("/{username}")
    public R getContactsList(@PathVariable String username) {
        List<Contact> result = contactService.getListByCreator(username);
        return new R(result != null, result != null ? null : "failed to get contacts", result);
    }

    @PostMapping("/{username}")
    public R addContact(@PathVariable String username, @RequestBody Contact contact) {
        Contact c = contactService.getContactByAddressAndCreator(contact.getAddress(), username);
        if (c != null) {
            return new R(false, "contact already exists", null);
        }
        boolean success = contactService.addContact(contact);
        return new R(success, success ? null : "failed to add contact", null);
    }

    @DeleteMapping("/{username}")
    public R deleteContact(@PathVariable String username, @RequestBody ContactDTO contactDTO) {
        boolean success = contactService.deleteContact(contactDTO.getIds());
        return new R(success, success ? null : "failed to delete contact", null);
    }

}