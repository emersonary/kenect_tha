package com.kenect.tha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kenect.tha.model.Contact;
import com.kenect.tha.service.ContactService;

@RestController
public class ContactController {

  @Autowired
  private ContactService contactService;

  @GetMapping("/contacts")
  public List<Contact> getAllContacts() throws Exception {

    return contactService.getAllContacts();

  }
}
